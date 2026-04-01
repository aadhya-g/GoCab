package com.example.gocab

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MaintenanceStudentsViewModel<T> : ViewModel() {

    private val _students =
        MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> =
        _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    fun fetchStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getAllStudents()
                _students.value = response.students

                Log.d("MAINT_STUDENTS", response.students.toString())
                _errorMessage.value = ""
            } catch (e: Exception) {
                _students.value = emptyList()
                _errorMessage.value = e.message ?: "Error fetching students"
                Log.e("MAINT_STUDENTS", "Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}