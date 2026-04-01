
package com.example.gocab

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.network.DriverWithCarRequest
import com.example.gocab.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPersonalDetailsScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- Personal Details States ---
    var fullName by remember { mutableStateOf("") }
    val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var nameError by remember { mutableStateOf<String?>(null) }
    var dob by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var aadhaarError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var currentCity by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var licenceError by remember { mutableStateOf<String?>(null) }
    var driverStatus by remember { mutableStateOf("Available") } //  ADDED: Default value
    var costPerKm by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    // --- Car Details States ---
    var carNumber by remember { mutableStateOf("") }
    var carName by remember { mutableStateOf("") } //  ADDED
    var carModel by remember { mutableStateOf("") } //  ADDED
    var carColour by remember { mutableStateOf("") } // ADDED
    var carSeater by remember { mutableStateOf("") } // ADDED
    var carAcStatus by remember { mutableStateOf("") } //  ADDED
    var carAcExpanded by remember { mutableStateOf(false) } //  ADDED
    var carCarrier by remember { mutableStateOf("") } //  ADDED
    var carCarrierExpanded by remember { mutableStateOf(false) } //  ADDED
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img13),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Driver & Car Details", //  MODIFIED
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete your profile to start driving", //  MODIFIED
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- Personal Details ---
                    Text("Personal Details", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    /*DriverTextField (value = fullName, label = "Full Name") { fullName = it }*/
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            nameError = DvalidateName(it)
                        },
                        label = { Text("Full Name") },
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (nameError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = nameError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // DriverTextField(value = email, label = "Email ID") { email = it }

                    OutlinedTextField(
                        value = firebaseEmail,
                        onValueChange = {},
                        label = { Text("Email ID") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White.copy(alpha = 0f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0f)
                        )
                    )

                    DriverTextField(value =dob, label = "Date of Birth (YYYY-MM-DD)",keyboardType = KeyboardType.Phone) { dob = it }

                    // Gender Dropdown
                    GenderDropdown(
                        gender = gender,
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                        onGenderSelect = { gender = it }
                    )

                    /*DriverTextField(value = aadhar, label = "Aadhar Number", keyboardType = KeyboardType.Number) { aadhar = it }*/
                    OutlinedTextField(
                        value = aadhaar,
                        onValueChange = {
                            if (it.length <= 12 && it.all { ch -> ch.isDigit() }) {
                                aadhaar = it
                                aadhaarError = validateAadhaar(it)
                            }
                        },
                        label = { Text("Aadhaar Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = aadhaarError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (aadhaarError != null) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = aadhaarError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    DriverTextField(value = phoneNo, label = "Phone Number", keyboardType = KeyboardType.Phone) { phoneNo = it }
                    DriverTextField(value = address, label = "Permanent Address") { address = it }
                    DriverTextField(value = currentCity, label = "Current City") { currentCity = it }
                   /* DriverTextField(value = licenseNumber, label = "License Number") { licenseNumber = it }*/
                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = {
                            licenseNumber = it.uppercase() //  optional: auto uppercase
                            licenceError = validateLicenceNumber(it)
                        },
                        label = { Text("Driving Licence Number") },
                        isError = licenceError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (licenceError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = licenceError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }

                    DriverTextField(value = costPerKm, label = "Cost per Km", keyboardType = KeyboardType.Number) { costPerKm = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Car Details ---
                    Text("Car Details", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    DriverTextField(value = carNumber, label = "Car Number (e.g., RJ20-CB-1234)") { carNumber = it }
                    DriverTextField(value = carName, label = "Car Name (e.g., My Swift)") { carName = it } // ADDED
                    DriverTextField(value = carModel, label = "Car Model (e.g., Maruti Swift 2022)") { carModel = it } //  ADDED
                    DriverTextField(value = carColour, label = "Car Colour") { carColour = it } //  ADDED
                    DriverTextField(value = carSeater, label = "Seater (e.g., 4)", keyboardType = KeyboardType.Number) { carSeater = it } // ✅ ADDED

                    // AC / Non-AC Dropdown //  ADDED
                    CarOptionDropdown(
                        label = "AC / Non-AC",
                        options = listOf("AC", "non-AC"),
                        selectedOption = carAcStatus,
                        expanded = carAcExpanded,
                        onExpandedChange = { carAcExpanded = it },
                        onOptionSelect = { carAcStatus = it }
                    )

                    // Carrier Dropdown // ✅ ADDED
                    CarOptionDropdown(
                        label = "Roof Carrier Available",
                        options = listOf("Yes", "No"),
                        selectedOption = carCarrier,
                        expanded = carCarrierExpanded,
                        onExpandedChange = { carCarrierExpanded = it },
                        onOptionSelect = { carCarrier = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔄 MODIFIED Button Logic
                    Button(
                        onClick = {
                            val nameValidation = DvalidateName(fullName)
                            if (nameValidation != null) {
                                Toast.makeText(context, nameValidation, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val aadhaarValidation = DvalidateAadhaar(aadhaar)
                            if (aadhaarValidation != null) {
                                Toast.makeText(context, aadhaarValidation, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val licenceValidation = validateLicenceNumber(licenseNumber)
                            if (licenceValidation != null) {
                                Toast.makeText(context, licenceValidation, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            //  BASIC VALIDATION (DRIVER + CAR)
                            if (
                                fullName.isBlank() ||
                                gender.isBlank() ||
                                aadhaar.isBlank() ||
                                phoneNo.isBlank() ||
                                licenseNumber.isBlank() ||
                                carName.isBlank() ||
                                carNumber.isBlank() ||
                                carModel.isBlank() ||
                                carColour.isBlank() ||
                                carAcStatus.isBlank() ||
                                carCarrier.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Fill all Driver & Car details",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                                ?: return@Button

                            val driverEmail = firebaseUser.email ?: return@Button
                            val userId = firebaseUser.uid
                            val request = DriverWithCarRequest(
                                firebase_uid = userId,
                                D_eid = driverEmail,
                                D_name = fullName,
                                D_aadhar_no = aadhaar,
                                D_phone_no = phoneNo,
                                D_address = address,
                                D_licence_no = licenseNumber,
                                D_status = "Available",
                                D_gender = gender,
                                cost_per_km = costPerKm.toDoubleOrNull() ?: 10.0,
                                current_city = currentCity.ifBlank { "Unknown" },
                                D_dob = dob,

                                C_id = UUID.randomUUID().toString(),
                                C_name = carName,
                                C_number = carNumber,
                                C_colour = carColour,
                                C_model = carModel,
                                C_ac_nac = carAcStatus,
                                C_seater = carSeater.toIntOrNull() ?: 4,
                                C_carrier = carCarrier
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                val response = RetrofitInstance.api.addDriverWithCar(request)

                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Driver & Car saved!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onFinished()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            response.errorBody()?.string(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    )

                    {

                        Text("Save & Continue", color = Color.White)
                        // Spacer(modifier = Modifier.width(8.dp))

                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

fun DvalidateName(name: String): String? {
    if (name.isBlank()) {
        return "Name cannot be empty"
    }
    if (!name.all { it.isLetter() || it.isWhitespace() }) {
        return "Name should contain only letters"
    }
    return null
}
fun DvalidateAadhaar(aadhaar: String): String? {
    val regex = Regex("^[0-9]{12}$")
    return if (!regex.matches(aadhaar)) {
        "Please enter valid Aadhaar number"
    } else null
}

fun validateLicenceNumber(licence: String): String? {
    val regex = Regex("^[A-Za-z]{2}[0-9]{13}$")
    return if (!regex.matches(licence)) {
        "Please enter valid licence number"
    } else null
}

@Composable
fun DriverTextField(
    value: String,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF000000),
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color(0xFF000000),
            unfocusedLabelColor = Color.DarkGray,
            cursorColor = Color(0xFF000000),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White.copy(alpha=0f), //  ADDED
            unfocusedContainerColor = Color.White.copy(alpha=0f) //  ADDED
        )
    )
}

// Gender Dropdown (Refactored)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    gender: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenderSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = gender,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            placeholder = { Text("Select Gender", color = Color.Gray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = TextStyle(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF000000),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF000000),
                unfocusedLabelColor = Color.Black,
                cursorColor = Color(0xFF000000),
                focusedContainerColor = Color.White.copy(alpha=0f),
                unfocusedContainerColor = Color.White.copy(alpha=0f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(text = { Text("Male") }, onClick = { onGenderSelect("Male"); onExpandedChange(false) })
            DropdownMenuItem(text = { Text("Female") }, onClick = { onGenderSelect("Female"); onExpandedChange(false) })
            DropdownMenuItem(text = { Text("Other") }, onClick = { onGenderSelect("Other"); onExpandedChange(false) })
        }
    }
}

//  ADDED: Reusable Dropdown for Car options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarOptionDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select", color = Color.Gray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = TextStyle(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF000000),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF000000),
                unfocusedLabelColor = Color.Black,
                cursorColor = Color(0xFF000000),
                focusedContainerColor = Color.White.copy(alpha=0f),
                unfocusedContainerColor = Color.White.copy(alpha=0f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelect(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}