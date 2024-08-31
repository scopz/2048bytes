package org.oar.bytes.features.animate

data class AnimationListener(
    val animations: List<AnimationChain>,
    val consumer: (Int, Boolean) -> Unit,
    var blocked: Boolean = true
)