package com.example.najwa_belajarnavigationdrawer

import android.graphics.BitmapFactory
import android.util.Base64
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
        val binding = ItemBlogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val post = items[position]
        val b = holder.binding

        b.tvTitle.text = post.title.orEmpty()

        // ✅ bersihkan request glide sebelumnya (biar tidak nyangkut)
        Glide.with(b.imgThumb.context).clear(b.imgThumb)
        b.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)

        val b64 = post.thumbnailBase64.orEmpty().trim()
        val url = post.thumbnailUrl.orEmpty().trim()

        if (b64.isNotEmpty()) {
            // Base64 -> Bitmap
            try {
                val clean = b64
                    .removePrefix("data:image/jpeg;base64,")
                    .removePrefix("data:image/png;base64,")
                    .trim()

                val bytes = Base64.decode(clean, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                if (bmp != null) {
                    b.imgThumb.setImageBitmap(bmp)
                } else if (url.isNotEmpty()) {
                    Glide.with(b.imgThumb.context).load(url).centerCrop().into(b.imgThumb)
                }
            } catch (_: Exception) {
                if (url.isNotEmpty()) {
                    Glide.with(b.imgThumb.context).load(url).centerCrop().into(b.imgThumb)
                }
            }
        } else if (url.isNotEmpty()) {
            // fallback URL (kalau SSL nya aman)
            Glide.with(b.imgThumb.context)
                .load(url)
                .centerCrop()
                .into(b.imgThumb)
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
