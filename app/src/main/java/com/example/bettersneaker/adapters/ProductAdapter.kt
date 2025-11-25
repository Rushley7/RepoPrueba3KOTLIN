package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.databinding.ItemProductBinding
import com.example.bettersneaker.models.Producto

class ProductAdapter(
    private var items: List<Producto>,
    private val listener: OnProductClickListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    interface OnProductClickListener {
        fun onAgregar(producto: Producto)
        fun onItemClick(producto: Producto)
    }

    fun setData(newItems: List<Producto>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val item = items[bindingAdapterPosition]
                listener.onItemClick(item)
            }
            binding.imgProduct.setOnClickListener {
                val item = items[bindingAdapterPosition]
                listener.onItemClick(item)
            }
            binding.btnAdd.setOnClickListener {
                val item = items[bindingAdapterPosition]
                listener.onAgregar(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtName.text = item.nombre
        holder.binding.txtPrice.text = "$${String.format("%.2f", item.precio)}"
        val url = item.imagenes?.firstOrNull()
        if (url != null) {
            Glide.with(holder.binding.imgProduct).load(url).into(holder.binding.imgProduct)
        } else {
            holder.binding.imgProduct.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    override fun getItemCount(): Int = items.size
}