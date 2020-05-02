import QtQuick 2.0
import QtQuick.Window 2.2

import QuickFlux 1.1

import "../actions"
import "../views"

Store {

    property Window mainWindow:
    MainWindow {
        // never close the window, only hide.
        onClosing: (event) => {
            event.accepted = false;
            hide();
        }

        // initialize at screen's center.
        function positionOnScreenCenter() {
            x = Screen.width / 2 - width / 2
            y = Screen.height / 2 - height / 2
        }

        Component.onCompleted: {
            positionOnScreenCenter();
        }
    }

    function isVisibleAndActive(window) {
        return window.visible && window.active;
    }

    function showAndActivate(window) {
        if (window === null) return;

        window.show();
        window.raise();
        window.requestActivate();
    }

    function hide(window) {
        if (window === null) return;

        window.hide();
    }

    function toggleWindow(window) {
        if (window === null) return;

        if (isVisibleAndActive(window)) {
            hide(window)
        } else {
            showAndActivate(window);
        }
    }

    function namedWindow(name) {
        switch (name) {
            case WindowNames.account:
                return mainWindow;
            default:
                console.warn("Named window not found: " + name);
                return null;
        }
    }

    Filter {
        type: ActionTypes.showWindow
        onDispatched: showAndActivate(namedWindow(message.name))
    }

    Filter {
        type: ActionTypes.toggleWindow
        onDispatched: toggleWindow(namedWindow(message.name))
    }
}
