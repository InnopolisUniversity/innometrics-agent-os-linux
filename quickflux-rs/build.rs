use std::process::Command;
use std::env;

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

/// Cargo build profile, value of `$PROFILE` env variable.
enum Profile {
    /// Default mode
    Debug,
    /// `--release` mode.
    Release,
}

impl Profile {
    pub fn get() -> Self {
        match env::var("PROFILE").expect("Cargo profile is not set").as_str() {
            "debug" => Profile::Debug,
            "release" => Profile::Release,
            x => panic!("unknown cargo profile: {}", x),
        }
    }
}

fn main() {
    /*
     * Qt links
     */

    // parts of this code were also copied from qmetaobject-rs
    let qt_include_path = qmake_query("QT_INSTALL_HEADERS");
    let qt_library_path = qmake_query("QT_INSTALL_LIBS");

    println!("cargo:rustc-link-search={}", qt_library_path.trim());
    println!("cargo:rustc-link-lib=Qt5Core");
    println!("cargo:rustc-link-lib=Qt5Quick");
    println!("cargo:rustc-link-lib=Qt5Qml");

    /*
     * QuickFlux links
     */

    let dst = cmake::Config::new("vendor/quickflux").build();
    let quickflux = match Profile::get() {
        Profile::Debug => "quickfluxd",
        Profile::Release => "quickflux",
    };

    println!("cargo:rustc-link-search=native={}", dst.join("lib").display());
    println!("cargo:rustc-link-lib=static={}", quickflux);

    /*
     * cpp! macro
     */

    cpp_build::Config::new()
        .include(qt_include_path.trim())
        .include(format!("{}/QtCore", qt_include_path.trim()))
        .include(format!("{}/QtQuick", qt_include_path.trim()))
        .include(format!("{}/QtQml", qt_include_path.trim()))
        .include(dst.join("include/quickflux"))
        .build("src/lib.rs");
}
