package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.databinding.ItemImageSliderBinding
import com.example.bettersneaker.models.XanoImage

class ImageSliderAdapter(private val images: List<XanoImage>) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemImageSliderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = images[position]
        Glide.with(holder.binding.imgSlider.context)
            .load(item.url)
            .into(holder.binding.imgSlider)
    }

    override fun getItemCount(): Int = images.size
}