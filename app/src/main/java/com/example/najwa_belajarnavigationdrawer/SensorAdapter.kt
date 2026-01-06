package com.example.najwa_belajarnavigationdrawer

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.najwa_belajarnavigationdrawer.databinding.ItemSensorDataBinding

class SensorAdapter(private var list: List<SensorData>) :
    RecyclerView.Adapter<SensorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSensorDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSensorDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val b = holder.binding

        b.tvTitle.text = "Data #${position + 1}"

        try {
            // Debug log untuk item pertama
            if (position == 0) {
                Log.d("SensorAdapter", "First item - AcX: ${item.getAcX()}, Pitch: ${item.getPitch()}")
            }

            // Gunakan safe getters
            val acX = item.getAcX()
            val acY = item.getAcY()
            val acZ = item.getAcZ()
            val gyX = item.getGyX()
            val gyY = item.getGyY()
            val gyZ = item.getGyZ()
            val pitch = item.getPitch()

            // Format dan tampilkan
            b.tvAcX.text = String.format("%.2f", acX.toDouble())
            b.tvAcY.text = String.format("%.2f", acY.toDouble())
            b.tvAcZ.text = String.format("%.2f", acZ.toDouble())

            b.tvGyX.text = String.format("%.2f", gyX.toDouble())
            b.tvGyY.text = String.format("%.2f", gyY.toDouble())
            b.tvGyZ.text = String.format("%.2f", gyZ.toDouble())

            b.tvPitch.text = String.format("%.2f°", pitch)

        } catch (e: Exception) {
            Log.e("SensorAdapter", "Error binding item $position", e)
            // Fallback values
            b.tvAcX.text = "ERR"
            b.tvAcY.text = "ERR"
            b.tvAcZ.text = "ERR"
            b.tvGyX.text = "ERR"
            b.tvGyY.text = "ERR"
            b.tvGyZ.text = "ERR"
            b.tvPitch.text = "ERR"
        }
    }

    /**
     * Update data list untuk pagination dan search
     */
    fun updateData(newList: List<SensorData>) {
        Log.d("SensorAdapter", "Updating adapter with ${newList.size} items")
        if (newList.isNotEmpty()) {
            val first = newList[0]
            Log.d("SensorAdapter", "First item values - AcX: ${first.getAcX()}, AcY: ${first.getAcY()}, Pitch: ${first.getPitch()}")
        }
        list = newList
        notifyDataSetChanged()
    }
}