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

class SandpileEngine(
    val size: Int,
    private val period: Duration = 25.milliseconds
) {
    companion object {
        private const val TAG = "SandpileEngine"
    }

    private val board = GridsBoard(size)

    private val viewportMutex = Mutex()
    private var viewport: ViewportSpec? = null

    val palette = Palettes.Default.colors
    /* 不動態改變 / 先 mark
    private val paletteMutex = Mutex()
    private var paletteColors: IntArray = Palettes.SciFi.colors

    suspend fun updatePalette(id: PaletteId) {
        paletteMutex.withLock { paletteColors = Palettes.byId(id).colors }
    }
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
    val snapshot: StateFlow<ViewportSnapshot> = _snapshot.asStateFlow()

    private var job: Job? = null

    suspend fun updateViewport(spec: ViewportSpec) {
        viewportMutex.withLock { viewport = spec }
    }

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

    fun stop() {
        job?.cancel()
        job = null
    }

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
