package com.neojou

/**
 * A value object that describes grid settings.
 *
 * @property height The grid height.
 * @property r The red channel component of the grid color.
 * @property g The green channel component of the grid color.
 * @property b The blue channel component of the grid color.
 */
data class Grids(
    val height: Int,
    val r: Int, val g: Int, val b: Int
)
