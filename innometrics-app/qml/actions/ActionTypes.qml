pragma Singleton

import QtQuick 2.0

import QuickFlux 1.1

KeyTable {
    property string startApp

    // system's window management
    property string showWindow
    property string toggleWindow
    property string quit

    // account management
    property string authLogin
    property string authStopLoading
    property string authLogout
    property string saveCredentials
    property string authFailedRequiresAttention
}
