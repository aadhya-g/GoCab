package com.example.gocab.ui.DataModels

data class DriverRideResponse(
    val R_id: Int,
    val initial_loc: String,
    val final_loc: String,
    val R_date: String,
    val R_timing: String,
    val distance_km: Double,
    val fare_amount: Double,
    val R_status: String,
    val pickup_city: String,
    val drop_city: String
)
/*
package com.example.gocab.ui.DataModels

data class DriverRideResponse(
    val R_id: Int,
    val initial_loc: String,
    val final_loc: String,
    val R_date: String,
    val R_timing: String,
    val distance_km: Double,
    val fare_amount: Double,
    val R_status: String
)*/
