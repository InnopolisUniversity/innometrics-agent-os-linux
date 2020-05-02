pragma Singleton

import QtQuick 2.0
import QuickFlux 1.1

KeyTable {
    // KeyTable is an object with properties equal to its key name

    property string addTask

    property string setTaskDone

    property string setShowCompletedTasks

    property string startApp

    // system's window management
    property string showWindow
    property string toggleWindow
    property string quit
}
