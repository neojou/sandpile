package com.neojou

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.exp

class SandpileViewModel(
    private val scope: CoroutineScope,
    boardSize: Int = 3967
) {
    companion object {
        private const val TAG = "SandpileViewModel"
    }

    private val engine = SandpileEngine(size = boardSize)

    var snapshot by mutableStateOf(engine.snapshot.value)
        private set

    // camera (cell-space)
    private var centerCellX by mutableStateOf((boardSize / 2).toDouble())
    private var centerCellY by mutableStateOf((boardSize / 2).toDouble())

    // zoom：pxPerCell（要看全圖時會 < 1）
    var pxPerCell by mutableStateOf(0.25f)
        private set

    // 畫面尺寸（px）
    private var canvasW by mutableStateOf(1)
    private var canvasH by mutableStateOf(1)

    // zoom-out 時的品質/效能控制：每個 block 至少畫幾 px
    var minBlockPx by mutableStateOf(4f)
        private set

    fun start() {
        engine.start(scope)
        scope.launch { engine.snapshot.collect { snapshot = it } }
        pushViewport()
    }

    fun onCanvasSize(w: Int, h: Int) {
        canvasW = w.coerceAtLeast(1)
        canvasH = h.coerceAtLeast(1)
        pushViewport()
    }

    fun panByPixels(dxPx: Float, dyPx: Float) {
        centerCellX -= dxPx / pxPerCell
        centerCellY -= dyPx / pxPerCell
        pushViewport()
    }

    // wheelDeltaY：滾輪向上通常是負值/正值依平台而異，你可在 UI 端調整方向
    fun zoomAtPixels(anchorX: Float, anchorY: Float, wheelDeltaY: Float) {
        val old = pxPerCell
        val factor = exp((-wheelDeltaY * 0.001f).toDouble()).toFloat() // 平滑縮放
        val newPx = (old * factor).coerceIn(0.01f, 200f)

        // 以滑鼠所在點作 zoom anchor：放大後該點對應的 cell 不漂移
        val beforeX = centerCellX + (anchorX - canvasW / 2f) / old
        val beforeY = centerCellY + (anchorY - canvasH / 2f) / old

        pxPerCell = newPx

        val afterX = centerCellX + (anchorX - canvasW / 2f) / newPx
        val afterY = centerCellY + (anchorY - canvasH / 2f) / newPx

        centerCellX += (beforeX - afterX)
        centerCellY += (beforeY - afterY)

        pushViewport()
    }

    private fun pushViewport() {
        val spec = ViewportSpec(
            canvasW = canvasW,
            canvasH = canvasH,
            centerCellX = centerCellX,
            centerCellY = centerCellY,
            pxPerCell = pxPerCell,
            minBlockPx = minBlockPx
        )
        scope.launch { engine.updateViewport(spec) }
    }

    fun zoomByFactor(anchorX: Float, anchorY: Float, factor: Float) {
        val old = pxPerCell
        val newPx = (old * factor).coerceIn(0.01f, 200f)

        val beforeX = centerCellX + (anchorX - canvasW / 2f) / old
        val beforeY = centerCellY + (anchorY - canvasH / 2f) / old

        pxPerCell = newPx

        val afterX = centerCellX + (anchorX - canvasW / 2f) / newPx
        val afterY = centerCellY + (anchorY - canvasH / 2f) / newPx

        centerCellX += (beforeX - afterX)
        centerCellY += (beforeY - afterY)

        pushViewport()
    }

    fun zoomIn() {
        MyLog.add(TAG, "zoomIn")
        zoomByFactor(canvasW / 2f, canvasH / 2f, 1.2f)
    }

    fun zoomOut() {
        MyLog.add(TAG, "zoomOut")
        zoomByFactor(canvasW / 2f, canvasH / 2f, 1f / 1.2f)
    }

    var paletteId by mutableStateOf(PaletteId.SCIFI)
        private set

    /* mark off first
    fun setPalette(id: PaletteId) {
        paletteId = id
        scope.launch { engine.updatePalette(id) }
    }

    fun nextPalette() {
        val next = when (paletteId) {
            PaletteId.SCIFI -> PaletteId.WARM
            PaletteId.WARM -> PaletteId.DEEP
            PaletteId.DEEP -> PaletteId.SCIFI
        }
        setPalette(next)
    }
    */
}
