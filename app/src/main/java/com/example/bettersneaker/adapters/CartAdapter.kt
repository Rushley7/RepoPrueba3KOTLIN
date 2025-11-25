package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.R
import com.example.bettersneaker.databinding.ItemCartBinding
import com.example.bettersneaker.models.CartItem
import java.util.Locale

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val listener: OnCartInteractionListener
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    interface OnCartInteractionListener {
        fun onIncrease(item: CartItem)
        fun onDecrease(item: CartItem)
        fun onDelete(item: CartItem)
    }

    fun setData(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnIncrease.setOnClickListener { listener.onIncrease(items[bindingAdapterPosition]) }
            binding.btnDecrease.setOnClickListener { listener.onDecrease(items[bindingAdapterPosition]) }
            binding.btnDelete.setOnClickListener { listener.onDelete(items[bindingAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val nombre = item.productName ?: item.productObj?.nombre ?: "Producto"
        val precio = item.productPrice ?: item.productObj?.precio ?: 0.0

        holder.binding.txtCartName.text = nombre

        val precioFmt = String.format(Locale.US, "%.2f", precio)
        holder.binding.txtCartPrice.text = "$${precioFmt} x ${item.cantidad}"

        holder.binding.txtQuantity.text = item.cantidad.toString()

        val imageUrl = item.productImages?.firstOrNull()?.url
            ?: item.productObj?.imagenes?.firstOrNull()?.url
        Glide.with(holder.binding.root.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.binding.imgCartImage)
    }

    override fun getItemCount(): Int = items.size
}