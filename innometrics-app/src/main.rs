#[macro_use]
extern crate cstr;
#[macro_use]
extern crate cpp;

use qmetaobject::*;
use cpp::cpp;

// use innometrics_api as api;

mod innometrics;
mod qrc;
mod log;

cpp!{{

#include <QtCore/QDebug>
#include <QtCore/QCoreApplication>
#include <QtCore/QObject>
#include <QtCore/QUrl>
#include <QtQml/QQmlApplicationEngine>
#include <QtQuickControls2/QQuickStyle>
#include <QtGui/QIcon>

}}

fn main() {
    log::init();
    qrc::register();
    quickflux::register_quickflux_qml_types();
    innometrics::register_qml_types();

    let mut engine = QmlEngine::new();

    engine.load_file("qrc:///qml/settings/AppQtSettings.qml".into());
    engine.load_file("qrc:///qml/main.qml".into());
    engine.exec();
}
