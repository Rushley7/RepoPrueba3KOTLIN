package com.example.bettersneaker.ui.product

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityProductDetailBinding
import com.example.bettersneaker.models.AddCartItemRequest
import com.example.bettersneaker.models.Producto
import com.example.bettersneaker.adapters.ImageSliderAdapter
import com.example.bettersneaker.utils.getSerializable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private var producto: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        producto = intent.getSerializable("producto", Producto::class.java)
        producto?.let { p ->
            val imgs = p.imagenes ?: emptyList()
            binding.viewPagerImages.adapter = ImageSliderAdapter(imgs)

            val total = imgs.size.coerceAtLeast(1)
            binding.txtImageIndicator.text = "1/$total"
            binding.viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.txtImageIndicator.text = "${position + 1}/$total"
                }
            })

            binding.txtDetailName.text = p.nombre
            binding.txtDetailPrice.text = "$${String.format("%.2f", p.precio)}"
            val meta = "Stock: ${p.stock ?: 0}  ·  Marca: ${p.marca ?: "-"}"
            binding.txtDetailMeta.text = meta
            binding.chipMarca.text = p.marca ?: "-"
            binding.chipCategoria.text = p.categoria ?: "-"
            binding.txtDetailDescription.text = p.descripcion ?: ""
        }

        binding.btnAddToCart.setOnClickListener {
            val p = producto ?: return@setOnClickListener
            RetrofitClient.storeService(this)
                .agregarItem(AddCartItemRequest(p.id, 1))
                .enqueue(object : Callback<com.example.bettersneaker.models.Carrito> {
                    override fun onResponse(call: Call<com.example.bettersneaker.models.Carrito>, response: Response<com.example.bettersneaker.models.Carrito>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProductDetailActivity, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProductDetailActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<com.example.bettersneaker.models.Carrito>, t: Throwable) {
                        Toast.makeText(this@ProductDetailActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.fabBack.setOnClickListener { finish() }
    }
}