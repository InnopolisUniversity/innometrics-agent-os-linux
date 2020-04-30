#[macro_use]
extern crate cstr;
#[macro_use]
extern crate qmetaobject;

use qmetaobject::*;

use innometrics_api as api;

mod qrc;

// #[derive(QObject, Default)]
// struct Greeter {
//     base: qt_base_class!(trait QObject),
//     name: qt_property!(QString; NOTIFY name_changed),
//     name_changed: qt_signal!(),
//     compute_greetings: qt_method!(fn compute_greetings(&self, verb : String) -> QString {
//         return (verb + " " + &self.name.to_string()).into()
//     }),
// }

#[derive(QObject, Default)]
struct AuthStore {
    base: qt_base_class!(trait QObject),
    state: qt_property!(QString; NOTIFY state_changed),
    state_changed: qt_signal!(),
}

fn main() {
    qrc::register();
    qml_register_type::<Greeter>(cstr!("Greeter"), 1, 0, cstr!("Greeter"));
    // qmlRegisterSingletonInstance<AuthStore>("guru.innometrics", 1, 0, "AuthStore", store);

    let mut engine = QmlEngine::new();
    engine.load_file("qrc:///qml/TrayIcon.qml".into());
    engine.exec();
}
