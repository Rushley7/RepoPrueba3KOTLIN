package com.example.bettersneaker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bettersneaker.databinding.ItemOrderBinding
import com.example.bettersneaker.models.Orden

class OrdersAdapter(
    private var items: List<Orden>
) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    fun setData(newItems: List<Orden>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtOrderId.text = "#${item.id}"
        holder.binding.txtOrderTotal.text = "$${String.format("%.2f", item.total)}"
        holder.binding.txtOrderStatus.text = item.estado
        val color = when (item.estado.lowercase()) {
            "pendiente" -> Color.YELLOW
            "pagado" -> Color.CYAN
            "enviado" -> Color.GREEN
            "rechazado" -> Color.RED
            else -> Color.LTGRAY
        }
        holder.binding.statusDot.setBackgroundColor(color)
    }

    override fun getItemCount(): Int = items.size
}