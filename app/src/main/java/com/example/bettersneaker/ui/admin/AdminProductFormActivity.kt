package com.example.bettersneaker.ui.admin

import android.net.Uri
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import android.util.Log
import com.bumptech.glide.Glide
import com.example.bettersneaker.api.RetrofitClient
import com.example.bettersneaker.data.api.ApiService
import com.example.bettersneaker.databinding.ActivityAdminProductFormBinding
import com.example.bettersneaker.models.Producto
import com.example.bettersneaker.models.UpdateProductRequest
import com.example.bettersneaker.models.XanoImage
import com.example.bettersneaker.utils.getSerializable
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import android.provider.OpenableColumns

class AdminProductFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProductFormBinding
    private var producto: Producto? = null
    private var selectedUri: Uri? = null
    private var existingImages: List<XanoImage> = emptyList()
    private val selectedImageUris = mutableListOf<Uri>()
    private var isProcessing: Boolean = false

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            selectedImageUris.clear()
            val clip = data?.clipData
            if (clip != null) {
                for (i in 0 until clip.itemCount) {
                    val uri = clip.getItemAt(i).uri
                    selectedImageUris.add(uri)
                }
            } else {
                val uri = result.data?.data
                if (uri != null) selectedImageUris.add(uri)
            }
            selectedUri = selectedImageUris.firstOrNull()
            selectedUri?.let { binding.imgPreview.setImageURI(it) }
            Toast.makeText(this, "${selectedImageUris.size} imagen(es) seleccionada(s)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProductFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.black))
        binding.toolbar.setNavigationOnClickListener {
            if (isProcessing) {
                Toast.makeText(this, "Espera a que termine el guardado", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isProcessing) {
                    Toast.makeText(this@AdminProductFormActivity, "Espera a que termine el guardado", Toast.LENGTH_SHORT).show()
                    return
                }
                finish()
            }
        })

        // Recibir datos si es edición
        producto = intent.getSerializable("producto", Producto::class.java)

        setupUI()
        supportActionBar?.title = if (producto == null) "NUEVO PRODUCTO" else "EDITAR"
    }

    private fun setupUI() {
        // Rellenar campos si existe producto
        producto?.let { p ->
            binding.editNombre.setText(p.nombre)
            binding.editPrecio.setText(p.precio.toString())
            binding.editDescripcion.setText(p.descripcion ?: "")
            binding.editStock.setText(p.stock?.toString() ?: "")
            binding.editCategoria.setText(p.categoria ?: "")
            binding.editMarca.setText(p.marca ?: "")

            if (!p.imagenes.isNullOrEmpty()) {
                Glide.with(this).load(p.imagenes[0].url).into(binding.imgPreview)
            }
            existingImages = p.imagenes ?: emptyList()
            binding.btnGuardarProducto.text = "Actualizar"
        }

        binding.btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickImagesLauncher.launch(intent)
        }

        // Botón Guardar
        binding.btnGuardarProducto.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            val nombre = binding.editNombre.text.toString().trim()
            val precio = binding.editPrecio.text.toString().toDoubleOrNull()
            val descripcion = binding.editDescripcion.text.toString().trim()
            val stock = binding.editStock.text.toString().toIntOrNull()
            val categoria = binding.editCategoria.text.toString().trim()
            val marca = binding.editMarca.text.toString().trim()

            if (nombre.isEmpty() || precio == null) {
                Toast.makeText(this, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImagesAndSendJson(nombre, precio, descripcion, stock, categoria, marca)
        }
    }

    
    private fun uploadImagesAndSendJson(
        nombre: String,
        precio: Double,
        descripcion: String,
        stock: Int?,
        categoria: String?,
        marca: String?
    ) {
        setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val api = RetrofitClient.getStoreClient(this@AdminProductFormActivity).create(ApiService::class.java)
            val uploadedImages = mutableListOf<XanoImage>()

            if (selectedImageUris.isEmpty()) {
                val existingHttps = existingImages.filter { it.url.startsWith("https") }
                withContext(Dispatchers.Main) {
                    enviarJson(nombre, precio, descripcion, stock, categoria, marca, existingHttps, producto?.id)
                }
                return@launch
            }

            var completed = 0
            val total = selectedImageUris.size
            selectedImageUris.forEach { uri ->
                val file = getFileFromUri(uri)
                if (file == null) {
                    completed++
                    if (completed == total) {
                        val uploadedFiltered = uploadedImages.filter { it.url.startsWith("https") }
                        val existingFiltered = existingImages.filter { it.url.startsWith("https") }
                        val finalImagesToKeep = if (producto == null) uploadedFiltered else if (uploadedFiltered.isEmpty()) existingFiltered else existingFiltered + uploadedFiltered
                        withContext(Dispatchers.Main) {
                            enviarJson(nombre, precio, descripcion, stock, categoria, marca, finalImagesToKeep, producto?.id)
                        }
                    }
                } else {
                    val mime = contentResolver.getType(uri) ?: "image/*"
                    val body = file.asRequestBody(mime.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, body)
                    api.uploadFiles(part).enqueue(object : Callback<XanoImage> {
                        override fun onResponse(call: Call<XanoImage>, response: Response<XanoImage>) {
                            if (response.isSuccessful) {
                                response.body()?.let { uploadedImages.add(it) }
                            } else {
                                Toast.makeText(this@AdminProductFormActivity, "No se pudo subir una imagen (${response.code()})", Toast.LENGTH_SHORT).show()
                            }
                            completed++
                            if (completed == total) {
                                val uploadedFiltered = uploadedImages.filter { it.url.startsWith("https") }
                                val existingFiltered = existingImages.filter { it.url.startsWith("https") }
                                val finalImagesToKeep = if (producto == null) uploadedFiltered else if (uploadedFiltered.isEmpty()) existingFiltered else existingFiltered + uploadedFiltered
                                enviarJson(nombre, precio, descripcion, stock, categoria, marca, finalImagesToKeep, producto?.id)
                            }
                        }
                        override fun onFailure(call: Call<XanoImage>, t: Throwable) {
                            Toast.makeText(this@AdminProductFormActivity, "No se pudo subir una imagen por red", Toast.LENGTH_SHORT).show()
                            completed++
                            if (completed == total) {
                                val uploadedFiltered = uploadedImages.filter { it.url.startsWith("https") }
                                val existingFiltered = existingImages.filter { it.url.startsWith("https") }
                                val finalImagesToKeep = if (producto == null) uploadedFiltered else if (uploadedFiltered.isEmpty()) existingFiltered else existingFiltered + uploadedFiltered
                                enviarJson(nombre, precio, descripcion, stock, categoria, marca, finalImagesToKeep, producto?.id)
                            }
                        }
                    })
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val mime = contentResolver.getType(uri)
        val extension = getFileExtension(mime)
        val fileName = "upload_${System.currentTimeMillis()}$extension"
        val tempFile = File(cacheDir, fileName)

        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile
            } else {
                Toast.makeText(this, "No se pudo procesar la imagen seleccionada.", Toast.LENGTH_SHORT).show()
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("FILE_ERROR", "Error al procesar archivo", e)
            Toast.makeText(this, "No se pudo procesar la imagen seleccionada.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun getFileExtension(mimeType: String?): String {
        return when (mimeType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/gif" -> ".gif"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) it.getString(nameIndex) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Función genérica para enviar el JSON final (sirve para crear y actualizar)
    private fun enviarJson(
        nombre: String, precio: Double, descripcion: String,
        stock: Int?, categoria: String?, marca: String?,
        imagenes: List<XanoImage>?, id: Int? = producto?.id
    ) {
        val body = UpdateProductRequest(
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            marca = marca,
            categoria = categoria,
            activo = true,
            imagenes = (imagenes ?: emptyList())
        )

        val api = RetrofitClient.getStoreClient(this).create(ApiService::class.java)
        val firstCall: Call<Producto> = if (id == null) api.createProduct(body) else api.updateProduct(id, body)

        firstCall.enqueue(object : Callback<Producto> {
            override fun onResponse(call: Call<Producto>, response: Response<Producto>) {
                setLoading(false)
                if (response.isSuccessful) {
                    val msg = if (id == null) "Producto Creado" else "Producto Actualizado"
                    Toast.makeText(this@AdminProductFormActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    if (id == null) {
                        val fallback = api.createProductPublic(body)
                        fallback.enqueue(object : Callback<Producto> {
                            override fun onResponse(call2: Call<Producto>, resp2: Response<Producto>) {
                                setLoading(false)
                                if (resp2.isSuccessful) {
                                    Toast.makeText(this@AdminProductFormActivity, "Producto Creado", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@AdminProductFormActivity, "Error API: ${resp2.code()} (${resp2.errorBody()?.string()})", Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onFailure(call2: Call<Producto>, t2: Throwable) {
                                setLoading(false)
                                Toast.makeText(this@AdminProductFormActivity, "Error red: ${t2.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        val fallback = api.updateProductAdmin(id, body)
                        fallback.enqueue(object : Callback<Producto> {
                            override fun onResponse(call2: Call<Producto>, resp2: Response<Producto>) {
                                setLoading(false)
                                if (resp2.isSuccessful) {
                                    Toast.makeText(this@AdminProductFormActivity, "Producto Actualizado", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@AdminProductFormActivity, "Error API: ${resp2.code()} (${resp2.errorBody()?.string()})", Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onFailure(call2: Call<Producto>, t2: Throwable) {
                                setLoading(false)
                                Toast.makeText(this@AdminProductFormActivity, "Error red: ${t2.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
            override fun onFailure(call: Call<Producto>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@AdminProductFormActivity, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        isProcessing = isLoading
        binding.btnGuardarProducto.isEnabled = !isLoading
        binding.btnSeleccionarImagen.isEnabled = !isLoading
        binding.editNombre.isEnabled = !isLoading
        binding.editPrecio.isEnabled = !isLoading
        binding.editDescripcion.isEnabled = !isLoading
        binding.editStock.isEnabled = !isLoading
        binding.editCategoria.isEnabled = !isLoading
        binding.editMarca.isEnabled = !isLoading
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
