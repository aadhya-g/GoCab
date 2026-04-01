package com.example.gocab.network
data class DriverWithCarRequest(
    // DRIVER
    val firebase_uid: String,
    val D_eid: String,
    val D_name: String,
    val D_aadhar_no: String,
    val D_phone_no: String,
    val D_address: String,
    val D_licence_no: String,
    val D_status: String,
    val D_gender: String,
    val cost_per_km: Double,
    val current_city: String,
    val D_dob: String,
    // CAR
    val C_id: String,
    val C_name: String,
    val C_number: String,
    val C_colour: String,
    val C_model: String,
    val C_ac_nac: String,
    val C_seater: Int,
    val C_carrier: String
)

data class DriverData(
    val firebase_uid: String,
    val D_eid: String,
    val D_name: String,
    val D_aadhar_no: String?,
    val D_phone_no: String?,
    val D_address: String?,
    val D_licence_no: String,
    val D_status: String,
    val D_avg_rating: Double?,
    val D_gender: String?,
    val cost_per_km: Double,
    val current_city: String,
    val D_dob: String,
    // 🚗 NESTED CAR (THIS IS THE FIX)
    val car: CarData?
)


data class DriverUpdateRequest(
    val firebase_uid: String,
    val D_address: String?,
    val D_status: String,
    val cost_per_km: Double,
    val D_phone_no: String,
    val current_city: String,
    // 🚗 CAR UPDATE
    val C_name: String?,
    val C_number: String?,
    val C_colour: String?,
    val C_model: String?,
    val C_ac_nac: String?,
    val C_seater: Int?,
    val C_carrier: String?
)

data class CarData(
    val C_name: String?,
    val C_number: String?,
    val C_colour: String?,
    val C_model: String?,
    val C_ac_nac: String?,
    val C_seater: Int?,
    val C_carrier: String?
)

data class DriverWithCarResponse(
    val D_name: String,
    val D_phone_no: String,
    val D_avg_rating: Double,
    val cost_per_km: Double,
    val C_id: String?,
    val C_name: String?,
    val C_number: String?,
    val C_seater: Int?
)