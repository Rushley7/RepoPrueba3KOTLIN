package com.example.bettersneaker.ui.client

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.databinding.ActivityClientOrdersBinding
import com.example.bettersneaker.models.User
import com.example.bettersneaker.ui.adapters.ClientOrdersAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.example.bettersneaker.models.Orden // Asegurarse de que este modelo exista
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientOrdersBinding
    private lateinit var adapter: ClientOrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarOrders
        toolbar.setNavigationOnClickListener { finish() }

        // Usamos el adaptador corregido
        adapter = ClientOrdersAdapter(emptyList())
        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = adapter

        cargarPedidos()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun cargarPedidos() {
        // Obtenemos el usuario autenticado (para asegurar que solo vea sus pedidos)
        RetrofitClient.authService(this).me().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        val api = RetrofitClient.getStoreClient(this@ClientOrdersActivity).create(ApiService::class.java)

                        // FIX: Llama a la API para obtener Órdenes del usuario
                        api.getMyOrders(user.id).enqueue(object : Callback<JsonElement> {
                            override fun onResponse(call: Call<JsonElement>, resp: Response<JsonElement>) {
                                if (resp.isSuccessful && resp.body() != null) {
                                    val json = resp.body()!!
                                    val gson = Gson()
                                    // Usamos el TypeToken para manejar la lista de Órdenes
                                    val listType = object : TypeToken<List<Orden>>() {}.type

                                    var lista: List<Orden> = emptyList()
                                    try {
                                        // Manejar estructura de Xano (puede ser { "items": [...] } o un array directo)
                                        lista = if (json.isJsonObject && json.asJsonObject.has("items")) {
                                            gson.fromJson(json.asJsonObject.get("items"), listType)
                                        } else if (json.isJsonArray) {
                                            gson.fromJson(json, listType)
                                        } else emptyList()

                                        adapter.setData(lista)
                                        if (lista.isEmpty()) Toast.makeText(this@ClientOrdersActivity, "No has realizado pedidos.", Toast.LENGTH_SHORT).show()

                                    } catch (e: Exception) {
                                        Toast.makeText(this@ClientOrdersActivity, "Error al parsear datos: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(this@ClientOrdersActivity, "Error al cargar órdenes: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                                Toast.makeText(this@ClientOrdersActivity, "Error de red al cargar pedidos", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(this@ClientOrdersActivity, "Error al obtener perfil: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ClientOrdersActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}