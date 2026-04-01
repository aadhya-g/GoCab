/*package com.example.gocab.model

data class RideRequest(

    val driverEmail: String,
    val carId: String,          // ✅ REPLACE customerId WITH carId
    val studentEmail: String,
    val pickup: String,
    val drop: String,
    val distanceKm: Double,
    val fare: Double,
    val time: String,
    val date: String
)*/
//RideRequest.kt
package com.example.gocab.model

data class RideRequest(

    val driverEmail: String,
    val carId: String,          // ✅ REPLACE customerId WITH carId
    val studentEmail: String,
    val pickup: String,
    val drop: String,
    val distanceKm: Double,
    val fare: Double,
    val time: String,
    val date: String,
    val pickupCity: String,
    val dropCity: String
)
