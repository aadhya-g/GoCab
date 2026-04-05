//RetrofitClient.kt
package com.example.gocab.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    private const val BASE_URL = "http://10.200.92.204:5000/"
    val api: GoCabApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoCabApi::class.java)
    }
}

