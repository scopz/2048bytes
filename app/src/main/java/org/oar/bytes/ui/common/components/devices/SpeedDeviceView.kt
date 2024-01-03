package org.oar.bytes.ui.common.components.devices

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import org.oar.bytes.R
import org.oar.bytes.databinding.ComponentDeviceBinding
import org.oar.bytes.model.Device
import org.oar.bytes.model.SByte
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.getJSONArrayOrNull
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.sByte
import java.util.function.Consumer

class SpeedDeviceView(
    context: Context,
    attr: AttributeSet? = null
) : RecyclerView(context, attr) {

    companion object {
        private const val MAX_LEVEL = 30
    }

    val levels = mutableMapOf<Int, Int>()
    private val percentLevels = mutableMapOf<Int, Int>()

    private var onSpeedChangedListener: Consumer<SByte>? = null
    fun setOnSpeedChangedListener(listener: Consumer<SByte>) { onSpeedChangedListener = listener }

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = CustomAdapter()
    }

    private fun updateTotalSpeed() {
        val speed = Data.devices
            .map {
                val level = levels[it.id] ?: 0
                it.speed * level.sByte
            }
            .reduce { a,b -> a + b }

        onSpeedChangedListener?.accept(speed)
    }

    private inner class CustomAdapter: Adapter<CustomViewHolder>() {
        override fun getItemCount() = Data.devices.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(
                ComponentDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        private fun updateLevel(holder: CustomViewHolder, device: Device, level: Int) {
            val levelByte = level.sByte
            holder.level.text = "$level"

            val speed = device.speed * levelByte
            holder.current.text = "$speed/s"

            if (level < MAX_LEVEL) {
//                if (level == 0) {
//                    holder.price.text = "${device.unlockFee} COINS"
//                } else {
                    holder.price.text = "${device.cost(level)}"
                    disableButton(holder, false)
//                }
            } else {
                holder.price.text = "MAX"
                disableButton(holder)
            }
        }

        private fun disableButton(holder: CustomViewHolder, disable: Boolean = true) {
            if (disable) {
                if (holder.button.isEnabled) {
                    holder.itemView.alpha = 0.6f
                    holder.itemView.setBackgroundResource(R.color.disabledPanelColor)
                    holder.button.setBackgroundResource(R.color.disabledPanelButtonColor)
                    holder.button.isEnabled = false
                }
            } else if(!holder.button.isEnabled) {
                holder.itemView.alpha = 1f
                holder.itemView.setBackgroundResource(R.color.speedPanelColor)
                holder.button.setBackgroundResource(R.color.speedPanelButtonColor)
                holder.button.isEnabled = true
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val device = Data.devices[position]

            holder.name.text = device.name
            holder.valueToAdd.text = "+${device.speed}"

            updateLevel(holder, device, levels[device.id] ?: 0)

            holder.button.setOnClickListener {
                val level = levels[device.id] ?: 0
                if (level < MAX_LEVEL) {
                    val cost = device.cost(level)
                    if (Data.consumeBytes(cost)) {
                        levels[device.id] = level + 1
                        updateLevel(holder, device, level + 1)
                        updateTotalSpeed()
                    }
                }
            }
        }
    }

    private inner class CustomViewHolder(binding: ComponentDeviceBinding): ViewHolder(binding.root) {
        val name = binding.name
        val current = binding.current
        val level = binding.level
        val price = binding.price
        val valueToAdd = binding.value
        val button = binding.button
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            levels.entries
                .map {
                    JSONObject().apply {
                        put("id", it.key)
                        put("level", it.value)
                    }
                }
                .jsonArray()
                .also { put("devsLevel", it) }

            percentLevels.entries
                .map {
                    JSONObject().apply {
                        put("id", it.key)
                        put("percent", it.value)
                    }
                }
                .jsonArray()
                .also { put("devsPercent", it) }
        }
    }

    fun fromJson(json: JSONObject) {
        levels.clear()
        json.getJSONArrayOrNull("devsLevel")?.mapJsonObject {
            levels.put(it.getInt("id"), it.getInt("level"))
        }

        percentLevels.clear()
        json.getJSONArrayOrNull("devsPercent")?.mapJsonObject {
            percentLevels.put(it.getInt("id"), it.getInt("percent"))
        }

        updateTotalSpeed()
    }
}