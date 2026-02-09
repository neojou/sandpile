package com.neojou

import androidx.compose.runtime.mutableStateOf

/**
 * Immutable viewport parameters requested by the UI for rendering.
 *
 * This spec describes how to map the world (cell-space) onto the canvas (pixel-space),
 * and is consumed by the renderer to produce a corresponding [ViewportSnapshot].
 *
 * @property canvasW Canvas width in pixels.
 * @property canvasH Canvas height in pixels.
 * @property centerCellX Viewport center X in cell-space coordinates.
 * @property centerCellY Viewport center Y in cell-space coordinates.
 * @property pxPerCell Zoom level: how many pixels represent one cell.
 * @property minBlockPx Minimum pixel size per rendered block when zoomed out (performance/quality control).
 */
data class ViewportSpec(
    val canvasW: Int,
    val canvasH: Int,
    val centerCellX: Double,
    val centerCellY: Double,
    val pxPerCell: Float,     // zoom：每個 cell 幾個像素
    val minBlockPx: Float     // zoom-out 時每個區塊至少畫多大像素（用來控效能）
)

/**
 * Rendered result of a viewport, suitable for fast UI drawing.
 *
 * The renderer downsamples the board into blocks of [blockCells] x [blockCells] cells.
 * Each entry in [argbBlocks] stores the averaged color of one block.
 *
 * Indexing:
 * - `argbBlocks` length is `blocksX * blocksY`.
 * - The block at `(bx, by)` is located at index `bx * blocksY + by` (consistent with the producer loop).
 *
 * @property canvasW Canvas width in pixels used for rendering this snapshot.
 * @property canvasH Canvas height in pixels used for rendering this snapshot.
 * @property pxPerCell Effective pixels-per-cell used during rendering (after coercion/clamping).
 * @property blockCells Block size in cells (one block represents this many cells per side).
 * @property originCellX Cell-space X coordinate of the top-left corner after block alignment.
 * @property originCellY Cell-space Y coordinate of the top-left corner after block alignment.
 * @property blocksX Number of blocks along the X direction.
 * @property blocksY Number of blocks along the Y direction.
 * @property blockPx Block size in pixels, equal to `blockCells * pxPerCell`.
 * @property argbBlocks ARGB color buffer for blocks; each value is `0xAARRGGBB`.
 * @property totalGrains Total grains added to the simulation (copied from the board/engine).
 */
data class ViewportSnapshot(
    val canvasW: Int,
    val canvasH: Int,
    val pxPerCell: Float,
    val blockCells: Int,
    val originCellX: Int,     // 左上角對應的 cell (block 對齊後)
    val originCellY: Int,
    val blocksX: Int,
    val blocksY: Int,
    val blockPx: Float,       // blockCells * pxPerCell
    val argbBlocks: IntArray, // length = blocksX*blocksY，每格是一個 block 的「平均色」
    val totalGrains: Long
)

