package com.example.gocab.network

import com.example.gocab.AdminRideResponse
import com.example.gocab.MaintenanceProfileResponse
import com.example.gocab.StudentsResponse
import com.example.gocab.model.AdminStudentResponse
import com.example.gocab.model.RideRequest
import com.example.gocab.model.SearchRideResponse
import com.example.gocab.model.UpcomingRideResponse
import com.example.gocab.ui.DataModels.DriverRideResponse
import com.example.gocab.ui.DataModels.RatingRequest
import com.example.gocab.ui.DataModels.RideHistoryResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// --- Data Models ---

data class UserRequest(
    val firebase_uid: String,
    val email_id: String,
    val user_type: String
)
data class ApiResponse(
    val success: Boolean,
    val message: String
)
// ✅ Corrected model for getrole endpoint
data class UserRoleResponse(
    val success: Boolean,
    val user_type: String?
)
data class DriverResponse(
    val success: Boolean,
    val data: DriverData
)

data class DriverProfileResponse(
    val success: Boolean,
    val data: DriverData?
)


// --- API Interface ---
interface ApiService {
    // ✅ GET student profile (MATCH BACKEND)
    @GET("/getStudentDetails/{firebase_uid}")
    suspend fun getStudentProfile(
        @Path("firebase_uid") firebaseUid: String
    ): Response<StudentProfileResponse>

    // ✅ ADD student (MATCH BACKEND)
    @POST("/api/student/add")
    suspend fun addStudent(
        @Body request: StudentRequest
    ): Response<ApiResponse>
    // ✅ UPDATE student (MATCH BACKEND)
    @PUT("/updateStudentProfile")
    suspend fun updateStudentProfile(
        @Body request: StudentUpdateRequest
    ): Response<ApiResponse>

    @POST("api/user/register")
    suspend fun registerUser(@Body user: UserRequest): Response<ApiResponse>


    // ✅ Get user role by Firebase UID
    @POST("api/user/getrole")
    suspend fun getUserRole(@Body request: Map<String, String>): Response<UserRoleResponse>

    @POST("/api/driver/add-with-car")
    suspend fun addDriverWithCar(
        @Body request: DriverWithCarRequest
    ): Response<ApiResponse>

    // ✅ Fetch driver profile
    @GET("/driver/profile/full/{firebase_uid}")
    suspend fun getDriverProfile(
        @Path("firebase_uid") firebase_uid: String
    ): Response<DriverProfileResponse>

    @PUT("driver/profile/{firebase_uid}")
    suspend fun updateDriverProfile(
        @Path("firebase_uid") firebaseUid: String,
        @Body request: DriverUpdateRequest
    ): Response<ApiResponse>
    @GET("/api/maintenance/students")
    suspend fun getAllStudents(): StudentsResponse

    @GET("/api/maintenance/drivers")
    suspend fun getAllDrivers(): DriversResponse

    // ================= MaintenanceTeam =================
    @GET("maintenance/profile/{uid}")
    suspend fun getMaintenanceProfile(
        @Path("uid") uid: String
    ): Response<MaintenanceProfileResponse>

    @GET("admin/students")
    suspend fun getAdminStudents(
        @Query("domain") domain: String,
        @Query("search") search: String
    ): Response<AdminStudentResponse>
    //apiservices

    @GET("admin/drivers")
    suspend fun getAdminDrivers(
        @Query("search") search: String
    ): Response<AdminDriverResponse>//apiservices

    @POST("api/ride/request")
    suspend fun createRide(
        @Body request: RideRequest
    ): Response<ApiResponse>

    @GET("api/driver/confirmed-rides/{driverId}")
    suspend fun getConfirmedRides(
        @Path("driverId") driverId: String
    ): Response<List<DriverRideResponse>>

    @GET("api/driver/details/{email}")
    suspend fun getDriverDetails(
        @Path("email") email: String
    ): Response<DriverWithCarResponse>

    @GET("api/student/upcoming-rides/{email}")
    suspend fun getUpcomingRides(
        @Path("email") email: String
    ): Response<UpcomingRideResponse>


    @POST("api/admin/verify-driver")
    suspend fun verifyDriver(
        @Body body: Map<String, String>
    ): Response<Unit>

    @POST("api/admin/reject-driver")
    suspend fun rejectDriver(
        @Body body: Map<String, String>
    ): Response<Unit>

    @PUT("startRide/{rideId}")
    suspend fun startRide(
        @Path("rideId") rideId: Int
    ): Response<Map<String, String>>

    @PUT("endRide/{rideId}")
    suspend fun endRide(
        @Path("rideId") rideId: Int
    ): Call<Map<String, String>>

    @GET("ride-history/{email}")
    suspend fun getRideHistory(
        @Path("email") email: String
    ): RideHistoryResponse

    @POST("rate-driver")
    suspend fun rateDriver(@Body request: RatingRequest): Response<Unit>


    @POST("/api/search-rides-v2")
    suspend fun searchRidesV2(
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<SearchRideResponse>

    @GET("admin/todays-rides")
    suspend fun getTodaysRides(
        @Query("email") email: String
    ): AdminRideResponse


}

