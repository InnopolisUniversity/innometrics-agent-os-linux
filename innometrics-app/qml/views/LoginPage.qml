import QtQuick 2.14
import QtQuick.Controls 2.14 as QQC2
import QtQuick.Layouts 1.2
import QtQuick.Window 2.2

import org.kde.kirigami 2.8 as Kirigami
import QuickFlux 1.1

import guru.innometrics 1.0 as Innometrics

import "../actions"
import "../stores"

Kirigami.Page {
    property AuthStore authStore: MainStore.authStore

    Innometrics.AuthUtils {
        id: authUtils
        state: authStore.state
    }

    title: qsTr("Innometrics Account")

    TextMetrics {
        id: textMetrics
        elide: Text.ElideNone
        text: "1234567890abcdefh@innopolis.ru"
    }

    Kirigami.FormLayout {
        id: form
        anchors.fill: parent

        Kirigami.ActionTextField {
            Kirigami.FormData.label: qsTr("Email:")

            id: fieldEmail
            implicitWidth: textMetrics.width

            placeholderText: qsTr("Your email")
            text: MainStore.authStore.email

            validator: RegExpValidator {
                regExp: /.{1,}/
            }
            enabled: authStore.isNotAuthorizedNorLoading
            onAccepted: actionLogIn.trigger()
        }

        Kirigami.PasswordField {
            Kirigami.FormData.label: qsTr("Password:")

            id: fieldPassword
            implicitWidth: textMetrics.width

            text: MainStore.authStore.password

            validator: RegExpValidator {
                regExp: /.{1,}/
            }
            enabled: authStore.isNotAuthorizedNorLoading
            onAccepted: actionLogIn.trigger()
        }

        Kirigami.Separator {
            Kirigami.FormData.isSection: true
        }

        Kirigami.InlineMessage {
            Kirigami.FormData.label: qsTr("Status:")

            id: inlineMessage
            visible: true

            type: authUtils.inlineMessageType
            text: qsTr(authUtils.description)
        }

        QQC2.Button {
            text: "Save email &&& password"

            onClicked: {
                AppActions.saveCredentials(fieldEmail.text, fieldPassword.text, "abc_token_123");
            }
        }

        QQC2.Button {
            text: "Magic Log in"

            onClicked: {
                AppActions.authLogin(fieldEmail.text, "@@magic@@");
            }
        }
    }

    // either login, logout or stop loading
    Item {
        Kirigami.Action {
            id: actionLogIn
            text: qsTr("Log In")
            iconName: "unlock"

            enabled: fieldEmail.acceptableInput && fieldPassword.acceptableInput
            onTriggered: AppActions.authLogin(fieldEmail.text, fieldPassword.text)
        }

        Kirigami.Action {
            id: actionStopLoading
            text: qsTr("Stop Loading")
            iconName: "process-stop"

            onTriggered: AppActions.authStopLoading()
        }

        Kirigami.Action {
            id: actionLogOut
            text: qsTr("Log Out")
            iconName: "lock"

            onTriggered: AppActions.authLogout()
        }
    }

    actions.main: {
        const actions = {
            [Innometrics.AuthState.None]: actionLogIn,
            [Innometrics.AuthState.Failed]: actionLogIn,
            [Innometrics.AuthState.Loading]: actionStopLoading,
            [Innometrics.AuthState.Authorized]: actionLogOut,
        };
        return actions[authStore.state];
    }

    states: [
        State {
            name: "None"
            when: authStore.state === Innometrics.AuthState.None
        },
        State {
            name: "Loading"
            when: authStore.state === Innometrics.AuthState.Loading
        },
        State {
            name: "Authorized"
            when: authStore.state === Innometrics.AuthState.Authorized
        },
        State {
            name: "Failed"
            when: authStore.state === Innometrics.AuthState.Failed
        }
    ]
}
