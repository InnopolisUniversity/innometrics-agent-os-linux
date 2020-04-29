#[macro_use]
extern crate cpp;
#[macro_use]
extern crate lazy_static;

#[cfg(test)]
mod tests;

cpp! {{
    #include <QuickFlux>
}}

pub fn register_quickflux_qml_types() {
    cpp!(unsafe [] {
        registerQuickFluxQmlTypes();
    });
}
