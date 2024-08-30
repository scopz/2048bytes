package org.oar.bytes.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


open class CustomLiveData<T>(
    value: T
) {
    companion object {
        private val internalDataField = LiveData::class.java.getDeclaredField("mData")
            .apply { isAccessible = true}
    }

    private val liveData = MutableLiveData(value)

    @Suppress("UNCHECKED_CAST")
    var value: T
        get() = liveData.value as T
        set(value) {
            liveData.value = value
        }

    fun postValue(value: T) =
        liveData.postValue(value)

    fun setWithoutTriggerEvent(value: T) =
        internalDataField.set(liveData, value)

    fun observe(owner: LifecycleOwner, callback: (T) -> Unit) =
        liveData.observe(owner, callback)
}
