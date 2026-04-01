package com.example.gocab.util

data class JoinRideFilterData(
    val sameCollegeOnly: Boolean = false,
    val userCollege: String? = null   // ✅ ADD THIS LINE
)