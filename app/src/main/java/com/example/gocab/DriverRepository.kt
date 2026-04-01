package com.example.gocab

data class Driver(
    val id: Int,
    val name: String,
    val vehicle: String,
    val currentLocation: String,
    val costPerKm: Double
)


data class RideFullDetailsResponse(
    val success: Boolean,
    val ride: RideData,
    val students: List<StudentData>
)

data class RideData(
    val R_id: Int,
    val initial_loc: String,
    val final_loc: String,
    val R_date: String,
    val distance_km: Double,
    val fare_amount: Double,
    val D_name: String,
    val D_phone_no: String,
    val C_name: String,
    val C_number: String
)

data class StudentData(
    val S_name: String,
    val student_pickup: String,
    val student_drop: String,
    val fare_per_student: Double
)
