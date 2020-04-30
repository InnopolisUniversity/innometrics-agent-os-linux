#include "credentialsmanager.h"

#include <QString>
#include <qstring.h>
#include <qstring.h>

#define CREDENTIALS_PREFIX "credentials"

static const QString KEY_EMAIL = QStringLiteral(CREDENTIALS_PREFIX "/email");
static const QString KEY_PASSWORD = QStringLiteral(CREDENTIALS_PREFIX "/password");
static const QString KEY_PROJECT_ID = QStringLiteral(CREDENTIALS_PREFIX "/project_id");

CredentialsManager::CredentialsManager(QObject *parent):
    QObject(parent),
    m_settings()
//    m_credentials(Credentials())
{
    connect(&m_credentials, SIGNAL(emailChanged(QString)), this, SLOT(setEmail(QString)));
    connect(&m_credentials, SIGNAL(passwordChanged(QString)), this, SLOT(setPassword(QString)));
    connect(&m_credentials, SIGNAL(projectIdChanged(QString)), this, SLOT(setProjectId(QString)));
}

CredentialsManager::~CredentialsManager()
{
}

Credentials *CredentialsManager::load()
{
    m_settings.sync();
    m_credentials.setEmail(m_settings.value(KEY_EMAIL).toString());
    auto convertPassword = QByteArray::fromBase64(m_settings.value(KEY_PASSWORD).toString().toUtf8());
    m_credentials.setPassword(QString::fromUtf8(convertPassword));
    m_credentials.setProjectId(m_settings.value(KEY_PROJECT_ID).toString());
    return &m_credentials;
}

void CredentialsManager::store()
{
    m_settings.sync();
}

void CredentialsManager::setEmail(QString email)
{
    m_settings.setValue(KEY_EMAIL, email);
}

void CredentialsManager::setPassword(QString password)
{
    auto convertPassword = password.toUtf8().toBase64();
    m_settings.setValue(KEY_PASSWORD, QString::fromUtf8(convertPassword));
}

void CredentialsManager::setProjectId(QString projectId)
{
    m_settings.setValue(KEY_PROJECT_ID, projectId);
}
