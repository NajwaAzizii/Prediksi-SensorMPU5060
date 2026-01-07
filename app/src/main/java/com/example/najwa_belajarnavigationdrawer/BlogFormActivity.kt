package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import kotlin.math.max

class BlogFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogFormBinding

    companion object {
        private const val TAG = "BlogFormActivity"
        private const val DB_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"
        private const val BLOG_NODE = "blogs"
        private const val ADMIN_EMAIL = "admin@gmail.com"

        private const val IMGBB_API_KEY = "eef89ba7d305aa358b0e99202f73fa9d"
    }

    private val db by lazy { FirebaseDatabase.getInstance(DB_URL) }
    private val dbRef by lazy { db.reference.child(BLOG_NODE) }

    private val http by lazy { OkHttpClient() }

    private var blogId: String? = null
    private var pickedImage: Uri? = null
    private var oldThumbUrl: String? = null
    private var oldThumbBase64: String? = null
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

        binding.btnPickImage.setOnClickListener { pickImageLauncher.launch("image/*") }
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
                oldThumbBase64 = post.thumbnailBase64
                oldCreatedAt = post.createdAt

                // tampilkan base64 dulu (stabil), baru url
                when {
                    !post.thumbnailBase64.isNullOrBlank() -> {
                        setImageFromBase64(post.thumbnailBase64!!, binding.imgThumb)
                    }
                    !post.thumbnailUrl.isNullOrBlank() -> {
                        Glide.with(this).load(post.thumbnailUrl).centerCrop().into(binding.imgThumb)
                    }
                    else -> binding.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
            .addOnFailureListener { toast("Gagal ambil data: ${it.message}") }
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
            // base64 thumbnail kecil biar RTDB gak berat
            val thumbBytes = uriToCompressedJpeg(uri, maxSize = 420, quality = 70)
            if (thumbBytes == null) {
                setLoading(false)
                toast("Gagal baca gambar dari HP")
                return
            }
            val thumbBase64 = Base64.encodeToString(thumbBytes, Base64.NO_WRAP)

            // upload ImgBB -> kalau sukses simpan url+base64
            uploadThumbToImgBB(
                uri = uri,
                onSuccess = { imageUrl ->
                    val bustedUrl = cacheBust(imageUrl)
                    upsertPost(id, title, author, content, bustedUrl, thumbBase64)
                },
                onError = { err ->
                    Log.e(TAG, "ImgBB error: $err")

                    // kalau ImgBB bermasalah, tetap simpan base64 agar gambar tetap bisa tampil
                    // thumbUrl pakai old (biar tidak jadi kosong)
                    upsertPost(id, title, author, content, oldThumbUrl.orEmpty(), thumbBase64)
                }
            )
        } else {
            // tidak pilih gambar baru -> pakai yang lama
            upsertPost(
                id = id,
                title = title,
                author = author,
                content = content,
                thumbUrl = oldThumbUrl.orEmpty(),
                thumbBase64 = oldThumbBase64.orEmpty()
            )
        }
    }

    private fun cacheBust(url: String): String {
        val ts = System.currentTimeMillis()
        return if (url.contains("?")) "$url&ts=$ts" else "$url?ts=$ts"
    }

    private fun uploadThumbToImgBB(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val jpegBytes = uriToCompressedJpeg(uri, maxSize = 1280, quality = 80)
                    ?: run {
                        runOnUiThread { onError("Gagal baca gambar") }
                        return@Thread
                    }

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        "thumb.jpg",
                        jpegBytes.toRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val url = "https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY"
                val req = Request.Builder().url(url).post(body).build()

                http.newCall(req).execute().use { res ->
                    val raw = res.body?.string().orEmpty()

                    if (!res.isSuccessful) {
                        runOnUiThread { onError("HTTP ${res.code}: $raw") }
                        return@use
                    }

                    val json = JSONObject(raw)
                    val data = json.getJSONObject("data")

                    val imageUrl = data.optString("display_url").ifBlank {
                        data.optJSONObject("image")?.optString("url").orEmpty()
                    }

                    if (imageUrl.isBlank()) {
                        runOnUiThread { onError("ImgBB tidak mengembalikan url") }
                        return@use
                    }

                    runOnUiThread { onSuccess(imageUrl) }
                }
            } catch (e: Exception) {
                runOnUiThread { onError(e.message ?: "Unknown error") }
            }
        }.start()
    }

    private fun uriToCompressedJpeg(uri: Uri, maxSize: Int, quality: Int): ByteArray? {
        val input = contentResolver.openInputStream(uri) ?: return null
        val bmp = input.use { BitmapFactory.decodeStream(it) } ?: return null

        val largest = max(bmp.width, bmp.height)
        val scaled: Bitmap = if (largest > maxSize) {
            val ratio = maxSize.toFloat() / largest.toFloat()
            val w = (bmp.width * ratio).toInt().coerceAtLeast(1)
            val h = (bmp.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bmp, w, h, true)
        } else bmp

        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        return baos.toByteArray()
    }

    private fun upsertPost(
        id: String,
        title: String,
        author: String,
        content: String,
        thumbUrl: String,
        thumbBase64: String
    ) {
        val post = BlogPost(
            id = id,
            title = title,
            author = author,
            content = content,
            thumbnailUrl = thumbUrl,
            thumbnailBase64 = thumbBase64,
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

    private fun setImageFromBase64(b64: String, target: ImageView) {
        runCatching {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            target.setImageBitmap(bmp)
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
