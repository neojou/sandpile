package com.neojou

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.yield
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Sandpile simulation engine with periodic rendering to a viewport snapshot.
 *
 * The engine runs a background coroutine that:
 * - Adds grains to the center cell for (roughly) one [period].
 * - Renders a downsampled view of the board into a [ViewportSnapshot] based on the latest
 *   [ViewportSpec] (if any).
 * - Publishes the result via [snapshot] for UI consumption.
 *
 * Threading model:
 * - The simulation loop runs on [Dispatchers.Default].
 * - Viewport updates are guarded by [viewportMutex] to allow safe updates from other coroutines.
 *
 * @property size Board edge length (number of cells per row/column).
 * @param period Time budget per loop iteration used to drive simulation steps.
 */
class SandpileEngine(
    val size: Int,
    private val period: Duration = 25.milliseconds
) {
    companion object {
        /** Log tag for this engine. */
        private const val TAG = "SandpileEngine"
    }

    /** Underlying sandpile board state. */
    private val board = GridsBoard(size)

    /** Guards concurrent access to [viewport]. */
    private val viewportMutex = Mutex()

    /** Latest viewport specification requested by the UI (nullable until first update). */
    private var viewport: ViewportSpec? = null

    /**
     * Active palette colors used for rendering.
     *
     * Indexing convention: `palette[height and 3]` -> `0xAARRGGBB`.
     */
    val palette = Palettes.Default.colors

    /* 不動態改變 / 先 mark
    private val paletteMutex = Mutex()
    private var paletteColors: IntArray = Palettes.SciFi.colors

    suspend fun updatePalette(id: PaletteId) {
        paletteMutex.withLock { paletteColors = Palettes.byId(id).colors }
    }
    */

    /**
     * Internal mutable snapshot state.
     *
     * Initialized with a minimal non-zero canvas to avoid invalid downstream assumptions.
     */
    private val _snapshot = MutableStateFlow(
        ViewportSnapshot(
            canvasW = 1, canvasH = 1,
            pxPerCell = 1f,
            blockCells = 1,
            originCellX = 0, originCellY = 0,
            blocksX = 1, blocksY = 1,
            blockPx = 1f,
            argbBlocks = intArrayOf(0xFF000000.toInt()),
            totalGrains = 0L
        )
    )

    /**
     * Latest rendered viewport snapshot suitable for UI rendering.
     *
     * This flow updates whenever the engine loop renders a new frame for the current viewport.
     */
    val snapshot: StateFlow<ViewportSnapshot> = _snapshot.asStateFlow()

    /** Background job created by [start], or null if not running. */
    private var job: Job? = null

    /**
     * Updates the current viewport specification used by the renderer.
     *
     * This is safe to call from any coroutine; the value is guarded by [viewportMutex].
     *
     * @param spec New viewport specification (canvas size, scale, and center position).
     */
    suspend fun updateViewport(spec: ViewportSpec) {
        viewportMutex.withLock { viewport = spec }
    }

    /**
     * Starts the simulation loop in [scope].
     *
     * If the engine is already running (job is non-null), this function logs a warning and returns.
     *
     * @param scope Coroutine scope that owns the engine lifecycle.
     */
    fun start(scope: CoroutineScope) {
        MyLog.add(TAG, "start()", LogLevel.DEBUG)
        if (job != null) {
            MyLog.add(TAG, "job($job) is not null, start() return", LogLevel.WARN)
            return
        }

        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val mark = TimeSource.Monotonic.markNow()

                // 盡量跑滿這個 period
                while (mark.elapsedNow() < period && isActive) {
                    board.addGrainAtCenter()
                }

                val spec = viewportMutex.withLock { viewport }
                if (spec != null && spec.canvasW > 0 && spec.canvasH > 0) {
                    //val palette = paletteMutex.withLock { paletteColors }
                    _snapshot.value = renderViewport(spec, board, palette)
                }

                yield()
            }
        }
    }

    /**
     * Stops the engine loop if running.
     *
     * Cancels the background job and clears the job reference.
     */
    fun stop() {
        job?.cancel()
        job = null
    }

    /**
     * Renders the given board [b] into a downsampled [ViewportSnapshot] for [spec].
     *
     * Rendering strategy:
     * - Converts pixels to cells using `pxPerCell`.
     * - Groups cells into blocks of [blockCells] to reduce work at small zoom levels.
     * - Computes each block color by averaging cell colors (see [averageArgbBlock]).
     *
     * @param spec Viewport parameters (canvas size, scale, and center position).
     * @param b Board to sample.
     * @param palette Palette lookup table indexed by `height and 3`.
     * @return A new [ViewportSnapshot] containing block colors and metadata.
     */
    private fun renderViewport(spec: ViewportSpec, b: GridsBoard, palette: IntArray): ViewportSnapshot {
        val pxPerCell = spec.pxPerCell.coerceIn(0.01f, 200f)
        val blockCells = max(1, ceil(spec.minBlockPx / pxPerCell).toInt())
        val blockPx = blockCells * pxPerCell

        val blocksX = max(1, ceil(spec.canvasW / blockPx).toInt() + 1)
        val blocksY = max(1, ceil(spec.canvasH / blockPx).toInt() + 1)

        // 把畫面左上角換算到 cell 座標，再對齊到 blockCells
        val worldLeft = spec.centerCellX - (spec.canvasW / 2.0) / pxPerCell
        val worldTop  = spec.centerCellY - (spec.canvasH / 2.0) / pxPerCell
        val originX = floor(worldLeft / blockCells).toInt() * blockCells
        val originY = floor(worldTop  / blockCells).toInt() * blockCells

        val out = IntArray(blocksX * blocksY)

        var i = 0
        for (bx in 0 until blocksX) {
            val cellX0 = originX + bx * blockCells
            for (by in 0 until blocksY) {
                val cellY0 = originY + by * blockCells
                out[i++] = averageArgbBlock(b, cellX0, cellY0, blockCells, palette)
            }
        }

        return ViewportSnapshot(
            canvasW = spec.canvasW,
            canvasH = spec.canvasH,
            pxPerCell = pxPerCell,
            blockCells = blockCells,
            originCellX = originX,
            originCellY = originY,
            blocksX = blocksX,
            blocksY = blocksY,
            blockPx = blockPx,
            argbBlocks = out,
            totalGrains = b.totalGrains
        )
    }

    /**
     * Computes the average ARGB color for a block of cells.
     *
     * Only cells within board bounds are included in the average.
     * If the block contains no in-bounds cells, this returns opaque black (`0xFF000000`).
     *
     * @param b Board to sample.
     * @param x0 Block origin X in cell coordinates.
     * @param y0 Block origin Y in cell coordinates.
     * @param blockCells Block width/height in cells.
     * @param palette Palette lookup table indexed by `height and 3`.
     * @return Averaged ARGB color for the block.
     */
    private fun averageArgbBlock(b: GridsBoard, x0: Int, y0: Int, blockCells: Int, palette: IntArray): Int {
        var sr = 0L; var sg = 0L; var sb = 0L; var count = 0L

        for (dx in 0 until blockCells) {
            val x = x0 + dx
            if (x !in 0 until b.size) continue
            for (dy in 0 until blockCells) {
                val y = y0 + dy
                if (y !in 0 until b.size) continue

                val h = b.heightAt(x, y)
                val c = palette[h and 3]
                //val c = b.argbAt(x, y)
                sr += (c ushr 16) and 0xFF
                sg += (c ushr 8) and 0xFF
                sb += (c ushr 0) and 0xFF
                count++
            }
        }

        if (count == 0L) return 0xFF000000.toInt()

        val r = (sr / count).toInt().coerceIn(0, 255)
        val g = (sg / count).toInt().coerceIn(0, 255)
        val bl = (sb / count).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
    }
}
