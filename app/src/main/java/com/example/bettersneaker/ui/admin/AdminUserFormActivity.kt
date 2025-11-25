package com.example.bettersneaker.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.databinding.ActivityAdminUserFormBinding
import com.example.bettersneaker.models.CreateUserRequest
import com.example.bettersneaker.models.UpdateUserRequest
import com.example.bettersneaker.models.User
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminUserFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserFormBinding
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        user = intent.getSerializableExtra("user") as? User

        if (user != null) {
            binding.txtTitle.text = "EDITAR USUARIO"
            binding.editName.setText(user!!.nombre)
            binding.editEmail.setText(user!!.email)
            binding.inputPassword.visibility = View.GONE
        } else {
            binding.txtTitle.text = "NUEVO USUARIO"
            binding.inputPassword.visibility = View.VISIBLE
        }

        binding.btnSave.setOnClickListener {
            guardarUsuario()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun guardarUsuario() {
        val nombre = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        
        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)

        if (user == null) {
            // CREAR
            val password = binding.editPassword.text.toString().trim()
            if (password.isEmpty()) {
                Toast.makeText(this, "Contraseña requerida", Toast.LENGTH_SHORT).show()
                return
            }
            val request = CreateUserRequest(nombre, email, password)
            
            api.createUser(request).enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Usuario Creado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // EDITAR (Actualizado para coincidir con ApiService)
            val request = UpdateUserRequest(
                userId = user!!.id,
                nombre = nombre,
                email = email
            )

            api.updateUser(request).enqueue(object : Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Usuario Actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}