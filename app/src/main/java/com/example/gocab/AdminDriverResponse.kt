package com.example.gocab.network

import com.example.gocab.model.AdminDriver

data class AdminDriverResponse(
    val success: Boolean,
    val drivers: List<AdminDriver>
)
//AdminDriverResponse.kt