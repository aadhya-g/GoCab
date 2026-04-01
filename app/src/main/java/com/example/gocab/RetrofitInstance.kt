package com.example.gocab.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://172.30.14.204:5000/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
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


