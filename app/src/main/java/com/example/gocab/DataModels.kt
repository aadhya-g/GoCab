package com.example.gocab.ui.DataModels

import com.google.gson.annotations.SerializedName

data class RideRequest(
    val driverEmail: String,
    val customerId: String,
    val studentEmail: String,
    val pickup: String,
    val drop: String,
    val distanceKm: Double,
    val fare: Double
)

data class RideResponse(
    val success: Boolean,
    val message: String,
    val rideId: Int
)

data class UpdateRideRequest(
    val rideId: Int,
    val driverId: String,
    val status: String
)

data class GenericResponse(
    val message: String
)
data class UpdateRideStatusResponse(
    val message: String
)

data class JoinRequest(
    val id: Int,
    val R_id: Int,
    val S_email_id: String,
    val Pickup_loc: String,
    val Drop_loc: String,
    val R_date: String
)

data class RideHistory(
    val R_id: Int,
    val initial_loc: String,
    val final_loc: String,
    val fare_amount: Double,
    val distance_km: Double,
    val R_date: String,

    @SerializedName("Ride_status")
    val ride_status: String,

    val driver_name: String,
    //val D_eid: String,            // ✅ ADD THIS
    val D_eid: String? ,  // ✅ make nullable
    val D_avg_rating: Double?     // ✅ ADD THIS
)


data class RideHistoryResponse(
    val success: Boolean,
    val rides: List<RideHistory>
)

data class RatingRequest(
    val R_id: Int,
    val email: String,
    val driverId: String,
    val rating: Float
)
