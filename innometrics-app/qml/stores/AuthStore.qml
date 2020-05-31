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

    Item {
        id: privates

        property var xhr: null
        property string base_url: "https://innometric.guru:9091";
    }

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

    Filter {
        type: ActionTypes.authLogin
        onDispatched: {
            cancelRunningLoginRequest();

            store.state = Innometrics.AuthState.Loading;

            // TODO: expose from Rust API
            const json = {
                "email": message.email,
                "password": message.password,
                "projectID": ""
            };

            const xhr = new XMLHttpRequest();
            xhr.onload = () => {
                if (xhr.status === 200) {
                    const json = JSON.parse(xhr.response);
                    const { token } = json;
                    AppActions.saveCredentials(message.email, message.password, token);
                    store.state = Innometrics.AuthState.Authorized;
                } else {
                    console.log(`Authentication failed: ` +
                                `code: ${xhr.status} text: ${xhr.statusText} ` +
                                `response type: ${xhr.responseType} response: ${xhr.response}`);
                    AppActions.authFailedRequiresAttention();
                }
            };
            xhr.open("POST", `${privates.base_url}/login`);
            xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
            xhr.send(JSON.stringify(json));
            privates.xhr = xhr;
        }
    }

    function cancelRunningLoginRequest() {
        if (privates.xhr !== null) {
            privates.xhr.abort();
        }
    }

    Filter {
        type: ActionTypes.authStopLoading
        onDispatched: {
            cancelRunningLoginRequest();
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
