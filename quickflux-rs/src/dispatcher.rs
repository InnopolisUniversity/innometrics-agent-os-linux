use qmetaobject::*;

cpp! {{
    #include <QtCore/QVariant>
    #include <QtQuick/QtQuick>
    #include <QtWidgets/QApplication>
    #include <QtQml/QQmlComponent>

    #include <QuickFlux>

    #include <cpp/include/QmlEngineHolder>
    #include <cpp/include/QFDispatcherPtr>
    #include <cpp/include/SignalCppRepresentation>
}}

cpp_class!(
    /// Wrapper for `QFDispatcher *`.
    pub unsafe struct QFDispatcher as "QFDispatcherPtr"
);

pub trait QFDispatcherClass {
    fn dispatch(
        &mut self,
        typ: QString,
        message: &QVariant,
    );

    /// See Qt documentation for
    /// ```Q_SIGNAL void dispatched(QString type,QJSValue message);```
    fn dispatched() -> CppSignal<fn(QString, QJSValue)> {
        unsafe {
            CppSignal::new(cpp!([] -> SignalCppRepresentation as "SignalCppRepresentation"  {
                return &QFDispatcher::dispatched;
            }))
        }
    }
}

impl QFDispatcherClass for QFDispatcher {
    fn dispatch(
        &mut self,
        typ: QString,
        message: &QVariant,
    ) {
        cpp!(unsafe [
            self as "QFDispatcherPtr *",
            typ as "QString",
            message as "QVariant *"
        ] {
            self->dispatcher->dispatch(typ, *message);
        });
    }
}

impl QFDispatcher {
    pub fn new(parent: QObjectCppWrapper) -> Self {
        let parent = parent.get();
        cpp!(unsafe [parent as "QObject *"] -> QFDispatcher as "QFDispatcherPtr" {
            return QFDispatcherPtr(new QFDispatcher(parent));
        })
    }
}

#[cfg(test)]
mod tests {
    use crate::QFAppDispatcher;
    use crate::tests::*;

    use super::*;

    #[test]
    fn test_dispatch() {
        let engine = QML_ENGINE.lock().unwrap();

        let mut dispatcher = QFAppDispatcher::instance(&engine);

        let arg1 = QVariant::from(42);
        dispatcher.dispatch(QString::from("@@init@@"), &arg1);

        let arg2 = QVariant::from(QString::from("world"));
        dispatcher.dispatch(QString::from("hello"), &arg2);
    }

    #[test]
    fn test_signal() {

    }
}
