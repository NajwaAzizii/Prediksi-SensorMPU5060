package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aman walau binding field btnKembali belum kebentuk
        val btnKembali = binding.root.findViewById<Button>(R.id.btnKembali)
        btnKembali.setOnClickListener { finish() }
    }
}
