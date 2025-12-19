package com.example.practica3

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.practica3.api.ApiClient
import com.example.practica3.api.ApiService
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProductActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.initialize(applicationContext)
        apiService = ApiClient.retrofit.create(ApiService::class.java)

        setContentView(R.layout.activity_edit_product)

        val toolbar = findViewById<MaterialToolbar>(R.id.editToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val productId = intent.getLongExtra("product_id", -1L)
        val originalName = intent.getStringExtra("product_name").orEmpty()
        val originalPrice = intent.getDoubleExtra("product_price", 0.0)

        if (productId <= 0) {
            Toast.makeText(this, "Producto inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val editName = findViewById<EditText>(R.id.editTextEditName)
        val editPrice = findViewById<EditText>(R.id.editTextEditPrice)
        val btnSave = findViewById<Button>(R.id.buttonSaveEdit)

        editName.setText(originalName)
        editPrice.setText(originalPrice.toString())

        btnSave.setOnClickListener {
            val newName = editName.text.toString().trim()
            val priceText = editPrice.text.toString().trim()

            if (newName.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = priceText.toDoubleOrNull()
            if (newPrice == null) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            apiService.editProduct(productId, newName, newPrice)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditProductActivity, "Producto actualizado", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Log.e("EDIT", "Error edit: ${response.code()}")
                            Toast.makeText(this@EditProductActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("EDIT", "Fallo edit: ${t.message}")
                        Toast.makeText(this@EditProductActivity, "No se pudo conectar", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
