package com.example.gocab.network
data class StudentRequest(
    val firebase_uid: String,
    val S_email_id: String,
    val S_name: String,
    val Smartcard_id: String,
    val dateofbirth: String,
    val gender: String,
    val aadhar_number: String,
    val course: String,
    val branch: String,
    val year: String,
    val Permanent_address: String,
    val hostel: String,
    // 🆕 Guardian details
    val G_name: String,
    val G_phone_no: String,
    val G_eid: String,
    val College_name: String
)
data class StudentProfileResponse(
    val success: Boolean,
    val data: StudentProfileData,
    val message: String?
)

data class StudentProfileData(
    val firebase_uid: String,
    val S_email_id: String,
    val S_name: String,
    val Smartcard_id: String,
    val College_name: String,
    val dateofbirth: String,
    val gender: String,
    val aadhar_number: String,
    val course: String,
    val branch: String,
    val year: String,
    val Permanent_address: String,
    val hostel: String,

    // 👇 ADD THESE
    val guardian_name: String?,
    val guardian_phone: String?,
    val guardian_email: String?
)

data class StudentUpdateRequest(
    val firebase_uid: String,
    val course: String,
    val branch: String,
    val year: String,
    val Permanent_address: String,
    val hostel: String,
    val G_name: String,
    val G_phone_no: String,   // 🔥 CHANGE
    val G_eid: String         // 🔥 CHANGE
)


