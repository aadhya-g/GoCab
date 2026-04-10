package com.example.gocab.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

     //const val BASE_URL ="https://expository-seaworthy-allegra.ngrok-free.dev/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 User / Auth related APIs
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // 🔹 Ride related APIs
    val rideApi: RideApi by lazy {
        retrofit.create(RideApi::class.java)
    }
}


