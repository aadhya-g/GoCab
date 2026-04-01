package com.example.gocab.ui.DataModels

data class UpdateRideStatusRequest(
    val rideId: Int,
    val driverId: String,
    val status: String
)