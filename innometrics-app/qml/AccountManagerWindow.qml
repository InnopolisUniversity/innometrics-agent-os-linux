import QtQuick 2.14
import QtQuick.Window 2.0;
import org.kde.kirigami 2.4 as Kirigami
import QtQuick.Layouts 1.2
import QtQuick.Controls 2.0 as QQC2
import org.kde.kirigami 2.8 as Kirigami
import guru.innometrics 1.0 as Innometrics

Kirigami.ApplicationWindow {
    id: kwin
    width: minimumWidth
    height: minimumHeight
    minimumWidth: 450
    minimumHeight: 300
    pageStack {
        initialPage: mainPageComponent
    }

    property var inno: Innometrics.AuthStore

    property bool autoPositionCenter: false
    Component.onCompleted: {
        if (autoPositionCenter) {
            positionOnScreenCenter();
        }
    }
    function positionOnScreenCenter() {
        x = Screen.width / 2 - width / 2
        y = Screen.height / 2 - height / 2
    }

//    states: [
//        State {
//            name: "NotAuthenticated"
//            when: enginioClient.authenticationState === Enginio.NotAuthenticated
//            PropertyChanges {
//                target: proccessButton
//                text: "Login"
//                onClicked: {
//                    enginioClient.identity = identity
//                }
//            }
//        },
//        State {
//            name: "Authenticating"
//            when: enginioClient.authenticationState === Enginio.Authenticating
//            PropertyChanges {
//                target: proccessButton
//                text: "Authenticating..."
//                enabled: false
//            }
//        },
//        State {
//            name: "AuthenticationFailure"
//            when: enginioClient.authenticationState === Enginio.AuthenticationFailure
//            PropertyChanges {
//                target: proccessButton
//                text: "Authentication failed, restart"
//                onClicked: {
//                    enginioClient.identity = null
//                }
//            }
//        },
//        State {
//            name: "Authenticated"
//            when: enginioClient.authenticationState === Enginio.Authenticated
//            PropertyChanges {
//                target: proccessButton
//                text: "Logout"
//                onClicked: {
//                    enginioClient.identity = null
//                }
//            }
//        }
//    ]

    Component {
        id: mainPageComponent
        Kirigami.Page {
            id: page
            title: "Innometrics Account"

            TextMetrics {
                id: textMetrics
                // font.family: "Arial"
                elide: Text.ElideNone
                // elideWidth: 1000
                text: "1234567890abcdefh@innopolis.ru"
            }


            Kirigami.FormLayout {
                id: form
                anchors.fill: parent

                QQC2.TextField {
                    id: fieldEmail
                    Kirigami.FormData.label: "Email:"
                    placeholderText: "Your email"
                    implicitWidth: textMetrics.width
                    Component.onCompleted: {text = Innometrics.AuthStore.credentials.email}
                    validator: RegExpValidator {
                        regExp: /.{1,}/
                    }
                }
                Kirigami.PasswordField {
                    id: fieldPassword
                    placeholderText: "Your password"
                    Kirigami.FormData.label: "Password:"
                    implicitWidth: textMetrics.width
                    Component.onCompleted: {text = Innometrics.AuthStore.credentials.password}
                    validator: RegExpValidator {
                        regExp: /.{1,}/
                    }
                }
                Kirigami.Separator {
                    Kirigami.FormData.isSection: true
                }
                Kirigami.InlineMessage {
                    id: inlineMessage

                    function stateToInlineMessageType(state) {
                        const map = {
                            [Innometrics.AuthState.None]: Kirigami.MessageType.Warning,
                            [Innometrics.AuthState.Authorized]: Kirigami.MessageType.Positive,
                            [Innometrics.AuthState.Failed]: Kirigami.MessageType.Error,
                        };
                        return map[state];
                    }

                    function trState(state) {
                        const map = {
                            [Innometrics.AuthState.None]: qsTr("Not logged in"),
                            [Innometrics.AuthState.Authorized]: qsTr("Authorized"),
                            [Innometrics.AuthState.Failed]: qsTr("Authorization failed"),
                        };
                        return map[state];
                    }

                    type: stateToInlineMessageType(inno.state)
                    text: trState(inno.state);
                    visible: true
                }
            }

            Item {
                id: actionSwitch
                function shouldBeActive(_state) {
                    // really only depends on whether the request is currently running
                    return true;
                }
            }

            Item {
                Kirigami.Action {
                    id: actionLogIn
                    text: "Log In"
                    iconName: "unlock"
                    enabled: actionSwitch.shouldBeActive() // is query running?
                    onTriggered: {
                        print("Action button log in in buttons page clicked");
                        inno.attemptLogIn({
                            "email": fieldEmail.text,
                            "password": fieldPassword.text,
                            "proj": "",
                        });
                    }
                }
                Kirigami.Action {
                    id: actionLogOut
                    text: "Log Out"
                    iconName: "lock"
                    enabled: actionSwitch.shouldBeActive() // is query running?
                    onTriggered: {
                        print("Action button log out in buttons page clicked");
                        inno.logOut();
                    }
                }
            }

            actions {
                main: !inno.isAuthorized ? actionLogIn : actionLogOut;
            }
        }
    }
}
