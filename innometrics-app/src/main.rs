#[macro_use]
extern crate cstr;
#[macro_use]
extern crate qmetaobject;
#[macro_use]
extern crate cpp;

use qmetaobject::*;
use cpp::cpp;

use innometrics_api as api;

mod qrc;
mod log;

#[derive(QEnum)]
#[repr(u32)]
pub enum AuthState {
    None = 0,
    Loading = 1,
    Authorized = 2,
    Failed = 3,
}

impl Default for AuthState {
    fn default() -> Self {
        AuthState::None
    }
}

// #[derive(QObject, Default)]
// pub struct AuthStore {
//     base: qt_base_class!(trait QObject),
//     state: qt_property!(AuthState; NOTIFY state_changed),
//     state_changed: qt_signal!(),
// }

fn register_innometrics_qml_types() {
    qml_register_enum::<AuthState>(cstr!("guru.innometrics"), 1, 0, cstr!("AuthState"));
    // qml_register_type::<Greeter>(cstr!("Greeter"), 1, 0, cstr!("Greeter"));
    // qmlRegisterSingletonInstance<AuthStore>("guru.innometrics", 1, 0, "AuthStore", store);
}

fn main() {
    log::init();
    qrc::register();
    quickflux::register_quickflux_qml_types();
    register_innometrics_qml_types();

    let mut engine = QmlEngine::new();

    engine.load_file("qrc:///qml/settings/AppQtSettings.qml".into());
    engine.load_file("qrc:///qml/main.qml".into());
    engine.exec();
}
