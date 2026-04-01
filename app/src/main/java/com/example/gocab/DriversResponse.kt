package com.example.gocab.network

import com.example.gocab.DriverMaintenance
import com.google.gson.annotations.SerializedName

data class DriversResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("drivers")
    val drivers: List<DriverMaintenance>
)
