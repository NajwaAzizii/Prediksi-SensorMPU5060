package com.example.najwa_belajarnavigationdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BlogViewPagerAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BlogViewPagerAdapter.BlogViewHolder>() {

    data class BlogItem(
        val id: String,
        val title: String,
        val author: String,
        val imageUrl: String?,
        val placeholderRes: Int
    )

    private val items = mutableListOf<BlogItem>()

    fun submitList(newItems: List<BlogItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blog_viewpager, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBlog: ImageView = itemView.findViewById(R.id.ivBlogItem)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBlogItemTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvBlogItemAuthor)

        fun bind(item: BlogItem) {
            tvTitle.text = item.title
            tvAuthor.text = item.author

            val url = item.imageUrl?.trim().takeIf { !it.isNullOrEmpty() }

            Glide.with(itemView.context)
                .load(url)
                .placeholder(item.placeholderRes)
                .error(item.placeholderRes)
                .centerCrop()
                .into(ivBlog)

            itemView.setOnClickListener {
                onItemClick(item.id)
            }
        }
    }
}
