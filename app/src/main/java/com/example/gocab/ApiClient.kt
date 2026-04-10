package com.example.gocab.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    const val BASE_URL = "https://expository-seaworthy-allegra.ngrok-free.dev/"
    // ⚠ If using emulator:
    // 10.0.2.2 instead of localhost

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}