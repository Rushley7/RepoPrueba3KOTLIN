package com.example.bettersneaker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityRegisterBinding
import com.example.bettersneaker.models.LoginResponse
import com.example.bettersneaker.models.SignupRequest
import com.example.bettersneaker.ui.client.ClientActivity
import com.example.bettersneaker.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.GONE

        binding.btnRegister.setOnClickListener {
            val nombre = binding.editNombre.text.toString().trim()
            val apellido = binding.editApellido.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val telefono = binding.editTelefono.text.toString().trim()
            val direccion = binding.editDireccion.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            val req = SignupRequest(nombre, apellido, email, password, telefono.ifEmpty { null }, direccion.ifEmpty { null })
            RetrofitClient.authService(this).signup(req).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Toast.makeText(this@RegisterActivity, "Usuario creado", Toast.LENGTH_SHORT).show()
                            SessionManager(this@RegisterActivity).saveSession(body.authToken, body.user.role)
                            startActivity(Intent(this@RegisterActivity, ClientActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Respuesta vacía", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@RegisterActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}