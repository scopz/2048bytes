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
import org.oar.bytes.model.EnergyDevice
import org.oar.bytes.utils.Data
import org.oar.bytes.utils.JsonExt.getJSONArrayOrNull
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.mapJsonObject
import org.oar.bytes.utils.NumbersExt.toHHMMSS
import org.oar.bytes.utils.NumbersExt.toMins
import java.util.function.Consumer

class TimeEnergyView(
    context: Context,
    attr: AttributeSet? = null
) : RecyclerView(context, attr) {

    val levels = mutableMapOf<Int, Int>()
    val percentLevels = mutableMapOf<Int, Int>()

    private var onEnergyChangedListener: Consumer<Int>? = null
    fun setOnEnergyChangedListener(listener: Consumer<Int>) { onEnergyChangedListener = listener }

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = CustomAdapter()
    }

    private fun updateTotalEnergy() {
        val energy = Data.energyDevices
            .map {
                val level = levels[it.id] ?: 0
                it.capacity * level
            }
            .reduce { a,b -> a + b }

        onEnergyChangedListener?.accept(energy)
    }

    private inner class CustomAdapter: Adapter<CustomViewHolder>() {
        override fun getItemCount() = Data.energyDevices.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(
                ComponentDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        private fun updateEnergy(holder: CustomViewHolder, device: EnergyDevice, level: Int) {
            holder.level.text = "$level"

            val time = device.capacity * level
            holder.current.text = time.toHHMMSS()

            if (level < 30) {
//                if (level == 0) {
//                    holder.price.text = "${device.unlockFee} COINS"
//                } else {
                    holder.price.text = "${device.cost(level)}"
//                }
            } else {
                holder.price.text = "MAX"
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val device = Data.energyDevices[position]
            holder.itemView.setBackgroundResource(R.color.timePanelColor)

            holder.name.text = device.name
            holder.valueToAdd.text = "+${device.capacity.toMins()}m"

            updateEnergy(holder, device, levels[device.id] ?: 0)

            holder.button.setOnClickListener {
                val level = levels[device.id] ?: 0
                if (level < 30) {
                    val cost = device.cost(level)
                    if (Data.consumeBytes(cost)) {
                        levels[device.id] = level + 1
                        updateEnergy(holder, device, level + 1)
                        updateTotalEnergy()
                    }
                }
            }
        }
    }

    internal inner class CustomViewHolder(binding: ComponentDeviceBinding): ViewHolder(binding.root) {
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
                .also { put("energyLevel", it) }

            percentLevels.entries
                .map {
                    JSONObject().apply {
                        put("id", it.key)
                        put("percent", it.value)
                    }
                }
                .jsonArray()
                .also { put("energyPercent", it) }
        }
    }

    fun fromJson(json: JSONObject) {
        levels.clear()
        json.getJSONArrayOrNull("energyLevel")?.mapJsonObject {
            levels.put(it.getInt("id"), it.getInt("level"))
        }

        percentLevels.clear()
        json.getJSONArrayOrNull("energyPercent")?.mapJsonObject {
            percentLevels.put(it.getInt("id"), it.getInt("percent"))
        }

        updateTotalEnergy()
    }
}