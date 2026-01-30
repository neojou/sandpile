package com.neojou

enum class PaletteId { USER, SCIFI, WARM, DEEP }

data class Palette(
    val id: PaletteId,
    val name: String,
    val colors: IntArray // index = height & 3, value = 0xAARRGGBB
)

object Palettes {

    // 你的原始配色：白/綠/紫/金 [file:3]
    val User = Palette(
        id = PaletteId.USER,
        name = "User (White/Green/Purple/Gold)",
        colors = intArrayOf(
            0xFFFFFFFF.toInt(), // 0 白
            0xFF00FF00.toInt(), // 1 綠
            0xFF800080.toInt(), // 2 紫
            0xFFFFD700.toInt()  // 3 金
        )
    )

    val SciFi = Palette(
        id = PaletteId.SCIFI,
        name = "Sci‑Fi",
        colors = intArrayOf(
            0xFF05060A.toInt(),
            0xFF00E5FF.toInt(),
            0xFFFF00FF.toInt(),
            0xFFB6FF00.toInt()
        )
    )

    val Warm = Palette(
        id = PaletteId.WARM,
        name = "Warm",
        colors = intArrayOf(
            0xFFFFFFFF.toInt(),
            0xFFFFE066.toInt(),
            0xFFFF9F1C.toInt(),
            0xFFD62828.toInt()
        )
    )

    val Deep = Palette(
        id = PaletteId.DEEP,
        name = "Deep",
        colors = intArrayOf(
            0xFF081A33.toInt(),
            0xFF00B8D9.toInt(),
            0xFF7C4DFF.toInt(),
            0xFFFFAB00.toInt()
        )
    )

    // 預設先用你的
    val Default: Palette = User
}
