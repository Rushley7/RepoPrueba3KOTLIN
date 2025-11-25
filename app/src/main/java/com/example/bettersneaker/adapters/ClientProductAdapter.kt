package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.databinding.ItemClientProductBinding
import com.example.bettersneaker.models.Producto
import java.util.Locale

class ClientProductAdapter(
    private var items: List<Producto>,
    private val listener: OnClientProductAction
) : RecyclerView.Adapter<ClientProductAdapter.ViewHolder>() {

    interface OnClientProductAction {
        fun onAddToCart(producto: Producto, quantity: Int)
        fun onItemClick(producto: Producto)
    }

    fun setData(newItems: List<Producto>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemClientProductBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentQuantity: Int = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClientProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtClientProdName.text = item.nombre
        holder.binding.txtClientProdPrice.text = "$${String.format(Locale.US, "%.2f", item.precio)}"

        val imageUrl = item.imagenes?.firstOrNull()?.url
        Glide.with(holder.binding.imgClientProd.context)
            .load(imageUrl)
            .placeholder(com.example.bettersneaker.R.drawable.ic_launcher_foreground)
            .error(com.example.bettersneaker.R.drawable.ic_launcher_foreground)
            .into(holder.binding.imgClientProd)

        holder.currentQuantity = 1
        holder.binding.txtQuantity.text = holder.currentQuantity.toString()

        holder.binding.btnMinus.setOnClickListener {
            if (holder.currentQuantity > 1) {
                holder.currentQuantity -= 1
                holder.binding.txtQuantity.text = holder.currentQuantity.toString()
            }
        }

        holder.binding.btnPlus.setOnClickListener {
            holder.currentQuantity += 1
            holder.binding.txtQuantity.text = holder.currentQuantity.toString()
        }

        holder.binding.root.setOnClickListener { listener.onItemClick(item) }
        holder.binding.btnAddToCart.setOnClickListener { listener.onAddToCart(item, holder.currentQuantity) }
    }

    override fun getItemCount(): Int = items.size
}