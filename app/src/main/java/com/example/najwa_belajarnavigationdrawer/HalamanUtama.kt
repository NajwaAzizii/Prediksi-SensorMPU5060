package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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

        // 🔹 Tampilkan isi utama (main_content)
        val isiUtama = LayoutInflater.from(this).inflate(R.layout.main_content, null)
        binding.drawerlayout.findViewById<android.widget.FrameLayout>(R.id.containerUtama)
            .addView(isiUtama)
    }


    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        // Sementara belum ada aksi
        binding.drawerlayout.closeDrawers()
        return true
    }
}
