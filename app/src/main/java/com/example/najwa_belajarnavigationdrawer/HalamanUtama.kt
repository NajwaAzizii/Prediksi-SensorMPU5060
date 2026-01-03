package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityHalamanUtamaBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*

class HalamanUtama : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHalamanUtamaBinding
    private lateinit var toggle: ActionBarDrawerToggle

    // ====== HOME VIEW (main_content) ======
    private lateinit var homeView: View

    // ====== FIREBASE BLOG ======
    private val BLOG_NODE = "blogs"
    private val DATABASE_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"

    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL)
    }
    private val blogsRef: DatabaseReference by lazy {
        db.reference.child(BLOG_NODE)
    }
    private val latestQuery: Query by lazy {
        blogsRef.orderByChild("createdAt").limitToLast(3)
    }
    private var latestListener: ValueEventListener? = null

    private data class BlogItem(
        val id: String,
        val title: String,
        val author: String,
        val content: String,
        val imageUrl: String?,
        val createdAt: Long
    )

    // ====== REKOMENDASI VIEWS (di main_content.xml) ======
    private lateinit var cardBlog1: CardView
    private lateinit var cardBlog2: CardView
    private lateinit var cardBlog3: CardView

    private lateinit var ivBlog1: ImageView
    private lateinit var ivBlog2: ImageView
    private lateinit var ivBlog3: ImageView

    private lateinit var tvBlogTitle1: TextView
    private lateinit var tvBlogTitle2: TextView
    private lateinit var tvBlogTitle3: TextView

    private lateinit var tvBlogAuthor1: TextView
    private lateinit var tvBlogAuthor2: TextView
    private lateinit var tvBlogAuthor3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHalamanUtamaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===== TOOLBAR & DRAWER =====
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerlayout,
            binding.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerlayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // ===== LOAD main_content.xml ke containerUtama =====
        homeView = LayoutInflater.from(this).inflate(R.layout.main_content, binding.containerUtama, false)
        binding.containerUtama.addView(homeView)

        // ===== CLICK LISTENER CARD FITUR (tetap) =====
        setupStaticCardClickListeners(homeView)

        // ===== BLOG REKOMENDASI: bind view + loading + attach listener =====
        bindRecommendationViews(homeView)
        setRecommendationLoadingState()

        // (opsional) kalau kamu sudah tambahkan id btnOpenBlogList di main_content
        homeView.findViewById<View?>(R.id.btnOpenBlogList)?.setOnClickListener {
            startActivity(Intent(this, BlogListUserActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        attachLatestBlogsListener()
    }

    override fun onStop() {
        super.onStop()
        latestListener?.let { latestQuery.removeEventListener(it) }
        latestListener = null
    }

    private fun setupStaticCardClickListeners(view: View) {
        view.findViewById<CardView>(R.id.cardCekSudut)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardDatasetSensor)?.setOnClickListener {
            startActivity(Intent(this, DatasetSensorActivity::class.java))
        }
        view.findViewById<CardView>(R.id.cardLokasiPengujian)?.setOnClickListener {
            startActivity(Intent(this, LokasiPengujianActivity::class.java))
        }

        // ❌ HAPUS toast blog1/blog2/blog3 lama, karena sekarang blog diisi dari firebase
    }

    private fun bindRecommendationViews(view: View) {
        cardBlog1 = view.findViewById(R.id.cardBlog1)
        cardBlog2 = view.findViewById(R.id.cardBlog2)
        cardBlog3 = view.findViewById(R.id.cardBlog3)

        ivBlog1 = view.findViewById(R.id.ivBlog1)
        ivBlog2 = view.findViewById(R.id.ivBlog2)
        ivBlog3 = view.findViewById(R.id.ivBlog3)

        tvBlogTitle1 = view.findViewById(R.id.tvBlogTitle1)
        tvBlogTitle2 = view.findViewById(R.id.tvBlogTitle2)
        tvBlogTitle3 = view.findViewById(R.id.tvBlogTitle3)

        tvBlogAuthor1 = view.findViewById(R.id.tvBlogAuthor1)
        tvBlogAuthor2 = view.findViewById(R.id.tvBlogAuthor2)
        tvBlogAuthor3 = view.findViewById(R.id.tvBlogAuthor3)
    }

    private fun setRecommendationLoadingState() {
        tvBlogTitle1.text = "Memuat..."
        tvBlogTitle2.text = "Memuat..."
        tvBlogTitle3.text = "Memuat..."
        tvBlogAuthor1.text = ""
        tvBlogAuthor2.text = ""
        tvBlogAuthor3.text = ""
    }

    private fun attachLatestBlogsListener() {
        if (latestListener != null) return

        latestListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HalamanUtama", "path=${snapshot.ref} count=${snapshot.childrenCount}")

                val list = mutableListOf<BlogItem>()

                for (child in snapshot.children) {
                    val id = child.key ?: continue

                    val title = child.child("judul").getValue(String::class.java)
                        ?: child.child("title").getValue(String::class.java)
                        ?: "-"

                    val author = child.child("penulis").getValue(String::class.java)
                        ?: child.child("author").getValue(String::class.java)
                        ?: child.child("nama").getValue(String::class.java)
                        ?: "-"

                    val content = child.child("isi").getValue(String::class.java)
                        ?: child.child("content").getValue(String::class.java)
                        ?: child.child("deskripsi").getValue(String::class.java)
                        ?: ""

                    val imageUrl = child.child("thumbnailUrl").getValue(String::class.java)
                        ?: child.child("imageUrl").getValue(String::class.java)
                        ?: child.child("gambar").getValue(String::class.java)
                        ?: child.child("thumbnail").getValue(String::class.java)

                    val createdAt = child.child("createdAt").getValue(Long::class.java)
                        ?: child.child("createdAt").getValue(String::class.java)?.toLongOrNull()
                        ?: 0L

                    list.add(BlogItem(id, title, author, content, imageUrl, createdAt))
                }

                val sorted = list.sortedWith(
                    compareByDescending<BlogItem> { it.createdAt }
                        .thenByDescending { it.id }
                )

                bindRecommendationCards(sorted.take(3))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HalamanUtama, "Gagal load blog: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("HalamanUtama", "load blog cancelled", error.toException())
            }
        }

        latestQuery.addValueEventListener(latestListener!!)
    }

    private fun bindRecommendationCards(latest: List<BlogItem>) {
        bindOneCard(latest.getOrNull(0), cardBlog1, ivBlog1, tvBlogTitle1, tvBlogAuthor1, R.drawable.traning)
        bindOneCard(latest.getOrNull(1), cardBlog2, ivBlog2, tvBlogTitle2, tvBlogAuthor2, R.drawable.gambar_sensor)
        bindOneCard(latest.getOrNull(2), cardBlog3, ivBlog3, tvBlogTitle3, tvBlogAuthor3, R.drawable.prediksi)
    }

    private fun bindOneCard(
        blog: BlogItem?,
        card: View,
        iv: ImageView,
        tvTitle: TextView,
        tvAuthor: TextView,
        placeholderRes: Int
    ) {
        if (blog == null) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE
        tvTitle.text = blog.title
        tvAuthor.text = blog.author

        val url = blog.imageUrl?.trim().takeIf { !it.isNullOrEmpty() }

        Glide.with(this)
            .load(url)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .into(iv)

        card.setOnClickListener {
            startActivity(Intent(this, BlogDetailActivity::class.java).putExtra("BLOG_ID", blog.id))
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { /* sudah di beranda */ }
            R.id.nav_prediksi -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_dataset -> startActivity(Intent(this, DatasetSensorActivity::class.java))
            R.id.nav_lokasi -> startActivity(Intent(this, LokasiPengujianActivity::class.java))

            // ✅ BLOG MENU sekarang buka BlogListUserActivity (bukan toast)
            R.id.nav_blog -> startActivity(Intent(this, BlogListUserActivity::class.java))

            R.id.nav_login -> startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.drawerlayout.closeDrawers()
        return true
    }
}
