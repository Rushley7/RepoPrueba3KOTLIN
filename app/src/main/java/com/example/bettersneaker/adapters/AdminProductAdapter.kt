package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.R
import com.example.bettersneaker.databinding.ItemAdminProductBinding
import com.example.bettersneaker.models.Producto
import android.content.Intent
import com.example.bettersneaker.ui.product.ProductDetailActivity

class AdminProductAdapter(
    private var products: List<Producto>,
    private val actions: ProductActions
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    interface ProductActions {
        fun onEdit(producto: Producto)
        fun onDelete(producto: Producto)
    }

    fun setData(newProducts: List<Producto>) {
        this.products = newProducts
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAdminProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnEdit.setOnClickListener {
                val item = products[bindingAdapterPosition]
                actions.onEdit(item)
            }
            binding.btnDelete.setOnClickListener {
                val item = products[bindingAdapterPosition]
                actions.onDelete(item)
            }
            binding.root.setOnClickListener {
                val item = products[bindingAdapterPosition]
                val ctx = binding.root.context
                val i = Intent(ctx, ProductDetailActivity::class.java)
                i.putExtra("producto", item)
                ctx.startActivity(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = products[position]
        holder.binding.txtAdminProdName.text = item.nombre
        holder.binding.txtAdminProdPrice.text = "$${String.format("%.2f", item.precio)}"
        holder.binding.txtAdminProdStock.text = "Stock: ${item.stock ?: 0}"

        val imageUrl = item.imagenes?.firstOrNull()?.url
        Glide.with(holder.binding.imgAdminProd.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.binding.imgAdminProd)
    }

    override fun getItemCount(): Int = products.size
}