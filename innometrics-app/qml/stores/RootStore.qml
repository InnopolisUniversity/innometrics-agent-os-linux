import QtQuick 2.0

import QuickFlux 1.1

Store {

    /// User's perferences
    property alias userPrefs: userPrefs

    UserPrefsStore {
        id: userPrefs
    }

    /// Holds main window with account management page
    property alias mainWindow: mainWindow

    MainWindowStore {
        id: mainWindow
    }

    // Credentials management
    property alias authStore: authStore

    AuthStore {
        id: authStore
    }
}
