package com.neojou

import androidx.compose.runtime.mutableStateOf

data class ViewportSpec(
    val canvasW: Int,
    val canvasH: Int,
    val centerCellX: Double,
    val centerCellY: Double,
    val pxPerCell: Float,     // zoom：每個 cell 幾個像素
    val minBlockPx: Float     // zoom-out 時每個區塊至少畫多大像素（用來控效能）
)

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

