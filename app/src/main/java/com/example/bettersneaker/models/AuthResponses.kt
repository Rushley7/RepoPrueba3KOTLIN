package com.example.bettersneaker.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val telefono: String?,
    val direccion: String?
)

data class LoginResponse(
    @SerializedName("authToken") val authToken: String,
    @SerializedName("user") val user: User
)

data class UpdateMeRequest(
    val nombre: String?,
    val telefono: String?,
    val direccion: String?
)