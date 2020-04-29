#![allow(dead_code)]

#[macro_use]
extern crate cpp;
#[allow(unused_imports)]
#[macro_use]
extern crate lazy_static;

pub use dispatcher::QFDispatcher;
pub use app_dispatcher::QFAppDispatcher;

#[cfg(test)]
mod tests;

mod app_dispatcher;
mod dispatcher;

cpp! {{
    #include <QuickFlux>
}}

pub fn register_quickflux_qml_types() {
    cpp!(unsafe [] {
        registerQuickFluxQmlTypes();
    });
}
