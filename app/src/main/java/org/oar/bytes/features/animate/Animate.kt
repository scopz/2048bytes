package org.oar.bytes.features.animate

interface Animate {
    fun start()
    fun updateAnimation(moment: Long): Boolean
    fun end(moment: Long)
}