package com.example.gocab

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MaintenanceDriversViewModel : ViewModel() {

    private val _drivers =
        MutableStateFlow<List<DriverMaintenance>>(emptyList())
    val drivers: StateFlow<List<DriverMaintenance>> =
        _drivers.asStateFlow()

    fun fetchDrivers() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllDrivers()
                _drivers.value = response.drivers

                Log.d("MAINT_DRIVERS", response.drivers.toString())
            } catch (e: Exception) {
                _drivers.value = emptyList()
                Log.e("MAINT_DRIVERS", "Error fetching drivers", e)
            }
        }
    }
}
//MaintenanceDriversViewModel