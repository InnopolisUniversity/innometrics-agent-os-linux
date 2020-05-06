# Innometrics for Linux

Innometrics collector implementation for Linux / X Window System

## Build

Requirements:

 1. [rustup](https://www.rust-lang.org/learn/get-started) with latest stable Rust distribution
    1. Install rustup
    2. Update stable channel:
       `$ rustup default stable`
 2. CMake >= 3, required to build [`quickflux`](https://github.com/benlau/quickflux/)
    - [`extra-cmake-modules`](https://github.com/KDE/extra-cmake-modules) package
 3. XCB library with ewmh and icccm extensions.
    On Arch Linux they can be found in `community/wmctrl` package.
 4. Qt5 libraries with some components:
    - QtCore
    - QtQuick
    - QtQml
    - QtGui
    - QtWidgets
 5. KDE Framework's Kirigami library (runtime only)
