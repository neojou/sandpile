package com.neojou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*

/**
 * Main screen for the sandpile game UI.
 *
 * Layout:
 * - A full-size [SandpileBoard] that renders the simulation output.
 * - A control row with zoom buttons and a counter label.
 *
 * Interaction:
 * - Zoom actions are delegated to [SandpileViewModel.zoomIn] and [SandpileViewModel.zoomOut].
 * - The grain counter uses [SandpileViewModel.snapshot] (e.g., `totalGrains`) for display.
 *
 * The commented section contains a keyboard-driven palette switcher (currently disabled).
 *
 * @param vm View model that owns simulation state and exposes UI actions.
 */
@Composable
fun SandpileScreen(vm: SandpileViewModel)
{
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SandpileBoard(vm = vm, modifier = Modifier.fillMaxSize())
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = vm::zoomIn) { Text("Zoom In") }
            Button(onClick = vm::zoomOut) { Text("Zoom Out") }
            Text("Grids : ${vm.snapshot.totalGrains}")
        }
    }

    /*
     * 不動態改變 palette , 先 mark off
     *

    Column(
        Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                when (e.key) {
                    Key.One -> { vm.setPalette(PaletteId.SCIFI); true }
                    Key.Two -> { vm.setPalette(PaletteId.WARM); true }
                    Key.Three -> { vm.setPalette(PaletteId.DEEP); true }
                    Key.P -> { vm.nextPalette(); true }
                    else -> false
                }
            }
    ) {
        SandpileBoard(vm, Modifier.weight(1f).fillMaxWidth())

        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { vm.setPalette(PaletteId.SCIFI) }) { Text("Sci‑Fi (1)") }
            Button(onClick = { vm.setPalette(PaletteId.WARM) }) { Text("Warm (2)") }
            Button(onClick = { vm.setPalette(PaletteId.DEEP) }) { Text("Deep (3)") }
            Text("Grids : ${vm.snapshot.totalGrains}")
        }
    }

     */
}
