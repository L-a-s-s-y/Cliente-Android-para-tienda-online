package com.example.practica3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3.adapters.AdminProductAdapter
import com.example.practica3.api.ApiClient
import com.example.practica3.api.ApiService
import com.example.practica3.models.ProductModel
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity: AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var adminAdapter: AdminProductAdapter

    private lateinit var editTextProductName: EditText
    private lateinit var editTextProductPrice: EditText
    private lateinit var buttonSubmitProduct: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos ApiClient (misma idea que en MainActivity)
        ApiClient.initialize(applicationContext)
        apiService = ApiClient.retrofit.create(ApiService::class.java)

        setContentView(R.layout.activity_administration)

        // Toolbar de la pantalla
        val toolbar = findViewById<MaterialToolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Administración"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Referencias UI
        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductPrice = findViewById(R.id.editTextProductPrice)
        buttonSubmitProduct = findViewById(R.id.buttonSubmitProduct)
        recyclerView = findViewById(R.id.recyclerViewAdministracion)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adminAdapter = AdminProductAdapter(
            emptyList(),
            onDeleteClick = { productId -> deleteProduct(productId) },
            onEditClick = { product ->
                val intent = Intent(this, EditProductActivity::class.java).apply {
                    putExtra("product_id", product.id)
                    putExtra("product_name", product.name)
                    putExtra("product_price", product.price)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = adminAdapter

        // Cargar productos al entrar
        loadProducts()

        // Añadir producto
        buttonSubmitProduct.setOnClickListener {
            val name = editTextProductName.text.toString().trim()
            val priceText = editTextProductPrice.text.toString().trim()

            if (name.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            if (price == null) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addProduct(name, price)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun loadProducts() {
        apiService.getAllProducts().enqueue(object : Callback<List<ProductModel>> {
            override fun onResponse(
                call: Call<List<ProductModel>>,
                response: Response<List<ProductModel>>
            ) {
                if (response.isSuccessful) {
                    val list = response.body().orEmpty()
                    adminAdapter.updateProducts(list)
                } else {
                    Log.e("ADMIN", "Error al cargar productos: ${response.code()}")
                    Toast.makeText(
                        this@AdminActivity,
                        "Error al cargar productos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<ProductModel>>, t: Throwable) {
                Log.e("ADMIN", "Fallo al cargar productos: ${t.message}")
                Toast.makeText(
                    this@AdminActivity,
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addProduct(name: String, price: Double) {
        apiService.addProductAdmin(name, price).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminActivity,
                        "Producto añadido",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpiar campos
                    editTextProductName.text.clear()
                    editTextProductPrice.text.clear()

                    // Refrescar lista
                    loadProducts()

                    // Avisar a MainActivity de que hay cambios
                    setResult(RESULT_OK)
                } else {
                    Log.e("ADMIN", "Error al añadir producto: ${response.code()}")
                    Toast.makeText(
                        this@AdminActivity,
                        "Error al añadir producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ADMIN", "Fallo al añadir producto: ${t.message}")
                Toast.makeText(
                    this@AdminActivity,
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun deleteProduct(productId: Long) {
        apiService.deleteProduct(productId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AdminActivity,
                        "Producto eliminado",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadProducts()
                    setResult(RESULT_OK)
                } else {
                    Log.e("ADMIN", "Error al eliminar producto: ${response.code()}")
                    Toast.makeText(
                        this@AdminActivity,
                        "Error al eliminar producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ADMIN", "Fallo al eliminar producto: ${t.message}")
                Toast.makeText(
                    this@AdminActivity,
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}