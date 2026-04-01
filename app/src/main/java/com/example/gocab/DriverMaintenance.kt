package com.example.gocab

import com.google.gson.annotations.SerializedName

data class DriverMaintenance(

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("licenceNumber")
    val licenceNumber: String
)