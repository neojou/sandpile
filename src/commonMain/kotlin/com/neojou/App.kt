package com.neojou

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.neojou.MyLog


private const val TAG = "APP"

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
