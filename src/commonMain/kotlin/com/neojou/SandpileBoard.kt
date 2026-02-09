package com.neojou

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.min

private const val TAG = "SandpileBoard"

@Composable
fun SandpileBoard(
    vm: SandpileViewModel,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .onSizeChanged { vm.onCanvasSize(it.width, it.height) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    vm.panByPixels(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val c = event.changes.first()
                            val deltaY = c.scrollDelta.y
                            vm.zoomAtPixels(c.position.x, c.position.y, deltaY)
                            c.consume()
                        }
                    }
                }
            }
    ) {
        drawRect(Color(0xFF111111))

        val snap = vm.snapshot
        val blockPx = snap.blockPx
        val blocksX = snap.blocksX
        val blocksY = snap.blocksY
        val colors = snap.argbBlocks

        // 置中：snapshot 的 blocks 對應到 canvas 左上 (0,0)，所以不需要再算 startX/startY
        var i = 0
        for (bx in 0 until blocksX) {
            val x = bx * blockPx
            for (by in 0 until blocksY) {
                val y = by * blockPx
                val c = Color(colors[i++].toLong() and 0xFFFFFFFFL)
                drawRect(
                    color = c,
                    topLeft = Offset(x, y),
                    size = Size(blockPx, blockPx)
                )
            }
        }
    }
}
