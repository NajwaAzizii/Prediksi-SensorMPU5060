package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityDatasetSensorBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DatasetSensorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatasetSensorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatasetSensorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.btnKembali.setOnClickListener { finish() }

        binding.rvDataset.layoutManager = LinearLayoutManager(this)

        val jsonString = loadJson("Dataset-MPU5060.json")
        val dataList = parseJson(jsonString)

        binding.rvDataset.adapter = SensorAdapter(dataList)
    }

    private fun loadJson(fileName: String): String =
        assets.open(fileName).bufferedReader().use { it.readText() }

    private fun parseJson(json: String): List<SensorData> {
        val type = object : TypeToken<List<SensorData>>() {}.type
        return Gson().fromJson(json, type)
    }
}
