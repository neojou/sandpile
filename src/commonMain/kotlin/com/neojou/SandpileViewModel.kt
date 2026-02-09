package com.neojou

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.exp

/**
 * View-model for the sandpile UI.
 *
 * This class bridges UI interactions (pan/zoom, canvas resize) to the simulation engine by
 * publishing viewport parameters and exposing the latest [snapshot] as Compose state.
 *
 * Lifecycle:
 * - Call [start] once to start the underlying [SandpileEngine] and begin collecting its snapshots.
 *
 * @param scope Coroutine scope that owns the engine lifecycle and snapshot collection.
 * @param boardSize Board edge length (cells per row/column) used to construct the engine.
 */
class SandpileViewModel(
    private val scope: CoroutineScope,
    boardSize: Int = 3967
) {
    companion object {
        /** Log tag for this view-model. */
        private const val TAG = "SandpileViewModel"
    }

    /** Simulation engine that owns the sandpile board and rendering pipeline. */
    private val engine = SandpileEngine(size = boardSize)

    /**
     * Latest rendered viewport snapshot.
     *
     * This is updated by collecting [SandpileEngine.snapshot] in [start].
     */
    var snapshot by mutableStateOf(engine.snapshot.value)
        private set

    /**
     * Camera center (cell-space X coordinate).
     */
    private var centerCellX by mutableStateOf((boardSize / 2).toDouble())

    /**
     * Camera center (cell-space Y coordinate).
     */
    private var centerCellY by mutableStateOf((boardSize / 2).toDouble())

    /**
     * Zoom level expressed as pixels per cell.
     *
     * Values may be < 1 when zoomed out to see more of the board.
     */
    var pxPerCell by mutableStateOf(0.25f)
        private set

    /**
     * Current canvas width in pixels.
     *
     * Used to convert pixel deltas into cell-space deltas and to compute zoom anchors.
     */
    private var canvasW by mutableStateOf(1)

    /**
     * Current canvas height in pixels.
     *
     * Used to convert pixel deltas into cell-space deltas and to compute zoom anchors.
     */
    private var canvasH by mutableStateOf(1)

    /**
     * Quality/performance knob for zoomed-out rendering.
     *
     * The renderer will choose a block size such that each block is at least this many pixels.
     */
    var minBlockPx by mutableStateOf(4f)
        private set

    /**
     * Starts the simulation engine and begins collecting viewport snapshots into [snapshot].
     *
     * Call this once during screen setup; repeated calls would start additional collectors.
     */
    fun start() {
        engine.start(scope)
        scope.launch { engine.snapshot.collect { snapshot = it } }
        pushViewport()
    }

    /**
     * Updates the canvas size and pushes a new viewport to the engine.
     *
     * @param w Canvas width in pixels (coerced to at least 1).
     * @param h Canvas height in pixels (coerced to at least 1).
     */
    fun onCanvasSize(w: Int, h: Int) {
        canvasW = w.coerceAtLeast(1)
        canvasH = h.coerceAtLeast(1)
        pushViewport()
    }

    /**
     * Pans the camera by a pixel delta.
     *
     * The pixel delta is converted into cell-space using the current [pxPerCell].
     *
     * @param dxPx Delta X in pixels (positive moves content right; camera center moves left).
     * @param dyPx Delta Y in pixels (positive moves content down; camera center moves up).
     */
    fun panByPixels(dxPx: Float, dyPx: Float) {
        centerCellX -= dxPx / pxPerCell
        centerCellY -= dyPx / pxPerCell
        pushViewport()
    }

    /**
     * Zooms around a pixel anchor using a smooth exponential factor derived from wheel input.
     *
     * The anchor behavior is stable: after zooming, the cell under ([anchorX], [anchorY])
     * stays the same by adjusting [centerCellX]/[centerCellY].
     *
     * @param anchorX Anchor X in canvas pixels (typically mouse position).
     * @param anchorY Anchor Y in canvas pixels (typically mouse position).
     * @param wheelDeltaY Wheel delta (sign/direction may vary by platform).
     */
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

    /**
     * Pushes the current camera/canvas parameters to the engine as a [ViewportSpec].
     *
     * This launches an async update on [scope].
     */
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

    /**
     * Zooms by a multiplicative [factor] around the given pixel anchor.
     *
     * This is the same anchor-stable zoom behavior as [zoomAtPixels], but uses a direct factor.
     *
     * @param anchorX Anchor X in canvas pixels.
     * @param anchorY Anchor Y in canvas pixels.
     * @param factor Multiplicative zoom factor; values > 1 zoom in, < 1 zoom out.
     */
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

    /**
     * Zooms in around the canvas center.
     */
    fun zoomIn() {
        MyLog.add(TAG, "zoomIn")
        zoomByFactor(canvasW / 2f, canvasH / 2f, 1.2f)
    }

    /**
     * Zooms out around the canvas center.
     */
    fun zoomOut() {
        MyLog.add(TAG, "zoomOut")
        zoomByFactor(canvasW / 2f, canvasH / 2f, 1f / 1.2f)
    }

    /**
     * Currently selected palette id (palette switching is currently disabled/marked off).
     */
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
