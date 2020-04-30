#ifndef CREDENTIALS_H
#define CREDENTIALS_H

#include <QObject>

class Credentials : public QObject
{
    Q_OBJECT
public:
    explicit Credentials(QObject *parent = nullptr);
    Q_PROPERTY(QString email READ email WRITE setEmail NOTIFY emailChanged)
    Q_PROPERTY(QString password READ password WRITE setPassword NOTIFY passwordChanged)
    Q_PROPERTY(QString projectId READ projectId WRITE setProjectId NOTIFY projectIdChanged)

    QString email() const;
    QString password() const;
    QString projectId() const;

    void setEmail(QString email);
    void setPassword(QString password);
    void setProjectId(QString projectId);    
signals:
    void emailChanged(QString);
    void passwordChanged(QString);
    void projectIdChanged(QString);

private:
    QString m_email;
    QString m_password;
    QString m_projectId;
};

QDebug operator<<(QDebug dbg, const Credentials *message);

#endif // CREDENTIALS_H
