#ifndef AUTHSTATE_H
#define AUTHSTATE_H

#include <QObject>

namespace AuthState {

    Q_NAMESPACE
    enum AuthState {
        None,
        Authorized,
        Failed,
    };
    Q_ENUMS(AuthState);

    void registerQmlType();
}

#endif // AUTHSTATE_H
