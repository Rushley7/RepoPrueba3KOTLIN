package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("blocked") val blocked: Boolean,
    @SerializedName("role") val role: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("direccion") val direccion: String? = null
) : Serializable