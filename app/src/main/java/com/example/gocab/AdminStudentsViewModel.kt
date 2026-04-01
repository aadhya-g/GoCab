package com.example.gocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.model.AdminStudent
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminStudentsViewModel : ViewModel() {

    private val _students = MutableStateFlow<List<AdminStudent>>(emptyList())
    val students: StateFlow<List<AdminStudent>> = _students

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var domain = ""

    // 1️⃣ Admin email se domain set
    fun setAdminEmail(email: String) {
        domain = email.substringAfter("@")
        loadStudents("")   // first time load all students
    }

    // 2️⃣ Name ya ID se search
    fun loadStudents(search: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitInstance.api.getAdminStudents(domain, search)
                if (response.isSuccessful && response.body()?.success == true) {
                    _students.value = response.body()!!.students
                }
            } finally {
                _loading.value = false
            }
        }
    }
}




//AdminStudentsViewModel.kt