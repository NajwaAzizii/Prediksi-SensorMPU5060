package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogDetailBinding
import com.google.firebase.database.FirebaseDatabase

class BlogDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailBinding

    private val DATABASE_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"
    private val dbRef by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL).reference.child("blogs")
    }

    private var currentBlogTitle = ""
    private var currentBlogContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val blogId = intent.getStringExtra("BLOG_ID")?.trim()
            ?: intent.getStringExtra("BLOG ID")?.trim()
            ?: intent.getStringExtra("id")?.trim()

        if (blogId.isNullOrEmpty()) {
            Toast.makeText(this, "Blog tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("BlogDetailActivity", "Open blogId=$blogId")
        loadDetail(blogId)

        binding.btnShare.setOnClickListener { shareContent() }
    }

    private fun loadDetail(id: String) {
        dbRef.child(id).get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    Toast.makeText(this, "Blog tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val title = snap.child("judul").getValue(String::class.java)
                    ?: snap.child("title").getValue(String::class.java)
                    ?: "-"

                val author = snap.child("penulis").getValue(String::class.java)
                    ?: snap.child("author").getValue(String::class.java)
                    ?: snap.child("nama").getValue(String::class.java)
                    ?: "-"

                val content = snap.child("isi").getValue(String::class.java)
                    ?: snap.child("content").getValue(String::class.java)
                    ?: snap.child("deskripsi").getValue(String::class.java)
                    ?: ""

                val createdAt = snap.child("createdAt").getValue(Long::class.java) ?: 0L

                val imageUrl = snap.child("thumbnailUrl").getValue(String::class.java)
                    ?: snap.child("imageUrl").getValue(String::class.java)
                    ?: snap.child("gambar").getValue(String::class.java)
                    ?: snap.child("thumbnail").getValue(String::class.java)

                // ✅ INI YANG PENTING: ambil base64 juga
                val thumbBase64 = snap.child("thumbnailBase64").getValue(String::class.java)
                    ?: snap.child("thumbBase64").getValue(String::class.java)

                currentBlogTitle = title
                currentBlogContent = content

                binding.tvTitle.text = title
                binding.tvTitleOverlay.text = title
                binding.tvAuthor.text = "Oleh: $author"
                binding.tvContent.text = content

                // ✅ Prioritas base64 dulu (biar pasti tampil)
                val b64 = thumbBase64?.trim().orEmpty()
                if (b64.isNotEmpty()) {
                    if (!setImageFromBase64(b64)) {
                        binding.imgCover.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                    return@addOnSuccessListener
                }

                // Fallback ke URL
                val url = imageUrl?.trim().takeIf { !it.isNullOrEmpty() }
                if (url != null) {
                    // cache-bust pakai createdAt biar gambar baru kebaca
                    val busted = cacheBust(url, createdAt)

                    Glide.with(this)
                        .load(busted)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(binding.imgCover)
                } else {
                    binding.imgCover.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal ambil data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setImageFromBase64(b64Raw: String): Boolean {
        return try {
            // kalau ada prefix "data:image/jpeg;base64,..."
            val clean = b64Raw.substringAfter("base64,", b64Raw)
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                binding.imgCover.setImageBitmap(bmp)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    private fun cacheBust(url: String, createdAt: Long): String {
        if (createdAt <= 0L) return url
        return if (url.contains("?")) "$url&v=$createdAt" else "$url?v=$createdAt"
    }

    private fun shareContent() {
        val shareText = """
            📝 $currentBlogTitle
            
            ${currentBlogContent.take(200)}${if (currentBlogContent.length > 200) "..." else ""}
            
            Baca selengkapnya di aplikasi MPU6050 Blog!
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Bagikan blog via"))
    }
}
