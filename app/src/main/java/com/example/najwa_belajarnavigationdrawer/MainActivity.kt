package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var predictionHelper: PitchPredictionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan ActionBar
        supportActionBar?.hide()

        // Inisialisasi prediction helper
        predictionHelper = PitchPredictionHelper(this)

        // Tombol kembali
        binding.btnKembali.setOnClickListener {
            finish()
        }

        // Setup button untuk prediksi
        binding.btnPredict.setOnClickListener {
            performPrediction()
        }
    }

    private fun performPrediction() {
        // Validasi input
        if (!validateInputs()) {
            Toast.makeText(this, "Mohon isi semua field dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data dari input
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
            binding.tvResult.text = "Sudut Kemiringan (Pitch °): ${"%.2f".format(predictedPitch)}"
            Toast.makeText(this, "Prediksi berhasil!", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvResult.text = "Sudut Kemiringan (Pitch °): Error"
            Toast.makeText(this, "Prediksi gagal, coba lagi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        return binding.etAcX.text.isNotEmpty() &&
                binding.etAcY.text.isNotEmpty() &&
                binding.etAcZ.text.isNotEmpty() &&
                binding.etGyX.text.isNotEmpty() &&
                binding.etGyY.text.isNotEmpty() &&
                binding.etGyZ.text.isNotEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        predictionHelper.close()
    }
}