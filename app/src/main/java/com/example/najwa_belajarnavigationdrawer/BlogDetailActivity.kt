package com.example.najwa_belajarnavigationdrawer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogDetailBinding
import com.google.firebase.database.FirebaseDatabase

class BlogDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailBinding
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference.child("blogs") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val blogId = intent.getStringExtra("BLOG_ID")?.trim()
        if (blogId.isNullOrEmpty()) {
            Toast.makeText(this, "Blog tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

                val post = snap.getValue(BlogPost::class.java)
                if (post == null) {
                    Toast.makeText(this, "Data blog kosong", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                binding.tvTitle.text = post.title.orEmpty()
                binding.tvAuthor.text = post.author.orEmpty()
                binding.tvContent.text = post.content.orEmpty()

                val url = post.thumbnailUrl.orEmpty()
                if (url.isNotBlank()) {
                    Glide.with(this)
                        .load(url)
                        .centerCrop()
                        .into(binding.imgCover)
                } else {
                    binding.imgCover.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal ambil data: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
