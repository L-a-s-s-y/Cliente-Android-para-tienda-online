package com.example.practica3

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3.adapters.CheckoutAdapter
import com.example.practica3.api.ApiClient
import com.example.practica3.api.ApiService
import com.example.practica3.models.CartDto
import com.example.practica3.models.CartItemDto
import com.google.android.material.appbar.MaterialToolbar
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import android.content.Intent


class CheckoutActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var recyclerViewCheckout: RecyclerView
    private lateinit var checkoutTotalPrice: TextView
    private lateinit var buttonPayment: Button
    private lateinit var checkoutAdapter: CheckoutAdapter
    private var paymentCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.initialize(applicationContext)
        apiService = ApiClient.retrofit.create(ApiService::class.java)

        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<MaterialToolbar>(R.id.checkoutToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)   // muestra la flecha
            setHomeButtonEnabled(true)        // hace clicable el botón
            title = "Checkout"
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerViewCheckout = findViewById(R.id.recyclerViewCheckout)
        checkoutTotalPrice = findViewById(R.id.checkoutTotalPrice)
        buttonPayment = findViewById(R.id.buttonPayment)

        recyclerViewCheckout.layoutManager = LinearLayoutManager(this)

        buttonPayment.setOnClickListener {
            makePayment()
        }

        loadCheckoutItems()
    }

    private fun loadCheckoutItems() {
        apiService.getFullCart().enqueue(object : Callback<CartDto> {
            override fun onResponse(call: Call<CartDto>, response: Response<CartDto>) {
                if (response.isSuccessful) {
                    val cartDto = response.body()
                    if (cartDto != null) {
                        val cartItems: List<CartItemDto> = cartDto.items
                        checkoutAdapter = CheckoutAdapter(cartItems)
                        recyclerViewCheckout.adapter = checkoutAdapter

                        checkoutTotalPrice.text = "Total: €%.2f".format(cartDto.total)
                        Log.d("CHECKOUT", "EXITO: $cartItems")
                    }
                } else {
                    Log.e("CHECKOUT", "Error al cargar carrito: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CartDto>, t: Throwable) {
                Log.e("CHECKOUT", "Error de red: ${t.message}")
            }
        })
    }

    private fun makePayment() {
        apiService.processPayment().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        try {
                            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(downloadsDir, "invoice.pdf")

                            FileOutputStream(file).use { output ->
                                output.write(body.bytes())
                            }

                            Log.d("PAYMENT", "PDF guardado en: ${file.absolutePath}")

                            Toast.makeText(
                                this@CheckoutActivity,
                                "Payment successful. Invoice downloaded.",
                                Toast.LENGTH_LONG
                            ).show()

                            paymentCompleted = true

                            // En lugar de openPdf(file), saltamos a MainActivity
                            val intent = Intent(this@CheckoutActivity, MainActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                                putExtra("invoice_path", file.absolutePath)
                            }
                            startActivity(intent)

                            // Cerramos CheckoutActivity para que no quede debajo del visor PDF
                            finish()

                        } catch (e: Exception) {
                            Log.e("PAYMENT", "Error guardando PDF: ${e.message}")
                            showPaymentError()
                        }
                    } else {
                        Log.e("PAYMENT", "PDF nulo")
                        showPaymentError()
                    }
                } else {
                    Log.e("PAYMENT", "Error: ${response.code()}")
                    showPaymentError()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("PAYMENT", "Error de red: ${t.message}")
                showPaymentError()
            }
        })
    }

    private fun showPaymentError() {
        Toast
            .makeText(this, "Error while processing the payment. Please try again.", Toast.LENGTH_LONG)
            .show()
    }

}
