package org.oar.bytes.ui.common.components.grid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.animations.BumpTileAnimation
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMerge
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.ui.common.components.grid.services.GridStepsGenerator
import org.oar.bytes.ui.common.components.grid.services.GridTouchControl
import org.oar.bytes.ui.common.components.grid.services.GridTouchControl.Action.*
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.mapJsonArray
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.ListExt.findByPosition
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import org.oar.bytes.utils.TriConsumer
import java.util.*

class Grid2048View(
    context: Context,
    attr: AttributeSet? = null
) : View(context, attr) {

    private val baseByteValue
        get() = 1.sByte.double(Data.gridLevel-1)

    private var tileSize: Int = 0
    private val tiles = mutableListOf<GridTile>()

    private val MAX_REVERTS = 10
    private val lastSteps = mutableListOf<List<StepAction>>()
    private val lastSpawn = mutableListOf<Position>()

    var paused = false
        set(value) {
            field = value
            alpha = if (value) .5f else 1f
        }

    // services
    private val touchControl = GridTouchControl(this)
    private val stepsGenerator = GridStepsGenerator(this)

    // listeners
    private var onProduceByteListener: TriConsumer<Int, Int, SByte>? = null
    fun setOnProduceByteListener(listener: TriConsumer<Int, Int, SByte>) { onProduceByteListener = listener }

    private var onGameOverListener: Runnable? = null
    fun setOnGameOverListener(listener: Runnable) { onGameOverListener = listener }

    private var onReadyListener: Runnable? = null
    fun setOnReadyListener(listener: Runnable) { onReadyListener = listener }

    init {
        setBackgroundColor(R.color.itemDefaultBackground.color(context))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        tileSize = width / 4
        stepsGenerator.speed = (width / FRAME_RATE * 3.5).toInt()
        setMeasuredDimension(width, width)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        onReadyListener?.run()
    }

    fun restart() {
        lastSpawn.clear()
        lastSteps.clear()
        tiles.clear()
        repeat(4) { generateRandom() }
        postInvalidate()
    }

    fun advancedGridLevel() {
        tiles.map {
            if (it.level == 1) {
                it.advancedGridLevel()
            } else {
                it.level--
            }

            AnimationChain(it).next { _ -> BumpTileAnimation(it) }
        }.also {
            Animator.addAndStart(it)
        }
        lastSpawn.clear()
        lastSteps.clear()
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            tiles
                .map { it.toJson() }
                .jsonArray()
                .also { put("tiles", it) }

            lastSteps
                .map { steps -> steps
                    .map { it.toJson() }
                    .jsonArray()
                }
                .jsonArray()
                .also { put("lastSteps", it) }

            lastSpawn
                .map { listOf(it.x, it.y).jsonArray() }
                .jsonArray()
                .also { put("lastSpawns", it) }
        }
    }

    fun fromJson(json: JSONObject) {
        val baseByte = baseByteValue

        tiles.clear()
        json.getJSONArray("tiles")
            .mapJsonObject { tileObj ->
                val tileLevel = tileObj.getInt("level")
                val pos = Position(tileObj.getInt("x"), tileObj.getInt("y"))
                val value = baseByte.double(tileLevel - 1)

                GridTile(this, value, pos, tileLevel, tileSize)
            }
            .also { tiles.addAll(it) }

        this.lastSteps.clear()
        if (json.has("lastSteps")) {
            val lastSteps = json.getJSONArray("lastSteps")
                .mapJsonArray {
                    it.mapJsonObject(StepAction::fromJson)
                }
            this.lastSteps.addAll(lastSteps)
        }

        this.lastSpawn.clear()
        if (json.has("lastSpawns")) {
            val lastSpawn = json.getJSONArray("lastSpawns")
                .mapJsonArray { Position(it.getInt(0), it.getInt(1)) }
            this.lastSpawn.addAll(lastSpawn)
        }

        postInvalidate()
    }

    private val gameOver: Boolean
        get() {
            if (tiles.size < 16) {
                return false
            }
            (0 until 4).forEach { xy ->
                val first = tiles.findByPosition(Position(0, xy))!!
                val left = tiles.findByPosition(first.pos.right!!)!!
                if (first.value == left.value) return false
                val right = tiles.findByPosition(left.pos.right!!)!!
                if (left.value == right.value) return false
                val last = tiles.findByPosition(right.pos.right!!)!!
                if (right.value == last.value) return false
                val firstY = tiles.findByPosition(Position(xy, 0))!!
                val leftY = tiles.findByPosition(firstY.pos.bottom!!)!!
                if (firstY.value == leftY.value) return false
                val rightY = tiles.findByPosition(leftY.pos.bottom!!)!!
                if (leftY.value == rightY.value) return false
                val lastY = tiles.findByPosition(rightY.pos.bottom!!)!!
                if (rightY.value == lastY.value) return false
            }
            return true
        }

    private fun generateRandom(): GridTile {
        val rnd = Random()
        var position: Position
        do {
            position = Position(
                rnd.nextInt(4),
                rnd.nextInt(4)
            )
            val tile = tiles.findByPosition(position)
        } while(tile != null)

        val tile = if (rnd.nextInt(10) < 1)
            GridTile(this, baseByteValue.double(), position, 2, tileSize)
        else
            GridTile(this, baseByteValue.clone(), position, 1, tileSize)

        tiles.add(tile)
        return tile
    }

    fun revertLast(): Boolean {
        if (Animator.blockedGrid || lastSpawn.isEmpty() || lastSteps.isEmpty())
            return false

        val spawnPos = lastSpawn.removeLast()
        val steps = lastSteps.removeLast()

        if (steps.isNotEmpty()) {
            steps
                .sortedWith { a, b ->
                    if (a is StepMerge)
                        if (b is StepMerge) 0 else -1
                    else
                        if (b is StepMerge) 1 else 0
                }
                .mapNotNull { step ->
                    when(step) {
                        is StepMove -> {
                            val tile = tiles.findByPosition(step.positionDest)!!
                            listOf(stepsGenerator.addMoveAnimation(tile, step.positionTile))
                        }
                        is StepMerge -> {
                            val tile = tiles.findByPosition(step.positionDest)!!
                            tile.reduceTileLevel()

                            val cloned = tile.clone()
                            tiles.add(cloned)

                            listOf(
                                stepsGenerator.addBumpAnimation(tile),
                                stepsGenerator.addBumpAnimation(cloned)
                                    .also { stepsGenerator.addMoveAnimation(cloned, step.positionBase, it) }
                            )
                        }
                        else -> null
                    }
                }
                .flatten()
                .toMutableList()

                .let { list ->
                    val tile = tiles.findByPosition(spawnPos)!!
                    AnimationChain(tile)
                        .next { BumpTileAnimation(tile) }
                        .end { tiles.remove(tile) }
                        .also { list.add(it) }

                    AnimationChain.reduce(list)
                }
                .also { Animator.addAndStart(it) }

            return true
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver || paused || MotionEvent.ACTION_MOVE == event.action && Animator.blockedGrid) {
            return super.onTouchEvent(event)
        }

        val steps = mutableListOf<StepAction>()
        fun startAnimation(chains: List<AnimationChain>) {
            if (chains.isNotEmpty()) {
                Animator.addAndStart(chains)
                Animator.join(chains) { action, value ->
                    if (Animator.BLOCK_CHANGED == action && !value) {
                        if (onProduceByteListener != null) {
                            val mergedValue = chains
                                .mapNotNull<AnimationChain, SByte> { it["mergedValue"] }
                                .fold(0.sByte) { acc, it -> acc + it }

                            val mergedLevels = chains
                                .mapNotNull<AnimationChain, Int> { it["mergedLevel"] }

                            onProduceByteListener?.accept(mergedLevels.size, mergedLevels.sum(), mergedValue)
                        }

                        val newTile = generateRandom()
                        AnimationChain(newTile)
                            .next { BumpTileAnimation(newTile) }
                            .also { Animator.addAndStart(it) }

                        lastSteps.add(steps)
                        lastSpawn.add(newTile.pos)

                        while (lastSteps.size > MAX_REVERTS) {
                            lastSteps.removeFirst()
                        }
                        while (lastSpawn.size > MAX_REVERTS) {
                            lastSpawn.removeFirst()
                        }
                    }
                }
            }
        }

        when(touchControl.onTouchEvent(event)) {
            MOVE_RIGHT -> stepsGenerator.moveRight(tiles, steps).also { startAnimation(it) }
            MOVE_LEFT -> stepsGenerator.moveLeft(tiles, steps).also { startAnimation(it) }
            MOVE_DOWN -> stepsGenerator.moveDown(tiles, steps).also { startAnimation(it) }
            MOVE_UP -> stepsGenerator.moveUp(tiles, steps).also { startAnimation(it) }
            else -> {}
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        tiles.toList().forEach { it.draw(canvas) }
    }
}