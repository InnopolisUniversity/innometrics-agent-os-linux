import QtQuick 2.0
import QtQuick.Window 2.0
import guru.innometrics 1.0 as Innometrics

Item {
    id: root

    Connections {
        target: Innometrics.AuthStore
        onShouldManageAccountChanged: abc();
        onShouldActivateWindow: activateAccountWindow();
    }

    property var _window: null

    Component.onCompleted: abc();

    function abc() {
        if (Innometrics.AuthStore.shouldManageAccount) {
            showAccountManager();
        } else {
            hideAccountManager();
        }
    }

    function showAccountManager() {
        if (_window === null) {
            _window = accountManagerComponent.createObject();
            _window.show();
            _window.raise();
            _window.requestActivate();
        }
    }

    function hideAccountManager() {
       if (_window !== null) {
           _window.close();
       }
    }

    function activateAccountWindow() {
        if (_window !== null) {
            _window.show();
            _window.raise();
            _window.requestActivate();
        }
    }

    function positionCenter() {
        if (_window !== null) {
        }
    }

    Component {
        id: accountManagerComponent
        AccountManagerWindow {
            autoPositionCenter: true
            onClosing: {
                root._window = null;
                print("No more manager");
            }
        }
    }
}
