package org.oar.bytes.features.animate

interface Animation {
    val ref: Any
    val blockingGrid: Boolean
        get() = true

    fun startAnimation()
    fun nextAnimation(): Boolean
    fun applyAnimation()
    fun endAnimation() {}
}