package com.example.practica3

import android.view.View
import org.osmdroid.config.Configuration
import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.example.practica3.adapters.ProductAdapter

import com.example.practica3.api.ApiClient
import com.example.practica3.api.ApiService
import com.example.practica3.models.ProductModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var productAdapter: ProductAdapter

    private lateinit var recyclerView: RecyclerView

    private lateinit var buttonCarrito: Button
    private lateinit var buttonAministration: Button
    private lateinit var buttonMaps: Button

    private lateinit var apiService: ApiService
    private lateinit var addProductLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        val context = applicationContext
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = "com.example.practica3"
        super.onCreate(savedInstanceState)
        ApiClient.initialize(applicationContext)
        apiService = ApiClient.retrofit.create(ApiService::class.java)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Productos"
            // Si quieres flecha de back en Main, normalmente NO, porque es la pantalla raíz
            // setDisplayHomeAsUpEnabled(false)
        }

        recyclerView = findViewById(R.id.recyclerViewProducts)

        buttonCarrito = findViewById(R.id.buttonGoToCart)
        buttonAministration = findViewById(R.id.buttonAministration)
        buttonMaps = findViewById(R.id.buttonMaps)

        addProductLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Si el producto se añadió con éxito, actualiza los productos
                fetchProducts()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchProducts()

        recyclerView.visibility = View.VISIBLE
        buttonMaps.visibility = View.VISIBLE
        buttonAministration.visibility = View.VISIBLE


        buttonCarrito.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        buttonMaps.setOnClickListener{
            goToMap()
        }

        buttonAministration.setOnClickListener{
            addProductAdmin()
        }

        handleInvoiceIntent(intent)

    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleInvoiceIntent(intent)
    }

    private fun handleInvoiceIntent(intent: Intent) {
        val path = intent.getStringExtra("invoice_path")
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                openPdf(file)
            } else {
                Log.e("MAIN", "Invoice file not found at $path")
            }
            // Opcional: si no quieres volver a abrirla al rotar la pantalla, puedes limpiar el extra
            intent.removeExtra("invoice_path")
        }
    }

    private fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // puedes dejar o quitar NO_HISTORY, pero no afecta al problema de la pila nuestra
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            val chooser = Intent.createChooser(intent, "Open invoice with:")
            startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_LONG).show()
            Log.e("OPEN_PDF", "Error: ${e.message}")
        }
    }

    private fun goToMap() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    private fun fetchProducts() {
        apiService.getAllProducts().enqueue(object : Callback<List<ProductModel>> {
            override fun onResponse(
                call: Call<List<ProductModel>>,
                response: Response<List<ProductModel>>
            ) {
                Log.d("FETCH_PRODUCTS", "llegó a onResponse")
                if (response.isSuccessful) {
                    val productList = response.body()
                    Log.d("FETCH_PRODUCTS", "Productos: $productList")
                    productList?.let {
                        // Initialize the adapter with the product list
                        productAdapter = ProductAdapter(
                            productModelList = it,
                            onAddToCart = { productId -> addToCart(productId) },
                        )
                        recyclerView.adapter = productAdapter
                    }
                } else {
                    Log.e("FETCH_PRODUCTS", "Error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ProductModel>>, t: Throwable) {
                Log.e("FETCH_PRODUCTS", "llegó a onFailure")
                Log.e("FETCH_PRODUCTS", "Failure: ${t.message}")
                Log.e("FETCH_PRODUCTS", "Failure: ${t.message}")
            }
        })
    }

    private fun addToCart(productId: Long) {
        apiService.addToCart(productId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    //Toast.makeText(this@MainActivity, "Producto añadido al carrito", Toast.LENGTH_SHORT).show()
                    Log.d("ADD_TO_CART", "Producto añadido al carrito: $productId")
                } else {
                    Log.e("ADD_TO_CART", "Error al añadir al carrito: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ADD_TO_CART", "Error: ${t.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        clearLocalData()
    }

    private fun clearLocalData() {
        // Eliminar cookies de WebView (no afecta a OkHttp)
        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()

        // Eliminar cookies de OkHttp (esto sí borra la sesión con la API)
        ApiClient.cookieJar.clear()
        Log.d("CLEAR_LOCAL_DATA", "Cookies de OkHttp eliminadas")

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .remove("auth_token")
            .putBoolean("logged_in", false)
            .apply()

        Log.d("CLEAR_LOCAL_DATA", "Datos locales eliminados: auth_token + cookies")

        finish()
    }

    private fun addProductAdmin(){
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val loggedIn = sharedPref.getBoolean("logged_in", false)

        if (!loggedIn) {
            // No está logueado -> voy al Login y le digo que luego vaya a Admin
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.putExtra("after_login_destination", "admin")
            startActivity(loginIntent)
        } else {
            // Ya logueado -> abro directamente la actividad de administración
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}
