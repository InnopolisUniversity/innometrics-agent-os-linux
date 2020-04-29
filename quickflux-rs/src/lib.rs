#[macro_use]
extern crate cpp;

cpp!{{

#include <QuickFlux>

}}

fn register_quickflux_qml_types() {
    cpp!(unsafe [] {
        registerQuickFluxQmlTypes();
    });
}
