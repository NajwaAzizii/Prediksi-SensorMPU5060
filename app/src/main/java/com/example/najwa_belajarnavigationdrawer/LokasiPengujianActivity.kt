package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.view.View
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

    private val locations = listOf(
        LokasiPengujian(
            nama = "Lokasi 1: Politeknik Caltex Riau",
            deskripsi = "Kampus PCR - Umban Sari\nLaboratorium Teknik Elektro\nPengujian indoor dengan kondisi terkontrol",
            lat = 0.507068,
            lng = 101.447779,
            isPrimary = true
        ),
        LokasiPengujian(
            nama = "Lokasi 2: Lapangan Purna MTQ Pekanbaru",
            deskripsi = "Area terbuka untuk pengujian outdoor\nJl. Hang Tuah, Sukajadi\nPengujian stabilitas sensor di berbagai kondisi cuaca",
            lat = 0.531889,
            lng = 101.447500,
            isPrimary = false
        )
    )

    private var currentLocationIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLokasiPengujianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupMap()
        setupListeners()
        showLocationInfo(currentLocationIndex)
    }

    private fun setupMap() {
        // OSM Configuration
        val cfg = Configuration.getInstance()
        cfg.userAgentValue = packageName

        val basePath = File(cacheDir, "osmdroid")
        val tileCache = File(basePath, "tile")
        cfg.osmdroidBasePath = basePath
        cfg.osmdroidTileCache = tileCache

        // Map setup
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Add markers for both locations
        addMarkers()

        // Center on first location
        val firstLocation = GeoPoint(locations[0].lat, locations[0].lng)
        map.controller.setZoom(15.0)
        map.controller.setCenter(firstLocation)
        map.invalidate()
    }

    private fun addMarkers() {
        locations.forEachIndexed { index, location ->
            val marker = Marker(map).apply {
                position = GeoPoint(location.lat, location.lng)
                title = location.nama
                snippet = location.deskripsi
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Use default OSM marker - simple red pin
                // Don't set custom icon - OSM default looks best

                setOnMarkerClickListener { clickedMarker, _ ->
                    // Update info card when marker clicked
                    val clickedIndex = locations.indexOfFirst {
                        it.lat == clickedMarker.position.latitude &&
                                it.lng == clickedMarker.position.longitude
                    }
                    if (clickedIndex != -1) {
                        currentLocationIndex = clickedIndex
                        showLocationInfo(clickedIndex)

                        // Center on clicked marker
                        map.controller.animateTo(clickedMarker.position)
                    }
                    true
                }
            }
            map.overlays.add(marker)
        }
    }

    private fun setupListeners() {
        binding.btnKembali.setOnClickListener {
            finish()
        }

        binding.btnLokasi1.setOnClickListener {
            currentLocationIndex = 0
            showLocationInfo(0)
            centerOnLocation(0)
        }

        binding.btnLokasi2.setOnClickListener {
            currentLocationIndex = 1
            showLocationInfo(1)
            centerOnLocation(1)
        }

        binding.btnZoomIn.setOnClickListener {
            map.controller.setZoom(map.zoomLevelDouble + 1)
        }

        binding.btnZoomOut.setOnClickListener {
            map.controller.setZoom(map.zoomLevelDouble - 1)
        }

        binding.btnShowBoth.setOnClickListener {
            showBothLocations()
        }
    }

    private fun showLocationInfo(index: Int) {
        val location = locations[index]

        binding.tvLokasiNama.text = location.nama
        binding.tvLokasiDeskripsi.text = location.deskripsi
        binding.tvLatitude.text = "Latitude: ${String.format("%.6f", location.lat)}"
        binding.tvLongitude.text = "Longitude: ${String.format("%.6f", location.lng)}"

        // Update button states
        if (index == 0) {
            binding.btnLokasi1.alpha = 1.0f
            binding.btnLokasi2.alpha = 0.6f
        } else {
            binding.btnLokasi1.alpha = 0.6f
            binding.btnLokasi2.alpha = 1.0f
        }

        // Show info card with animation
        binding.cardLokasiInfo.visibility = View.VISIBLE
        binding.cardLokasiInfo.animate()
            .alpha(1.0f)
            .setDuration(300)
            .start()
    }

    private fun centerOnLocation(index: Int) {
        val location = locations[index]
        val point = GeoPoint(location.lat, location.lng)
        map.controller.animateTo(point)
        map.controller.setZoom(17.0)
    }

    private fun showBothLocations() {
        // Calculate bounding box to show both locations
        val minLat = minOf(locations[0].lat, locations[1].lat)
        val maxLat = maxOf(locations[0].lat, locations[1].lat)
        val minLng = minOf(locations[0].lng, locations[1].lng)
        val maxLng = maxOf(locations[0].lng, locations[1].lng)

        val centerLat = (minLat + maxLat) / 2
        val centerLng = (minLng + maxLng) / 2

        map.controller.animateTo(GeoPoint(centerLat, centerLng))
        map.controller.setZoom(13.0)  // Adjusted for ~3km distance
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