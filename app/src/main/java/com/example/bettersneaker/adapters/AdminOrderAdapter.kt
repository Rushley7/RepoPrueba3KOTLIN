package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bettersneaker.R
import com.example.bettersneaker.databinding.ItemAdminOrderBinding
import com.example.bettersneaker.models.Order
import java.util.Locale
import androidx.core.content.ContextCompat

class AdminOrderAdapter(
    private var items: List<Order>,
    private val listener: OnOrderActionListener
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    interface OnOrderActionListener {
        fun onAccept(order: Order)
        fun onReject(order: Order)
        fun onEditStatus(order: Order)
    }

    fun setData(newItems: List<Order>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAdminOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnAccept.setOnClickListener { listener.onAccept(items[bindingAdapterPosition]) }
            binding.btnReject.setOnClickListener { listener.onReject(items[bindingAdapterPosition]) }
            binding.btnEditStatus.setOnClickListener { listener.onEditStatus(items[bindingAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtAdminOrderId.text = "Orden #${item.id}"
        holder.binding.txtAdminOrderDate.text = item.createdAt?.toString() ?: "-"
        holder.binding.txtAdminOrderTotal.text = "Total: $${String.format(Locale.US, "%.2f", item.total)}"

        holder.binding.txtAdminOrderStatus.text = "Estado: ${item.estado}"

        val isPending = item.estado.equals("pendiente", ignoreCase = true)
        val isRejected = item.estado.equals("rechazado", ignoreCase = true)

        holder.binding.btnEditStatus.visibility = View.VISIBLE

        if (isPending) {
            holder.binding.btnAccept.visibility = View.VISIBLE
            holder.binding.btnReject.visibility = View.VISIBLE
        } else {
            holder.binding.btnAccept.visibility = View.GONE
            holder.binding.btnReject.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}