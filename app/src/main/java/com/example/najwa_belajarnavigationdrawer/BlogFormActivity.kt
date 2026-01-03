package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

    companion object {
        private const val TAG = "BlogFormActivity"

        // RTDB kamu (sesuai screenshot)
        private const val DB_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"

        private const val BLOG_NODE = "blogs"
        private const val THUMB_FOLDER = "blog_thumbs"
        private const val ADMIN_EMAIL = "admin@gmail.com"
    }

    // ✅ Paksa DB yang sama dengan admin
    private val db by lazy { FirebaseDatabase.getInstance(DB_URL) }
    private val dbRef by lazy { db.reference.child(BLOG_NODE) }

    // ✅ Storage ikuti google-services.json (paling aman)
    private val storageRoot by lazy {
        FirebaseStorage.getInstance().reference.child(THUMB_FOLDER)
    }

    private var blogId: String? = null
    private var pickedImage: Uri? = null
    private var oldThumbUrl: String? = null
    private var oldCreatedAt: Long? = null

    private fun isAdmin(): Boolean {
        val email = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase()
        return email == ADMIN_EMAIL.lowercase()
    }

    override fun onStart() {
        super.onStart()
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

        binding.btnBack.setOnClickListener { finish() }

        blogId = intent.getStringExtra("BLOG_ID")?.trim()

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener { save() }

        if (!blogId.isNullOrEmpty()) loadDetail(blogId!!)
    }

    private fun loadDetail(id: String) {
        dbRef.child(id).get()
            .addOnSuccessListener { snap ->
                val post = snap.getValue(BlogPost::class.java) ?: return@addOnSuccessListener

                binding.etTitle.setText(post.title)
                binding.etAuthor.setText(post.author)
                binding.etContent.setText(post.content)

                oldThumbUrl = post.thumbnailUrl
                oldCreatedAt = post.createdAt

                if (!post.thumbnailUrl.isNullOrBlank()) {
                    Glide.with(this).load(post.thumbnailUrl).centerCrop().into(binding.imgThumb)
                }
            }
            .addOnFailureListener {
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

        val id = (blogId ?: dbRef.push().key)?.trim()
        if (id.isNullOrEmpty()) {
            setLoading(false)
            toast("Gagal membuat ID")
            return
        }

        val uri = pickedImage
        if (uri != null) {
            val ref = storageRoot.child("$id.jpg")

            Log.d(TAG, "Upload -> bucket=${ref.bucket} path=${ref.path} uri=$uri")

            // ✅ Pola upload paling aman: putFile -> kalau sukses baru ambil downloadUrl
            ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Upload gagal")
                    }
                    ref.downloadUrl
                }
                .addOnSuccessListener { url ->
                    Log.d(TAG, "downloadUrl OK: $url")
                    upsertPost(
                        id = id,
                        title = title,
                        author = author,
                        content = content,
                        thumbUrl = url.toString()
                    )
                }
                .addOnFailureListener { e ->
                    setLoading(false)
                    Log.e(TAG, "Upload/URL gagal", e)
                    toast("Upload thumbnail gagal: ${e.message}")
                }

        } else {
            // tidak pilih gambar baru -> pakai lama / kosong
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

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
