package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogDetailBinding
import com.google.firebase.database.FirebaseDatabase

class BlogDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailBinding

    // ✅ WAJIB samakan dengan RTDB di Firebase Console kamu
    private val DATABASE_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"
    private val dbRef by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL).reference.child("blogs")
    }

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

        Log.d("BlogDetailActivity", "Open blogId=$blogId, ref=${dbRef.ref}")
        loadDetail(blogId)
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

                val imageUrl = snap.child("thumbnailUrl").getValue(String::class.java)
                    ?: snap.child("imageUrl").getValue(String::class.java)
                    ?: snap.child("gambar").getValue(String::class.java)
                    ?: snap.child("thumbnail").getValue(String::class.java)

                binding.tvTitle.text = title
                binding.tvAuthor.text = author
                binding.tvContent.text = content

                val url = imageUrl?.trim().takeIf { !it.isNullOrEmpty() }
                if (url != null) {
                    Glide.with(this)
                        .load(url)
                        .centerCrop()
                        .into(binding.imgCover)
                } else {
                    // kalau thumbnailUrl kosong, ya wajar gambar tetap placeholder
                    binding.imgCover.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal ambil data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
