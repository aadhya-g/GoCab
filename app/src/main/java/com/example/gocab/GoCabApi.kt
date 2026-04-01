package com.example.gocab.network

import com.example.gocab.model.SearchRideRequest
import com.example.gocab.model.SearchRideResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GoCabApi {

    @POST("api/search-rides-v2")
    suspend fun searchRides(
        @Body request: SearchRideRequest
    ): Response<SearchRideResponse>


    @PUT("endRide/{rideId}")
    suspend fun endRide(
        @Path("rideId") rideId: Int
    ): Response<Map<String, String>>
}
/*
//GoCabApi.kt
package com.example.gocab.network

import com.example.gocab.model.SearchRideRequest
import com.example.gocab.model.SearchRideResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GoCabApi {

    @POST("api/search-rides-v2")
    suspend fun searchRides(
        @Body request: SearchRideRequest
    ): Response<SearchRideResponse>


}*/
