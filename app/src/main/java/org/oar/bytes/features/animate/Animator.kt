package org.oar.bytes.features.animate

import android.os.Handler
import android.os.HandlerThread
import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import org.oar.bytes.utils.extensions.ListExt.syncAdd
import org.oar.bytes.utils.extensions.ListExt.syncAny
import org.oar.bytes.utils.extensions.ListExt.syncFilter
import org.oar.bytes.utils.extensions.ListExt.syncRemoveIf

object Animator {
    const val END_ANIMATION = 1
    const val BLOCK_CHANGED = 2

    private val animations = mutableListOf<AnimationWrapper>()
    private var thread: Framer? = null

    val blockedGrid: Boolean
        get() = animations.syncAny { it.animation.blockingGrid }

    private val listeners = mutableListOf<AnimationListener>()

    fun listenAnimationsEnd(list: List<AnimationChain>, consumer: (Int, Boolean) -> Unit) {
        listeners.syncAdd(AnimationListener(list, consumer))
        checkListeners()
    }

    fun addAndStart(chain: AnimationChain) {
        if (!chain.hasNext()) return
        synchronized(animations) {
            val exists = animations.firstOrNull { it.animation.ref == chain.ref }
            if (exists != null) return

            chain.start()
            val animation = chain.next()!!
                .apply { pendingStart = true }
            animations.add(
                AnimationWrapper(chain, System.currentTimeMillis(), animation)
            )
        }

        if (thread == null) {
            thread = Framer().apply { start() }
        }
    }

    fun addAndStart(chains: List<AnimationChain>) {
        val addChains = chains.filter { it.hasNext() }
        if (addChains.isEmpty()) return

        synchronized(animations) {
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
                        .apply { pendingStart = true }
                    AnimationWrapper(chain, System.currentTimeMillis(), anim)
                }
                .also { animations.addAll(it) }
        }

        if (thread == null) {
            thread = Framer().apply { start() }
        }
    }

    fun stopAll() {
        synchronized(animations) {
            animations.forEach {
                it.animation.endAnimation()
                it.animation.applyAnimation()
                it.chain.end()
            }
            animations.clear()
        }

        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.consumer(END_ANIMATION, true)
                if (listener.blocked) {
                    listener.consumer(BLOCK_CHANGED, false)
                }
            }
            listeners.clear()
        }

        thread?.apply {
            quitSafely()
            thread = null
        }
    }

    private fun update() {

        animations.syncRemoveIf { wrapper ->
            val animation = wrapper.animation

            if (animation.pendingStart) {
                animation.pendingStart = false
                animation.startAnimation()
            }

            val animationEnded = !animation.nextAnimation()
            if (animationEnded) {
                animation.endAnimation()
                animation.applyAnimation()

                wrapper.chain.next()
                    ?.apply {
                        wrapper.animation = this
                        pendingStart = true
                    }
                    ?: run {
                        wrapper.chain.end()
                        return@syncRemoveIf true
                    }

            } else {
                animation.applyAnimation()
            }
            return@syncRemoveIf false
        }

        checkListeners()
    }

    private fun checkListeners() {
        listeners.syncRemoveIf { listener ->
            val anims = animations.syncFilter { listener.animations.contains(it.chain) }

            if (anims.isEmpty()) {
                listener.consumer(END_ANIMATION, true)
                if (listener.blocked) {
                    listener.consumer(BLOCK_CHANGED, false)
                }
                true

            } else {
                if (listener.blocked) {
                    val noBlockers = anims.none { it.animation.blockingGrid }
                    if (noBlockers) {
                        listener.blocked = false
                        listener.consumer(BLOCK_CHANGED, false)
                    }
                } else {
                    val anyBlocker = anims.any { it.animation.blockingGrid }
                    if (anyBlocker) {
                        listener.blocked = true
                        listener.consumer(BLOCK_CHANGED, true)
                    }
                }
                false
            }
        }
    }

    private class Framer : HandlerThread("animator") {
        private val handler: Handler by lazy { Handler(looper) }
        private val frameTime = (1000 / FRAME_RATE).toLong()

        override fun onLooperPrepared() {
            handler.post(frameRunnable)
        }

        private val frameRunnable = object : Runnable {
            override fun run() {
                if (animations.isEmpty()) {
                    quitSafely()
                    thread = null
                    return
                }

                val startTime = System.nanoTime()
                update()
                val endTime = System.nanoTime()

                val sleepTime = frameTime - (endTime - startTime) / 1_000_000
                if (sleepTime > 0) {
                    handler.postDelayed(this, sleepTime)
                } else {
                    handler.post(this)
                }
            }
        }
    }
}
