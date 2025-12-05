package com.example.najwa_belajarnavigationdrawer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityMainBinding
import com.example.najwa_belajarnavigationdrawer.PitchPredictionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var predictionHelper: PitchPredictionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi prediction helper
        predictionHelper = PitchPredictionHelper(this)

        // Setup button untuk prediksi
        binding.btnPredict.setOnClickListener {
            performPrediction()
        }
    }

    private fun performPrediction() {
        // Ambil data dari input (EditText atau sensor)
        val acX = binding.etAcX.text.toString().toFloatOrNull() ?: 0f
        val acY = binding.etAcY.text.toString().toFloatOrNull() ?: 0f
        val acZ = binding.etAcZ.text.toString().toFloatOrNull() ?: 0f
        val gyX = binding.etGyX.text.toString().toFloatOrNull() ?: 0f
        val gyY = binding.etGyY.text.toString().toFloatOrNull() ?: 0f
        val gyZ = binding.etGyZ.text.toString().toFloatOrNull() ?: 0f

        // Lakukan prediksi
        val predictedPitch = predictionHelper.predictPitch(
            acX, acY, acZ, gyX, gyY, gyZ
        )

        // Tampilkan hasil
        if (predictedPitch != null) {
            binding.tvResult.text = "Sudut Pitch: ${"%.2f".format(predictedPitch)}°"
        } else {
            binding.tvResult.text = "Prediksi gagal"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        predictionHelper.close()
    }
}