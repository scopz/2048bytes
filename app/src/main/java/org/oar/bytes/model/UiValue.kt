package org.oar.bytes.model

import androidx.lifecycle.LifecycleOwner
import org.oar.bytes.utils.CustomLiveData

class UiValue<T> (
    initValue: T
): Cloneable {
    private var futureValue: T? = null
    private val _internalValue = CustomLiveData(initValue)

    var limit: (T) -> T = { it }

    var value: T
        get() = _internalValue.value
        set(value) {
            val limitedValue = limit(value)
            _internalValue.postValue(limitedValue)
            futureValue = futureValue?.takeIf { limitedValue != it }
        }

    var finalValue: T
        get() = futureValue ?: value
        set(value) {
            futureValue = limit(value)
        }

    fun observe(owner: LifecycleOwner, callback: (T) -> Unit) =
        _internalValue.observe(owner, callback)

    fun silentSetValue(value: T) {
        val limitedValue = limit(value)
        _internalValue.setWithoutTriggerEvent(limitedValue)
        futureValue = futureValue?.takeIf { limitedValue != it }
    }

    fun clearFinal() {
        futureValue = null
    }

    fun operate(ignoreFuture: Boolean = false, function: (T) -> T) {
        value = limit(function(value))
        futureValue = futureValue
            ?.let { if (ignoreFuture) it else limit(function(it)) }
            ?.takeIf { value != it }
    }
}