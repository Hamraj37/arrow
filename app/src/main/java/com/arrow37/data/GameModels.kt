package com.arrow37.data

import androidx.compose.runtime.Immutable

@Immutable
enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0)
}

@Immutable
data class Point(val x: Float, val y: Float)

@Immutable
data class Arrow(
    val id: String,
    val head: Point,
    val direction: Direction,
    val body: List<Point>,
    val isEscaping: Boolean = false,
    val isEscaped: Boolean = false
)

@Immutable
data class GameState(
    val arrows: List<Arrow> = emptyList(),
    val lives: Int = 3,
    val gridSize: Int = 12,
    val level: Int = 1,
    val levelName: String = "Hard",
    val isGameOver: Boolean = false,
    val isLevelCleared: Boolean = false,
    val history: List<List<Arrow>> = emptyList(),
    val redoStack: List<List<Arrow>> = emptyList(),
    val showGrid: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val useNativeRefreshRate: Boolean = true
)
