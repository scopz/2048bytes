package org.oar.bytes.features.animate

abstract class Animation {
    abstract val ref: Any
    open val blockingGrid = true
    var pendingStart = true

    abstract fun startAnimation()
    abstract fun nextAnimation(): Boolean
    abstract fun applyAnimation()
    open fun endAnimation() {}

    fun createChain() = AnimationChain(ref).next { this }
}