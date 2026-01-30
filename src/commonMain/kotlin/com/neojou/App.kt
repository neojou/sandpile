package com.neojou

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.neojou.MyLog

private val TAG = "APP"

@Composable
fun App() {
    remember {
        SystemSettings.initOnce()
        //MyLog.add("Enter", TAG, LogLevel.DEBUG)
        true
    }

    MaterialTheme {
        SandpileGame()
    }
}
