package com.example.bettersneaker.data.api

import com.google.gson.JsonElement
import com.example.bettersneaker.models.User
import com.example.bettersneaker.models.Producto
import com.example.bettersneaker.models.UpdateProductRequest
import com.example.bettersneaker.models.CreateUserRequest
import com.example.bettersneaker.models.UpdateUserRequest
import com.example.bettersneaker.models.BlockUserRequest
import com.example.bettersneaker.models.OrderActionRequest
import com.example.bettersneaker.models.UpdateOrderStatusRequest
import com.example.bettersneaker.models.XanoImage
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    @GET("product")
    fun getProducts(
        @Query("q") q: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("marca") marca: String? = null,
        @Query("precio_min") precioMin: Double? = null,
        @Query("precio_max") precioMax: Double? = null
    ): Call<JsonElement>

    @POST("admin/product")
    fun createProduct(@retrofit2.http.Body request: UpdateProductRequest): Call<Producto>

    @POST("product")
    fun createProductPublic(@retrofit2.http.Body request: UpdateProductRequest): Call<Producto>

    @PATCH("product/{product_id}")
    fun updateProduct(@Path("product_id") id: Int, @retrofit2.http.Body request: UpdateProductRequest): Call<Producto>

    @PATCH("admin/product/{product_id}")
    fun updateProductAdmin(@Path("product_id") id: Int, @retrofit2.http.Body request: UpdateProductRequest): Call<Producto>

    @DELETE("product/{product_id}")
    fun deleteProduct(@Path("product_id") id: Int): Call<Producto>

    @GET("users")
    fun getUsers(@Query("q") query: String?): Call<JsonElement>

    @POST("users")
    fun createUser(@retrofit2.http.Body request: CreateUserRequest): Call<JsonElement>

    @PATCH("details")
    fun updateUser(@retrofit2.http.Body request: UpdateUserRequest): Call<JsonElement>

    @PATCH("users_update_blocked")
    fun toggleBlockUser(@retrofit2.http.Body request: BlockUserRequest): Call<JsonElement>

    @GET("ordenes")
    fun getOrders(): Call<JsonElement>

    @PATCH("admin/ordenes/aceptar")
    fun acceptOrder(@retrofit2.http.Body request: OrderActionRequest): Call<JsonElement>

    @PATCH("admin/ordenes/rechazar")
    fun rejectOrder(@retrofit2.http.Body request: OrderActionRequest): Call<JsonElement>

    @POST("admin/ordenes/envio")
    fun sendOrder(@retrofit2.http.Body request: OrderActionRequest): Call<JsonElement>

    @PATCH("admin/ordenes/update_status")
    fun updateOrderStatus(@retrofit2.http.Body request: UpdateOrderStatusRequest): Call<JsonElement>

    @POST("checkout")
    fun checkout(): Call<JsonElement>

    @GET("auth/me")
    fun getMe(): Call<User>

    @GET("ordenes")
    fun getMyOrders(@Query("user_id") userId: Int): Call<JsonElement>

    @Multipart
    @POST("upload/images")
    fun uploadFiles(@Part file: MultipartBody.Part): Call<XanoImage>
}
