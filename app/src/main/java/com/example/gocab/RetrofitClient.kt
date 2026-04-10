//RetrofitClient.kt
package com.example.gocab.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    const val BASE_URL = "https://expository-seaworthy-allegra.ngrok-free.dev/"
    val api: GoCabApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoCabApi::class.java)
    }
}

