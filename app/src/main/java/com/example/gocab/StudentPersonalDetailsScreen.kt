package com.example.gocab

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.network.StudentRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State variables
    var fullName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var smartCardId by remember { mutableStateOf("") }
    var collegeName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var aadhaarError by remember { mutableStateOf<String?>(null) }
    var course by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var hostel by remember { mutableStateOf("") }

    // Guardian details
    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var guardianEmail by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
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
                text = "Enter Personal Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Complete your profile to start booking rides",
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
                    // Student Details
                    Text("Personal Information", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
                    /*CustomTextField(value = fullName, label = "Full Name") { fullName = it }*/
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            nameError = validateName(it)
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

                    /*CustomTextField(value = email, label = "Email ID") { email = it }*/
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email ID") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.DarkGray,
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White.copy(alpha = 0f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0f)
                        )
                    )


                    CustomTextField(value = smartCardId, label = "Smartcard ID") { smartCardId = it }
                    CustomTextField(value = collegeName, label = "College Name") { collegeName = it }
                    CustomTextField(value = dob, label = "Date of Birth (YYYY-MM-DD)" ,keyboardType = KeyboardType.Phone) { dob = it }

                    // Gender Dropdown
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            placeholder = { Text("Select Gender", color = Color.Gray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            textStyle = TextStyle(color = Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF000000),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFF000000),
                                unfocusedLabelColor = Color.Black,
                                cursorColor = Color(0xFF000000),
                                focusedContainerColor = Color.White.copy(alpha = 0f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0f)
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Male") },
                                onClick = {
                                    gender = "Male"
                                    genderExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Female") },
                                onClick = {
                                    gender = "Female"
                                    genderExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Other") },
                                onClick = {
                                    gender = "Other"
                                    genderExpanded = false
                                }
                            )
                        }
                    }

                    /*CustomTextField(value = aadhar, label = "Aadhar Number", keyboardType = KeyboardType.Number) { aadhar = it }*/
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
                    CustomTextField(value = course, label = "Course") { course = it }
                    CustomTextField(value = branch, label = "Branch (if nothing then NA)") { branch = it }
                    CustomTextField(value = year, label = "Current Year Of Degree",keyboardType = KeyboardType.Number) { year = it }
                    CustomTextField(value = address, label = "Permanent Address") { address = it }
                    CustomTextField(value = hostel, label = "Hostel") { hostel = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Guardian Fields
                    Text("Parent / Guardian Information", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
                    CustomTextField(value = guardianName, label = "Guardian Name") { guardianName = it }
                    CustomTextField(value = guardianPhone, label = "Guardian Phone Number", keyboardType = KeyboardType.Phone) { guardianPhone = it }
                    CustomTextField(value = guardianEmail, label = "Guardian Email ID") { guardianEmail = it }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                            if (firebaseUid.isNullOrEmpty()) {
                                Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val aadhaarValidation = validateAadhaar(aadhaar)
                            if (aadhaarValidation != null) {
                                Toast.makeText(context, aadhaarValidation, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val nameValidation = validateName(fullName)
                            if (nameValidation != null) {
                                Toast.makeText(context, nameValidation, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val student = StudentRequest(
                                firebase_uid = firebaseUid,
                                S_email_id = email,
                                S_name = fullName,
                                Smartcard_id = smartCardId,
                                College_name = collegeName,
                                dateofbirth = dob,
                                gender = gender,
                                aadhar_number = aadhaar,
                                course = course,
                                branch = branch,
                                year = year,
                                Permanent_address = address,
                                hostel = hostel,
                                G_name = guardianName,
                                G_phone_no = guardianPhone,
                                G_eid = guardianEmail
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response = RetrofitInstance.api.addStudent(student)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "✅ Student info saved successfully", Toast.LENGTH_SHORT).show()
                                            Prefs.setDetailsFilled(context, true)
                                            onFinished()
                                        } else {
                                            Toast.makeText(context, "❌ Failed: ${response.body()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, "⚠️ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Text("Save & Continue", color = Color.White)
                    }

                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

fun validateName(name: String): String? {
    if (name.isBlank()) {
        return "!Enter Valid Name"
    }
    if (!name.all { it.isLetter() || it.isWhitespace() }) {
        return "!Enter Valid Name"
    }
    return null
}
fun validateAadhaar(aadhaar: String): String? {
    val regex = Regex("^[0-9]{12}$")
    return if (!regex.matches(aadhaar)) {
        "Please enter valid Aadhaar number"
    } else null
}
@Composable
fun CustomTextField(
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
            unfocusedTextColor = Color.Black
        )
    )
}
