#![allow(unused_imports)]

use std::{
    error::Error,
    mem::{MaybeUninit, size_of, size_of_val, zeroed},
    os::raw::c_int,
    ptr::{null, null_mut},
    sync::mpsc::{channel, Receiver, Sender},
};

use log::{info, warn};
use xcb::{
    base::{self, Connection},
    ConnResult,
    ffi::{
        xcb_connect, xcb_connection_t, xcb_disconnect, xcb_get_setup,
        xproto::{
            xcb_change_window_attributes, XCB_CW_EVENT_MASK, xcb_event_mask_t,
            xcb_intern_atom_cookie_t, XCB_PROPERTY_NOTIFY, xcb_screen_iterator_t, xcb_screen_t,
            xcb_setup_roots_iterator, xcb_setup_t, xcb_window_t,
        },
    },
    xproto::{PropertyNotifyEvent, Window},
};
use xcb_util::{
    ewmh::{self, get_active_window, get_wm_visible_name},
    ffi::ewmh::{
        xcb_ewmh_connection_t, xcb_ewmh_connection_wipe, xcb_ewmh_init_atoms,
        xcb_ewmh_init_atoms_replies,
    },
    icccm::{self, get_wm_class},
};

use self::imp::GetWmVisibleNameReplyExt;

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum ClientSignal {
    NewActiveWindow(NewActiveWindow),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct NewActiveWindow {
    class: String,
    title: String,
}

pub struct Client {
    ewmh_conn: ewmh::Connection,
    screen_num: i32,
}

impl Client {
    pub fn new() -> Result<Self, Box<dyn Error>> {
        let (xcb_conn, screen_num) = base::Connection::connect(None)?;
        let ewmh_conn = ewmh::Connection::connect(xcb_conn).map_err(|(e, _)| e)?;

        Ok(Client {
            ewmh_conn,
            screen_num,
        })
    }

    pub fn ewmh(&self) -> &ewmh::Connection {
        &self.ewmh_conn
    }

    pub fn xcb(&self) -> &base::Connection {
        &*self.ewmh_conn
    }

    pub fn run(&self, tx: Sender<ClientSignal>) -> Result<(), Box<dyn Error>> {
        const EVENT_MASK: &[xcb_event_mask_t] = &[xcb::ffi::xproto::XCB_EVENT_MASK_PROPERTY_CHANGE];

        let setup = self.xcb().get_setup();
        let screen = setup
            .roots()
            .nth(self.screen_num as usize)
            .ok_or("No screen")?;

        let _ = unsafe {
            xcb_change_window_attributes(
                self.xcb().get_raw_conn(),
                screen.root(),
                XCB_CW_EVENT_MASK,
                EVENT_MASK.as_ptr(),
            )
        };
        self.xcb().flush();

        let mut last_signal = None;
        loop {
            match self.xcb().wait_for_event() {
                None => break,
                Some(event) => {
                    const XCB_EVENT_RESPONSE_TYPE_MASK: u8 = 0x7f;
                    let r = event.response_type() & XCB_EVENT_RESPONSE_TYPE_MASK;
                    match r {
                        XCB_PROPERTY_NOTIFY => {
                            let property_notify_event: &PropertyNotifyEvent =
                                unsafe { xcb::cast_event(&event) };
                            let atom = unsafe { property_notify_event.ptr.read().atom };
                            if atom == self.ewmh().ACTIVE_WINDOW() {
                                if let Ok(signal) = self.on_active_window_changed() {
                                    // avoid duplicates
                                    if let Some(last) = last_signal.as_ref() {
                                        if last == &signal {
                                            continue;
                                        }
                                    }
                                    last_signal = Some(signal.clone());
                                    tx.send(signal).map_err(|_| "Receiver disconnected")?;
                                }
                            }
                        }
                        _ => {}
                    }
                }
            }
        }
        Ok(())
    }

    fn on_active_window_changed(&self) -> Result<ClientSignal, Box<dyn Error>> {
        let window: Window = get_active_window(self.ewmh(), self.screen_num).get_reply()?;
        let class = get_wm_class(self.ewmh(), window)
            .get_reply()?
            .class()
            .to_owned();
        let title = self.get_active_window_title(window)?;
        Ok(ClientSignal::NewActiveWindow(NewActiveWindow { class, title }))
    }

    fn get_active_window_title(&self, window: Window) -> Result<String, Box<dyn Error>> {
        let mut title: String = ewmh::get_wm_name(self.ewmh(), window)
            .get_reply()?
            .string()
            .to_owned();
        if title.is_empty() {
            title = get_wm_visible_name(self.ewmh(), window)
                .get_reply()?
                .string()
                .to_owned();
        }
        if title.is_empty() {
            title = icccm::get_wm_name(self.ewmh(), window)
                .get_reply()?
                .name()
                .to_owned();
        }
        Ok(title)
    }
}

mod imp {
    use std::intrinsics::transmute;

    use xcb_util::ewmh::GetWmVisibleNameReply;
    use xcb_util::ffi::ewmh::xcb_ewmh_get_utf8_strings_reply_t;

    use super::*;

    pub trait GetWmVisibleNameReplyExt {
        fn string(&self) -> &str;
    }

    impl GetWmVisibleNameReplyExt for GetWmVisibleNameReply {
        fn string(&self) -> &str {
            unsafe {
                let raw: &xcb_ewmh_get_utf8_strings_reply_t = transmute(self);
                utf8::into(raw.strings, raw.strings_len)
                    .get(0)
                    .unwrap_or(&"")
            }
        }
    }

    pub mod utf8 {
        use std::slice;
        use std::str;

        use libc::c_char;

        pub fn into<'a>(data: *const c_char, length: u32) -> Vec<&'a str> {
            if length == 0 {
                return Vec::new();
            }

            unsafe {
                let mut result = str::from_utf8_unchecked(slice::from_raw_parts(
                    data as *mut u8,
                    length as usize,
                ))
                    .split('\0')
                    .collect::<Vec<_>>();

                // Data is sometimes NULL-terminated and sometimes not. If there is a
                // NULL terminator, then our call to .split() will result in an extra
                // empty-string element at the end, so pop it.
                if let Some(&"") = result.last() {
                    result.pop();
                }

                result
            }
        }
    }
}
