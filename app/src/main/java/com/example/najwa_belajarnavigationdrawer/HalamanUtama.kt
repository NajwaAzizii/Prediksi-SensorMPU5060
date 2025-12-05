package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityHalamanUtamaBinding
import com.google.android.material.navigation.NavigationView

class HalamanUtama : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHalamanUtamaBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHalamanUtamaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerlayout,
            binding.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Tampilkan isi utama (main_content)
        val isiUtama = LayoutInflater.from(this).inflate(R.layout.main_content, null)
        binding.drawerlayout.findViewById<android.widget.FrameLayout>(R.id.containerUtama)
            .addView(isiUtama)

        // Setup click listeners untuk semua card
        setupCardClickListeners(isiUtama)
    }

    private fun setupCardClickListeners(view: android.view.View) {
        // Card Cek Sudut Kemiringan
        view.findViewById<CardView>(R.id.cardCekSudut)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Card Dataset Sensor
        view.findViewById<CardView>(R.id.cardDatasetSensor)?.setOnClickListener {
            Toast.makeText(this, "Fitur Dataset Sensor", Toast.LENGTH_SHORT).show()
            // TODO: Buka activity untuk melihat dataset sensor
        }

        // Card Lokasi Pengujian
        view.findViewById<CardView>(R.id.cardLokasiPengujian)?.setOnClickListener {
            Toast.makeText(this, "Fitur Lokasi Pengujian", Toast.LENGTH_SHORT).show()
            // TODO: Buka activity untuk lokasi pengujian
        }

        // Blog Card 1
        view.findViewById<CardView>(R.id.cardBlog1)?.setOnClickListener {
            Toast.makeText(this, "Panduan Kalibrasi Sensor MPU6050", Toast.LENGTH_SHORT).show()
            // TODO: Buka detail blog
        }

        // Blog Card 2
        view.findViewById<CardView>(R.id.cardBlog2)?.setOnClickListener {
            Toast.makeText(this, "Tips Mengambil Data Sensor", Toast.LENGTH_SHORT).show()
            // TODO: Buka detail blog
        }

        // Blog Card 3
        view.findViewById<CardView>(R.id.cardBlog3)?.setOnClickListener {
            Toast.makeText(this, "Cara Meningkatkan Akurasi Model", Toast.LENGTH_SHORT).show()
            // TODO: Buka detail blog
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Sudah di home
            }
            R.id.nav_prediksi -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_sensor -> {
                Toast.makeText(this, "Menu Sensor", Toast.LENGTH_SHORT).show()
                // TODO: Navigate ke fragment sensor
            }
            R.id.nav_regresi -> {
                Toast.makeText(this, "Menu Regresi", Toast.LENGTH_SHORT).show()
                // TODO: Navigate ke fragment regresi
            }
        }
        binding.drawerlayout.closeDrawers()
        return true
    }
}