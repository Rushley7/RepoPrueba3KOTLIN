package com.example.bettersneaker.api

import android.content.Context
import com.example.bettersneaker.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val AUTH_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:-8TYv8kL/"
    private const val STORE_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:IXVnIq8T/"

    fun getAuthClient(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp(context))
            .build()
    }

    fun getStoreClient(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(STORE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp(context))
            .build()
    }

    fun authService(context: Context): AuthApiService {
        return getAuthClient(context).create(AuthApiService::class.java)
    }

    fun storeService(context: Context): StoreApiService {
        return getStoreClient(context).create(StoreApiService::class.java)
    }

    private fun okHttp(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = SessionManager(context).getToken()
                val builder = original.newBuilder()
                if (!token.isNullOrEmpty()) {
                    builder.header("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .build()
    }
}