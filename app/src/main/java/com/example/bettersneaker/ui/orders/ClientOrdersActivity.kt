package com.example.bettersneaker.ui.orders

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bettersneaker.adapters.OrdersAdapter
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityClientOrdersBinding
import com.example.bettersneaker.models.Orden
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientOrdersBinding
    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = OrdersAdapter(emptyList())
        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        RetrofitClient.storeService(this).getOrdenes().enqueue(object : Callback<List<Orden>> {
            override fun onResponse(call: Call<List<Orden>>, response: Response<List<Orden>>) {
                if (response.isSuccessful) {
                    adapter.setData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@ClientOrdersActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Orden>>, t: Throwable) {
                Toast.makeText(this@ClientOrdersActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}