package com.example.gocab.model

import com.google.gson.annotations.SerializedName

data class Driver(

    @SerializedName("id")
    val driverEmail: String,

    val name: String,
    val city: String,
    val distanceKm: Double,
    val fare: Double,

    @SerializedName("carId")
    val carId: String,

    val verifiedCount: Int = 0,

//    @SerializedName("D_avg_rating")   // 👈 IMPORTANT
//    val rating: Double?    // 👈 ADD THIS
    @SerializedName("rating")   // ✅ ADD THIS LINE
    val rating: Double?         // ✅ NO DEFAULT VALUE

)


/*
package com.example.gocab.model

import com.google.gson.annotations.SerializedName

data class Driver(

    @SerializedName("id")
    val driverEmail: String,

    val name: String,
    val city: String,
    val distanceKm: Double,
    val fare: Double,

    @SerializedName("carId")
    val carId: String   ,   // ✅ ADD THIS
    val verifiedCount: Int = 0
)


*/
