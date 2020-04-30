#include <QQmlEngine>

#include "authstate.h"

namespace AuthState {
    void registerQmlType()
    {
        qmlRegisterUncreatableMetaObject(
                    staticMetaObject,
                    "guru.innometrics", 1, 0,
                    "AuthState",
                    "Access to enums & flags only");
    }
}
