package com.example.bettersneaker.ui.cart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bettersneaker.adapters.CartAdapter
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityCartBinding
import com.example.bettersneaker.models.AddCartItemRequest
import com.example.bettersneaker.models.Carrito
import com.example.bettersneaker.models.CartItem
import com.example.bettersneaker.models.Orden
import com.example.bettersneaker.ui.orders.ClientOrdersActivity
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class CartActivity : AppCompatActivity(), CartAdapter.OnCartInteractionListener {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private var isProcessing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarCart
        toolbar.setNavigationOnClickListener { finish() }

        adapter = CartAdapter(mutableListOf(), this)
        binding.recyclerCart.layoutManager = LinearLayoutManager(this)
        binding.recyclerCart.adapter = adapter

        binding.btnCheckout.setOnClickListener { confirmarCheckout() }
    }

    override fun onResume() {
        super.onResume()
        cargarCarrito()
    }

    private fun cargarCarrito() {
        RetrofitClient.storeService(this).getCarrito().enqueue(object : Callback<Carrito> {
            override fun onResponse(call: Call<Carrito>, response: Response<Carrito>) {
                if (response.isSuccessful) {
                    val carrito = response.body()
                    val itemsRaw = carrito?.items ?: emptyList()
                    val items = itemsRaw.groupBy { it.productId }.values.map { group ->
                        val base = group.first()
                        val qty = group.sumOf { it.cantidad }
                        base.copy(cantidad = qty)
                    }
                    adapter.setData(items)

                    if (items.isEmpty()) {
                        binding.txtTotal.text = "Tu carrito está vacío"
                        binding.btnCheckout.visibility = View.GONE
                    } else {
                        // CÁLCULO CORREGIDO: Usa productPrice (Double) para evitar ambigüedad en sumOf
                        val totalCalculado: Double = items.sumOf { item ->
                            val precio = item.productPrice ?: 0.0
                            item.cantidad * precio
                        }
                        val totalFinal = if ((carrito?.total ?: 0.0) > 0) carrito!!.total else totalCalculado

                        binding.txtTotal.text = "Total: $${String.format(Locale.US, "%.2f", totalFinal)}"
                        binding.btnCheckout.visibility = View.VISIBLE
                    }
                } else {
                    showError("Error al cargar: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Carrito>, t: Throwable) {
                showError(t.message ?: "Error de red")
            }
        })
    }

    private fun confirmarCheckout() {
        if (isProcessing) return
        val totalTexto = binding.txtTotal.text?.toString() ?: "Total: $0.00"
        AlertDialog.Builder(this)
            .setTitle("CONFIRMAR COMPRA")
            .setMessage("El cobro total será de ${totalTexto.replace("Total: ", "")}.\n\n¿Desea continuar con el pago simulado?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("PAGAR AHORA") { d, _ ->
                d.dismiss()
                realizarCheckout()
            }
            .show()
    }

    private fun realizarCheckout() {
        val token = SessionManager(this).getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Sesión expirada. Inicia sesión para continuar.", Toast.LENGTH_LONG).show()
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            return
        }
        isProcessing = true
        RetrofitClient.storeService(this).checkout().enqueue(object : Callback<Orden> {
            override fun onResponse(call: Call<Orden>, response: Response<Orden>) {
                isProcessing = false
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@CartActivity,
                        "¡PEDIDO REALIZADO! Su compra está en la sección 'Mis Pedidos'.",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(android.content.Intent(this@CartActivity, ClientOrdersActivity::class.java))
                    finish()
                } else {
                    val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    showError("Error en checkout: ${response.code()} ${err ?: ""}")
                }
            }
            override fun onFailure(call: Call<Orden>, t: Throwable) {
                isProcessing = false
                showError(t.message ?: "Error de red")
            }
        })
    }

    override fun onIncrease(item: CartItem) {
        if (isProcessing) return
        isProcessing = true
        // CORREGIDO: Usamos item.productId
        val request = AddCartItemRequest(item.productId, 1)
        enviarActualizacion(request)
    }

    override fun onDecrease(item: CartItem) {
        if (isProcessing) return

        if (item.cantidad > 1) {
            isProcessing = true
            // CORREGIDO: Usamos item.productId
            val request = AddCartItemRequest(item.productId, -1)
            enviarActualizacion(request)
        } else {
            // Si es 1, confirmamos eliminación
            AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar producto del carrito?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí") { d, _ ->
                    d.dismiss()
                    onDelete(item)
                }
                .show()
        }
    }

    private fun enviarActualizacion(req: AddCartItemRequest) {
        RetrofitClient.storeService(this).agregarItem(req).enqueue(object : Callback<Carrito> {
            override fun onResponse(call: Call<Carrito>, response: Response<Carrito>) {
                isProcessing = false
                if (response.isSuccessful) cargarCarrito()
                else showError("Error al actualizar: ${response.code()}")
            }
            override fun onFailure(call: Call<Carrito>, t: Throwable) {
                isProcessing = false
                showError("Error de red")
            }
        })
    }

    override fun onDelete(item: CartItem) {
        if (isProcessing) return
        isProcessing = true

        RetrofitClient.storeService(this).deleteCartItemByQuery(item.id).enqueue(object : Callback<Carrito> {
            override fun onResponse(call: Call<Carrito>, response: Response<Carrito>) {
                isProcessing = false
                if (response.isSuccessful) cargarCarrito()
                else showError("Error al eliminar")
            }
            override fun onFailure(call: Call<Carrito>, t: Throwable) {
                isProcessing = false
                showError("Error de red")
            }
        })
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
