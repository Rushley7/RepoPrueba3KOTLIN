package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("total") val total: Double,
    @SerializedName("status") val estado: String,
    @SerializedName("created_at") val createdAt: Long?
)