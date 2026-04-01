//StudentsResponse.kt
package com.example.gocab

import com.google.gson.annotations.SerializedName

data class StudentsResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("students")
    val students: List<Student>
)
data class StudentResponse(
    val student_id: Int,
    val name: String,
    val pickup: String,
    val drop: String
)