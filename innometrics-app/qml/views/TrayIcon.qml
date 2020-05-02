import QtQuick 2.0
import Qt.labs.platform 1.0

import guru.innometrics 1.0 as Innometrics

import "../actions"

SystemTrayIcon {
    visible: true
    iconSource: "qrc:///images/tray-icon.png"
    tooltip: "Innometrics - Linux Collector"

    onActivated: {
        AppActions.toggleWindow(WindowNames.account);
    }

    function trState(state) {
        const map = {
            [Innometrics.AuthState.None]: qsTr("Not logged in"),
            [Innometrics.AuthState.Authorized]: qsTr("Authorized"),
            [Innometrics.AuthState.Failed]: qsTr("Authorization failed"),
        };
        return map[state];
    }

    menu: Menu {
        visible: false
        MenuItem {
            // TODO: state
            text: "Status: " + trState(Innometrics.AuthState.Authorized)
            enabled: false
        }

        MenuItem {
            text: "Manage Account"
            onTriggered: AppActions.toggleWindow(WindowNames.account)
        }

        MenuItem {
            text: qsTr("Quit")
            onTriggered: AppActions.quit()
        }
    }
}
