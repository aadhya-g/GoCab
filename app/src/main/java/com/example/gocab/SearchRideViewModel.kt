package com.example.gocab.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.Driver
import com.example.gocab.network.RetrofitInstance.api
import com.example.gocab.util.FilterData
import kotlinx.coroutines.launch

class SearchRideViewModel : ViewModel() {
    var drivers by mutableStateOf<List<Driver>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    fun searchRide(pickup: String, drop: String, filters: FilterData) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val request = mapOf(
                    "pickup" to pickup,
                    "drop" to drop,
                    "rating" to filters.rating,
                    "costOrder" to filters.costOrder,
                    "acType" to filters.acType,
                    "seats" to filters.seats,
                    "carType" to filters.carType
                )
                val response = api.searchRidesV2(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    drivers = response.body()!!.drivers
                } else {
                    error = "No drivers found"
                }
            } catch (e: Exception) {
                error = e.localizedMessage
            }
            isLoading = false
        }
    }
}
