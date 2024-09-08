package org.oar.bytes.utils

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class CustomLiveData<T>(
    value: T,
    prevValue: T = value,
) {
    companion object {
        private val internalDataField = LiveData::class.java.getDeclaredField("mData")
            .apply { isAccessible = true}
    }

    private val liveData = MutableLiveData(prevValue to value)

    var value: T
        get() = (liveData.value as Pair<T, T>).second
        set(value) {
            val prevValue = (liveData.value as Pair<T, T>).second
            liveData.value = prevValue to value
        }

    fun postValue(value: T) {
        val pairValue = this.value to value
        if (Looper.myLooper() == Looper.getMainLooper()) {
            liveData.setValue(pairValue)
        } else {
            liveData.postValue(pairValue)
            internalDataField.set(liveData, pairValue)
        }
    }

    fun silentSetValue(value: T) = internalDataField.set(liveData, this.value to value)

    fun observe(owner: LifecycleOwner, callback: (Pair<T, T>) -> Unit) =
        liveData.observe(owner, callback)
}

