package com.example.najwa_belajarnavigationdrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.najwa_belajarnavigationdrawer.databinding.ItemBlogBinding

class BlogAdapter(
    private val items: MutableList<BlogPost>,
    private val onEdit: (BlogPost) -> Unit,
    private val onDelete: (BlogPost) -> Unit,
    private val onViewMore: (BlogPost) -> Unit
) : RecyclerView.Adapter<BlogAdapter.VH>() {

    inner class VH(val binding: ItemBlogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBlogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val post = items[position]
        val b = holder.binding

        b.tvTitle.text = post.title.orEmpty()


        // thumb
        val url = post.thumbnailUrl.orEmpty()
        if (url.isNotBlank()) {
            Glide.with(b.imgThumb.context)
                .load(url)
                .centerCrop()
                .into(b.imgThumb)
        } else {
            b.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        b.btnEdit.setOnClickListener { onEdit(post) }
        b.btnDelete.setOnClickListener { onDelete(post) }
        b.tvViewMore.setOnClickListener { onViewMore(post) }

    }

    fun submit(newItems: List<BlogPost>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
