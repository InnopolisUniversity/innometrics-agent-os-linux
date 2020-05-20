import QtQuick 2.0
import Qt.labs.settings 1.0

import QuickFlux 1.1

import guru.innometrics 1.0 as Innometrics

import "../actions"

Store {
    id: store

    // synchronized with local settings
    property alias email: settings.email
    property string password: Qt.atob(settings.password) // base64
    property alias token: settings.token

    property int state: Innometrics.AuthState.None
    // shortcuts
    property bool isNotAuthorizedNorLoading: !isAuthorized && !isLoading
    property bool isLoading: state === Innometrics.AuthState.Loading
    property bool isAuthorized: state === Innometrics.AuthState.Authorized

    Settings {
        id: settings

        category: "Auth"
        property string email: ""
        property string password: Qt.btoa(store.password) // base64
        property string token: ""
    }

    function saveCredentials(email, password, token) {
        store.email = email;
        store.password = password;
        store.token = token;
    }



    Filter {
        type: ActionTypes.authLogin
        onDispatched: {
            // TODO: remove @@magic@@
            if (message.password == "@@magic@@") {
                store.state = Innometrics.AuthState.Authorized;
                return;
            }
            store.state = Innometrics.AuthState.Loading;
            // TODO: send request on server via Innometrics.Api
        }
    }

    Filter {
        type: ActionTypes.authStopLoading
        onDispatched: {
            store.state = Innometrics.AuthState.Failed;
            // TODO: cancel loading request
        }
    }

    Filter {
        type: ActionTypes.authLogout
        onDispatched: {
            store.state = Innometrics.AuthState.None;
            store.token = null;
        }
    }

    Filter {
        type: ActionTypes.saveCredentials
        onDispatched: (_, { email, password, token }) => saveCredentials(email, password, token)
    }
}
