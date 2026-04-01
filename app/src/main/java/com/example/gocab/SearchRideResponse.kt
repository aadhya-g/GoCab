//SearchRideResponse.kt
package com.example.gocab.model

data class SearchRideResponse(
    val success: Boolean,
    val pickup: String,
    val drop: String,
    val drivers: List<Driver>
)