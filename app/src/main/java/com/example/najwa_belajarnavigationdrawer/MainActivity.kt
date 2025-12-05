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

        // Inisialisasi prediction helper
        predictionHelper = PitchPredictionHelper(this)

        // ✅ Tombol Prediksi
        binding.btnPredict.setOnClickListener {
            performPrediction()
        }

        // ✅ Tombol Panah Kembali
        binding.btnKembali.setOnClickListener {
            finish() // kembali ke halaman utama
        }
    }

    private fun performPrediction() {

        // ✅ Validasi: pastikan tidak ada input kosong
        if (binding.etAcX.text.isNullOrEmpty() ||
            binding.etAcY.text.isNullOrEmpty() ||
            binding.etAcZ.text.isNullOrEmpty() ||
            binding.etGyX.text.isNullOrEmpty() ||
            binding.etGyY.text.isNullOrEmpty() ||
            binding.etGyZ.text.isNullOrEmpty()
        ) {
            Toast.makeText(this, "Semua data sensor wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data dari input
        val acX = binding.etAcX.text.toString().toFloat()
        val acY = binding.etAcY.text.toString().toFloat()
        val acZ = binding.etAcZ.text.toString().toFloat()
        val gyX = binding.etGyX.text.toString().toFloat()
        val gyY = binding.etGyY.text.toString().toFloat()
        val gyZ = binding.etGyZ.text.toString().toFloat()

        // Lakukan prediksi
        val predictedPitch = predictionHelper.predictPitch(
            acX, acY, acZ, gyX, gyY, gyZ
        )

        // Tampilkan hasil
        if (predictedPitch != null) {
            binding.tvResult.text =
                "Sudut Kemiringan (Pitch °): ${"%.2f".format(predictedPitch)}"
        } else {
            binding.tvResult.text = "Prediksi gagal"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        predictionHelper.close()
    }
}
