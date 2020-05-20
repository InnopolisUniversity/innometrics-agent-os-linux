use qmetaobject::*;

use auth_state::AuthState;
use auth_utils::AuthUtils;

pub mod auth_state;
pub mod auth_utils;

pub fn register_qml_types() {
    let uri = cstr!("guru.innometrics");
    qml_register_enum::<AuthState>(uri, 1, 0, cstr!("AuthState"));
    qml_register_type::<AuthUtils>(uri, 1, 0, cstr!("AuthUtils"));
}
