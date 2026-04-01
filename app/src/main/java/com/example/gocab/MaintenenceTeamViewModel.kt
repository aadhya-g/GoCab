import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gocab.MaintenanceProfileData
import com.example.gocab.network.RetrofitInstance
import kotlinx.coroutines.launch


class MaintenanceProfileViewModel : ViewModel() {

    var profileData by mutableStateOf<MaintenanceProfileData?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    fun fetchProfile(uid: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = RetrofitInstance.api.getMaintenanceProfile(uid)
                if (res.isSuccessful && res.body()?.success == true) {
                    profileData = res.body()?.data
                } else {
                    errorMessage = res.body()?.message ?: "Something went wrong"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Network error"
            } finally {
                isLoading = false
            }
        }
    }
}
