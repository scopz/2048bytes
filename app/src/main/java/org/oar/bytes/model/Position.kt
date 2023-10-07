package org.oar.bytes.model

data class Position(
    val x: Int,
    val y: Int
) {

    init {
        if (x < 0 || x > 3 || y < 0 || y > 3) {
            throw Exception("Position out of range $x,$y")
        }
    }

    val left: Position?
        get() = if (x > 0) Position(x-1, y) else null

    val right: Position?
        get() = if (x < 3) Position(x+1, y) else null

    val top: Position?
        get() = if (y > 0) Position(x, y-1) else null

    val bottom: Position?
        get() = if (y < 3) Position(x, y+1) else null

    val leftPositions: List<Position>
        get() = (0 until x).map { Position(it, y) }.reversed()

    val rightPositions: List<Position>
        get() = (x+1 until 4).map { Position(it, y) }

    val topPositions: List<Position>
        get() = (0 until y).map { Position(x, it) }.reversed()

    val botPositions: List<Position>
        get() = (y+1 until 4).map { Position(x, it) }
}

