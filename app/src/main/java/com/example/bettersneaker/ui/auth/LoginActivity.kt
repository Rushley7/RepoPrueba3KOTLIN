package com.example.bettersneaker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.MainActivity
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityLoginBinding
import com.example.bettersneaker.models.LoginRequest
import com.example.bettersneaker.models.LoginResponse
import com.example.bettersneaker.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.GONE

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progressBar.visibility = View.VISIBLE
            RetrofitClient.authService(this).login(LoginRequest(email, password))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        binding.progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            val body = response.body()
                            val token = body?.authToken
                            if (!token.isNullOrEmpty()) {
                                SessionManager(this@LoginActivity).saveSession(token, body.user.role)
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Error: Token vacío", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        binding.txtGoToSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}