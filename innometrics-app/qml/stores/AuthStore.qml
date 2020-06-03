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

    property int state: settings.state
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
        // mirror of store.state, except for Loading is not persisted.
        property int state: Innometrics.AuthState.None
    }

    onStateChanged: {
        if (state !== Innometrics.AuthState.Loading) {
            settings.state = state;
        }
    }

    function saveCredentials(email, password, token) {
        store.email = email;
        store.password = password;
        store.token = token;
    }

    Innometrics.Api {
        id: api

        property var runningAuthRequest: null;

        onLoginSuccess: (token) => {
            AppActions.saveCredentials(runningAuthRequest.email, runningAuthRequest.password, token);
            store.state = Innometrics.AuthState.Authorized;
            runningAuthRequest = null;
        }

        onLoginFail: (status) => {
            console.log(`Authentication failed: code: ${status}`);
            AppActions.authFailedRequiresAttention();
            runningAuthRequest = null;
        }
    }

    Filter {
        type: ActionTypes.authLogin
        onDispatched: {
            store.state = Innometrics.AuthState.Loading;
            api.runningAuthRequest = api.createAuthRequest(message.email, message.password, "");
            api.login(api.runningAuthRequest);
        }
    }

    Filter {
        type: ActionTypes.authStopLoading
        onDispatched: {
            store.state = Innometrics.AuthState.Failed;
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

    Filter {
        type: ActionTypes.authFailedRequiresAttention
        onDispatched: {
            store.state = Innometrics.AuthState.Failed;
        }
    }

    function updateAuthToken() {
        AppActions.authLogin(email, password);
    }

    Component.onCompleted: {
        if (settings.state === Innometrics.AuthState.Authorized) {
            updateAuthToken();
        }
    }
}
