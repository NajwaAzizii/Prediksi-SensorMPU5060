package com.example.najwa_belajarnavigationdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BlogPageAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BlogPageAdapter.BlogPageViewHolder>() {

    data class BlogItem(
        val id: String,
        val title: String,
        val author: String,
        val imageUrl: String?,
        val placeholderRes: Int
    )

    private val pages = mutableListOf<List<BlogItem>>()

    fun submitList(allBlogs: List<BlogItem>) {
        pages.clear()
        // Group blogs into pages of 3
        pages.addAll(allBlogs.chunked(3))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogPageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_blog_page, parent, false)
        return BlogPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogPageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    inner class BlogPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardBlog1: CardView = itemView.findViewById(R.id.cardBlog1)
        private val cardBlog2: CardView = itemView.findViewById(R.id.cardBlog2)
        private val cardBlog3: CardView = itemView.findViewById(R.id.cardBlog3)

        private val ivBlog1: ImageView = itemView.findViewById(R.id.ivBlog1)
        private val ivBlog2: ImageView = itemView.findViewById(R.id.ivBlog2)
        private val ivBlog3: ImageView = itemView.findViewById(R.id.ivBlog3)

        private val tvTitle1: TextView = itemView.findViewById(R.id.tvBlogTitle1)
        private val tvTitle2: TextView = itemView.findViewById(R.id.tvBlogTitle2)
        private val tvTitle3: TextView = itemView.findViewById(R.id.tvBlogTitle3)

        private val tvAuthor1: TextView = itemView.findViewById(R.id.tvBlogAuthor1)
        private val tvAuthor2: TextView = itemView.findViewById(R.id.tvBlogAuthor2)
        private val tvAuthor3: TextView = itemView.findViewById(R.id.tvBlogAuthor3)

        fun bind(blogsOnPage: List<BlogItem>) {
            // Bind blog 1
            bindBlog(blogsOnPage.getOrNull(0), cardBlog1, ivBlog1, tvTitle1, tvAuthor1)
            // Bind blog 2
            bindBlog(blogsOnPage.getOrNull(1), cardBlog2, ivBlog2, tvTitle2, tvAuthor2)
            // Bind blog 3
            bindBlog(blogsOnPage.getOrNull(2), cardBlog3, ivBlog3, tvTitle3, tvAuthor3)
        }

        private fun bindBlog(
            blog: BlogItem?,
            card: CardView,
            iv: ImageView,
            tvTitle: TextView,
            tvAuthor: TextView
        ) {
            if (blog == null) {
                card.visibility = View.GONE
                return
            }

            card.visibility = View.VISIBLE
            tvTitle.text = blog.title
            tvAuthor.text = blog.author

            val url = blog.imageUrl?.trim().takeIf { !it.isNullOrEmpty() }

            Glide.with(itemView.context)
                .load(url)
                .placeholder(blog.placeholderRes)
                .error(blog.placeholderRes)
                .centerCrop()
                .into(iv)

            card.setOnClickListener {
                onItemClick(blog.id)
            }
        }
    }
}
