package org.oar.bytes.ui.common.components.grid.services

import android.view.View
import org.oar.bytes.features.animate.AnimationChain
import org.oar.bytes.model.Position
import org.oar.bytes.ui.animations.BumpTileAnimation
import org.oar.bytes.ui.animations.MoveTileAnimation
import org.oar.bytes.ui.common.components.grid.GridTile
import org.oar.bytes.ui.common.components.grid.model.StepAction
import org.oar.bytes.ui.common.components.grid.model.StepMerge
import org.oar.bytes.ui.common.components.grid.model.StepMove
import org.oar.bytes.utils.extensions.ListExt.active
import org.oar.bytes.utils.extensions.ListExt.asyncAdd
import org.oar.bytes.utils.extensions.ListExt.asyncRemove
import org.oar.bytes.utils.extensions.ListExt.findByPosition

class GridAnimatorService(
    val parent: View
) {
    var speed: Int = 30

    fun animateRevertSteps(
        tiles: MutableList<GridTile>,
        steps: List<StepAction>,
        spawnPos: Position? = null
    ): List<AnimationChain> {

        val activeTiles = tiles.active

        return  steps
            .sortedWith { a, b ->
                if (a is StepMerge)
                    if (b is StepMerge) 0 else -1
                else
                    if (b is StepMerge) 1 else 0
            }
            .mapNotNull { step ->
                when (step) {
                    is StepMove -> {
                        val tile = activeTiles.findByPosition(step.positionDest) ?: return@mapNotNull null
                        listOf(addMoveAnimation(tile, step.positionTile))
                    }
                    is StepMerge -> {
                        val tile = activeTiles.findByPosition(step.positionDest) ?: return@mapNotNull null
                        tile.reduceTileLevel()

                        val cloned = tile.clone()
                        tiles.asyncAdd(cloned)

                        listOf(
                            addBumpAnimation(tile),
                            addBumpAnimation(cloned)
                                .also { addMoveAnimation(cloned, step.positionBase, it) }
                        )
                    }
                    else -> null
                }
            }
            .flatten()
            .toMutableList()
            .let { list ->
                if (spawnPos != null) {
                    val tile = activeTiles.findByPosition(spawnPos)!!
                    AnimationChain(tile)
                        .start { tile.zombie = true }
                        .next { BumpTileAnimation(tile) }
                        .end { tiles.asyncRemove(tile) }
                        .also { list.add(it) }
                }
                AnimationChain.reduce(list)
            }
    }


    fun addMoveAnimation(
        tile: GridTile,
        pos: Position,
        chain: AnimationChain = AnimationChain(tile)
    ): AnimationChain {
        return chain
            .next { MoveTileAnimation(tile, pos, speed) }
            .end { tile.pos = pos }
    }

    fun addBumpAnimation(
        tile: GridTile,
        chain: AnimationChain = AnimationChain(tile)
    ): AnimationChain {
        return chain.next { BumpTileAnimation(tile) }
    }

    fun addMergeAnimation(
        tileBase: GridTile,
        tileDest: GridTile,
        posDest: Position,
        tiles: MutableList<GridTile>,
        chain: AnimationChain = AnimationChain(tileBase)
    ): AnimationChain {
        return chain
            .start { tileDest.zombie = true }
            .next { MoveTileAnimation(tileBase, posDest, speed) }
            .next {
                tiles.asyncRemove(tileDest)
                tileBase.pos = tileDest.pos
                tileBase.advanceTileLevel()
                it["mergedValue"] = tileBase.value
                it["mergedLevel"] = tileBase.level
                BumpTileAnimation(tileBase)
            }
    }
}
