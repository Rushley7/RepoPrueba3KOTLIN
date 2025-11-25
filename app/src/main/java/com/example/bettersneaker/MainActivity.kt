package com.example.bettersneaker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.databinding.ActivityMainBinding
import com.example.bettersneaker.R
import com.example.bettersneaker.ui.admin.AdminActivity
import com.example.bettersneaker.ui.client.ClientActivity
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.utils.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(findViewById(R.id.root))

        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            val role = session.getUserRole()
            if (role == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
                return
            }
            if (role == "cliente") {
                startActivity(Intent(this, ClientActivity::class.java))
                finish()
                return
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }
}