qrc!(
    pub register,
    "qml" as "qml" {
        "AccountManagerWindow.qml",
        "AccountManagerActor.qml",
        "TrayIcon.qml",

        "main.qml",

        "actions/qmldir",
        "actions/AppActions.qml",
        "actions/ActionTypes.qml",

        "middlewares/SystemMiddleware.qml",

        "stores/qmldir",
        "stores/MainStore.qml",
        "stores/RootStore.qml",
        "stores/UserPrefsStore.qml",

        "views/MainWindow.qml",
    },
    "images" as "images" {
        "tray-icon.png",
    },
);
