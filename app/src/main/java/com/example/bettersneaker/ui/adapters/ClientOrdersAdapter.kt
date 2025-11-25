package com.example.bettersneaker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bettersneaker.R
import com.example.bettersneaker.databinding.ItemClientOrderBinding
import com.example.bettersneaker.databinding.ItemOrderBinding
import com.example.bettersneaker.models.OrderItem
import com.example.bettersneaker.models.Orden
import java.util.Locale

class ClientOrdersAdapter(
    private var items: List<Orden>
) : RecyclerView.Adapter<ClientOrdersAdapter.ViewHolder>() {

    private val expandedIds = mutableSetOf<Int>()

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
        val order = items[position]
        val isExpanded = expandedIds.contains(order.id)
        val context = holder.binding.root.context

        holder.binding.txtOrderId.text = "Orden #${order.id}"
        holder.binding.txtOrderTotal.text = "Total: $${String.format(Locale.US, "%.2f", order.total)}"

        // FIX ESTADO NULL: Asumimos "pagado" si es nulo, y limpiamos el texto
        val estadoSafe = order.estado?.takeIf { it.isNotBlank() } ?: "pagado"
        holder.binding.txtOrderStatus.text = "Estado: ${estadoSafe.uppercase()}"

        val colorResId = when (estadoSafe.lowercase(Locale.US)) {
            "pagado", "aceptado", "enviado", "entregado" -> android.R.color.holo_green_dark
            "rechazado", "cancelado" -> android.R.color.holo_red_dark
            else -> android.R.color.black
        }
        holder.binding.txtOrderStatus.setTextColor(ContextCompat.getColor(context, colorResId))

        // FIX CRASH: Aseguramos que la lista de items nunca sea nula
        holder.binding.recyclerOrderItems.layoutManager = LinearLayoutManager(context)
        holder.binding.recyclerOrderItems.adapter = ClientOrderItemAdapter(order.items ?: emptyList())

        // Lógica de expansión
        holder.binding.recyclerOrderItems.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.root.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                if (isExpanded) {
                    expandedIds.remove(order.id)
                } else {
                    expandedIds.add(order.id)
                }
                notifyItemChanged(pos)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ClientOrderItemAdapter(
        private var items: List<OrderItem>
    ) : RecyclerView.Adapter<ClientOrderItemAdapter.ItemViewHolder>() {

        inner class ItemViewHolder(val binding: ItemClientOrderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val b = ItemClientOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(b)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val it = items[position]
            val prod = it.product

            // Datos del Producto
            holder.binding.txtClientOrderItemName.text = prod?.nombre ?: "Producto Desconocido"

            // Calculamos precio unitario
            val unitPrice = prod?.precio ?: 0.0
            val priceFmt = String.format(Locale.US, "%.2f", unitPrice)

            // Desglose de Cantidad y Precio Unitario
            holder.binding.txtClientOrderItemDetails.text = "Cant: ${it.cantidad} | Precio Unitario: $$priceFmt"

            // Cargar Imagen
            val imageUrl = prod?.imagenes?.firstOrNull()?.url
            Glide.with(holder.binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.binding.imgOrderItem)
        }

        override fun getItemCount(): Int = items.size
    }
}