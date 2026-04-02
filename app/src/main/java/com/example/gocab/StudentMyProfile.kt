package com.example.gocab.ui.student
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.AppScaffold
import com.example.gocab.DetailRow
import com.example.gocab.EditField
import com.example.gocab.EditableCard
import com.example.gocab.InfoCard
import com.example.gocab.R
import com.example.gocab.network.StudentUpdateRequest
import com.example.gocab.viewmodel.StudentProfileViewModel

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"

    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val date = input.parse(dateString)
        output.format(date!!)
    } catch (e: Exception) {
        "N/A"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMyProfile(
    firebaseUid: String,
    isEditing: Boolean,
    onEditToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,

    // 👇 ADD THESE
    onProfile: () -> Unit,
    onHistory: () -> Unit,
    onScheduledRides: () -> Unit,

    viewModel: StudentProfileViewModel = viewModel(key = "student_profile_vm")
) {
    BackHandler { onBack()              // 👈 previous screen
    }
    val context = LocalContext.current
    val student = viewModel.studentData
    // Editable states
    val courseState = remember { mutableStateOf("") }
    val branchState = remember { mutableStateOf("") }
    val yearState = remember { mutableStateOf("") }
    val addressState = remember { mutableStateOf("") }
    val hostelState = remember { mutableStateOf("") }
    val guardianPhoneState = remember { mutableStateOf("") }
    val guardianEmailState = remember { mutableStateOf("") }

    LaunchedEffect(firebaseUid) {
        if (firebaseUid.isNotBlank()) {
            viewModel.fetchStudentProfile(firebaseUid)
        }
    }
    LaunchedEffect(viewModel.message) {
        if (viewModel.message == "success") {

            // 1️⃣ Edit mode off
            onEditToggle(false)

            // 2️⃣ Toast
            Toast.makeText(
                context,
                "Profile Updated!",
                Toast.LENGTH_SHORT
            ).show()
            // 4️⃣ Reset message (VERY IMPORTANT)
            viewModel.message = null
        }
    }
    // Sync states
    LaunchedEffect(student) {
        student?.let {
            courseState.value = it.course ?: ""
            branchState.value = it.branch ?: ""
            yearState.value = it.year ?: ""
            addressState.value = it.Permanent_address ?: ""
            hostelState.value = it.hostel ?: ""

            guardianPhoneState.value = it.guardian_phone ?: ""
            guardianEmailState.value = it.guardian_email ?: ""
        }
    }

    // 🔥 APP SCAFFOLD YAHAN
    AppScaffold(
        title = "My Profile",
        onLogout = onLogout,

        // 👇 FIX HERE
        onProfile = onProfile,
        onHistory = onHistory,
        onScheduledRides = onScheduledRides,

        actions = {
            Icon(
                imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { onEditToggle(!isEditing) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            //Background Layer
            Image(
                painter = painterResource(id = R.drawable.img11),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.50f),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.10f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (student == null) {
                Text(
                    text = viewModel.errorMessage.ifBlank { "No profile data found" },
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red,
                    fontSize = 16.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp)
                ) {
                    item {

                        ProfileHeader(
                            name = student.S_name ?: "Student",
                            email = student.S_email_id ?: ""
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        EditableCard("Academic Details", Icons.Default.School, isEditing) {
                            DetailRow("College", value = student.College_name)
                            EditField("Course", courseState, isEditing)
                            EditField("Branch", branchState, isEditing)
                            EditField("Year", yearState, isEditing)
                        }
                    }
                    item {
                        InfoCard("Personal Details", Icons.Default.Person) {
                            DetailRow(
                                "DOB",
                                formatDate(student.dateofbirth)
                            )
                            DetailRow("Gender", student.gender)
                            DetailRow("Aadhar", student.aadhar_number ?: "N/A")
                        }
                    }
                    item {
                        EditableCard("Address", Icons.Default.LocationOn, isEditing) {
                            EditField("Permanent Address", addressState, isEditing)
                            EditField("Hostel", hostelState, isEditing)
                        }
                    }
                    if (!student.guardian_name.isNullOrBlank()) {
                        item {
                            EditableCard("Guardian", Icons.Default.People, isEditing) {
                                DetailRow("Name", student.guardian_name)
                                EditField(
                                    "Phone",
                                    guardianPhoneState,
                                    isEditing,
                                    KeyboardType.Phone
                                )
                                EditField(
                                    "Email",
                                    guardianEmailState,
                                    isEditing,
                                    KeyboardType.Email
                                )
                            }
                        }
                    }

                    item {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    val request = StudentUpdateRequest(
                                        firebase_uid = firebaseUid,
                                        course = courseState.value,
                                        branch = branchState.value,
                                        year = yearState.value,
                                        Permanent_address = addressState.value,
                                        hostel = hostelState.value,
                                        G_name = student.guardian_name ?: "NA",
                                        G_phone_no = guardianPhoneState.value.ifBlank { "NA" },
                                        G_eid = guardianEmailState.value.ifBlank { "NA" }
                                    )
                                    viewModel.updateStudentProfile(firebaseUid, request)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF3F51B5
                                    )
                                )
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)

                            }

                        } else {
                            // 🔥 LOGOUT BUTTON (DRIVER JAISA)
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Logout Account",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, email: String) {

    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Column(
        modifier = Modifier
            .fillMaxWidth()               // 🔥 full width
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Box(

            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,

            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = name.ifBlank { "Student" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = email,
            fontSize = 18.sp,
            color = Color.DarkGray ,
            fontWeight = FontWeight.Bold
        )
    }
}
