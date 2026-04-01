
package com.example.gocab

data class VerifyDriverRequest(
    val D_eid: String,
    val college_name: String,
    val admin_email: String,
    val verification_status: String
)

data class VerifyDriverResponse(
    val success: Boolean,
    val message: String
)