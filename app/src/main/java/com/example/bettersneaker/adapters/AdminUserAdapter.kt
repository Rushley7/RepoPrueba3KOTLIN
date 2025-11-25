package com.example.bettersneaker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bettersneaker.R
import com.example.bettersneaker.databinding.ItemAdminUserBinding
import com.example.bettersneaker.models.User

class AdminUserAdapter(
    private var items: MutableList<User>,
    private val actions: OnUserAction
) : RecyclerView.Adapter<AdminUserAdapter.ViewHolder>() {

    interface OnUserAction {
        fun onEdit(user: User)
        fun onBlockToggle(user: User)
    }

    fun setData(newItems: List<User>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun updateItem(user: User, position: Int) {
        items[position] = user
        notifyItemChanged(position)
    }

    inner class ViewHolder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnEdit.setOnClickListener {
                val item = items[bindingAdapterPosition]
                actions.onEdit(item)
            }
            binding.btnBlock.setOnClickListener {
                val item = items[bindingAdapterPosition]
                actions.onBlockToggle(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtUserName.text = item.nombre
        holder.binding.txtUserEmail.text = item.email
        val ctx = holder.binding.btnBlock.context
        if (item.blocked) {
            holder.binding.btnBlock.text = "Desbloquear"
            holder.binding.btnBlock.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.holo_red_dark))
        } else {
            holder.binding.btnBlock.text = "Bloquear"
            holder.binding.btnBlock.setBackgroundColor(ContextCompat.getColor(ctx, R.color.black))
        }
    }

    override fun getItemCount(): Int = items.size
}