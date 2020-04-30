use std::{sync::mpsc::channel, thread::spawn};

pub mod ewmh;

fn main() {
    println!("Hello, world!");
    let client = ewmh::Client::new().unwrap();

    let (tx, rx) = channel();
    spawn(move || {
        client.run(tx).unwrap();
    });

    loop {
        match rx.recv() {
            Ok(ewmh::ClientSignal::NewActiveWindow(event)) => {
                println!("event: {:?}", event);
            }
            Err(err) => {
                println!("error: {:?}", err);
                break;
            }
        }
    }
}
