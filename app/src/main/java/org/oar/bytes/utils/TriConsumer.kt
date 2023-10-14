package org.oar.bytes.utils

import java.util.*

/**
 * Represents an operation that accepts two input arguments and returns no
 * result.  This is the two-arity specialization of [Consumer].
 * Unlike most other functional interfaces, `TriConsumer` is expected
 * to operate via side-effects.
 *
 *
 * This is a [functional interface](package-summary.html)
 * whose functional method is [.accept].
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <V> the type of the third argument to the operation
 *
 * @see Consumer
 *
 * @since 1.8
 */
fun interface TriConsumer<T, U, V> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     */
    fun accept(t: T, u: U, v: V)

    /**
     * Returns a composed `TriConsumer` that performs, in sequence, this
     * operation followed by the `after` operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the `after` operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed `TriConsumer` that performs in sequence this
     * operation followed by the `after` operation
     * @throws NullPointerException if `after` is null
     */
    fun andThen(after: TriConsumer<in T, in U, in V>): TriConsumer<T, U, V> {
        return TriConsumer { l: T, r: U, m: V ->
            accept(l, r, m)
            after.accept(l, r, m)
        }
    }
}