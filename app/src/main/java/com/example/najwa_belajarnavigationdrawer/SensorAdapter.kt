package com.example.najwa_belajarnavigationdrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.najwa_belajarnavigationdrawer.databinding.ItemSensorDataBinding

class SensorAdapter(private val list: List<SensorData>) :
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
        b.tvAcX.text = "AcX : ${item.acX}"
        b.tvAcY.text = "AcY : ${item.acY}"
        b.tvAcZ.text = "AcZ : ${item.acZ}"

        b.tvGyX.text = "GyX : ${item.gyX}"
        b.tvGyY.text = "GyY : ${item.gyY}"
        b.tvGyZ.text = "GyZ : ${item.gyZ}"

        b.tvPitch.text = "Pitch (°): ${"%.2f".format(item.pitch)}"


    }
}
