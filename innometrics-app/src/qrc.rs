qrc!(
    pub register,
    "qml" as "qml" {
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
    "icons" as "icons" {
        "innometrics/index.theme",
        "innometrics/apps/22/cool-rect.png",
        "innometrics/apps/22/innometrics-tray.png",
        "innometrics/apps/64/innometrics-tray.png",
        "innometrics/apps/22/innometrics-tray-dark.png",
        "innometrics/apps/64/innometrics-tray-dark.png",
        "innometrics/apps/scalable/cool-rect.svg",
    }
);
