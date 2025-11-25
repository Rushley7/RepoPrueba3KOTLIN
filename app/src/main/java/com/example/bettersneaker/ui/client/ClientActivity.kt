package com.example.bettersneaker.ui.client

import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bettersneaker.R
import com.example.bettersneaker.adapters.ClientProductAdapter
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityClientBinding
import com.example.bettersneaker.models.Producto
import com.example.bettersneaker.models.Orden
import com.example.bettersneaker.ui.cart.CartActivity
import com.example.bettersneaker.ui.product.ProductDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientActivity : AppCompatActivity(), ClientProductAdapter.OnClientProductAction {
    private lateinit var binding: ActivityClientBinding
    private lateinit var adapter: ClientProductAdapter
    private var allProducts: List<Producto> = emptyList()
    private var isProcessing: Boolean = false // Bloqueo de spam

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = binding.toolbarClient
        toolbar.setNavigationOnClickListener { finish() }

        adapter = ClientProductAdapter(emptyList(), this)
        binding.recyclerProducts.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerProducts.adapter = adapter

        // Navegación Bottom Bar
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Ya estamos aquí
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, ClientOrdersActivity::class.java)) // Conexión Pedidos
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)) // Conexión Perfil
                    true
                }
                else -> false
            }
        }

        // El botón btnGoCart ahora está en el menú, pero por si acaso, lo dejo conectado:
        binding.btnGoCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.editSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = allProducts.filter { (it.nombre ?: "").lowercase().contains(q) }
                adapter.setData(filtered)
            }
        })

        cargarProductos()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun cargarProductos() {
        RetrofitClient.storeService(this).getProductCatalog().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    allProducts = list.filter { it.activo != false }
                    adapter.setData(allProducts)
                } else {
                    Toast.makeText(this@ClientActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                Toast.makeText(this@ClientActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun realizarCheckout() {
        if (isProcessing) return
        isProcessing = true
        RetrofitClient.storeService(this).checkout().enqueue(object : Callback<Orden> {
            override fun onResponse(call: Call<Orden>, response: Response<Orden>) {
                isProcessing = false
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ClientActivity,
                        "¡PEDIDO REALIZADO! Su compra está en la sección 'Mis Pedidos'.",
                        Toast.LENGTH_LONG
                    ).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this@ClientActivity, "Error en checkout: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Orden>, t: Throwable) {
                isProcessing = false
                Toast.makeText(this@ClientActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Lógica onAddToCart (asumiendo que tiene la protección isProcessing del prompt anterior)
    override fun onAddToCart(producto: Producto, quantity: Int) {
        if (isProcessing) return
        isProcessing = true

        RetrofitClient.storeService(this)
            .agregarItem(com.example.bettersneaker.models.AddCartItemRequest(producto.id, quantity))
            .enqueue(object : retrofit2.Callback<com.example.bettersneaker.models.Carrito> {
                override fun onResponse(
                    call: retrofit2.Call<com.example.bettersneaker.models.Carrito>,
                    response: retrofit2.Response<com.example.bettersneaker.models.Carrito>
                ) {
                    isProcessing = false
                    if (response.isSuccessful) {
                        Toast.makeText(this@ClientActivity, "Producto agregado", Toast.LENGTH_SHORT).show()
                        Log.d("CART_DEBUG", "Añadido con éxito")
                    } else {
                        Toast.makeText(this@ClientActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(
                    call: retrofit2.Call<com.example.bettersneaker.models.Carrito>,
                    t: Throwable
                ) {
                    isProcessing = false
                    Toast.makeText(this@ClientActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onItemClick(producto: Producto) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("producto", producto)
        startActivity(intent)
    }
}