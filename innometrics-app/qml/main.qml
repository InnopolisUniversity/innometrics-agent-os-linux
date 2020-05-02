import QtQuick 2.3
import QtQuick.Window 2.2
import QtQuick.Layouts 1.0

import QuickFlux 1.1

import "./views"
import "./middlewares"
import "./actions"

QtObject {
    property MiddlewareList middlewares:
    MiddlewareList {
        applyTarget: AppActions

        SystemMiddleware {
            mainWindow: mainWindow
        }
    }

    property Window mainWindow:
    MainWindow {}
}
