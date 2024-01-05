package org.oar.bytes.utils

import org.oar.bytes.model.Position
import org.oar.bytes.ui.common.components.grid.GridTile
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

object ListExt {
    fun <T : GridTile> List<T>.findActiveByPosition(pos: Position) = active.findByPosition(pos)
    fun <T : GridTile> List<T>.findByPosition(pos: Position) = find { it.pos == pos }

    val <T : GridTile> List<T>.active
        get() = syncFilter { !it.zombie }

    fun <T> List<T>.syncForEach(action: Consumer<in T>) {
        if (isNotEmpty()) {
            synchronized(this) {
                forEach(action)
            }
        }
    }

    fun <T> List<T>.syncFilter(predicate: (T) -> Boolean): List<T> {
        if (isEmpty()) return listOf()
        synchronized(this) {
            return filter(predicate)
        }
    }

    fun <T, V> List<T>.syncAssociateWith(valueSelector: (T) -> V): Map<T, V> {
        if (isEmpty()) return mapOf()
        synchronized(this) {
            return associateWith(valueSelector)
        }
    }

    fun <T> List<T>.syncAny(predicate: (T) -> Boolean): Boolean {
        if (isEmpty()) return false
        synchronized(this) {
            return any(predicate)
        }
    }

    fun <T, R> List<T>.syncMap(transform: (T) -> R): List<R> {
        if (isEmpty()) return listOf()
        synchronized(this) {
            return map(transform)
        }
    }

    fun <T> MutableList<T>.syncClear() {
        if (isNotEmpty()) {
            synchronized(this) {
                clear()
            }
        }
    }

    fun <T> MutableList<T>.syncAdd(element: T): Boolean {
        synchronized(this) {
            return add(element)
        }
    }

    fun <T> MutableList<T>.asyncAdd(element: T) {
        Thread {
            synchronized(this) {
                add(element)
            }
        }.apply(Thread::start)
    }

    fun <T> MutableList<T>.syncAddAll(elements: Collection<T>): Boolean {
        synchronized(this) {
            return addAll(elements)
        }
    }

    fun <T> MutableList<T>.syncRemove(element: T): Boolean {
        if (isEmpty()) return false
        synchronized(this) {
            return remove(element)
        }
    }

    fun <T> MutableList<T>.asyncRemove(element: T) {
        if (isEmpty()) return
        Thread {
            synchronized(this) {
                remove(element)
            }
        }.apply(Thread::start)
    }

    fun <T> MutableList<T>.syncRemoveIf(filter: Predicate<in T>): Boolean {
        if (isEmpty()) return false
        synchronized(this) {
            return removeIf(filter)
        }
    }

    fun <T> MutableList<T>.syncReplaceAll(operator: UnaryOperator<T>) {
        if (isNotEmpty()) {
            synchronized(this) {
                return replaceAll(operator)
            }
        }
    }
}