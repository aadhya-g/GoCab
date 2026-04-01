package com.example.gocab.model
import com.example.gocab.model.StudentUpcomingRide

data class UpcomingRideResponse(
    val success: Boolean,
    val rides: List<StudentUpcomingRide>
)