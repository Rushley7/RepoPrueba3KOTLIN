package com.example.bettersneaker.api

import com.example.bettersneaker.models.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface StoreApiService {
    // Admin
    @GET("product")
    fun adminListarProductos(): Call<List<Producto>>

    @POST("admin/product")
    fun adminCrearProductoJson(@Body body: UpdateProductRequest): Call<Producto>

    @Multipart
    @POST("upload/images")
    fun subirImagen(@Part imagen: MultipartBody.Part): Call<Map<String, Any>>

    @PATCH("productos_update")
    fun actualizarProductoGeneral(@Body body: UpdateProductRequest): Call<Producto>

    @DELETE("admin/productos")
    fun adminBorrarProducto(@Query("product_id") productId: Int): Call<Producto>

    @PATCH("admin/ordenes/aceptar")
    fun adminAceptarOrden(@Body body: Map<String, Any>): Call<Orden>

    @PATCH("admin/ordenes/rechazar")
    fun adminRechazarOrden(@Body body: Map<String, Any>): Call<Orden>

    @POST("admin/ordenes/envio")
    fun adminEnviarOrden(@Body body: Map<String, Any>): Call<Orden>

    @PATCH("admin/ordenes/update_status")
    fun adminActualizarEstado(@Body body: Map<String, Any>): Call<Orden>

    // Cliente - Productos
    @GET("product")
    fun getProductCatalog(): Call<List<Producto>>

    @GET("product/{product_id}")
    fun getProductDetail(@Path("product_id") id: Int): Call<Producto>

    // Cliente - Carrito
    @GET("carrito")
    fun getCarrito(): Call<Carrito>

    @POST("carrito/items")
    fun agregarItem(@Body body: AddCartItemRequest): Call<Carrito>

    @DELETE("delete_cart_item")
    fun deleteCartItemByQuery(@Query("item_id") itemId: Int): Call<Carrito>

    @POST("carrito/calcular")
    fun calcularCarrito(): Call<Carrito>

    @POST("checkout")
    fun checkout(): Call<Orden>

    // Cliente - Ordenes
    @GET("ordenes")
    fun getOrdenes(): Call<List<Orden>>

    @POST("ordenes_pagar")
    fun pagarOrdenBody(@Body body: Map<String, Any>): Call<Orden>

    @POST("ordenes/solicitar_envio")
    fun solicitarEnvio(@Body body: Map<String, Any>): Call<Orden>

    @GET("ordenes")
    fun adminGetOrdenes(): Call<List<Orden>>

    @GET("users")
    fun adminListarUsuarios(): Call<List<User>>

    @PATCH("user/{id}")
    fun adminUpdateUserStatus(
        @Path("id") id: Int,
        @Body body: UpdateUserStatusRequest
    ): Call<User>
}