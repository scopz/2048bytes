package org.oar.bytes.features.animate

import java.util.function.BiConsumer

data class AnimationListener(
    val animations: List<AnimationChain>,
    val consumer: BiConsumer<Int, Boolean>,
    var blocked: Boolean = true
)