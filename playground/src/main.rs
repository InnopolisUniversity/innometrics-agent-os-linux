use quickflux::*;

use qmetaobject::*;

fn main() {
    register_quickflux_qml_types();
    let engine = QmlEngine::new();
    let mut disp = QFAppDispatcher::instance(&engine);
    unsafe { connect(
        disp.into_raw(),
        QFDispatcher::dispatched(),
        |typ: &QString, _message: &QJSValue| {
            println!("hello, {}", typ.to_string());
        }
    ) };
    disp.dispatch("world!".into(), &QVariant::from(0));
    engine.exec();
}
