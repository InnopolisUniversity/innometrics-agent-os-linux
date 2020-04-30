use std::{sync::mpsc::channel, thread::spawn};

use qmetaobject::*;

use innometrics_collector::*;
use quickflux::*;

fn main() {
    register_quickflux_qml_types();
    let engine = QmlEngine::new();
    let mut disp = QFAppDispatcher::instance(&engine);
    unsafe { connect(
        disp.into_raw(),
        QFDispatcher::dispatched(),
        |typ: &QString, message: &QJSValue| {
            println!("X Event: {}, {:?}", typ.to_string(), message.to_string());
        }
    ) };

    let client = Client::new().unwrap();
    let (tx, rx) = channel();
    spawn(move || {
        client.run(tx).unwrap();
    });

    spawn(move || {
        loop {
            match rx.recv() {
                Ok(ClientSignal::NewActiveWindow(event)) => {
                    disp.dispatch("x_event".into(), &QVariant::from(QString::from(format!("{:?}", event))));
                }
                Err(err) => {
                    println!("error: {:?}", err);
                    break;
                }
            }
        }
    });

    engine.exec();
}
