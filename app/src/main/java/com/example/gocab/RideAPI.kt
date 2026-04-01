package com.example.gocab.network

import com.example.gocab.ui.DataModels.DriverRideResponse
import com.example.gocab.ui.DataModels.JoinRequest
import com.example.gocab.ui.DataModels.RideRequest
import com.example.gocab.ui.DataModels.RideResponse
import com.example.gocab.ui.DataModels.UpdateRideRequest
import com.example.gocab.ui.DataModels.UpdateRideStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RideApi {

    @POST("api/ride/request")
    suspend fun requestRide(
        @Body request: RideRequest
    ): Response<RideResponse>

    // ✅ GET DRIVER RIDES
    @GET("api/driver/{driverId}/rides")
    suspend fun getDriverRides(
        @Path("driverId") driverId: String
    ): Response<List<DriverRideResponse>>

    @PUT("api/ride/update-status")
    suspend fun updateRideStatus(
        @Body request: UpdateRideRequest
    ): Response<UpdateRideStatusResponse>


    @GET("api/driver/join-requests/{driverEmail}")
    suspend fun getJoinRequests(
        @Path("driverEmail") driverEmail: String
    ): Response<List<JoinRequest>>

    @POST("api/driver/accept-join")
    suspend fun acceptJoin(
        @Body body: Map<String, Int>
    ): Response<Unit>

    @POST("api/driver/reject-join")
    suspend fun rejectJoin(
        @Body body: Map<String, Int>
    ): Response<Unit>


/*
    // ===========================
// JOIN RIDE SECTION
// ===========================

    // 🔍 Search Existing Rides
    @POST("api/ride/search-existing")
    suspend fun searchExistingRides(
        @Body request: JoinRideRequest
    ): Response<RideResponse>


    // 🙋 Student Join Ride
    @POST("api/ride/join")
    suspend fun joinRide(
        @Body request: JoinRideRequest
    ): Response<JoinRideResponse>


    // 🚗 Driver Get Join Requests
    @GET("api/driver/{driverId}/join-requests")
    suspend fun getDriverJoinRequests(
        @Path("driverId") driverId: String
    ): Response<List<DriverJoinRequestResponse>>


    // ✅ Driver Accept / Reject Join
    @PUT("api/join/update-status")
    suspend fun updateJoinRideStatus(
        @Body request: UpdateJoinRideRequest
    ): Response<JoinRideResponse>


 */
}


