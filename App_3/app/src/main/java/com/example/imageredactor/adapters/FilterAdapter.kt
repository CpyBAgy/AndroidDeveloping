package com.example.imageredactor.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.imageredactor.R

class FilterAdapter(
    private val originalBitmap: Bitmap,
    private val filters: List<FilterItem>,
    private val onFilterSelected: (FilterItem) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    data class FilterItem(
        val name: String,
        val filterApplier: (Bitmap) -> Bitmap
    )

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val filterPreview: ImageView = itemView.findViewById(R.id.filter_preview)
        val filterName: TextView = itemView.findViewById(R.id.filter_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_item, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filterItem = filters[position]

        val filterPreview = filterItem.filterApplier(originalBitmap)
        holder.filterPreview.setImageBitmap(filterPreview)
        holder.filterName.text = filterItem.name

        holder.itemView.setOnClickListener {
            onFilterSelected(filterItem)
        }
    }

    override fun getItemCount(): Int = filters.size
}