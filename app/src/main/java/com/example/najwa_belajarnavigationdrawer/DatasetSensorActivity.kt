package com.example.najwa_belajarnavigationdrawer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityDatasetSensorBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter

class DatasetSensorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatasetSensorBinding

    private var allData: List<SensorData> = emptyList()
    private var filteredData: List<SensorData> = emptyList()
    private lateinit var adapter: SensorAdapter

    // Pagination
    private var currentPage = 1
    private val itemsPerPage = 10
    private var totalPages = 1

    companion object {
        private const val TAG = "DatasetSensor"
        private const val REQUEST_WRITE_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatasetSensorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupRecyclerView()
        loadData()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = SensorAdapter(emptyList())
        binding.rvDataset.layoutManager = LinearLayoutManager(this)
        binding.rvDataset.adapter = adapter
    }

    private fun loadData() {
        try {
            val jsonString = loadJson("Dataset-MPU5060.json")
            allData = parseJson(jsonString)
            filteredData = allData

            updatePagination()
            displayCurrentPage()
            updateInfo()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnKembali.setOnClickListener { finish() }

        // Search
        binding.btnSearch.setOnClickListener {
            if (binding.cardSearch.visibility == View.VISIBLE) {
                closeSearch()
            } else {
                openSearch()
            }
        }

        binding.btnCloseSearch.setOnClickListener {
            closeSearch()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterData(s?.toString()?.trim().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Export
        binding.btnExport.setOnClickListener {
            showExportDialog()
        }

        // Pagination
        binding.btnPrevious.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                displayCurrentPage()
                binding.rvDataset.scrollToPosition(0)
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                displayCurrentPage()
                binding.rvDataset.scrollToPosition(0)
            }
        }
    }

    private fun openSearch() {
        binding.cardSearch.visibility = View.VISIBLE
        binding.etSearch.requestFocus()
    }

    private fun closeSearch() {
        binding.cardSearch.visibility = View.GONE
        binding.etSearch.text = null
        filteredData = allData
        currentPage = 1
        updatePagination()
        displayCurrentPage()
        updateInfo()
    }

    private fun filterData(query: String) {
        filteredData = if (query.isEmpty()) {
            allData
        } else {
            allData.filter { data ->
                data.getAcX().toString().contains(query, true) ||
                        data.getAcY().toString().contains(query, true) ||
                        data.getAcZ().toString().contains(query, true) ||
                        data.getGyX().toString().contains(query, true) ||
                        data.getGyY().toString().contains(query, true) ||
                        data.getGyZ().toString().contains(query, true) ||
                        String.format("%.2f", data.getPitch()).contains(query, true)
            }
        }

        currentPage = 1
        updatePagination()
        displayCurrentPage()
        updateInfo()
    }

    private fun updatePagination() {
        totalPages = if (filteredData.isEmpty()) 1
        else (filteredData.size + itemsPerPage - 1) / itemsPerPage
    }

    private fun displayCurrentPage() {
        val startIndex = (currentPage - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, filteredData.size)

        val pageData = if (filteredData.isEmpty()) emptyList()
        else filteredData.subList(startIndex, endIndex)

        adapter.updateData(pageData)

        binding.tvPageInfo.text = "Halaman $currentPage dari $totalPages"

        binding.btnPrevious.alpha = if (currentPage > 1) 1.0f else 0.3f
        binding.btnPrevious.isEnabled = currentPage > 1

        binding.btnNext.alpha = if (currentPage < totalPages) 1.0f else 0.3f
        binding.btnNext.isEnabled = currentPage < totalPages

        if (pageData.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvDataset.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvDataset.visibility = View.VISIBLE
        }
    }

    private fun updateInfo() {
        binding.tvTotalData.text = filteredData.size.toString()

        val startIndex = (currentPage - 1) * itemsPerPage + 1
        val endIndex = minOf(startIndex + itemsPerPage - 1, filteredData.size)

        binding.tvShowing.text = if (filteredData.isEmpty()) "0" else "$startIndex-$endIndex"
    }

    private fun showExportDialog() {
        if (allData.isEmpty()) {
            Toast.makeText(this, "⚠️ Tidak ada data untuk di-export", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("📊 Export Data")
            .setMessage("Export semua ${allData.size} data ke file CSV?\n\n(File CSV bisa dibuka di Excel)")
            .setPositiveButton("Export CSV") { dialog, _ ->
                checkPermissionAndExport()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkPermissionAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToCSV()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_PERMISSION
                )
            } else {
                exportToCSV()
            }
        }
    }

    private fun exportToCSV() {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "MPU6050_Dataset_$timestamp.csv"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            // Create proper CSV file with comma separator
            FileWriter(file).use { writer ->
                // Header
                writer.append("No,AcX (LSB),AcY (LSB),AcZ (LSB),GyX (LSB),GyY (LSB),GyZ (LSB),Pitch (°)\n")

                // Data - use comma separator for CSV
                allData.forEachIndexed { index, data ->
                    writer.append("${index + 1},")
                    writer.append("${data.getAcX()},")
                    writer.append("${data.getAcY()},")
                    writer.append("${data.getAcZ()},")
                    writer.append("${data.getGyX()},")
                    writer.append("${data.getGyY()},")
                    writer.append("${data.getGyZ()},")
                    writer.append("${data.getPitch()}\n")
                }
            }

            Log.d(TAG, "CSV exported: ${file.absolutePath}")

            // Show success dialog
            showOpenFileDialog(file)

        } catch (e: Exception) {
            Log.e(TAG, "Export error", e)
            Toast.makeText(this, "❌ Error export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showOpenFileDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle("✅ Export Berhasil!")
            .setMessage("""
                File CSV berhasil disimpan dan bisa dibuka di Excel!
                
                📊 ${file.name}
                
                Lokasi: Downloads
                
                Buka aplikasi Files untuk melihat file, atau buka Excel dan pilih "Open" → "Browse" → cari file di Downloads.
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                // Copy filename to clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("filename", file.name)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this,
                    "✅ Nama file disalin ke clipboard!\n" +
                            "Buka Excel → Open → Browse → Downloads\n" +
                            "File: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()

                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToCSV()
            } else {
                Toast.makeText(this, "Permission ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadJson(fileName: String): String =
        assets.open(fileName).bufferedReader().use { it.readText() }

    private fun parseJson(json: String): List<SensorData> {
        val type = object : TypeToken<List<SensorData>>() {}.type
        return Gson().fromJson(json, type)
    }
}