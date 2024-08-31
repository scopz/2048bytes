package org.oar.bytes.features.animate

data class AnimationWrapper(
    val chain: AnimationChain,
    val startTime: Long,
    var animation: Animation,
)