#ifndef CREDENTIALSMANAGER_H
#define CREDENTIALSMANAGER_H

#include <QObject>
#include <QSettings>

#include "credentials.h"

class CredentialsManager : public QObject
{
    Q_OBJECT
public:
    explicit CredentialsManager(QObject *parent = nullptr);
    ~CredentialsManager();

    Credentials *load();
    void store();

signals:
    void credentialsLoaded();

private slots:
    void setEmail(QString email);
    void setPassword(QString password);
    void setProjectId(QString projectId);

private:
    QSettings m_settings;
    Credentials m_credentials;
};

#endif // CREDENTIALSMANAGER_H
