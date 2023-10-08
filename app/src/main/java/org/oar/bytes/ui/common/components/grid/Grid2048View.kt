package org.oar.bytes.ui.common.components.grid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.json.JSONObject
import org.oar.bytes.features.animate.Animate
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMerge
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.ui.common.components.grid.services.GridStepsGenerator
import org.oar.bytes.ui.common.components.grid.services.GridTouchControl
import org.oar.bytes.ui.common.components.grid.services.GridTouchControl.Action.*
import org.oar.bytes.utils.Constants
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import java.util.*
import java.util.function.Consumer

class Grid2048View(
    context: Context,
    attr: AttributeSet? = null
) : View(context, attr), Animate {

    var gridLevel = 1
    val baseByteValue
        get() = 1.sByte.double(gridLevel-1)

    private var tileSize: Int = 0
    private var tileSpeed: Int = 0
    private val tiles = TileList<GridTile>()

    // services
    private val touchControl = GridTouchControl(context)
    private val stepsGenerator = GridStepsGenerator(context)

    // animation
    private var pendingSteps = listOf<StepAction>()
    private var pendingGenerateNewTile = false
    private val movingTiles = mutableSetOf<GridTile>()
    private val bumpingTiles = mutableSetOf<GridTile>()

    // listeners
    private var onProduceByteListener: Consumer<SByte>? = null
    fun setOnProduceByteListener(listener: Consumer<SByte>) { onProduceByteListener = listener }

    private var onGameOverListener: Runnable? = null
    fun setOnGameOverListener(listener: Runnable) { onGameOverListener = listener }

    private var onReadyListener: Runnable? = null
    fun setOnReadyListener(listener: Runnable) { onReadyListener = listener }

    init {
        setBackgroundColor(Constants.SHADE_COLORS[0])
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        tileSize = width / 4
        tileSpeed = (width / FRAME_RATE * 3.5).toInt()
        setMeasuredDimension(width, width)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        onReadyListener?.run()
    }

    fun restart() {
        tiles.clear()
        repeat(4) { generateRandom() }
        postInvalidate()
    }

    fun advanceLevel() {
        gridLevel++
        tiles.forEach {
            if (it.level == 1) {
                it.advancedGridLevel()
            } else {
                it.level--
            }
            it.prepareBumpAnimation()
        }

        bumpingTiles.addAll(tiles)
        pendingGenerateNewTile = false
        Animator.addAndStart(this)
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            val arrTiles = tiles
                .map { it.toJson() }
                .jsonArray()
            put("tiles", arrTiles)
            put("gridLevel", gridLevel)
        }
    }

    fun fromJson(json: JSONObject) {
        gridLevel = json.getInt("gridLevel")
        val baseByte = baseByteValue

        tiles.clear()
        json.getJSONArray("tiles")
            .mapJsonObject { tileObj ->
                val tileLevel = tileObj.getInt("level")
                val pos = Position(tileObj.getInt("x"), tileObj.getInt("y"))
                val value = baseByte.double(tileLevel-1)

                GridTile(context, value, pos, tileLevel, tileSize)
            }
            .also { tiles.addAll(it) }

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

        return if (rnd.nextInt(10) < 1)
            GridTile(context, baseByteValue.double(), position, 2, tileSize).also { tiles.add(it) }
        else
            GridTile(context, baseByteValue.clone(), position, 1, tileSize).also { tiles.add(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (pendingSteps.isNotEmpty() && movingTiles.isNotEmpty() || gameOver) {
            return super.onTouchEvent(event)
        }

        when(touchControl.onTouchEvent(event)) {
            MOVE_RIGHT -> {
                pendingSteps = stepsGenerator.moveRight(tiles)
                pendingGenerateNewTile = true
                if (pendingSteps.isNotEmpty()) Animator.addAndStart(this)
            }
            MOVE_LEFT -> {
                pendingSteps = stepsGenerator.moveLeft(tiles)
                pendingGenerateNewTile = true
                if (pendingSteps.isNotEmpty()) Animator.addAndStart(this)
            }
            MOVE_DOWN -> {
                pendingSteps = stepsGenerator.moveDown(tiles)
                pendingGenerateNewTile = true
                if (pendingSteps.isNotEmpty()) Animator.addAndStart(this)
            }
            MOVE_UP -> {
                pendingSteps = stepsGenerator.moveUp(tiles)
                pendingGenerateNewTile = true
                if (pendingSteps.isNotEmpty()) Animator.addAndStart(this)
            }
            else -> {}
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        synchronized(tiles) {
            canvas?.let {
                tiles.forEach { it.draw(canvas) }
            }
        }
    }

    override fun start() {
        pendingSteps.forEach {
            when(it) {
                is StepMove -> {
                    tiles.findByPosition(it.positionTile)
                        ?.apply { movingTiles.add(this) }
                        ?.prepareStepAnimation(it.positionDest, tileSpeed)
                }
                is StepMerge -> {
                    tiles.findByPosition(it.positionBase)
                        ?.apply { movingTiles.add(this) }
                        ?.prepareStepAnimation(it.positionDest, tileSpeed)
                }
            }
        }
    }

    override fun updateAnimation(moment: Long): Boolean {
        synchronized(tiles) {
            bumpingTiles.removeIf { tile -> !tile.bumpAnimation() }

            movingTiles.removeIf { tile ->
                val cont = tile.stepAnimation()
                if (!cont) {
                    val stepMove = pendingSteps.find {
                        it is StepMove && it.positionTile == tile.pos
                    } as StepMove?

                    if (stepMove != null) {
                        stepsGenerator.applyStep(tiles, stepMove)

                    } else {
                        val stepMerge = pendingSteps.find {
                            it is StepMerge && it.positionBase == tile.pos
                        } as StepMerge?

                        stepMerge?.also {
                            tiles.findByPosition(stepMerge.positionDest)?.also { dest ->
                                tiles.remove(tile)
                                dest.advanceTileLevel()
                                bumpingTiles.add(dest)
                                dest.prepareBumpAnimation()
                            }
                        }
                    }
                }
                !cont
            }

            if (pendingGenerateNewTile && movingTiles.isEmpty()) {
                pendingGenerateNewTile = false
                generateRandom().also {
                    it.prepareBumpAnimation()
                    bumpingTiles.add(it)
                }
            }

            postInvalidate()
        }

        return movingTiles.isNotEmpty() || bumpingTiles.isNotEmpty()
    }

    override fun end(moment: Long) {
        onProduceByteListener?.also { listener ->
            pendingSteps
                .filterIsInstance<StepMerge>()
                .map { tiles.findByPosition(it.positionDest)!! }
                .fold(0.sByte) { acc, it -> acc + it.value }
                .also { if (!it.isZero) listener.accept(it) }
        }

        pendingSteps = listOf()
        pendingGenerateNewTile = false

        if (gameOver) {
            onGameOverListener?.run()
        }
    }
}