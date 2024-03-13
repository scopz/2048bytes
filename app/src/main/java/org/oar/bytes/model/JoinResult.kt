package org.oar.bytes.model

data class JoinResult (
    val mergedTiles: Int,
    val mergedValue: SByte,
    val mergedLevels: List<Int>
) : Cloneable
