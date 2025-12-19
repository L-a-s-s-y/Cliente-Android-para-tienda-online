package com.example.practica3.api
import com.example.practica3.models.ProductModel
import com.example.practica3.models.CartDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {

    @POST("/api/cart/add/{id}")
    fun addToCart(
        @Path("id") id: Long
    ): Call<Void>

    @POST("/api/cart/remove/{id}")
    fun remove(
        @Path("id") id: Long
    ): Call<Void>

    @Streaming
    @GET("/api/cart/invoice")
    fun processPayment(): Call<ResponseBody>

    @GET("/api/cart")
    fun getFullCart(): Call<CartDto>

    @GET("/api/products")
    fun getAllProducts(): Call<List<ProductModel>>

    @POST("/api/delete/{id}")
    fun deleteProduct(
        @Path("id") productId: Long
    ): Call<Void>

    @POST("/api/add")
    fun addProductAdmin(
        @Query("name") name: String,
        @Query("price") price: Double
    ): Call<Void>

    @PUT("/api/edit/{id}")
    fun editProduct(
        @Path("id") productId: Long,
        @Query("name") name: String,
        @Query("price") price: Double
    ): Call<Void>

    @FormUrlEncoded
    @POST("/login")
    fun loginForm(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<Void>
}