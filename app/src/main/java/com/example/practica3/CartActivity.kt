package com.example.practica3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.practica3.api.ApiClient
import com.example.practica3.api.ApiService
import com.example.practica3.adapters.CartAdapter
import com.example.practica3.models.CartDto
import com.example.practica3.models.CartItemDto
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var buttonCheckout: Button
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        apiService = ApiClient.createService(ApiService::class.java)
        setContentView(R.layout.activity_cart)

        val toolbar = findViewById<MaterialToolbar>(R.id.cartToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)   // muestra la flecha
            setHomeButtonEnabled(true)        // hace clicable el botón
            title = "Carrito"
        }

        toolbar.setNavigationOnClickListener {
            // O solo finish() si siempre vienes de MainActivity
            onBackPressedDispatcher.onBackPressed()
        }

        recyclerViewCart = findViewById(R.id.recyclerViewCart)
        buttonCheckout = findViewById(R.id.buttonCheckout)

        recyclerViewCart.layoutManager = LinearLayoutManager(this)

        buttonCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        loadCartItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // vuelve a MainActivity
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadCartItems() {
        apiService.getFullCart().enqueue(object : Callback<CartDto> {
            override fun onResponse(call: Call<CartDto>, response: Response<CartDto>) {
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    if (cartDto != null) {
                        val cartItems: List<CartItemDto> = cartDto.items

                        cartAdapter = CartAdapter(cartItems) { item ->
                            remove(item.productId)
                        }
                        recyclerViewCart.adapter = cartAdapter
                    }
                } else {
                    Log.e("CART", "Error al cargar carrito: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CartDto>, t: Throwable) {
                Log.e("CART", "Error de red: ${t.message}")
            }
        })
    }

    private fun remove(productId: Long) {
        apiService.remove(productId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    loadCartItems()
                    Log.d("CART", "Producto eliminado del carrito")
                } else {
                    Log.e("CART", "Error al eliminar del carrito: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("CART", "Error: ${t.message}")
            }
        })
    }
}
