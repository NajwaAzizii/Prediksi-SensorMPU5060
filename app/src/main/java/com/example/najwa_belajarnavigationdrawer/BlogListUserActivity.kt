package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlogListUserActivity : AppCompatActivity() {

    private val DATABASE_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"
    private val BLOG_NODE = "blogs"

    private val dbRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance(DATABASE_URL).reference.child(BLOG_NODE)
    }

    private val query: Query by lazy {
        dbRef.orderByChild("createdAt")
    }

    private data class BlogItem(
        val id: String,
        val title: String,
        val author: String,
        val content: String,
        val thumbnailUrl: String?,
        val createdAt: Long
    )

    private lateinit var rv: RecyclerView
    private lateinit var emptyState: LinearLayout

    private lateinit var btnBack: View
    private lateinit var btnSearch: View
    private lateinit var cardSearch: View
    private lateinit var btnCloseSearch: View
    private lateinit var etSearch: TextView

    private val allBlogs = mutableListOf<BlogItem>()
    private val adapter = BlogUserAdapter(
        onOpenDetail = { blogId ->
            startActivity(Intent(this, BlogDetailActivity::class.java).putExtra("BLOG_ID", blogId))
        }
    )

    private var listener: ValueEventListener? = null

    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_list_user)

        rv = findViewById(R.id.rvBlogUser)
        emptyState = findViewById(R.id.emptyState)

        btnBack = findViewById(R.id.btnBack)
        btnSearch = findViewById(R.id.btnSearch)
        cardSearch = findViewById(R.id.cardSearch)
        btnCloseSearch = findViewById(R.id.btnCloseSearch)
        etSearch = findViewById(R.id.etSearch)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnSearch.setOnClickListener {
            cardSearch.visibility = View.VISIBLE
            etSearch.text = ""
            etSearch.requestFocus()
        }

        btnCloseSearch.setOnClickListener {
            etSearch.text = ""
            cardSearch.visibility = View.GONE
            renderList(allBlogs)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.isEmpty()) {
                    renderList(allBlogs)
                    return
                }
                val filtered = allBlogs.filter {
                    it.title.contains(q, true) ||
                            it.author.contains(q, true) ||
                            it.content.contains(q, true)
                }
                renderList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onStart() {
        super.onStart()
        attachListener()
    }

    override fun onStop() {
        super.onStop()
        listener?.let { query.removeEventListener(it) }
        listener = null
    }

    private fun attachListener() {
        if (listener != null) return

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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

                    val thumb = child.child("thumbnailUrl").getValue(String::class.java)
                        ?: child.child("thumbnail").getValue(String::class.java)
                        ?: child.child("imageUrl").getValue(String::class.java)
                        ?: child.child("gambar").getValue(String::class.java)

                    val createdAt = child.child("createdAt").getValue(Long::class.java)
                        ?: child.child("timestamp").getValue(Long::class.java)
                        ?: 0L

                    list.add(BlogItem(id, title, author, content, thumb, createdAt))
                }

                val sorted = list.sortedWith(
                    compareByDescending<BlogItem> { it.createdAt }
                        .thenByDescending { it.id }
                )

                allBlogs.clear()
                allBlogs.addAll(sorted)

                val q = etSearch.text?.toString()?.trim().orEmpty()
                if (cardSearch.visibility == View.VISIBLE && q.isNotEmpty()) {
                    val filtered = allBlogs.filter {
                        it.title.contains(q, true) ||
                                it.author.contains(q, true) ||
                                it.content.contains(q, true)
                    }
                    renderList(filtered)
                } else {
                    renderList(allBlogs)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                emptyState.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        }

        query.addValueEventListener(listener!!)
    }

    private fun renderList(list: List<BlogItem>) {
        if (list.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
        adapter.submit(list, dateFmt)
    }

    private class BlogUserAdapter(
        private val onOpenDetail: (String) -> Unit
    ) : RecyclerView.Adapter<BlogUserAdapter.VH>() {

        private val items = mutableListOf<BlogItem>()
        private var dateFmt: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        fun submit(newItems: List<BlogItem>, fmt: SimpleDateFormat) {
            dateFmt = fmt
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_blog_user, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position], dateFmt, onOpenDetail)
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardRoot: View = itemView.findViewById(R.id.cardRoot)
            private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            private val tvSnippet: TextView = itemView.findViewById(R.id.tvSnippet)
            private val tvViewMore: View = itemView.findViewById(R.id.tvViewMore)

            fun bind(item: BlogItem, dateFmt: SimpleDateFormat, onOpenDetail: (String) -> Unit) {
                tvTitle.text = item.title

                val dateText = if (item.createdAt > 0) {
                    dateFmt.format(Date(item.createdAt))
                } else {
                    "Tanggal tidak tersedia"
                }
                tvDate.text = dateText

                tvSnippet.text = item.content.trim().ifEmpty { "-" }

                val url = item.thumbnailUrl?.trim().takeIf { !it.isNullOrEmpty() }

                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imgCover)

                val click = View.OnClickListener { onOpenDetail(item.id) }
                tvViewMore.setOnClickListener(click)
                cardRoot.setOnClickListener(click)
            }
        }
    }
}