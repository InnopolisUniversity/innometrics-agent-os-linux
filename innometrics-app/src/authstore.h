#ifndef AUTH_STORE_H
#define AUTH_STORE_H

#include <QObject>
#include <QQuickItem>

#include "authstate.h"
#include "credentials.h"
#include "credentialsmanager.h"

class AuthStore : public QObject
{
    Q_OBJECT

public:
    explicit AuthStore(QObject *parent = nullptr);
    ~AuthStore();

    Q_PROPERTY(AuthState::AuthState state READ state WRITE setState NOTIFY stateChanged)
    Q_PROPERTY(bool isAuthorized READ isAuthorized NOTIFY isAuthorizedChanged)
    Q_PROPERTY(bool shouldManageAccount READ shouldManageAccount NOTIFY shouldManageAccountChanged)
    Q_PROPERTY(Credentials *credentials READ credentials)

    AuthState::AuthState state() const;
    void setState(AuthState::AuthState state);

    bool isAuthorized() const;

    bool shouldManageAccount() const;

    Credentials *credentials();

signals:
    void stateChanged();
    void isAuthorizedChanged();
    void shouldManageAccountChanged();
    void shouldActivateWindow();

public slots:
    // XXX: create separate dispatcher
    void manageAccount(bool shouldManage);
    void focusOpenedWindow();
    void attemptLogIn(QVariantMap credentials);
    void logOut();

private:
    AuthState::AuthState m_state;
    bool m_shouldManageAccount;

    CredentialsManager m_credentialsManager;
};

#endif // AUTH_STORE_H
