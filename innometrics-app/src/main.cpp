#include <QApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QQmlComponent>
#include <QQuickView>

#include "authstore.h"
#include "authstate.h"

int main(int argc, char *argv[])
{
    QCoreApplication::setOrganizationName("Innopolis");
    QCoreApplication::setApplicationName("Innometrics");
    QCoreApplication::setOrganizationDomain("guru.innometrics");
    QApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QApplication app(argc, argv);

    // global application store
    AuthStore *store = new AuthStore();
    store->manageAccount(true);

    // register types to QML
    qmlRegisterSingletonInstance<AuthStore>("guru.innometrics", 1, 0, "AuthStore", store);
    AuthState::registerQmlType();

    // create engine, then create context, and instantiate menu in that context
    QQmlEngine *engine = new QQmlEngine();
    // new context is not required unless we are adding custom globals
    // QQmlContext *qmlCtx = new QQmlContext(engine->rootContext());

    // Kick off tray icon with menu
    QQmlComponent trayIconComponent(engine, QUrl(QStringLiteral("qrc:///qml/TrayIcon.qml")));
    QObject *trayIcon = trayIconComponent.create();
    Q_UNUSED(trayIcon);

    // Create Kirigami login view, but do not show it just yet.
    QQmlComponent accountManagerActorComponent(engine, QUrl(QStringLiteral("qrc:///qml/AccountManagerActor.qml")));
    QObject *accountManagerActor = accountManagerActorComponent.create();
    Q_UNUSED(accountManagerActor);

    return app.exec();
}
