qrc!(
    pub register,
    "qml" as "qml" {
        "AccountManagerWindow.qml",
        "AccountManagerActor.qml",

        "main.qml",

        "actions/qmldir",
        "actions/AppActions.qml",
        "actions/ActionTypes.qml",
        "actions/WindowNames.qml",

        "middlewares/SystemMiddleware.qml",

        "stores/qmldir",
        "stores/MainStore.qml",
        "stores/RootStore.qml",
        "stores/UserPrefsStore.qml",
        "stores/MainWindowStore.qml",

        "views/MainWindow.qml",
        "views/TrayIcon.qml",
    },
    "images" as "images" {
        "tray-icon.png",
    },
);
