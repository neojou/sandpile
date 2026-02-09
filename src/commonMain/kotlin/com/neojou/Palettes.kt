package com.neojou

/**
 * Identifier of a predefined color palette.
 */
enum class PaletteId {
    /** User-defined / original palette. */
    USER,

    /** Sci‑Fi themed palette. */
    SCIFI,

    /** Warm color palette. */
    WARM,

    /** Deep / high-contrast palette. */
    DEEP
}

/**
 * A color palette used to map sandpile cell heights to ARGB colors.
 *
 * The [colors] array is expected to have 4 entries and is indexed by `height & 3`
 * (i.e., height modulo 4), where each value is an `0xAARRGGBB` integer.
 *
 * @property id Stable identifier of this palette.
 * @property name Human-readable palette name (for UI display).
 * @property colors ARGB colors indexed by `height & 3`, each value is `0xAARRGGBB`.
 */
data class Palette(
    val id: PaletteId,
    val name: String,
    val colors: IntArray // index = height & 3, value = 0xAARRGGBB
)

/**
 * Registry of built-in palettes.
 *
 * All palettes here are immutable references; use [Default] as the app's initial selection.
 */
object Palettes {

    /**
     * Original user palette: White / Green / Purple / Gold.
     */
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

    /**
     * Sci‑Fi themed palette.
     */
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

    /**
     * Warm palette with bright highlights.
     */
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

    /**
     * Deep palette with darker base tones.
     */
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

    /**
     * Default palette used by the app at startup.
     */
    val Default: Palette = User
}
