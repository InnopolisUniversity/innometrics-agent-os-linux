import QtQuick 2.0
import Qt.labs.platform 1.0
import guru.innometrics 1.0 as Innometrics

SystemTrayIcon {
    visible: true
    iconSource: "qrc:/images/tray-icon.png"
    tooltip: "Innometrics - Linux Collector"

    onActivated: {
        Innometrics.AuthStore.focusOpenedWindow();
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
            text: "Status: " + trState(Innometrics.AuthStore.state)
            enabled: false
            iconName: Innometrics.AuthStore.isAuthorized ? "unlock" : "lock"
            iconSource: "qrc:///images/tray-icon.png"
        }

        MenuItem {
            text: "Manage Account"
            onTriggered: Innometrics.AuthStore.manageAccount(
                             !Innometrics.AuthStore.shouldManageAccount)
        }

        MenuItem {
            text: qsTr("Quit")
            onTriggered: Qt.quit()
        }
    }
}
