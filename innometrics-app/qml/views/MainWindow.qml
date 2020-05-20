import QtQuick 2.14
import QtQuick.Controls 2.14 as QQC2
import QtQuick.Layouts 1.2
import QtQuick.Window 2.2

import org.kde.kirigami 2.8 as Kirigami
import QuickFlux 1.1

import guru.innometrics 1.0 as Innometrics

import "../actions"
import "../stores"

Kirigami.ApplicationWindow {
    id: kwin
    width: 520
    height: minimumHeight
    minimumWidth: 450
    minimumHeight: 300

    pageStack.initialPage: loginPageComponent

    Component {
        id: loginPageComponent
        LoginPage {}
    }
}
