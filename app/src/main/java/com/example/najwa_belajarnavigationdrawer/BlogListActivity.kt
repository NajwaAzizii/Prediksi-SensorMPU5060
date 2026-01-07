package com.example.najwa_belajarnavigationdrawer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityBlogListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogListBinding

    companion object {
        private const val DB_URL = "https://dbmpu5060-default-rtdb.firebaseio.com"
        private const val BLOG_NODE = "blogs"
        private const val ADMIN_EMAIL = "admin@gmail.com"
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val dbRef by lazy {
        FirebaseDatabase.getInstance(DB_URL).reference.child(BLOG_NODE)
    }

    private lateinit var adapter: BlogAdapter
    private var allBlogs: List<BlogPost> = emptyList()

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        val email = user?.email?.trim()?.lowercase()

        if (user == null || email == null || email != ADMIN_EMAIL.lowercase()) {
            auth.signOut()
            Toast.makeText(this, "Akses hanya untuk admin", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = BlogAdapter(
            items = mutableListOf(),
            onEdit = { post ->
                startActivity(Intent(this, BlogFormActivity::class.java).apply {
                    putExtra("BLOG_ID", post.id)
                })
            },
            onDelete = { post ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Blog?")
                    .setMessage("Yakin hapus '${post.title.orEmpty()}'?")
                    .setPositiveButton("Hapus") { _, _ -> deletePost(post) }
                    .setNegativeButton("Batal", null)
                    .show()
            },
            onViewMore = { post ->
                startActivity(Intent(this, BlogDetailActivity::class.java).apply {
                    putExtra("BLOG_ID", post.id)
                })
            }
        )

        binding.rvBlog.layoutManager = LinearLayoutManager(this)
        binding.rvBlog.adapter = adapter

        binding.btnTambah.setOnClickListener {
            startActivity(Intent(this, BlogFormActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        // Search
        binding.btnSearch.setOnClickListener { toggleSearch() }
        binding.btnCloseSearch.setOnClickListener { closeSearch() }

        binding.etSearch.addTextChangedListener { text ->
            applyFilter(text?.toString().orEmpty())
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else false
        }

        // Back: kalau search lagi terbuka, tutup dulu
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.cardSearchBar.visibility == View.VISIBLE) closeSearch()
                else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        listenBlogs()
    }

    private fun listenBlogs() {
        dbRef.orderByChild("createdAt").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BlogPost>()
                for (child in snapshot.children) {
                    val item = child.getValue(BlogPost::class.java)
                    if (item != null) {
                        item.id = child.key
                        list.add(item)
                    }
                }
                list.reverse()
                allBlogs = list

                val q = binding.etSearch.text?.toString().orEmpty()
                applyFilter(q)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BlogListActivity, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun applyFilter(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) {
            allBlogs
        } else {
            allBlogs.filter { post ->
                post.title.orEmpty().contains(q, true) ||
                        post.author.orEmpty().contains(q, true) ||
                        post.content.orEmpty().contains(q, true)
            }
        }
        adapter.submit(filtered)
    }

    private fun toggleSearch() {
        if (binding.cardSearchBar.visibility == View.VISIBLE) closeSearch() else openSearch()
    }

    private fun openSearch() {
        binding.cardSearchBar.visibility = View.VISIBLE
        binding.etSearch.setText("")
        binding.etSearch.requestFocus()
        showKeyboard()
    }

    private fun closeSearch() {
        binding.cardSearchBar.visibility = View.GONE
        binding.etSearch.setText("")
        applyFilter("")
        hideKeyboard()
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun deletePost(post: BlogPost) {
        val id = post.id ?: return
        dbRef.child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }
}
