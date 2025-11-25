package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("id") val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_price") val productPrice: Double?,
    @SerializedName("product_images") val productImages: List<XanoImage>?,
    @SerializedName("product") val productObj: Producto? = null
)