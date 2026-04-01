package com.example.gocab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.ui.DataModels.RatingRequest
import com.example.gocab.ui.DataModels.RideHistory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RideViewModel : ViewModel() {

    var rides by mutableStateOf<List<RideHistory>>(emptyList())


    // 🔥 ADD THIS
    private val api = RetrofitInstance.api
    /*
        fun fetchRideHistory(email: String) {
            viewModelScope.launch {
                try {
                    val response = api.getRideHistory(email)

                    rides = response.filter {
                        it.ride_status.trim().equals("Completed", ignoreCase = true)
                    }


                    println("RIDES = $rides")

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }*/
    fun fetchRideHistory(email: String) {
        viewModelScope.launch {
            try {
                println("EMAIL = $email")

                val response = api.getRideHistory(email)

                // ✅ Access rides inside response
                println("FULL RESPONSE = $response")
                println("RIDES FROM API SIZE = ${response.rides.size}")

                response.rides.forEach {
                    println("STATUS = '${it.ride_status}'")
                }

                // ✅ Apply filter correctly
                rides = response.rides.filter {
                    it.ride_status.trim().equals("Completed", ignoreCase = true)
                }

                println("FINAL RIDES SIZE = ${rides.size}")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitRating(ride: RideHistory, rating: Float) {
        viewModelScope.launch {
            try {
                println("INSIDE VIEWMODEL 🚀")

                val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                println("EMAIL = $email")
                println("DRIVER ID = ${ride.D_eid}")
                val request = RatingRequest(
                    R_id = ride.R_id,
                    email = email,
                    //driverId = ride.D_eid,
                    driverId = ride.D_eid ?: "",
                    rating = rating
                )

                println("REQUEST CREATED = $request")

                val response = RetrofitInstance.api.rateDriver(request)

                println("API CALLED ✅")
                println("API RESPONSE = $response")

            } catch (e: Exception) {
                println("ERROR OCCURRED ❌")
                e.printStackTrace()
            }
        }
    }



}