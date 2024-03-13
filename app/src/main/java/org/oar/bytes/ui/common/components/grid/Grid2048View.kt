package org.oar.bytes.ui.common.components.grid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.features.animate.Animator
import org.oar.bytes.model.JoinResult
import org.oar.bytes.model.Position
import org.oar.bytes.model.SByte
import org.oar.bytes.ui.animations.BumpTileAnimation
import org.oar.bytes.ui.common.LimitedDrawView
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.ui.common.components.grid.services.GridAnimatorService
import org.oar.bytes.ui.common.components.grid.services.GridStepsGeneratorService
import org.oar.bytes.ui.common.components.grid.services.GridTouchControlService
import org.oar.bytes.ui.common.components.grid.services.GridTouchControlService.Action.*
import org.oar.bytes.utils.ComponentsExt.runOnUiThread
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.map
import org.oar.bytes.utils.JsonExt.mapJsonArray
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.ListExt.active
import org.oar.bytes.utils.ListExt.findActiveByPosition
import org.oar.bytes.utils.ListExt.findByPosition
import org.oar.bytes.utils.ListExt.syncAdd
import org.oar.bytes.utils.ListExt.syncAddAll
import org.oar.bytes.utils.ListExt.syncClear
import org.oar.bytes.utils.ListExt.syncForEach
import org.oar.bytes.utils.ListExt.syncRemove
import org.oar.bytes.utils.ListExt.syncReplaceAll
import org.oar.bytes.utils.NumbersExt.color
import org.oar.bytes.utils.NumbersExt.sByte
import org.oar.bytes.utils.ScreenProperties.FRAME_RATE
import java.util.*
import java.util.function.Consumer

