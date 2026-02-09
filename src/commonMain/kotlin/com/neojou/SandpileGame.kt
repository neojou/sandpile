package com.neojou

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope


/**
 * Entry-point Composable for the sandpile game UI.
 *
 * Responsibilities:
 * - Creates a coroutine scope via [rememberCoroutineScope] for long-running work tied to composition.
 * - Instantiates and remembers a [SandpileViewModel].
 * - Triggers [SandpileViewModel.start] once using [LaunchedEffect].
 * - Renders the UI via [SandpileScreen].
 *
 * This Composable has a side effect (starting the engine) that is scoped to the composition
 * lifecycle through [LaunchedEffect].
 */
@Composable
fun SandpileGame() {
    val scope = rememberCoroutineScope()
    val vm = remember { SandpileViewModel(scope) }

    LaunchedEffect(Unit) {
        vm.start()
    }

    SandpileScreen(vm)
}