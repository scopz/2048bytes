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
        private set(value) {
            _internalValue.postValue(value)
        }

    var finalValue: T
        get() = futureValue ?: value
        set(value) {
            futureValue = limit(value)
        }

    fun observe(owner: LifecycleOwner, callback: (T) -> Unit) =
        _internalValue.observe(owner, callback)

    fun clearFinal() {
        futureValue = null
    }

    fun silentOperate(ignoreFuture: Boolean = false, function: (T) -> T) {
        synchronized(this) {
            val limitedValue = limit(function(value))
            _internalValue.silentSetValue(limitedValue)
            futureValue = futureValue
                ?.let { if (ignoreFuture) it else limit(function(it)) }
                ?.takeIf { value != it }
        }
    }

    fun operate(ignoreFuture: Boolean = false, function: (T) -> T) {
        synchronized(this) {
            value = limit(function(value))
            futureValue = futureValue
                ?.let { if (ignoreFuture) it else limit(function(it)) }
                ?.takeIf { value != it }
        }
    }
}