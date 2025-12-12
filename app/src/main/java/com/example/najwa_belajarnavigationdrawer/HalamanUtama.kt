package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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

        // ===== TOOLBAR & DRAWER =====
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

        binding.btnLogin.setOnClickListener {
            Toast.makeText(this, "Menuju halaman Login", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, LoginActivity::class.java))
            binding.drawerlayout.closeDrawers()
        }

        val isiUtama = LayoutInflater.from(this).inflate(R.layout.main_content, null)
        binding.containerUtama.addView(isiUtama)

        setupCardClickListeners(isiUtama)
    }

    private fun setupCardClickListeners(view: View) {

        view.findViewById<CardView>(R.id.cardCekSudut)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardDatasetSensor)?.setOnClickListener {
            startActivity(Intent(this, DatasetSensorActivity::class.java))
        }

        view.findViewById<CardView>(R.id.cardLokasiPengujian)?.setOnClickListener {
            Toast.makeText(this, "Fitur Lokasi Pengujian", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<CardView>(R.id.cardBlog1)?.setOnClickListener {
            Toast.makeText(this, "Panduan Kalibrasi Sensor MPU6050", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<CardView>(R.id.cardBlog2)?.setOnClickListener {
            Toast.makeText(this, "Tips Mengambil Data Sensor", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<CardView>(R.id.cardBlog3)?.setOnClickListener {
            Toast.makeText(this, "Cara Meningkatkan Akurasi Model", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // sudah di beranda
            }
            R.id.nav_prediksi -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_dataset -> {
                startActivity(Intent(this, DatasetSensorActivity::class.java))
            }
            R.id.nav_lokasi -> {
                Toast.makeText(this, "Lokasi Pengujian", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_blog -> {
                Toast.makeText(this, "Menu Blog", Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerlayout.closeDrawers()
        return true
    }
}