//RideSearchResult.kt
package com.example.gocab
data class RideSearchResult(

    val R_id: Int,
    val D_name: String,
    val R_date: String,
    val R_timing: String,
    val distance_km: Double,
    val fare_amount: Double,

    val seats_left: Int,
    val fare_per_student: Double,

    val verifiedCount: Int,

    val colleges: String?,   // ✅ FIXED NAME
    val year: String?,
    val branch: String?,
    val course: String?
)


/*//RideSearchResult.kt
package com.example.gocab
data class RideSearchResult(

    val R_id: Int,
    val D_name: String,
    val R_date: String,
    val R_timing: String,
    val distance_km: Double,
    val fare_amount: Double,

    val seats_left: Int,
    val fare_per_student: Double,

    val verifiedCount: Int,

    val College_name: String?,
    val year: String?,
    val branch: String?,
    val course: String?
)*/
/*data class RideSearchResult(
    val R_id: Int,
    val D_name: String,
    val R_date: String,
    val distance_km: Double,
    val fare_amount: Double,
    val verifiedCount: Int
)*/
