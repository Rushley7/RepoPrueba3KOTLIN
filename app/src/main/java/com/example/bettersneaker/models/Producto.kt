package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Producto(
    @SerializedName("id") val id: Int,
    @SerializedName("created_at") val created_at: Long?,
    @SerializedName("name") val nombre: String,
    @SerializedName("price") val precio: Double,
    @SerializedName("description") val descripcion: String?,
    @SerializedName("images") val imagenes: List<XanoImage>?,
    @SerializedName("active") val activo: Boolean?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("category") val categoria: String?,
    @SerializedName("brand") val marca: String?
) : Serializable