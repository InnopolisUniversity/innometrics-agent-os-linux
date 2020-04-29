use qmetaobject::*;

use crate::dispatcher::*;

cpp! {{
    #include <QtQuick/QtQuick>
    #include <QtWidgets/QApplication>
    #include <QtQml/QQmlComponent>

    #include <QuickFlux>

    #include <cpp/include/QmlEngineHolder>
    #include <cpp/include/QFDispatcherPtr>
    #include <cpp/include/QFAppDispatcherPtr>
}}

cpp_class!(
    /// Wrapper for `QFAppDispatcher *`.
    pub unsafe struct QFAppDispatcher as "QFAppDispatcherPtr"
);

pub trait QFAppDispatcherClass: QFDispatcherClass {
    /* no member functions */
}

impl QFAppDispatcherClass for QFAppDispatcher {}
impl QFDispatcherClass for QFAppDispatcher {
    fn dispatch(
        &mut self,
        typ: QString,
        message: &QVariant,
    ) {
        cpp!(unsafe [
            self as "QFAppDispatcherPtr *",
            typ as "QString",
            message as "QVariant *"
        ] {
            self->dispatcher->dispatch(typ, *message);
        });
    }
}

// static member functions
impl QFAppDispatcher {
    pub fn instance(engine: &QmlEngine) -> QFAppDispatcher {
        cpp!(unsafe [engine as "QmlEngineHolder*"] -> QFAppDispatcher as "QFAppDispatcherPtr" {
            return QFAppDispatcherPtr(QFAppDispatcher::instance(&*engine->engine));
        })
    }

    pub fn singleton_object(
        engine: &QmlEngine,
        package: QString,
        version_major: i32,
        version_minor: i32,
        type_name: QString,
    ) -> QObjectCppWrapper {
        cpp!(unsafe [
            engine as "QmlEngineHolder *",
            package as "QString",
            version_major as "int",
            version_minor as "int",
            type_name as "QString"
        ] -> QObjectCppWrapper as "QObject *" {
            return QFAppDispatcher::singletonObject(
                &*engine->engine,
                package,
                version_major,
                version_minor,
                type_name);
        })
    }
}

#[cfg(test)]
mod tests {
    use crate::tests::*;

    use super::*;

    #[test]
    fn test_instance() {
        let engine = QML_ENGINE.lock().unwrap();
        let _dispatcher = QFAppDispatcher::instance(&engine);
    }
}
