import QtQuick 2.0
import Qt.labs.platform 1.1

import guru.innometrics 1.0 as Innometrics

import "../actions"
import "../stores"

SystemTrayIcon {
    visible: true
    iconSource: "qrc:/icons/innometrics/apps/256/metrics-collector.png"
    tooltip: "Innometrics - Linux Collector"

    onActivated: {
        AppActions.toggleWindow(WindowNames.account);
    }

    property Innometrics.AuthUtils authUtils: Innometrics.AuthUtils {
        state: MainStore.authStore.state
    }

    menu: Menu {
        visible: false

        MenuItem {
            text: "Status: " + qsTr(authUtils.description)
            enabled: false
        }

        MenuSeparator {}

        MenuItem {
            text: "Manage Account"
            onTriggered: AppActions.toggleWindow(WindowNames.account)
        }

        MenuItem {
            text: qsTr("Quit")
            onTriggered: AppActions.quit()

            role: MenuItem.QuitRole
        }
    }
}
