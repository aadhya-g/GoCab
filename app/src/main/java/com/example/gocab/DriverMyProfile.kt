package com.example.gocab
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.network.DriverUpdateRequest
import com.example.gocab.viewmodel.DriverProfileViewModel

// --- Custom Colors ---
private val CardBackgroundColor = Color(0xFFFFFFFF)
private val SectionIconColor = Color(0xFF3F51B5)
private val TextColorPrimary = Color(0xFF212121)
private val TextColorSecondary = Color(0xFF757575)
private val LogoutButtonColor = Color(0xFFD32F2F)
private val HeaderCardColor = Color.White.copy(alpha = 0.8f)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(
    firebase_uid: String,
    viewModel: DriverProfileViewModel = viewModel(),
    onLogout: () -> Unit,
    isEditing: Boolean,                 // ✅ ADD
    onEditToggle: (Boolean) -> Unit     // ✅ ADD
) {
    val context = LocalContext.current
    val driver = viewModel.driverData.value
    val message = viewModel.message.value

    //var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // --- All Editable States ---
    val phoneState = remember { mutableStateOf("") }
    val addressState = remember { mutableStateOf("") }
    val costState = remember { mutableStateOf("") }
    val currentCityState = remember { mutableStateOf("") }
    val carNameState = remember { mutableStateOf("") }
    val carNoState = remember { mutableStateOf("") }
    val carColorState = remember { mutableStateOf("") }
    val carModelState = remember { mutableStateOf("") }
    val carSeaterState = remember { mutableStateOf("") }
    val carAcState = remember { mutableStateOf("") }
    val carCarrierState = remember { mutableStateOf("") }
    val statusState = remember { mutableStateOf("Active") }

    // Fetch Profile
    LaunchedEffect(Unit) { viewModel.fetchDriverProfile(firebase_uid) }

    // Sync States when data arrives
    LaunchedEffect(driver) {
        driver?.let {

            // DRIVER FIELDS
            phoneState.value = it.D_phone_no ?: ""
            addressState.value = it.D_address ?: ""
            currentCityState.value = it.current_city
            costState.value = it.cost_per_km.toString()
            statusState.value = it.D_status

            // 🚗 CAR FIELDS (THIS IS THE FIX 🔥)
            carNameState.value = it.car?.C_name ?: ""
            carModelState.value = it.car?.C_model ?: ""
            carNoState.value = it.car?.C_number ?: ""
            carColorState.value = it.car?.C_colour ?: ""
            carSeaterState.value = it.car?.C_seater?.toString() ?: ""
            carAcState.value = it.car?.C_ac_nac ?: ""
            carCarrierState.value = it.car?.C_carrier ?: ""
        }
    }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            isLoading = false
            if (message.contains("success", true)) {
                onEditToggle(false)
                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
            // Background Layer
            Image(
                painter = painterResource(id = R.drawable.img10),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.70f),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )

            if (driver == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    // Profile Header Section
                    ProfileHeader(name = driver.D_name ?: "Driver", email = driver.D_eid ?: "")

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Personal Fixed Details
                    InfoCard(title = "Personal Info", icon = Icons.Default.Person) {
                       // DetailRow("Date of Birth", driver.D_dob)
                        DetailRow("Gender", driver.D_gender ?: "N/A")
                        DetailRow("Aadhaar", driver.D_aadhar_no ?: "N/A")
                        DetailRow(
                            label = "Average Rating",
                            value = driver.D_avg_rating?.let { "⭐ $it / 5" } ?: "Not Rated"
                        )
                    }

                    // 2. Contact Details (Editable)
                    EditableCard(title = "Contact & Location", icon = Icons.Default.ContactMail, isEditing = isEditing) {
                        EditField("Phone", phoneState, isEditing, KeyboardType.Phone)
                        EditField("Current City", currentCityState, isEditing)
                        EditField("Full Address", addressState, isEditing)
                    }
                    // 3. Vehicle Details (Editable)
                    EditableCard(title = "Vehicle Information", icon = Icons.Default.DirectionsCar, isEditing = isEditing) {
                        EditField("Car Name", carNameState, isEditing)
                        EditField("Car Model", carModelState, isEditing)
                        EditField("Car Number", carNoState, isEditing)
                        EditField("Car Colour", carColorState, isEditing)
                        EditField("Seater (Capacity)", carSeaterState, isEditing, KeyboardType.Number)
                        EditField("AC/Non-AC", carAcState, isEditing)
                        EditField("Carrier (Yes/No)", carCarrierState, isEditing)
                    }
                    // 4. Status & Pricing
                    EditableCard(title = "Status & Pricing", icon = Icons.Default.AttachMoney, isEditing = isEditing) {
                        EditField("Cost per Km", costState, isEditing, KeyboardType.Decimal)
                        DetailRow("License", driver.D_licence_no ?: "N/A")
                        // Status Toggle in Edit Mode
                        if (isEditing) {
                            StatusDropdown(statusState)
                        } else {
                            DetailRow("Status", statusState.value)
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    if (isEditing) {
                        Button(
                            onClick = {
                                isLoading = true

                                val request = DriverUpdateRequest(
                                    firebase_uid = firebase_uid,
                                    D_phone_no = phoneState.value,
                                    D_address = addressState.value,
                                    D_status = statusState.value,
                                    cost_per_km = costState.value.toDoubleOrNull() ?: 0.0,
                                    current_city = currentCityState.value,   // ✅ FIXED
                                    C_name = carNameState.value,
                                    C_number = carNoState.value,
                                    C_colour = carColorState.value,
                                    C_model = carModelState.value,
                                    C_ac_nac = carAcState.value,
                                    C_seater = carSeaterState.value.toIntOrNull() ?: 4,
                                    C_carrier = carCarrierState.value
                                )
                                viewModel.updateDriverProfile(firebase_uid, request)
                            },
                            modifier = Modifier.fillMaxWidth().height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }


// --- UI Components ---

@Composable
fun ProfileHeader(name: String, email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF2196F3)))),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(email, fontSize = 19.sp, color = Color.White)
    }
}

@Composable
fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        //elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            content()
        }
    }
}

@Composable
fun EditableCard(title: String, icon: ImageVector, isEditing: Boolean, content: @Composable ColumnScope.() -> Unit) {
    InfoCard(title, icon, content)
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun EditField(label: String, state: MutableState<String>, isEditing: Boolean, keyboardType: KeyboardType = KeyboardType.Text) {
    if (isEditing) {
        OutlinedTextField(
            value = state.value,
            onValueChange = { state.value = it },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    } else {
        DetailRow(label, state.value)
    }
}

@Composable
fun StatusDropdown(statusState: MutableState<String>) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Active", "Inactive")

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = statusState.value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Driver Status") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    statusState.value = option
                    expanded = false
                })
            }
        }
    }
}

