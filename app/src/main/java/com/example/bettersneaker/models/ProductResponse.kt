package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("items") val items: List<Producto>,
    @SerializedName("nextPage") val nextPage: Any?
)