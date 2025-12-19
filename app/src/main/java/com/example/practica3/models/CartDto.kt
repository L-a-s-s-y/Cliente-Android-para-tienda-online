package com.example.practica3.models

data class CartDto (
    val items: List<CartItemDto>,
    val total: Double
)