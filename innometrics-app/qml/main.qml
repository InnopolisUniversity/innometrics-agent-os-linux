import QuickFlux 1.1
import QtQuick 2.14
import QtQuick.Controls 2.14
import org.kde.kirigami 2.8 as Kirigami
import guru.innometrics 1.0 as Innometrics

Kirigami.ApplicationWindow {
    id: kwin
    width: minimumWidth
    height: minimumHeight
    minimumWidth: 450
    minimumHeight: 300

    pageStack.initialPage: mainPageComponent

    property var inno: Innometrics.AuthState

    Component.onCompleted: {
        print("Innometrics.AuthState.Authorized: " + Innometrics.AuthState.Authorized);
    }

    Component {
        id: mainPageComponent
        Kirigami.Page {
            id: page
            title: "Innometrics Account"

            Button {
                text: "Press"
                onClicked: {
                    print("ouch!")
                }
            }
        }
    }
}