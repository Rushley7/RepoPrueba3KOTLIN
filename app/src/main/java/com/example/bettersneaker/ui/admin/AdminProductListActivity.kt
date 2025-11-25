package com.example.bettersneaker.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.util.Log
import android.view.View
import android.widget.Toast
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bettersneaker.adapters.AdminProductAdapter
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityAdminProductListBinding
import com.example.bettersneaker.models.Producto
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminProductListActivity : AppCompatActivity(), AdminProductAdapter.ProductActions {
    private lateinit var binding: ActivityAdminProductListBinding
    private lateinit var adapter: AdminProductAdapter
    private var currentQuery: String? = null
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Inventario"

        adapter = AdminProductAdapter(emptyList(), this)
        binding.recyclerAdminProducts.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerAdminProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(this, AdminProductFormActivity::class.java))
        }

        binding.fabAddProduct.setOnLongClickListener {
            cargarDatosPrueba()
            true
        }

        binding.btnBackInventory.setOnClickListener {
            finish()
        }

        binding.editSearch.setOnEditorActionListener { v, actionId, event ->
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER
            val isSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            if (isEnter || isSearch) {
                currentQuery = binding.editSearch.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                cargarProductos()
                true
            } else false
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            currentCategory = when {
                checkedIds.contains(binding.chipHombre.id) -> "Hombre"
                checkedIds.contains(binding.chipMujer.id) -> "Mujer"
                else -> null
            }
            cargarProductos()
        }

        cargarProductos()
    }

    override fun onResume() {
        super.onResume()
        
    }

    private fun cargarProductos() {
        val token = SessionManager(this).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Sesión expirada. Inicia sesión para continuar.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        api.getProducts(currentQuery, currentCategory, null, null, null).enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.body() != null) {
                    val rawJson = response.body()?.toString() ?: "Cuerpo nulo"
                    Log.e("XANO_DEBUG", "JSON RECIBIDO: $rawJson")
                    val jsonElement = response.body()!!
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Producto>>() {}.type
                    var listaFinal: List<Producto> = emptyList()
                    try {
                        if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject
                            if (jsonObject.has("items")) {
                                val itemsArray = jsonObject.get("items")
                                listaFinal = gson.fromJson(itemsArray, itemType)
                            }
                        } else if (jsonElement.isJsonArray) {
                            listaFinal = gson.fromJson(jsonElement, itemType)
                        }
                        Log.d("API_DEBUG", "Productos procesados: ${listaFinal.size}")
                        adapter.setData(listaFinal)
                        if (listaFinal.isEmpty()) {
                            binding.txtEmptyState.visibility = View.VISIBLE
                            binding.recyclerAdminProducts.visibility = View.GONE
                            Toast.makeText(this@AdminProductListActivity, "Conexión exitosa, pero Xano devolvió 0 productos", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.txtEmptyState.visibility = View.GONE
                            binding.recyclerAdminProducts.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        Log.e("API_DEBUG", "Error parseando JSON manual: ${e.message}")
                        Toast.makeText(this@AdminProductListActivity, "Error datos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val err = response.errorBody()?.string()
                    Toast.makeText(this@AdminProductListActivity, "Error ${response.code()}: ${err ?: "Acceso Denegado (Verifica tu token)"}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.e("AdminProductList", "Fallo cargando productos", t)
                Toast.makeText(this@AdminProductListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                cargarDatosPrueba()
            }
        })
    }

    private fun cargarDatosPrueba() {
        val mockList = listOf(
            Producto(
                id = 1,
                created_at = null,
                nombre = "Zapatilla Test 1",
                precio = 50.0,
                descripcion = "Desc",
                imagenes = emptyList(),
                activo = true,
                stock = 10,
                categoria = "Urban",
                marca = "Nike"
            ),
            Producto(
                id = 2,
                created_at = null,
                nombre = "Zapatilla Test 2",
                precio = 80.0,
                descripcion = "Desc",
                imagenes = emptyList(),
                activo = true,
                stock = 5,
                categoria = "Sport",
                marca = "Adidas"
            )
        )
        binding.txtEmptyState.visibility = View.GONE
        binding.recyclerAdminProducts.visibility = View.VISIBLE
        adapter.setData(mockList)
    }

    override fun onEdit(producto: Producto) {
        val i = Intent(this, AdminProductFormActivity::class.java)
        i.putExtra("producto", producto)
        startActivity(i)
    }

    override fun onDelete(producto: Producto) {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        api.deleteProduct(producto.id).enqueue(object : Callback<Producto> {
            override fun onResponse(call: Call<Producto>, response: Response<Producto>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminProductListActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    cargarProductos()
                } else {
                    Toast.makeText(this@AdminProductListActivity, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Producto>, t: Throwable) {
                Toast.makeText(this@AdminProductListActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
