package com.example.imageredactor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.imageredactor.R
import com.example.imageredactor.models.ImageModel

class GalleryAdapter(
    private val images: MutableList<ImageModel> = mutableListOf(),
    private val onImageSelected: (ImageModel) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]

        holder.imageView.load(image.uri) {
            crossfade(true)
            placeholder(R.drawable.image_placeholder)
            error(R.drawable.image_error)
        }

        holder.itemView.setOnClickListener {
            onImageSelected(image)
        }
    }

    override fun getItemCount(): Int = images.size

    fun addImages(newImages: List<ImageModel>) {
        val startPosition = images.size
        images.addAll(newImages)
        notifyItemRangeInserted(startPosition, newImages.size)
    }

    fun clearImages() {
        val size = images.size
        images.clear()
        notifyItemRangeRemoved(0, size)
    }
}