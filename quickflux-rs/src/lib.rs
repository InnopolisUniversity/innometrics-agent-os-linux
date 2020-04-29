#[macro_use]
extern crate cpp;

cpp! {{
    #include <QuickFlux>
}}

pub fn register_quickflux_qml_types() {
    cpp!(unsafe [] {
        registerQuickFluxQmlTypes();
    });
}
