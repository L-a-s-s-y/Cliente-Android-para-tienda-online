package com.example.practica3.interfaces

import okhttp3.CookieJar

interface ClearableCookieJar : CookieJar {
    fun clear()
}