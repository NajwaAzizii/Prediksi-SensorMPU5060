package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class BlogFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogFormBinding

    private val dbRef by lazy { FirebaseDatabase.getInstance().reference.child("blogs") }
    private val storage by lazy { FirebaseStorage.getInstance().reference.child("blog_thumbs") }

    private var blogId: String? = null
    private var pickedImage: Uri? = null
    private var oldThumbUrl: String? = null
    private var oldCreatedAt: Long? = null

    // ✅ email admin (samakan)
    private val ADMIN_EMAIL = "admin@gmail.com"

    private fun isAdmin(): Boolean {
        val email = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
        return email == ADMIN_EMAIL.lowercase()
    }

    override fun onStart() {
        super.onStart()
        // ✅ pengaman: kalau bukan admin, jangan boleh akses
        if (!isAdmin()) {
            Toast.makeText(this, "Akses ditolak (admin saja)", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                pickedImage = uri
                Glide.with(this).load(uri).centerCrop().into(binding.imgThumb)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            // finish() cukup, karena BlogFormActivity dibuka dari BlogListActivity
            finish()
            // atau: onBackPressedDispatcher.onBackPressed()
        }
        blogId = intent.getStringExtra("BLOG_ID")

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener { save() }

        if (blogId != null) loadDetail(blogId!!)
    }

    private fun loadDetail(id: String) {
        dbRef.child(id).get().addOnSuccessListener { snap ->
            val post = snap.getValue(BlogPost::class.java) ?: return@addOnSuccessListener
            binding.etTitle.setText(post.title)
            binding.etAuthor.setText(post.author)
            binding.etContent.setText(post.content)

            oldThumbUrl = post.thumbnailUrl
            oldCreatedAt = post.createdAt

            if (!post.thumbnailUrl.isNullOrBlank()) {
                Glide.with(this).load(post.thumbnailUrl).centerCrop().into(binding.imgThumb)
            }
        }.addOnFailureListener {
            toast("Gagal ambil data: ${it.message}")
        }
    }

    private fun save() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val author = binding.etAuthor.text?.toString()?.trim().orEmpty()
        val content = binding.etContent.text?.toString()?.trim().orEmpty()

        if (title.isEmpty()) { toast("Judul wajib diisi"); return }
        if (author.isEmpty()) { toast("Penulis wajib diisi"); return }
        if (content.isEmpty()) { toast("Isi wajib diisi"); return }

        setLoading(true)

        val id = blogId ?: dbRef.push().key
        if (id == null) {
            setLoading(false)
            toast("Gagal membuat ID")
            return
        }

        // jika pilih gambar baru -> upload ke storage
        if (pickedImage != null) {
            val ref = storage.child("$id.jpg")

            ref.putFile(pickedImage!!)
                .continueWithTask { uploadTask ->
                    if (!uploadTask.isSuccessful) {
                        throw uploadTask.exception ?: Exception("Upload gagal")
                    }
                    ref.downloadUrl
                }
                .addOnSuccessListener { url ->
                    upsertPost(
                        id = id,
                        title = title,
                        author = author,
                        content = content,
                        thumbUrl = url.toString()
                    )
                }
                .addOnFailureListener {
                    setLoading(false)
                    toast("Upload thumbnail gagal: ${it.message}")
                }
        } else {
            // tidak pilih gambar baru -> pakai yang lama (edit) / kosong (create)
            upsertPost(
                id = id,
                title = title,
                author = author,
                content = content,
                thumbUrl = oldThumbUrl ?: ""
            )
        }
    }

    private fun upsertPost(id: String, title: String, author: String, content: String, thumbUrl: String) {
        val post = BlogPost(
            id = id,
            title = title,
            author = author,
            content = content,
            thumbnailUrl = thumbUrl,
            // ✅ kalau edit: pertahankan createdAt lama
            createdAt = oldCreatedAt ?: System.currentTimeMillis()
        )

        dbRef.child(id).setValue(post)
            .addOnSuccessListener {
                toast("Tersimpan")
                finish()
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Gagal simpan: ${it.message}")
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSave.isEnabled = !isLoading
        binding.btnPickImage.isEnabled = !isLoading
        binding.etTitle.isEnabled = !isLoading
        binding.etAuthor.isEnabled = !isLoading
        binding.etContent.isEnabled = !isLoading
        binding.btnSave.text = if (isLoading) "Menyimpan..." else "Simpan"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
