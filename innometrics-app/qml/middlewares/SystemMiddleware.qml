import QtQuick 2.0
import QtQuick.Dialogs 1.2

import QuickFlux 1.1

import "../actions"
import "../stores"

Middleware {
    function dispatch(type, message) {
        if (type === ActionTypes.startApp) {
            // print("Starting up");
        } else if (type === ActionTypes.quit) {
            return Qt.quit();
        }
        return next(type, message);
    }
}
