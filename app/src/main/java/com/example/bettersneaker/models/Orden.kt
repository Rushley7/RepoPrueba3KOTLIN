package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class Orden(
    @SerializedName("id") val id: Int,
    @SerializedName("items") val items: List<OrderItem>?,
    @SerializedName("total") val total: Double,
    @SerializedName("estado") val estado: String
)