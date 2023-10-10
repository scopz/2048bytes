package org.oar.bytes.features.animate

import java.util.function.Function

class AnimationChain(
    val ref: Any
) {
    private var nextAction = mutableListOf<Function<AnimationChain, Animation>>()
    private var endAction = mutableListOf<Runnable>()
    private val extras = mutableMapOf<String, Any>()

    companion object {
        fun reduce(list : List<AnimationChain>): List<AnimationChain> {
            val uniqueList = list.distinctBy { it.ref }
            val rest = list.filter { !uniqueList.contains(it) }
            rest.forEach {
                val chain = uniqueList.first { d -> d.ref == it.ref }
                chain.nextAction.addAll(it.nextAction)
                chain.endAction.addAll(it.endAction)
            }
            return uniqueList
        }
    }

    fun hasAnimations() = nextAction.isNotEmpty()

    fun next(next: Function<AnimationChain, Animation>): AnimationChain {
        this.nextAction.add(next)
        return this
    }

    internal fun next(): Animation? {
        if (nextAction.isNotEmpty()) {
            return nextAction.removeAt(0).apply(this)
        }
        return null
    }

    fun end(end: Runnable): AnimationChain {
        this.endAction.add(end)
        return this
    }

    internal fun end() {
        endAction.forEach(Runnable::run)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String) = extras[key] as T?

    operator fun set(key: String, value: Any) {
        extras[key] = value
    }
}