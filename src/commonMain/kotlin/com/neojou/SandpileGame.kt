package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.focusRequester

@Composable
fun SandpileGame() {
    val scope = rememberCoroutineScope()
    val vm = remember { SandpileViewModel(scope) }

    LaunchedEffect(Unit) {
        vm.start()
    }

    SandpileScreen(vm)
}