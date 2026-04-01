package com.example.gocab.model

data class AdminStudentResponse(
    val success: Boolean,
    val count: Int,
    val students: List<AdminStudent>
)

//AdminStudentResponse.kt