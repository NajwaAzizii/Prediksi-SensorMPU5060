package com.example.najwa_belajarnavigationdrawer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
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
        blogsRef.orderByChild("createdAt")
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

    // ====== ViewPager2 ======
    private var blogViewPager: ViewPager2? = null
    private lateinit var blogPageAdapter: BlogPageAdapter
    private var indicator1: View? = null
    private var indicator2: View? = null
    private var indicator3: View? = null

    // ====== Search Dialog ======
    private var searchDialog: Dialog? = null
    private var allBlogsList = listOf<BlogItem>()

    // ====== OLD VIEWS (for compatibility) ======
    private var cardBlog1: CardView? = null
    private var cardBlog2: CardView? = null
    private var cardBlog3: CardView? = null

    private var ivBlog1: ImageView? = null
    private var ivBlog2: ImageView? = null
    private var ivBlog3: ImageView? = null

    private var tvBlogTitle1: TextView? = null
    private var tvBlogTitle2: TextView? = null
    private var tvBlogTitle3: TextView? = null

    private var tvBlogAuthor1: TextView? = null
    private var tvBlogAuthor2: TextView? = null
    private var tvBlogAuthor3: TextView? = null

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

        // ===== CLICK LISTENER CARD FITUR =====
        setupStaticCardClickListeners(homeView)

        // ===== BLOG ViewPager2 Setup =====
        setupBlogViewPager(homeView)

        // ===== OLD BLOG VIEWS (hidden, for compatibility) =====
        bindCompatibilityViews(homeView)

        // ===== Blog List Button =====
        homeView.findViewById<View?>(R.id.btnOpenBlogList)?.setOnClickListener {
            startActivity(Intent(this, BlogListUserActivity::class.java))
        }

        // ===== Search Blog Button - SHOW DIALOG! =====
        homeView.findViewById<View?>(R.id.btnSearchBlog)?.setOnClickListener {
            showSearchDialog()
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
        searchDialog?.dismiss()
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
    }

    private fun setupBlogViewPager(view: View) {
        blogViewPager = view.findViewById(R.id.blogViewPager)
        indicator1 = view.findViewById(R.id.indicator1)
        indicator2 = view.findViewById(R.id.indicator2)
        indicator3 = view.findViewById(R.id.indicator3)

        if (blogViewPager == null) {
            Log.w("HalamanUtama", "blogViewPager not found")
            return
        }

        blogPageAdapter = BlogPageAdapter { blogId ->
            startActivity(Intent(this, BlogDetailActivity::class.java).putExtra("BLOG_ID", blogId))
        }

        blogViewPager?.adapter = blogPageAdapter
        blogViewPager?.offscreenPageLimit = 1

        // Page change callback for indicators
        blogViewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })
    }

    private fun updateIndicators(position: Int) {
        if (indicator1 == null || indicator2 == null || indicator3 == null) return

        val totalPages = blogPageAdapter.itemCount
        if (totalPages == 0) return

        val activeDrawable = R.drawable.blog_indicator_active
        val inactiveDrawable = R.drawable.blog_indicator_inactive

        indicator1?.setBackgroundResource(if (position == 0) activeDrawable else inactiveDrawable)
        indicator2?.setBackgroundResource(if (position == 1) activeDrawable else inactiveDrawable)
        indicator3?.setBackgroundResource(if (position == 2) activeDrawable else inactiveDrawable)

        // Update visibility based on total pages
        indicator1?.visibility = if (totalPages > 0) View.VISIBLE else View.GONE
        indicator2?.visibility = if (totalPages > 1) View.VISIBLE else View.GONE
        indicator3?.visibility = if (totalPages > 2) View.VISIBLE else View.GONE
    }

    private fun bindCompatibilityViews(view: View) {
        try {
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
        } catch (e: Exception) {
            Log.w("HalamanUtama", "Compatibility views setup", e)
        }
    }

    private fun showSearchDialog() {
        searchDialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_blog, null)
        searchDialog?.setContentView(dialogView)

        // Setup dialog window
        searchDialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.TOP)
            setBackgroundDrawableResource(android.R.color.transparent)
            attributes?.y = 100 // Position from top
        }

        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchBlog)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseSearch)

        btnClose.setOnClickListener {
            searchDialog?.dismiss()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.length >= 2) {
                    performSearch(query)
                }
            }
        })

        // Auto focus and show keyboard
        etSearch.requestFocus()
        searchDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        searchDialog?.show()
    }

    private fun performSearch(query: String) {
        val filtered = allBlogsList.filter { blog ->
            blog.title.lowercase().contains(query) ||
                    blog.author.lowercase().contains(query) ||
                    blog.content.lowercase().contains(query)
        }

        if (filtered.isNotEmpty()) {
            // Navigate to BlogListUserActivity with search results
            searchDialog?.dismiss()
            val intent = Intent(this, BlogListUserActivity::class.java)
            intent.putExtra("SEARCH_QUERY", query)
            startActivity(intent)
        }
    }

    private fun attachLatestBlogsListener() {
        if (latestListener != null) return

        latestListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HalamanUtama", "Firebase data: count=${snapshot.childrenCount}")

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

                allBlogsList = sorted
                Log.d("HalamanUtama", "Total blogs: ${sorted.size}")
                updateBlogViewPager(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HalamanUtama, "Gagal load blog: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("HalamanUtama", "Firebase error", error.toException())
            }
        }

        latestQuery.addValueEventListener(latestListener!!)
    }

    private fun updateBlogViewPager(allBlogs: List<BlogItem>) {
        val placeholders = listOf(
            R.drawable.traning,
            R.drawable.gambar_sensor,
            R.drawable.prediksi,
            R.drawable.logo_mpu6050,
            R.drawable.sensor,
            R.drawable.peta
        )

        val adapterItems = allBlogs.mapIndexed { index, blog ->
            BlogPageAdapter.BlogItem(
                id = blog.id,
                title = blog.title,
                author = blog.author,
                imageUrl = blog.imageUrl,
                placeholderRes = placeholders.getOrNull(index % placeholders.size) ?: R.drawable.logo_mpu6050
            )
        }

        Log.d("HalamanUtama", "Updating ViewPager with ${adapterItems.size} blogs (${(adapterItems.size + 2) / 3} pages)")
        blogPageAdapter.submitList(adapterItems)

        if (adapterItems.isNotEmpty()) {
            updateIndicators(0)
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { /* sudah di beranda */ }
            R.id.nav_prediksi -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_dataset -> startActivity(Intent(this, DatasetSensorActivity::class.java))
            R.id.nav_lokasi -> startActivity(Intent(this, LokasiPengujianActivity::class.java))
            R.id.nav_blog -> startActivity(Intent(this, BlogListUserActivity::class.java))
            R.id.nav_login -> startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.drawerlayout.closeDrawers()
        return true
    }
}