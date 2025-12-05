package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityViewPager2Binding
import com.google.android.material.tabs.TabLayoutMediator

class ViewPager : AppCompatActivity() {
    private lateinit var binding: ActivityViewPager2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPager2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val adapter = TampilanPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val tabTitles = arrayOf("Pendahuluan", "Sensor", "Regresi")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()
    }
}
