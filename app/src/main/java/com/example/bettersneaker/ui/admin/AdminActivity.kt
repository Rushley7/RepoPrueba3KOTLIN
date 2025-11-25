package com.example.bettersneaker.ui.admin

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.databinding.ActivityAdminBinding
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.ui.client.ClientActivity
import com.example.bettersneaker.utils.SessionManager

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardInventario.setOnClickListener {
            startActivity(Intent(this, AdminProductListActivity::class.java))
        }

        binding.cardUsuarios.setOnClickListener {
            startActivity(Intent(this, AdminUserListActivity::class.java))
        }

        binding.cardPedidos.setOnClickListener {
            startActivity(Intent(this, AdminOrderListActivity::class.java))
        }

        binding.cardTienda.setOnClickListener {
            startActivity(Intent(this, ClientActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            SessionManager(this).logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}