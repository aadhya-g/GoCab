package com.example.gocab.viewmodel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.network.DriverData
import com.example.gocab.network.DriverUpdateRequest
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.launch
class DriverProfileViewModel : ViewModel() {

    private val api = RetrofitInstance.api

    private val _driverData = mutableStateOf<DriverData?>(null)
    val driverData: State<DriverData?> = _driverData

    private val _message = mutableStateOf("")
    val message: State<String> = _message
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

   fun fetchDriverProfile(firebase_uid: String) {
       viewModelScope.launch {
           _loading.value = true
           try {
               val res = api.getDriverProfile(firebase_uid)

               if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                   _driverData.value = res.body()!!.data
                   _message.value = ""
               } else {
                   _message.value = "No profile data found"
               }

           } catch (e: Exception) {
               _message.value = "Error: ${e.message}"
           } finally {
               _loading.value = false
           }
       }
   }
    fun updateDriverProfile(firebase_uid: String, request: DriverUpdateRequest) {
        viewModelScope.launch {
            try {
                val res = api.updateDriverProfile(firebase_uid, request)
                if (res.isSuccessful && res.body()?.success == true) {
                    _message.value = "Profile updated successfully"
                    fetchDriverProfile(firebase_uid) // 🔄 refresh from Azure SQL
                } else {
                    _message.value = res.body()?.message ?: "Update failed"

                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }
}
