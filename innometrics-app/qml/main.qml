import QtQuick 2.3
import QtQuick.Window 2.2
import QtQuick.Layouts 1.0

import QuickFlux 1.1

import "./views"
import "./middlewares"
import "./actions"
import "./stores"

QtObject {
    property MiddlewareList middlewares:
    MiddlewareList {
        applyTarget: AppActions

        SystemMiddleware {}
    }

    // Make sure root store is initialized
    property MainStore mainStore: MainStore

    // Tray icon on desktop
    property TrayIcon trayIcon:
    TrayIcon {}
}
