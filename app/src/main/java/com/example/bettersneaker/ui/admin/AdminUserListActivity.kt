package com.example.bettersneaker.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bettersneaker.adapters.AdminUserAdapter
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.databinding.ActivityAdminUserListBinding
import com.example.bettersneaker.models.BlockUserRequest
import com.example.bettersneaker.models.User
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminUserListActivity : AppCompatActivity(), AdminUserAdapter.OnUserAction {
    private lateinit var binding: ActivityAdminUserListBinding
    private lateinit var adapter: AdminUserAdapter
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdminUserAdapter(mutableListOf(), this)
        binding.recyclerAdminUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerAdminUsers.adapter = adapter

        binding.btnBackUsers.setOnClickListener { finish() }
        binding.fabAddUser.setOnClickListener { startActivity(Intent(this, AdminUserFormActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        api.getUsers(null).enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful && response.body() != null) {
                    val jsonElement = response.body()!!
                    val gson = Gson()
                    val itemType = object : TypeToken<List<User>>() {}.type
                    var lista: List<User> = emptyList()
                    try {
                        if (jsonElement.isJsonObject) {
                            val obj = jsonElement.asJsonObject
                            if (obj.has("items")) lista = gson.fromJson(obj.get("items"), itemType)
                        } else if (jsonElement.isJsonArray) {
                            lista = gson.fromJson(jsonElement, itemType)
                        }
                        adapter.setData(lista)
                        if (lista.isEmpty()) {
                            Toast.makeText(this@AdminUserListActivity, "Sin usuarios", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminUserListActivity, "Error datos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AdminUserListActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(this@AdminUserListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEdit(user: User) {
        val i = Intent(this, AdminUserFormActivity::class.java)
        i.putExtra("user", user)
        startActivity(i)
    }

    override fun onBlockToggle(user: User) {
        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        api.toggleBlockUser(BlockUserRequest(user.id, !user.blocked)).enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                if (response.isSuccessful) {
                    cargarUsuarios()
                } else {
                    Toast.makeText(this@AdminUserListActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Toast.makeText(this@AdminUserListActivity, t.message ?: "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
