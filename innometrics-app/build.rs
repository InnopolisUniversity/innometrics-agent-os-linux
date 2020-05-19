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
    /*
     * Qt links
     */

    // parts of this code were also copied from qmetaobject-rs
    let qt_include_path = qmake_query("QT_INSTALL_HEADERS");
    let qt_library_path = qmake_query("QT_INSTALL_LIBS");

    /*
     * cpp! macro
     */

    cpp_build::Config::new()
        .include(qt_include_path.trim())
        .build("src/main.rs");

    /*
     * Link with Qt
     */

    cargo_emit::rustc_link_search!(qt_library_path.trim());
    cargo_emit::rustc_link_lib! {
        "Qt5Core",
        "Qt5QuickControls2",
        "Qt5Quick",
        "Qt5Qml",
    }
}
