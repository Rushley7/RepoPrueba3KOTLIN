package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class UpdateProductRequest(
    @SerializedName("name") val nombre: String?,
    @SerializedName("description") val descripcion: String?,
    @SerializedName("price") val precio: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("brand") val marca: String?,
    @SerializedName("category") val categoria: String?,
    @SerializedName("active") val activo: Boolean?,
    @SerializedName("images") val imagenes: List<XanoImage>? = null
)

data class AddCartItemRequest(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("cantidad") val cantidad: Int
)

data class UpdateCartItemRequest(
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("cantidad") val cantidad: Int
)

data class DeleteCartItemRequest(
    @SerializedName("item_id") val itemId: Int
)

data class UpdateOrderStatusRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("new_status") val newStatus: String
)

data class UpdateUserStatusRequest(
    val estado: String
)

data class CreateUserRequest(
    @SerializedName("name") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UpdateUserRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("email") val email: String
)

data class BlockUserRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("blocked") val blocked: Boolean
)

data class OrderActionRequest(
    @SerializedName("id") val id: Int
)