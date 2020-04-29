use std::sync::Mutex;

use qmetaobject::*;

use super::*;

lazy_static! {
    pub static ref QML_ENGINE: Mutex<QmlEngine> = {
        register_quickflux_qml_types();
        Mutex::new(QmlEngine::new())
    };
}
