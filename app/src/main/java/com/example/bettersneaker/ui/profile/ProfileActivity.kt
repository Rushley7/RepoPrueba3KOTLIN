package com.example.bettersneaker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityProfileBinding
import com.example.bettersneaker.models.User
import com.example.bettersneaker.models.UpdateMeRequest
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbarProfile
        toolbar.setNavigationOnClickListener { finish() }

        cargarPerfil()

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.editNombre.text.toString().trim()
            val direccion = binding.editDireccion.text.toString().trim()
            val telefono = binding.editTelefono.text.toString().trim()
            val body = UpdateMeRequest(nombre.ifEmpty { null }, telefono.ifEmpty { null }, direccion.ifEmpty { null })
            RetrofitClient.authService(this).updateMe(body).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.btnLogout.setOnClickListener {
            SessionManager(this).logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun cargarPerfil() {
        RetrofitClient.authService(this).me().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val u = response.body()
                    binding.editNombre.setText(u?.nombre ?: "")
                    binding.editEmail.setText(u?.email ?: "")
                    binding.editDireccion.setText(u?.direccion ?: "")
                    binding.editTelefono.setText(u?.telefono ?: "")
                } else {
                    Toast.makeText(this@ProfileActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}