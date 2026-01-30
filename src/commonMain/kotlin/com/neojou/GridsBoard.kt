package com.neojou

class GridsBoard(val size: Int = 401) {

    private val heights = IntArray(size * size)
    private val argb = IntArray(size * size) { 0xFFFFFFFF.toInt() } // 初始白

    var totalGrains: Long = 0
        private set

    private fun idx(x: Int, y: Int) = x * size + y

    fun heightAt(x: Int, y: Int): Int = heights[x * size + y]

    fun argbAt(x: Int, y: Int): Int = argb[x * size + y]

    fun getGrids(x: Int, y: Int): Grids {
        val i = idx(x, y)
        val c = argb[i]
        val r = (c ushr 16) and 0xFF
        val g = (c ushr 8) and 0xFF
        val b = (c ushr 0) and 0xFF
        return Grids(heights[i], r, g, b)
    }

    fun copyArgb(): IntArray = argb.copyOf()

    fun addGrainAtCenter() {
        val c = size / 2
        addToCell(c, c, 1)
        totalGrains++
        stabilizeFrom(c, c)
    }

    private fun addToCell(x: Int, y: Int, delta: Int) {
        val i = idx(x, y)
        heights[i] += delta
        applyColorRuleAtIndex(i)
    }


    private fun applyColorRuleAtIndex(i: Int) {
        // 你之後想做更複雜顏色規則，就集中改這裡
        when (heights[i] and 3) {
            0 -> argb[i] = 0xFFFFFFFF.toInt() // 白
            1 -> argb[i] = 0xFF00FF00.toInt() // 綠
            2 -> argb[i] = 0xFF800080.toInt() // 紫
            else -> argb[i] = 0xFFFFD700.toInt() // 金
        }
    }

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
