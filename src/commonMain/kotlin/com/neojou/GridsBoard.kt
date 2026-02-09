package com.neojou

/**
 * Square sandpile board that stores per-cell height (grain count) and a derived ARGB color.
 *
 * The board uses a fixed size \(size x size\) and keeps two parallel arrays:
 * - [heights] for the number of grains in each cell.
 * - [argb] for the color of each cell derived from [heights] (see [applyColorRuleAtIndex]).
 *
 * This class also provides a stabilization routine (toppling) that redistributes grains
 * whenever a cell reaches the threshold (>= 4), using boundary dissipation.
 *
 * @property size Board edge length (number of cells per row/column).
 */
class GridsBoard(val size: Int = 401) {

    /**
     * Per-cell grain heights in row-major order.
     */
    private val heights = IntArray(size * size)

    /**
     * Per-cell ARGB colors in row-major order.
     *
     * Initialized to opaque white for all cells.
     */
    private val argb = IntArray(size * size) { 0xFFFFFFFF.toInt() } // 初始白

    /**
     * Total grains added to the system via [addGrainAtCenter].
     */
    var totalGrains: Long = 0
        private set

    /**
     * Converts a 2D coordinate ([x], [y]) into a 1D array index.
     */
    private fun idx(x: Int, y: Int) = x * size + y

    /**
     * Returns the height (grain count) at cell ([x], [y]).
     */
    fun heightAt(x: Int, y: Int): Int = heights[x * size + y]

    /**
     * Returns the ARGB color value at cell ([x], [y]).
     */
    fun argbAt(x: Int, y: Int): Int = argb[x * size + y]

    /**
     * Returns a snapshot of the cell ([x], [y]) as a [Grids] value.
     *
     * The returned [Grids] contains the current height and the RGB components decoded
     * from the internally stored ARGB integer.
     */
    fun getGrids(x: Int, y: Int): Grids {
        val i = idx(x, y)
        val c = argb[i]
        val r = (c ushr 16) and 0xFF
        val g = (c ushr 8) and 0xFF
        val b = (c ushr 0) and 0xFF
        return Grids(heights[i], r, g, b)
    }

    /**
     * Returns a copy of the internal ARGB buffer.
     *
     * This avoids exposing the mutable internal [argb] array to callers.
     */
    fun copyArgb(): IntArray = argb.copyOf()

    /**
     * Adds one grain to the center cell and stabilizes the board.
     *
     * This increments [totalGrains] and then triggers toppling via [stabilizeFrom].
     */
    fun addGrainAtCenter() {
        val c = size / 2
        addToCell(c, c, 1)
        totalGrains++
        stabilizeFrom(c, c)
    }

    /**
     * Adds [delta] grains to cell ([x], [y]) and updates its color.
     */
    private fun addToCell(x: Int, y: Int, delta: Int) {
        val i = idx(x, y)
        heights[i] += delta
        applyColorRuleAtIndex(i)
    }

    /**
     * Applies the current color rule for the cell at array index [i].
     *
     * Current rule uses `heights[i] mod 4` (via `and 3`) to choose one of four colors.
     * Keep any future, more complex coloring logic centralized here.
     */
    private fun applyColorRuleAtIndex(i: Int) {
        // 你之後想做更複雜顏色規則，就集中改這裡
        when (heights[i] and 3) {
            0 -> argb[i] = 0xFFFFFFFF.toInt() // 白
            1 -> argb[i] = 0xFF00FF00.toInt() // 綠
            2 -> argb[i] = 0xFF800080.toInt() // 紫
            else -> argb[i] = 0xFFFFD700.toInt() // 金
        }
    }

    /**
     * Stabilizes (topples) the sandpile starting from ([startX], [startY]).
     *
     * Uses a queue to process potentially unstable cells; when a cell height is >= 4,
     * it topples by distributing `t = h / 4` grains to its four von Neumann neighbors
     * and reducing itself by `4 * t`.
     *
     * Boundary dissipation: if a neighbor would be outside the board, those grains are
     * considered lost (not added anywhere).
     */
    private fun stabilizeFrom(startX: Int, startY: Int) {
        val q = ArrayDeque<Int>()
        q.addLast(idx(startX, startY))

        while (q.isNotEmpty()) {
            val i = q.removeFirst()
            val x = i / size
            val y = i % size

            val h = heights[i]
            if (h < 4) continue

            val t = h / 4
            heights[i] = h - 4 * t
            applyColorRuleAtIndex(i)

            // 邊界耗散：越界就丟出系統
            if (x > 0) { val j = idx(x - 1, y); heights[j] += t; applyColorRuleAtIndex(j); q.addLast(j) }
            if (x + 1 < size) { val j = idx(x + 1, y); heights[j] += t; applyColorRuleAtIndex(j); q.addLast(j) }
            if (y > 0) { val j = idx(x, y - 1); heights[j] += t; applyColorRuleAtIndex(j); q.addLast(j) }
            if (y + 1 < size) { val j = idx(x, y + 1); heights[j] += t; applyColorRuleAtIndex(j); q.addLast(j) }
        }
    }
}