class Grid2048View(
    context: Context,
    attr: AttributeSet? = null
) : LimitedDrawView(context, attr) {

    private val baseByteValue
        get() = 1.sByte.double(Data.gridLevel-1)

    private var tileSize: Int = 0
    private val tiles = mutableListOf<GridTile>()

    private val MAX_REVERTS = 10
    private val lastSteps = mutableListOf<List<StepAction>>()
    private val lastSpawn = mutableListOf<Position?>()
    var selectTile: Consumer<Position>? = null

    var enableMove = true
    var paused = false
        set(value) {
            field = value
            alpha = if (value) .5f else 1f
        }

    // services
    private val touchControl = GridTouchControlService(this)
    private val stepsGenerator = GridStepsGeneratorService(this)
    private val animator = GridAnimatorService(this)

    // listeners
    private var onProduceByteListener: Consumer<JoinResult>? = null
    fun setOnProduceByteListener(listener: Consumer<JoinResult>) { onProduceByteListener = listener }

    private var onGameOverListener: Runnable? = null
    fun setOnGameOverListener(listener: Runnable) { onGameOverListener = listener }

    private var onLongClickListener: OnLongClickListener? = null
    override fun setOnLongClickListener(listener: OnLongClickListener?) { onLongClickListener = listener }

    init {
        setBackgroundColor(R.color.itemDefaultBackground.color(context))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        tileSize = width / 4
        animator.speed = (width / FRAME_RATE * 4.5).toInt()
        stepsGenerator.speed = animator.speed
        setMeasuredDimension(width, width)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        tiles.syncReplaceAll {
            GridTile(this, it.value, it.pos, it.level, tileSize)
        }
    }

    fun restart() {
        lastSpawn.clear()
        lastSteps.clear()
        tiles.syncClear()
        repeat(4) { generateRandom() }
        postInvalidate()
    }

    fun advancedGridLevel() {
        tiles.active
            .map {
                if (it.level == 1) it.advancedGridLevel()
                else               it.level--
                AnimationChain(it).next { _ -> BumpTileAnimation(it) }
            }
            .also { Animator.addAndStart(it) }
        lastSpawn.clear()
        lastSteps.clear()
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            tiles.active
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
                .map { if (it == null) null else listOf(it.x, it.y).jsonArray() }
                .jsonArray()
                .also { put("lastSpawns", it) }
        }
    }

    fun fromJson(json: JSONObject) {
        val baseByte = baseByteValue

        tiles.syncClear()
        json.getJSONArray("tiles")
            .mapJsonObject { tileObj ->
                val tileLevel = tileObj.getInt("level")
                val pos = Position(tileObj.getInt("x"), tileObj.getInt("y"))
                val value = baseByte.double(tileLevel - 1)

                GridTile(this, value, pos, tileLevel, tileSize)
            }
            .also { tiles.syncAddAll(it) }

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
                .map { it, idx -> if (it.isNull(idx)) null else it.getJSONArray(idx) }
                .map { if (it == null) null else Position(it.getInt(0), it.getInt(1)) }
            this.lastSpawn.addAll(lastSpawn)
        }
    }

    private val gameOver: Boolean
        get() {
            if (tiles.size < 16) {
                return false
            }
            val activeTiles = tiles.active
            (0 until 4).forEach { xy ->
                val first = activeTiles.findByPosition(Position(0, xy)) ?: return false
                val left = activeTiles.findByPosition(first.pos.right!!) ?: return false
                if (first.value == left.value) return false
                val right = activeTiles.findByPosition(left.pos.right!!) ?: return false
                if (left.value == right.value) return false
                val last = activeTiles.findByPosition(right.pos.right!!) ?: return false
                if (right.value == last.value) return false
                val firstY = activeTiles.findByPosition(Position(xy, 0)) ?: return false
                val leftY = activeTiles.findByPosition(firstY.pos.bottom!!) ?: return false
                if (firstY.value == leftY.value) return false
                val rightY = activeTiles.findByPosition(leftY.pos.bottom!!) ?: return false
                if (leftY.value == rightY.value) return false
                val lastY = activeTiles.findByPosition(rightY.pos.bottom!!) ?: return false
                if (rightY.value == lastY.value) return false
            }
            return true
        }

    private fun generateRandom(): GridTile {
        val rnd = Random()
        var position: Position
        val activeTiles = tiles.active
        do {
            position = Position(
                rnd.nextInt(4),
                rnd.nextInt(4)
            )
            val tile = activeTiles.findByPosition(position)
        } while(tile != null)

        val tile = if (rnd.nextInt(10) < 1)
            GridTile(this, baseByteValue.double(), position, 2, tileSize)
        else
            GridTile(this, baseByteValue.clone(), position, 1, tileSize)

        tiles.syncAdd(tile)
        return tile
    }

    fun clearSelectAction() {
        selectTile = null
    }

    fun addTileHint(): MutableLiveData<GridTile>? {
        if (Animator.blockedGrid || tiles.size == 16)
            return null

        val liveData = MutableLiveData<GridTile>()

        selectTile = Consumer {
            tiles.findActiveByPosition(it) ?: run {
                val tile = GridTile(this, baseByteValue.clone(), it, 1, tileSize)
                tiles.syncAdd(tile)

                AnimationChain(tile)
                    .next { BumpTileAnimation(tile) }
                    .also(Animator::addAndStart)

                liveData.value = tile
                selectTile = null
                lastSpawn.add(it)
                lastSteps.add(listOf())
            }
        }
        return liveData
    }

    fun improveLowerHint(): Boolean {
        if (Animator.blockedGrid)
            return false

        val updateTiles = tiles.active
            .filter { it.level == 1 }

        if (updateTiles.isEmpty())
            return false

        updateTiles
            .map {
                it.advancedGridLevel()
                it.level++
                AnimationChain(it).next { _ -> BumpTileAnimation(it) }
            }
            .also { Animator.addAndStart(it) }
        return true
    }

    fun revertLastHint(): Boolean {
        if (Animator.blockedGrid || lastSpawn.isEmpty() || lastSteps.isEmpty())
            return false

        val spawnPos = lastSpawn.removeLast()
        val steps = lastSteps.removeLast()

        val chains = animator.animateRevertSteps(tiles, steps, spawnPos)
        Animator.addAndStart(chains)
        return true
    }

    fun swapTilesHint() : MutableLiveData<GridTile>? {
        if (Animator.blockedGrid || tiles.size < 2)
            return null

        val liveData = MutableLiveData<GridTile>()

        selectTile = Consumer { posA ->
            val tileA = tiles.findActiveByPosition(posA) ?: return@Consumer

            selectTile = Consumer { posB ->
                val tileB = tiles.findActiveByPosition(posB)

                if (tileB != null && posA.touches(posB)) {
                    listOf(
                        animator.addMoveAnimation(tileA, posB),
                        animator.addMoveAnimation(tileB, posA),
                    ).also { Animator.addAndStart(it) }

                    liveData.value = tileA
                    selectTile = null
                    lastSteps.add(listOf(StepMove(posA, posB), StepMove(posB, posA)))
                    lastSpawn.add(null)
                }
            }
        }
        return liveData
    }

    fun removeTileHint(): MutableLiveData<GridTile>? {
        if (Animator.blockedGrid || tiles.size == 0)
            return null

        val liveData = MutableLiveData<GridTile>()

        selectTile = Consumer {
            tiles.findActiveByPosition(it)?.also { tile ->
                tile.zombie = true
                AnimationChain(tile)
                    .next { BumpTileAnimation(tile) }
                    .end { tiles.syncRemove(tile) }
                    .also(Animator::addAndStart)

                liveData.value = tile
                selectTile = null
                lastSpawn.clear()
                lastSteps.clear()
            }
        }

        return liveData
    }

    private val gestureDetector =  GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            onLongClickListener?.onLongClick(this@Grid2048View)
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (
            paused ||
            MotionEvent.ACTION_MOVE == event.action && (Animator.blockedGrid || !enableMove)
        ) {
            return super.onTouchEvent(event)
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            selectTile?.also { consumer ->
                val position = touchControl.getPosition(event, tileSize)
                consumer.accept(position)
                return false
            }
        }

        gestureDetector.onTouchEvent(event)

        if (gameOver) {
            return true
        }

        fun startAnimation(wrapper: GridStepsGeneratorService.MoveStepsWrapper) {
            val chains = wrapper.animationChain
            val steps = wrapper.steps

            if (chains.isNotEmpty()) {
                enableMove = false
                Animator.addAndStart(chains)
                Animator.join(chains) { action, value ->
                    if (Animator.BLOCK_CHANGED == action && !value) {
                        if (onProduceByteListener != null) {
                            val mergedValue = chains
                                .mapNotNull<AnimationChain, SByte> { it["mergedValue"] }
                                .fold(0.sByte) { acc, it -> acc + it }

                            val mergedLevels = chains
                                .mapNotNull<AnimationChain, Int> { it["mergedLevel"] }

                            runOnUiThread {
                                onProduceByteListener?.accept(
                                    JoinResult(
                                        mergedLevels.size,
                                        mergedValue,
                                        mergedLevels
                                    )
                                )
                            }
                        }

                        val newTile = generateRandom()
                        enableMove = true

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

                        if (gameOver) onGameOverListener?.run()
                    }
                }
            }
        }

        when(touchControl.onTouchEvent(event)) {
            MOVE_RIGHT -> stepsGenerator.moveRight(tiles).also { startAnimation(it) }
            MOVE_LEFT -> stepsGenerator.moveLeft(tiles).also { startAnimation(it) }
            MOVE_DOWN -> stepsGenerator.moveDown(tiles).also { startAnimation(it) }
            MOVE_UP -> stepsGenerator.moveUp(tiles).also { startAnimation(it) }
            null -> {}
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        tiles.syncForEach { it.draw(canvas) }
    }
}
