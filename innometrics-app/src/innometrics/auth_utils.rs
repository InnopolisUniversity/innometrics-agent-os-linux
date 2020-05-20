use std::convert::TryInto;

use ::log::warn;
use qmetaobject::*;

use super::auth_state::AuthState;

mod kirigami {
    /// Wrapper for Kirigami.MessageType
    #[repr(u32)]
    #[derive(Copy, Clone, Debug, Eq, PartialEq)]
    pub enum MessageType {
        Information = 0,
        Positive,
        Warning,
        Error
    }

    impl Default for MessageType {
        fn default() -> Self {
            Self::Information
        }
    }
}

#[allow(non_snake_case)]
#[derive(Default, QObject)]
pub struct AuthUtils {
    base: qt_base_class!(trait QObject),

    state: qt_property!(u32 /*AuthState*/; NOTIFY state_changed WRITE set_state),
    state_internal: AuthState,
    state_changed: qt_signal!(),

    description: qt_property!(QString; NOTIFY description_changed),
    description_changed: qt_signal!(),

    /// Kirigami.MessageType
    inlineMessageType: qt_property!(i32; NOTIFY inline_message_type_changed),
    inline_message_type_internal: kirigami::MessageType,
    inline_message_type_changed: qt_signal!(),
}

impl AuthUtils {
    fn update_description(&mut self) {
        self.description = match self.state_internal {
            AuthState::None => "Not logged in",
            AuthState::Loading => "Loading",
            AuthState::Authorized => "Authorized",
            AuthState::Failed => "Authorization failed",
        }.into();
        self.description_changed();
    }

    fn update_inline_message_type(&mut self) {
        self.inline_message_type_internal = match self.state_internal {
            AuthState::None => kirigami::MessageType::Warning,
            AuthState::Loading => kirigami::MessageType::Information,
            AuthState::Authorized => kirigami::MessageType::Positive,
            AuthState::Failed => kirigami::MessageType::Error,
        };
        self.inlineMessageType = self.inline_message_type_internal as _;
        self.inline_message_type_changed();
    }

    fn set_state(&mut self, state: u32 /*AuthState*/) {
        if let Ok(state_internal) = state.try_into() {
            self.state = state;
            self.state_internal = state_internal;
            self.state_changed();

            self.update_description();
            self.update_inline_message_type();
        } else {
            warn!("Not a valid AuthState: {}", state);
        }
    }
}

impl QSingletonInit for AuthUtils {
    fn init(&mut self) {}
}
