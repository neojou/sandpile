package com.neojou

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


/**
 * Log tag used by [App] for app-level logging.
 */
private const val TAG = "APP"

/**
* Root Composable of the application.
*
* This Composable:
* - Writes an "Enter" log event for tracing.
* - Initializes [SystemSettings] exactly once (via [remember]).
* - Applies [MaterialTheme] and renders the main content [SandpileGame].
*/
@Composable
fun App() {
    MyLog.add("Enter", TAG, LogLevel.DEBUG)

    remember {
        SystemSettings.initOnce()
        true
    }

    MaterialTheme {
        SandpileGame()
    }
}
