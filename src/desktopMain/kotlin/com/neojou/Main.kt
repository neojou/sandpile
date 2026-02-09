package com.neojou

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Desktop entry point for the sandpile app.
 *
 * Creates a Compose for Desktop [Window] within [application] and hosts the shared [App] composable.
 * The window title is currently hard-coded; replace it with an app build info source (e.g. AppBuildInfo)
 * when available.
 */
fun main() {
    // NEW: 移除 setupMacAppName、MySystemInfo.showAll() 等
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "sandpile"  // 硬碼標題，未來用 AppBuildInfo
        ) {
            App()  // 直接呼叫 common App()
        }
    }
}