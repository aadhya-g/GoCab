//AdminDriverViewModel
package com.example.gocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.AdminDriver
import com.example.gocab.network.ApiClient
import com.example.gocab.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDriversViewModel : ViewModel() {

    private val _drivers = MutableStateFlow<List<AdminDriver>>(emptyList())
    val drivers: StateFlow<List<AdminDriver>> = _drivers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // ✅ Load Drivers
    fun loadDrivers(search: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.api.getAdminDrivers(search)
                if (response.isSuccessful && response.body()?.success == true) {
                    _drivers.value = response.body()!!.drivers
                }
            } finally {
                _loading.value = false
            }
        }
    }

    // ✅ Verify Driver
    fun verifyDriver(driverEmail: String) {
        viewModelScope.launch {
            try {
                val collegeEmail = FirebaseAuth.getInstance().currentUser?.email
                    ?: return@launch

                val response = ApiClient.instance.verifyDriver(
                    mapOf(
                        "driverEmail" to driverEmail,
                        "collegeId" to collegeEmail
                    )
                )

                if (response.isSuccessful) {
                    _drivers.value = _drivers.value.filter {
                        it.D_eid != driverEmail
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Reject Driver
    fun rejectDriver(driverEmail: String) {
        viewModelScope.launch {
            try {
                val collegeEmail = FirebaseAuth.getInstance().currentUser?.email
                    ?: return@launch

                val response = ApiClient.instance.rejectDriver(
                    mapOf(
                        "driverEmail" to driverEmail,
                        "collegeId" to collegeEmail
                    )
                )

                if (response.isSuccessful) {
                    _drivers.value = _drivers.value.filter {
                        it.D_eid != driverEmail
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


/*package com.example.gocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.AdminDriver
import com.example.gocab.network.ApiClient
import com.example.gocab.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDriversViewModel : ViewModel() {

    private val _drivers = MutableStateFlow<List<AdminDriver>>(emptyList())
    val drivers: StateFlow<List<AdminDriver>> = _drivers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // ✅ Load Drivers
    fun loadDrivers(search: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.api.getAdminDrivers(search)
                if (response.isSuccessful && response.body()?.success == true) {
                    _drivers.value = response.body()!!.drivers
                }
            } finally {
                _loading.value = false
            }
        }
    }

    // ✅ Verify Driver
    fun verifyDriver(driverEmail: String) {
        viewModelScope.launch {
            try {
                val collegeEmail = FirebaseAuth.getInstance().currentUser?.email
                    ?: return@launch

                val response = ApiClient.instance.verifyDriver(
                    mapOf(
                        "driverEmail" to driverEmail,
                        "collegeId" to collegeEmail
                    )
                )

                if (response.isSuccessful) {
                    _drivers.value = _drivers.value.filter {
                        it.D_eid != driverEmail
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Reject Driver
    fun rejectDriver(driverEmail: String) {
        viewModelScope.launch {
            try {
                val collegeEmail = FirebaseAuth.getInstance().currentUser?.email
                    ?: return@launch

                val response = ApiClient.instance.rejectDriver(
                    mapOf(
                        "driverEmail" to driverEmail,
                        "collegeId" to collegeEmail
                    )
                )

                if (response.isSuccessful) {
                    _drivers.value = _drivers.value.filter {
                        it.D_eid != driverEmail
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}*/




/*
package com.example.gocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.AdminDriver
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDriversViewModel : ViewModel() {

    private val _drivers = MutableStateFlow<List<AdminDriver>>(emptyList())
    val drivers: StateFlow<List<AdminDriver>> = _drivers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadDrivers(search: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.api.getAdminDrivers(search)
                if (response.isSuccessful && response.body()?.success == true) {
                    _drivers.value = response.body()!!.drivers
                }
            } finally {
                _loading.value = false
            }
        }
    }
}
//AdminDriverViewModel.kt*/
