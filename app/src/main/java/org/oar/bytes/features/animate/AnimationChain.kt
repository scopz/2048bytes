package org.oar.bytes.features.animate

class AnimationChain(
    val ref: Any
) {
    private var startAction = mutableListOf<Runnable>()
    private var nextAction = mutableListOf<(AnimationChain) -> Animation>()
    private var endAction = mutableListOf<Runnable>()
    private val data = mutableMapOf<String, Any>()

    companion object {
        fun reduce(list : List<AnimationChain>): List<AnimationChain> {
            val uniqueList = list.distinctBy { it.ref }
            val rest = list.filter { !uniqueList.contains(it) }
            rest.forEach {
                val chain = uniqueList.first { d -> d.ref == it.ref }
                chain.startAction.addAll(it.startAction)
                chain.nextAction.addAll(it.nextAction)
                chain.endAction.addAll(it.endAction)
            }
            return uniqueList
        }
    }

    fun hasNext() = nextAction.isNotEmpty()

    fun next(next: (AnimationChain) -> Animation): AnimationChain {
        this.nextAction.add(next)
        return this
    }

    internal fun next(): Animation? {
        if (nextAction.isNotEmpty()) {
            return nextAction.removeAt(0)(this)
        }
        return null
    }

    fun start(start: Runnable): AnimationChain {
        this.startAction.add(start)
        return this
    }

    internal fun start() {
        startAction.forEach(Runnable::run)
    }

    fun end(end: Runnable): AnimationChain {
        this.endAction.add(end)
        return this
    }

    internal fun end() {
        endAction.forEach(Runnable::run)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String) = data[key] as T?

    operator fun set(key: String, value: Any) {
        data[key] = value
    }
}