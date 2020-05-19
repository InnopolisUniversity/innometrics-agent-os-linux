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
        "innometrics/apps/16/metrics-collector.png",
        "innometrics/apps/32/metrics-collector.png",
        "innometrics/apps/64/metrics-collector.png",
        "innometrics/apps/128/metrics-collector.png",
        "innometrics/apps/256/metrics-collector.png",
        "innometrics/apps/512/metrics-collector.png",
        "innometrics/apps/1024/metrics-collector.png",
    }
);
