pragma Singleton

import QtQuick 2.0

import QuickFlux 1.1

ActionCreator {
    signal startApp()

    // system's window management
    signal showWindow(string name)
    signal toggleWindow(string name)
    signal quit()

    // account management
    signal authLogin(string email, string password)
    signal authStopLoading()
    signal authLogout()
    signal saveCredentials(string email, string password, string token)
    signal authFailedRequiresAttention()
}
