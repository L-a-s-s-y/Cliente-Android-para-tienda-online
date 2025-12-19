package com.example.practica3

import android.content.Intent
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

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización de vistas
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        val toolbar = findViewById<MaterialToolbar>(R.id.loginToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)   // muestra la flecha
            setHomeButtonEnabled(true)        // hace clicable el botón
            title = "Login"
        }

        toolbar.setNavigationOnClickListener {
            // O solo finish() si siempre vienes de MainActivity
            onBackPressedDispatcher.onBackPressed()
        }
        // Botón de login
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        val apiService = ApiClient.retrofit.create(ApiService::class.java)

        apiService.loginForm(username, password).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // URL final después de que OkHttp siga los redirects
                val finalUrl = response.raw().request.url
                val path = finalUrl.encodedPath // ej: "/admin" o "/login"

                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                Log.d("LOGIN", "Final URL: $finalUrl (path=$path) code=${response.code()}")

                if (response.isSuccessful && path == "/admin") {

                    // Marcamos que estamos logueados
                    sharedPref.edit()
                        .putBoolean("logged_in", true)
                        .apply()

                    Toast.makeText(
                        this@LoginActivity,
                        "Login correcto",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Decidir a dónde ir después del login
                    val target = intent.getStringExtra("after_login_destination")
                    val nextIntent = when (target) {
                        "admin" -> Intent(this@LoginActivity, AdminActivity::class.java)
                        else    -> Intent(this@LoginActivity, MainActivity::class.java)
                    }
                    startActivity(nextIntent)
                    finish()

                } else {
                    // Login incorrecto: seguimos en LoginActivity
                    // Aseguramos que NO quede marcado como logueado
                    sharedPref.edit()
                        .putBoolean("logged_in", false)
                        .apply()

                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales incorrectas",
                        Toast.LENGTH_SHORT
                    ).show()
                    // No hacemos startActivity → el usuario se queda aquí para volver a intentarlo
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Error de red → tampoco estamos logueados
                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                sharedPref.edit()
                    .putBoolean("logged_in", false)
                    .apply()

                Toast.makeText(
                    this@LoginActivity,
                    "Error de red: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}