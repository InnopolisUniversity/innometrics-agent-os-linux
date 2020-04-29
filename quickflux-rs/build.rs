use std::process::Command;

/// This function was copied from qmetaobject-rs build script.
fn qmake_query(var: &str) -> String {
    let qmake = std::env::var("QMAKE").unwrap_or("qmake".to_string());
    String::from_utf8(
        Command::new(qmake)
            .env("QT_SELECT", "qt5")
            .args(&["-query", var])
            .output()
            .expect("Failed to execute qmake. Make sure 'qmake' is in your path")
            .stdout,
    )
    .expect("UTF-8 conversion failed")
}

fn main() {
    // parts of this code were also copied from qmetaobject-rs
    let qt_include_path = qmake_query("QT_INSTALL_HEADERS");
    let qt_library_path = qmake_query("QT_INSTALL_LIBS");

    let dst = cmake::Config::new("vendor/quickflux")
        .build();

    let mut quickflux_include_path = dst.clone();
    quickflux_include_path.push("include");
    quickflux_include_path.push("quickflux");

    println!("cargo:rustc-link-search={}", qt_library_path.trim());
    println!("cargo:rustc-link-lib=Qt5Core");
    println!("cargo:rustc-link-lib=Qt5Quick");
    println!("cargo:rustc-link-lib=Qt5Qml");

    cpp_build::Config::new()
        .include(qt_include_path.trim())
        .include(format!("{}/QtCore", qt_include_path.trim()))
        .include(format!("{}/QtQuick", qt_include_path.trim()))
        .include(format!("{}/QtQml", qt_include_path.trim()))
        .include(quickflux_include_path)
        .build("src/lib.rs");
}
