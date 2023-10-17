package org.oar.bytes.utils

object Utils {
    fun hashOf(vararg ints: Int) = ints.fold(9973) { acc, it -> 31 * acc + it }
}