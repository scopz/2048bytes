package org.oar.bytes.model

class AnimatedValue<T: Any> (
    initValue: T
): Cloneable {

    var onValueChanged: ((T) -> Unit)? = null

    private var futureValue: T? = null
    private var _internalValue: T = initValue

    var value: T
        get() = _internalValue
        set(value) {
            setWithoutTriggerEvent(value)
            onValueChanged?.let { it(value) }
        }

    var finalValue: T
        get() = futureValue ?: value
        set(value) {
            futureValue = value
        }

    fun setWithoutTriggerEvent(value: T) {
        _internalValue = value
        futureValue = futureValue?.takeIf { value != it }
    }

    fun clearFinal() {
        futureValue = null
    }

    fun operate(ignoreFuture: Boolean = false, function: (T) -> T) {
        value = function(value)
        futureValue = futureValue
            ?.let { if (ignoreFuture) it else function(it) }
            ?.takeIf { value != it }
    }
}