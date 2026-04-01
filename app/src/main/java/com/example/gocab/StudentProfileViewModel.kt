package com.example.gocab.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.network.StudentProfileData
import com.example.gocab.network.StudentUpdateRequest
import kotlinx.coroutines.launch


class StudentProfileViewModel : ViewModel() {
    var message by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var successMessage by mutableStateOf("")
    var studentData by mutableStateOf<StudentProfileData?>(null)
    //var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
    // =================================================
    // 📥 FETCH STUDENT PROFILE
    // =================================================

    fun fetchStudentProfile(uid: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.e("PROFILE_DEBUG", "fetchStudentProfile CALLED")
                val res = RetrofitInstance.api.getStudentProfile(uid)

                // Log.e("STUDENT_API", "URL HIT = getStudentProfile")
                Log.e("STUDENT_API", "HTTP CODE = ${res.code()}")
                // Log.e("STUDENT_API", "SUCCESS = ${res.isSuccessful}")
                Log.e("STUDENT_API", "BODY = ${res.body()}")
                Log.e("STUDENT_API", "ERROR = ${res.errorBody()?.string()}")

                if (res.isSuccessful && res.body()?.success == true) {
                    studentData = res.body()!!.data   // ✅ SET DATA
                    errorMessage = ""
                } else {
                    errorMessage =
                        "HTTP ${res.code()} : ${res.errorBody()?.string() ?: "Unknown error"}"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Exception"
                Log.e("STUDENT_API", "EXCEPTION", e)
            } finally {
                isLoading = false
            }
        }
    }

    // =================================================
    // ✏️ UPDATE STUDENT PROFILE (LIKE DRIVER)
    // =================================================
    fun updateStudentProfile(uid: String, request: StudentUpdateRequest) {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.e("STUDENT_API", "UPDATE REQUEST = $request")

                val res = RetrofitInstance.api.updateStudentProfile(request)

                Log.e("STUDENT_API", "UPDATE CODE = ${res.code()}")
                Log.e("STUDENT_API", "UPDATE BODY = ${res.body()}")
                Log.e("STUDENT_API", "UPDATE ERROR = ${res.errorBody()?.string()}")

                if (res.isSuccessful && res.body()?.success == true) {
                    message = "success"
                    fetchStudentProfile(uid)
                } else {
                    errorMessage =
                        res.body()?.message
                            ?: "HTTP ${res.code()} : ${res.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Exception"
                Log.e("STUDENT_API", "UPDATE EXCEPTION", e)
            } finally {
                isLoading = false
            }
        }
    }
}

    /*
        fun updateStudentProfile(uid: String, request: StudentUpdateRequest) {
            viewModelScope.launch {
                isLoading = true
                updateState = UpdateState.Loading

                try {
                    val res = RetrofitInstance.api.updateStudentProfile(request)

                    if (res.isSuccessful && res.body()?.success == true) {
                        message = "success" // 👈 ADD THIS
                        updateState = UpdateState.Success
                        fetchStudentProfile(uid)
                    } else {
                        updateState = UpdateState.Error("Update failed")
                    }

                } catch (e: Exception) {
                    updateState = UpdateState.Error(e.message ?: "Error")
                } finally {
                    isLoading = false
                }
            }
        }
        */

/*sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val msg: String) : UpdateState()
}*/

