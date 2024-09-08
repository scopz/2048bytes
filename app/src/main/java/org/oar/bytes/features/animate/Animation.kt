package org.oar.bytes.features.animate

abstract class Animation(
    val ref: Any,
    val blockingGrid: Boolean
) {
    var pendingStart = true

    abstract fun startAnimation()
    abstract fun nextAnimation(): Boolean
    abstract fun applyAnimation()
    open fun endAnimation() {}

    fun createChain() = AnimationChain(ref).next { this }
}