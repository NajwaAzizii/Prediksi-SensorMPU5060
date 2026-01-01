package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLokasiPengujianBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

class LokasiPengujianActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLokasiPengujianBinding
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLokasiPengujianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Toolbar + tombol back kiri atas
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // back ke activity sebelumnya
        }

        // Penting: user-agent biar tile OSM tidak diblok
        val cfg = Configuration.getInstance()
        cfg.userAgentValue = packageName

        // Cache di internal (tidak perlu permission storage)
        val basePath = File(cacheDir, "osmdroid")
        val tileCache = File(basePath, "tile")
        cfg.osmdroidBasePath = basePath
        cfg.osmdroidTileCache = tileCache

        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK) // OSM online
        map.setMultiTouchControls(true)

        // Lokasi PCR
        val pcr = GeoPoint(0.507068, 101.447779)

        // Marker
        val marker = Marker(map).apply {
            position = pcr
            title = "Lokasi Pengujian: PCR"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)

        // Kamera
        map.controller.setZoom(17.0)
        map.controller.setCenter(pcr)
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }
}
