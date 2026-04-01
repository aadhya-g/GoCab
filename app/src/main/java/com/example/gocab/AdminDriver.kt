package com.example.gocab.model

import com.google.gson.annotations.SerializedName

data class AdminDriver(

    @SerializedName("driver_name")
    val D_name: String,

    @SerializedName("licence_no")
    val D_licence_no: String,

    @SerializedName("email")
    val D_eid: String
)
//AdminDriver.kt