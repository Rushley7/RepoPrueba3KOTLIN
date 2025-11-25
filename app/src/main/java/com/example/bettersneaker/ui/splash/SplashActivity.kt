package com.example.bettersneaker.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.bettersneaker.databinding.ActivitySplashBinding
import com.example.bettersneaker.ui.admin.AdminActivity
import com.example.bettersneaker.ui.auth.LoginActivity
import com.example.bettersneaker.ui.client.ClientActivity
import com.example.bettersneaker.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            if (session.isLoggedIn()) {
                val role = session.getUserRole()
                if (role == "admin") {
                    startActivity(Intent(this, AdminActivity::class.java))
                    finish()
                    return@postDelayed
                }
                startActivity(Intent(this, ClientActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 5000)
    }
}