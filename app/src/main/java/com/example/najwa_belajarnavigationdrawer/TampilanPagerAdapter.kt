package com.example.najwa_belajarnavigationdrawer

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.najwa_belajarnavigationdrawer.fragment.FragmentPendahuluan
import com.example.najwa_belajarnavigationdrawer.fragment.FragmentSensor
import com.example.najwa_belajarnavigationdrawer.fragment.FragmentRegresi

class TampilanPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentPendahuluan()
            1 -> FragmentSensor()
            else -> FragmentRegresi()
        }
    }
}
