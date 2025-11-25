package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class XanoImage(
    @SerializedName("url") val url: String,
    @SerializedName("name") val name: String?,
    @SerializedName("mime") val mime: String?
) : java.io.Serializable