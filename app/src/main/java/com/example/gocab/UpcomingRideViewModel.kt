//UpcomingRideViewModel.kt
package com.example.gocab.viewmodel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.StudentUpcomingRide
import com.example.gocab.network.ApiClient
import kotlinx.coroutines.launch

class UpcomingRideViewModel : ViewModel() {

    var rides by mutableStateOf<List<StudentUpcomingRide>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadRides(studentEmail: String) {
        viewModelScope.launch {
            isLoading = true
            error = null

            try {
                val response = ApiClient.instance
                    .getUpcomingRides(studentEmail)

                if (response.isSuccessful && response.body()?.success == true) {
                    rides = response.body()!!.rides
                } else {
                    error = "No rides found"
                }

            } catch (e: Exception) {
                error = e.localizedMessage
            }

            isLoading = false
        }
    }
}