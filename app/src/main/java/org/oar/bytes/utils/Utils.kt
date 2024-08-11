package org.oar.bytes.utils

object Utils {
    fun hashOf(vararg ints: Int) = ints.fold(9973) { acc, it -> 31 * acc + it }

    fun Boolean.ifTrue(callback: () -> Unit): Executor? =
        if (this) {
            callback()
            null
        } else ELSE_INSTANCE

    fun Boolean.ifFalse(callback: () -> Unit): Executor? =
        if (!this) {
            callback()
            null
        } else ELSE_INSTANCE

    fun Executor?.otherwise(callback: () -> Unit) {
        if (this != null) callback()
    }

    private val ELSE_INSTANCE = Executor()
    class Executor
}