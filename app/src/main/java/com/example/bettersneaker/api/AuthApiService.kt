package com.example.bettersneaker.api

import com.example.bettersneaker.models.LoginRequest
import com.example.bettersneaker.models.LoginResponse
import com.example.bettersneaker.models.SignupRequest
import com.example.bettersneaker.models.User
import com.example.bettersneaker.models.UpdateMeRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/signup")
    fun signup(@Body body: SignupRequest): Call<LoginResponse>

    @POST("auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @GET("auth/me")
    fun me(): Call<User>

    @PATCH("auth/me")
    fun updateMe(@Body body: UpdateMeRequest): Call<User>
}