package org.oar.bytes.features.animate

import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import java.util.function.BiConsumer
import kotlin.math.roundToLong

object Animator {
    const val END_ANIMATION = 1
    const val BLOCK_CHANGED = 2

    private val animations = mutableListOf<AnimationWrapper>()
    private var thread: Framer? = null

    val blockedGrid
        get() = animations.any { it.animation.blockingGrid }

    private val listeners = mutableListOf<AnimationListener>()

    fun join(list: List<AnimationChain>, consumer: BiConsumer<Int, Boolean>) {
        synchronized(listeners) {
            listeners.add(AnimationListener(list, consumer))
        }
        checkListeners()
    }

    fun addAndStart(chain: AnimationChain) {
        if (!chain.hasAnimations()) return
        val exists = animations.firstOrNull { it.animation.ref == chain.ref }
        if (exists != null) return

        chain.start()
        val animation = chain.next()!!
        AnimationWrapper(
            chain,
            System.currentTimeMillis(),
            animation
        ).also { animations.add(it) }

        animation.startAnimation()
        if (thread == null) {
            thread = Framer().apply { start() }
        }
    }

    fun addAndStart(chains: List<AnimationChain>) {
        val addChains = chains.filter { it.hasAnimations() }
        if (addChains.isEmpty()) return

        addChains
            .onEach {
                val found = animations.indexOfFirst { wrap -> wrap.animation.ref == it.ref }
                if (found >= 0) {
                    animations.removeAt(found).animation.endAnimation()
                }
            }
            .map { chain ->
                chain.start()
                val anim = chain.next()!!
                anim.startAnimation()
                AnimationWrapper(chain, System.currentTimeMillis(), anim)
            }
            .also { animations.addAll(it) }

        if (thread == null) {
            thread = Framer().apply { start() }
        }
    }

    fun stopAll() {
        animations.toList().forEach {
            it.animation.endAnimation()
            it.animation.applyAnimation()
            it.chain.end()
        }

        listeners.forEach { listener ->
            listener.consumer.accept(END_ANIMATION, true)
            if (listener.blocked) {
                listener.consumer.accept(BLOCK_CHANGED, false)
            }
        }

        animations.clear()
        listeners.clear()

        thread?.apply {
            interrupt()
            thread = null
        }
    }

    private fun update() {
        val removeList = mutableListOf<AnimationWrapper>()

        animations.toList().forEach {
            val animationEnded = if (it.started) {
                !it.animation.nextAnimation()
            } else {
                it.started = true
                false
            }

            if (animationEnded) {
                it.animation.endAnimation()
                it.animation.applyAnimation()

                val nextAnimation = it.chain.next()

                nextAnimation?.apply {
                    it.animation = this
                    startAnimation()
                    applyAnimation()
                }

                if (nextAnimation == null) {
                    it.chain.end()
                    removeList.add(it)
                }

            } else {
                it.animation.applyAnimation()
            }
        }

        animations.removeAll(removeList)

        checkListeners()
    }

    private fun checkListeners() {
        if (listeners.isNotEmpty()) {
            synchronized(listeners) {
                listeners.removeIf { listener ->
                    val anims = animations.filter { listener.animations.contains(it.chain) }

                    if (anims.isEmpty()) {
                        listener.consumer.accept(END_ANIMATION, true)
                        if (listener.blocked) {
                            listener.consumer.accept(BLOCK_CHANGED, false)
                        }
                        true

                    } else {
                        if (listener.blocked) {
                            val noBlockers = anims.none { it.animation.blockingGrid }
                            if (noBlockers) {
                                listener.blocked = false
                                listener.consumer.accept(BLOCK_CHANGED, false)
                            }
                        } else {
                            val anyBlocker = anims.any { it.animation.blockingGrid }
                            if (anyBlocker) {
                                listener.blocked = true
                                listener.consumer.accept(BLOCK_CHANGED, true)
                            }
                        }
                        false
                    }
                }
            }
        }
    }

    private class Framer : Thread() {
        override fun run() {
            val time = 1000 / FRAME_RATE

            try {
                while (animations.isNotEmpty()) {
                    val initTime = System.nanoTime()
                    update()
                    val endTime = System.nanoTime()

                    val sleepTime = (time - (endTime - initTime) / 1000000).roundToLong()
                    if (sleepTime > 0) {
                        sleep(sleepTime)
                    }
                }
            } catch (_: InterruptedException) { }
            thread = null
        }
    }
}
