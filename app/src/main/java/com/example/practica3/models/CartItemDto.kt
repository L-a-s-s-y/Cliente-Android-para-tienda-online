package com.example.practica3.models

data class CartItemDto (
    val productId: Long,
    val name: String,
    val price: Double,
    val quantity: Int
)