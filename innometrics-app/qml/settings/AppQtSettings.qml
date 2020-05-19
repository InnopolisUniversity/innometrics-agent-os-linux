import QtQml 2.14

QtObject {
    Component.onCompleted: {
        Qt.application.version = "0.1";
        Qt.application.name = "Innometrics";
        Qt.application.organization = "Innopolis";
        Qt.application.domain = "guru.innometrics";
    }
}
