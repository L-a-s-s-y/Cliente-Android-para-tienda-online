package com.example.practica3.api

import android.content.Context
import android.content.SharedPreferences
import com.example.practica3.interfaces.ClearableCookieJar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://192.168.178.29:8080/"
    //private const val BASE_URL = "http://10.97.35.68:8080/"
    //private const val BASE_URL = "http://192.168.178.25:8080/"
    //private const val BASE_URL = "http://10.0.2.2:8080/"

    lateinit var cookieJar: ClearableCookieJar
        private set

    private lateinit var sharedPref: SharedPreferences

    lateinit var gson: Gson
        private set

    lateinit var client: OkHttpClient
        private set

    lateinit var retrofit: Retrofit
        private set

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val authInterceptor = Interceptor { chain ->
        val token = sharedPref.getString("auth_token", null)
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    fun initialize(context: Context) {
        sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        if (this::client.isInitialized) {
            return
        }

        cookieJar = object : ClearableCookieJar {

            private val cookieStore = HashMap<String, MutableList<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies.toMutableList()
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }

            override fun clear() {
                cookieStore.clear()
            }
        }

        sharedPref.edit()
            .putBoolean("logged_in", false)
            .apply()

        client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // (Opcional) Helper para crear servicios:
    fun <T> createService(serviceClass: Class<T>): T = retrofit.create(serviceClass)
}
