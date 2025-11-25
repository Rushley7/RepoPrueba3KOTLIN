package com.example.bettersneaker.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.example.bettersneaker.adapters.AdminOrderAdapter
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.databinding.ActivityAdminOrderListBinding
import com.example.bettersneaker.models.Order
import com.example.bettersneaker.models.OrderActionRequest
import com.example.bettersneaker.models.UpdateOrderStatusRequest
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminOrderListActivity : AppCompatActivity(), AdminOrderAdapter.OnOrderActionListener {
    private lateinit var binding: ActivityAdminOrderListBinding
    private lateinit var adapter: AdminOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdminOrderAdapter(emptyList(), this)
        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = adapter
        binding.btnBackOrders.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadOrders() {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        api.getOrders().enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val type = object : TypeToken<List<Order>>() {}.type
                    val body = response.body()!!
                    var list: List<Order> = emptyList()
                    try {
                        list = if (body.isJsonObject && body.asJsonObject.has("items")) {
                            gson.fromJson(body.asJsonObject.get("items"), type)
                        } else if (body.isJsonArray) {
                            gson.fromJson(body, type)
                        } else emptyList()
                        adapter.setData(list)
                        if (list.isEmpty()) {
                            binding.recyclerOrders.visibility = View.GONE
                            binding.txtEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.recyclerOrders.visibility = View.VISIBLE
                            binding.txtEmptyState.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminOrderListActivity, "Error datos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AdminOrderListActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(this@AdminOrderListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onAccept(order: Order) {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        val request = OrderActionRequest(id = order.id)
        api.acceptOrder(request).enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminOrderListActivity, "Orden Aceptada", Toast.LENGTH_SHORT).show()
                    loadOrders()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("API_ERROR", "Fallo al aceptar: $errorMsg")
                    Toast.makeText(this@AdminOrderListActivity, "Error 400: Verifica Logcat", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(this@AdminOrderListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onReject(order: Order) {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        val request = OrderActionRequest(id = order.id)
        api.rejectOrder(request).enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminOrderListActivity, "Pedido Rechazado", Toast.LENGTH_SHORT).show()
                    loadOrders()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("API_ERROR", "Fallo al rechazar: $errorMsg")
                    Toast.makeText(this@AdminOrderListActivity, "Error 400: Verifica Logcat", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(this@AdminOrderListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEditStatus(order: Order) {
        val opciones = arrayOf("pendiente", "pagado", "aceptado", "enviado", "entregado", "rechazado")
        AlertDialog.Builder(this)
            .setTitle("Editar Estado")
            .setItems(opciones) { _, which ->
                val status = opciones[which]
                val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
                val req = UpdateOrderStatusRequest(id = order.id, newStatus = status)
                api.updateOrderStatus(req).enqueue(object : Callback<JsonElement> {
                    override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminOrderListActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()
                            loadOrders()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                            Log.e("API_ERROR", "Fallo al actualizar estado: $errorMsg")
                            Toast.makeText(this@AdminOrderListActivity, "Error 400: Verifica Logcat", Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                        Toast.makeText(this@AdminOrderListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .show()
    }
}