package com.example.gocab.model
data class StudentUpcomingRide(
    val R_id: Int,
    val Pickup_loc: String,
    val Drop_loc: String,
    val pickup_city: String,
    val drop_city: String,
    val R_date: String,
    val Ride_status: String,
    val D_name: String,
    val verifiedCount: Int
)