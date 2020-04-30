#include "credentials.h"

Credentials::Credentials(QObject *parent):
    QObject(parent),
    m_email(""),
    m_password(""),
    m_projectId("")
{}

QString Credentials::email() const
{
    return m_email;
}

QString Credentials::password() const
{
    return m_password;
}

QString Credentials::projectId() const
{
    return m_projectId;
}

void Credentials::setEmail(QString email)
{
    m_email = email;
    emit emailChanged(m_email);
}

void Credentials::setPassword(QString password)
{
    m_password = password;
    emit passwordChanged(m_password);
}

void Credentials::setProjectId(QString projectId)
{
    m_projectId = projectId;
    emit projectIdChanged(m_projectId);
}
