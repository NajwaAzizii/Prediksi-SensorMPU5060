package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLayarPembukaBinding

class LayarPembuka : AppCompatActivity() {
    private lateinit var binding: ActivityLayarPembukaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayarPembukaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, ViewPager::class.java))
            finish()
        }, 2500)
    }
}
