#include <QTimer>

#include "authstore.h"

AuthStore::AuthStore(QObject *parent) :
    QObject(parent),
    m_state(AuthState::None),
    m_shouldManageAccount(false),
    m_credentialsManager(this)
{
}

AuthStore::~AuthStore()
{
}

AuthState::AuthState AuthStore::state() const
{
    return m_state;
}

void AuthStore::setState(AuthState::AuthState state)
{
    m_state = state;
    emit stateChanged();
    emit isAuthorizedChanged();
}

bool AuthStore::isAuthorized() const
{
    return m_state == AuthState::Authorized;
}

bool AuthStore::shouldManageAccount() const
{
    return m_shouldManageAccount;
}

Credentials *AuthStore::credentials()
{
    return m_credentialsManager.load();
}

void AuthStore::manageAccount(bool shouldManage)
{
    m_shouldManageAccount = shouldManage;
    emit shouldManageAccountChanged();
}

void AuthStore::focusOpenedWindow()
{
    emit shouldActivateWindow();
}

void AuthStore::attemptLogIn(QVariantMap newCredentials)
{
    Credentials *c = credentials();
    c->setEmail(newCredentials["email"].toString());
    c->setPassword(newCredentials["password"].toString());
    c->setProjectId(newCredentials["proj"].toString());
    m_credentialsManager.store();
    QPointer<AuthStore> that = this;

    QTimer::singleShot(2000, [that]() {
        qDebug() << "Got response!" << Qt::endl;
        if (!that.isNull()) {
            that->setState(AuthState::Authorized);
        }
        qDebug() << "Authorized!" << Qt::endl;
    });
}

void AuthStore::logOut()
{
    QPointer<AuthStore> that = this;
    QTimer::singleShot(0, [that]() {
        that->setState(AuthState::None);
    });
}
