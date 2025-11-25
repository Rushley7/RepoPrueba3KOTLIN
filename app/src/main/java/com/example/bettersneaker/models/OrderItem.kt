package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("product") val product: Producto?,
    @SerializedName("cantidad") val cantidad: Int,
    val subtotal: Double
)