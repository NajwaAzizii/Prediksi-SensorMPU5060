package com.example.najwa_belajarnavigationdrawer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLokasiPengujianBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LokasiPengujianActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityLokasiPengujianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLokasiPengujianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val pcr = LatLng(0.507068, 101.447779)
        mMap.addMarker(MarkerOptions().position(pcr).title("Lokasi Pengujian: PCR"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pcr, 17f))
    }
}
