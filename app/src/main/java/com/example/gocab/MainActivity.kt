package com.example.gocab

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.gocab.MainScreenNavigationHelper.currentScreen
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.network.UserRequest
import com.example.gocab.ui.screens.TrackRideScreen
import com.example.gocab.ui.student.StudentMyProfile
import com.example.gocab.ui.theme.GoCabTheme
import com.example.gocab.util.SelectedRideHolder
import com.example.gocab.util.SelectedRideHolder.driverEmail
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        // ✅ ADD THESE TWO LINES
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        setContent {
            GoCabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}


enum class Screen {
    SPLASH,
    ROLE_SELECTION,
    LOGIN,
    SIGNUP,
    PERSONAL_DETAILS,
    BOOK_RIDE,
    HOME,
    STUDENT_PROFILE,
    RIDE_HISTORY,
    SEARCH_RIDE,
    SEARCH_RIDE_J,
    DRIVER_PERSONAL_DETAILS,
    DRIVER_HOME,
    DRIVER_PROFILE,
    RIDE_REQUESTS,
    CONFIRMED_RIDES,
    MONTHLY_EARNINGS,

    MAINTENANCE_HOME,

    COLLEGE_HOME,
    SCHEDULED_RIDES,
    SCHEDULED_RIDE_DETAIL,
    CHAT_SCREEN,
    TRACK_RIDE,
    DRIVER_RIDE_DETAIL,
    HELP
}

val driverScreens = setOf(
    Screen.DRIVER_HOME,
    Screen.DRIVER_PROFILE,
    Screen.RIDE_REQUESTS,
    Screen.CONFIRMED_RIDES,
    Screen.MONTHLY_EARNINGS,
    Screen.DRIVER_RIDE_DETAIL
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val currentScreenState = currentScreen
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var loggedInAdminEmail by remember { mutableStateOf("") }
    var selectedRideId by remember { mutableStateOf<Int?>(null) }
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        delay(3000)
        val user = auth.currentUser
        val role = Prefs.getUserRole(context)
        val detailsFilled = Prefs.isDetailsFilled(context)


        if (user != null && !user.isEmailVerified) {
            FirebaseAuth.getInstance().signOut()
            Prefs.clear(context)
            currentScreenState.value = Screen.LOGIN
            return@LaunchedEffect
        }

        Log.e("NAV_DEBUG", "user=${user?.uid}")
        Log.e("NAV_DEBUG", "role=$role")
        Log.e("NAV_DEBUG", "detailsFilled=$detailsFilled")
        // 🔍 Strict validation
        currentScreenState.value = when {
            user == null -> {
                // No Firebase user → fresh app start
                Screen.ROLE_SELECTION
            }
            role.isNullOrEmpty() -> {
                // Logged in but role missing → reset
                FirebaseAuth.getInstance().signOut()
                Prefs.clear(context)
                Screen.ROLE_SELECTION
            }
            role == "Driver" && !detailsFilled -> Screen.DRIVER_PERSONAL_DETAILS
            role == "Driver" && detailsFilled -> Screen.DRIVER_HOME
            role == "Student" && !detailsFilled -> Screen.PERSONAL_DETAILS
            role == "Student" && detailsFilled -> Screen.HOME
            else -> {
                // Fallback if prefs are corrupted
                FirebaseAuth.getInstance().signOut()
                Prefs.clear(context)
                Screen.ROLE_SELECTION
            }
        }
    }
    when (currentScreenState.value) {
        Screen.SPLASH -> SplashScreen()

        Screen.ROLE_SELECTION -> RoleSelectionScreen { role ->
            currentScreenState.value = when (role) {
                "Student", "Driver" -> Screen.SIGNUP
                "Others" -> Screen.LOGIN
                else -> Screen.LOGIN
            }
        }


        Screen.LOGIN -> LoginScreen(
            onSignUpClicked = { currentScreenState.value = Screen.SIGNUP },
            onLoginSuccess = { email ->   // 👈 email receive karo

                loggedInAdminEmail = email   // 🔥 YAHI sabse important line


                val role = Prefs.getUserRole(context)
                val detailsFilled = Prefs.isDetailsFilled(context)

                currentScreenState.value = when {
                    role == "Driver" && !detailsFilled -> Screen.DRIVER_PERSONAL_DETAILS
                    role == "Driver" && detailsFilled -> Screen.DRIVER_HOME
                    role == "Student" && !detailsFilled -> Screen.PERSONAL_DETAILS
                    role == "Admin" -> Screen.COLLEGE_HOME   // 🔥 admin yahin aayega
                    else -> Screen.HOME
                }
            },
            onHelpClick = {
                currentScreenState.value = Screen.HELP     // 👈 NAVIGATE TO HELP
            }
        )

        Screen.SIGNUP -> SignupScreen(
            onSignupSuccess = {
                Toast.makeText(context, "Please verify your email", Toast.LENGTH_LONG).show()
                currentScreenState.value = Screen.LOGIN
            },
            onBackToLogin = { currentScreenState.value = Screen.LOGIN }
        )

        Screen.PERSONAL_DETAILS -> PersonalDetailsScreen(
            onFinished = {
                Prefs.setDetailsFilled(context, true)
                Toast.makeText(context, "Details saved!", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.HOME
            }
        )

        Screen.STUDENT_PROFILE -> {
            var isStudentEditing by remember { mutableStateOf(false) }

            StudentMyProfile(
                firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                isEditing = isStudentEditing,
                onEditToggle = { isStudentEditing = it },

                onLogout = {
                    auth.signOut()
                    currentScreen.value = Screen.LOGIN
                },

                onBack = {
                    currentScreen.value = Screen.HOME
                },

                // 👇 ADD THESE (IMPORTANT)
                onProfile = { currentScreen.value = Screen.STUDENT_PROFILE },
                onHistory = { currentScreen.value = Screen.RIDE_HISTORY },
                onScheduledRides = { currentScreen.value = Screen.SCHEDULED_RIDES }
            )
        }


        Screen.BOOK_RIDE -> BookRideScreen(
            onInitiateRide = { currentScreenState.value = Screen.SEARCH_RIDE },
            onJoinRide = { currentScreenState.value = Screen.SEARCH_RIDE_J },
            onBackToHome = { currentScreenState.value = Screen.HOME },

            onProfile = { currentScreenState.value = Screen.STUDENT_PROFILE },
            onHistory = { currentScreenState.value = Screen.RIDE_HISTORY },
            onScheduledRides = { currentScreenState.value = Screen.SCHEDULED_RIDES },
            onLogout = { currentScreenState.value = Screen.LOGIN }
        )

        Screen.HOME -> HomeScreen(
            onBookRide = { currentScreenState.value = Screen.BOOK_RIDE },
            onViewRides = { currentScreenState.value = Screen.RIDE_HISTORY },
            onLogout = {
                auth.signOut()
                Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.LOGIN
            },
            onProfile = { currentScreenState.value = Screen.STUDENT_PROFILE },
            onHistory = { currentScreenState.value = Screen.RIDE_HISTORY },
            currentScreen = { currentScreenState.value },
            onScheduledRides = { currentScreenState.value = Screen.SCHEDULED_RIDES      }
        )
        Screen.SCHEDULED_RIDES -> ScheduledRidesScreen1(
            onLogout = {
                auth.signOut()
                Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.LOGIN
            },
            onHome = { currentScreenState.value = Screen.HOME },
            onProfile = { currentScreenState.value = Screen.STUDENT_PROFILE },
            onRideHistory = { currentScreenState.value = Screen.RIDE_HISTORY },

            onRideClick = { rideId: Int ->
                selectedRideId = rideId
                currentScreenState.value = Screen.SCHEDULED_RIDE_DETAIL
            }
        )

        Screen.TRACK_RIDE -> {

        selectedRideId?.let { rideId ->   // 🔥 SAFE

            TrackRideScreen(
                rideId = rideId,
                onBack = { currentScreenState.value = Screen.SCHEDULED_RIDE_DETAIL },
                onHome = { currentScreenState.value = Screen.SCHEDULED_RIDE_DETAIL }
            )
        }
    }

        Screen.SCHEDULED_RIDE_DETAIL -> {

            selectedRideId?.let { rideId ->   // ✅ FIX

                ScheduledRideDetailScreen(
                    rideId = rideId,
                    onBack = { currentScreenState.value = Screen.SCHEDULED_RIDES },
                    onOpenChat = { id ->
                        selectedRideId = id
                        currentScreenState.value = Screen.CHAT_SCREEN
                    },
                    onTrackRide = { id ->
                        selectedRideId = id
                        currentScreenState.value = Screen.TRACK_RIDE
                    },
                    onHome = { currentScreenState.value = Screen.SCHEDULED_RIDES }
                )
            }
        }
        Screen.CHAT_SCREEN -> {

            val context = LocalContext.current
            val role = Prefs.getUserRole(context)   // 🔥 USE THIS

            selectedRideId?.let { rideId ->

                Log.d("CHAT_FLOW", "Chat rideId = $rideId role = $role")

                GroupChatScreen(
                    rideId = rideId,

                    onHome = {
                        currentScreenState.value =
                            if (role == "Driver") Screen.DRIVER_RIDE_DETAIL
                            else Screen.SCHEDULED_RIDE_DETAIL
                    },

                    onBack = {
                        currentScreenState.value =
                            if (role == "Driver") Screen.DRIVER_RIDE_DETAIL
                            else Screen.SCHEDULED_RIDE_DETAIL
                    }
                )
            }
        }

        Screen.RIDE_HISTORY -> RideHistoryScreen(
            onLogout = {
                auth.signOut()
                Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.LOGIN
            },
            onProfile = { currentScreenState.value = Screen.STUDENT_PROFILE },
            onScheduledRides = { currentScreenState.value = Screen.SCHEDULED_RIDES },
            onHome = { currentScreenState.value = Screen.HOME }
        )

        Screen.SEARCH_RIDE -> SearchRideNavScreen(
                onBackToHome = { currentScreenState.value = Screen.BOOK_RIDE },

                )

        Screen.SEARCH_RIDE_J -> JoinRideSearchNav1(
                    onBackToHome = { currentScreenState.value = Screen.BOOK_RIDE },
            )



        Screen.DRIVER_PERSONAL_DETAILS -> DriverPersonalDetailsScreen {
            Prefs.setDetailsFilled(context, true)
            currentScreenState.value = Screen.DRIVER_HOME
        }

        in driverScreens -> DriverAppContainer(
            currentScreen = currentScreenState.value,

            onScreenChange = { newScreen ->
                currentScreenState.value = newScreen
            },

            onLogout = {
                auth.signOut()
                Toast.makeText(context, "Driver logged out", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.ROLE_SELECTION
            },

            selectedRideId = selectedRideId,          // ✅ ADD THIS
            onRideSelected = { selectedRideId = it }  // ✅ ADD THIS
        )


        Screen.MAINTENANCE_HOME -> MaintenanceHomeScreens(

            onLogout = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                currentScreenState.value = Screen.LOGIN
            }
        )

        Screen.COLLEGE_HOME -> AdminApp(
            adminEmail = loggedInAdminEmail   // jo login se mila hai
        )
        Screen.HELP -> HelpScreen(
            onBack = { currentScreenState.value = Screen.LOGIN},
            onHome = { currentScreenState.value = Screen.LOGIN}
        )
        else -> {} // ✅ makes when exhaustive
    }
}


@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)) // reduce alpha to keep image bright
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
                .padding(top = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    disabledBorderColor = Color.Transparent,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = validatePassword(it) // 🔥 live validation
                },
                label = { Text("Password") },
                singleLine = true,
                isError = passwordError != null,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordError != null) Color.Red else Color.White,
                    unfocusedBorderColor = if (passwordError != null) Color.Red else Color.White.copy(alpha = 0.6f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            if (passwordError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = passwordError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    disabledBorderColor = Color.Transparent,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val e = email.trim()
                    val p = password.trim()
                    val c = confirmPassword.trim()
                    if (e.isBlank() || p.isBlank() || c.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val passwordValidation = validatePassword(p)
                    if (passwordValidation != null) {
                        Toast.makeText(context, passwordValidation, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (p != c) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val selectedRole = Prefs.getUserRole(context) ?: "Student"
                    val blockedDomains = listOf(
                        "gmail.com",
                        "yahoo.com",
                        "outlook.com",
                        "hotmail.com"
                    )

                    val emailDomain = e.substringAfter("@").lowercase()

                    if (selectedRole == "Student" && blockedDomains.contains(emailDomain)) {
                        Toast.makeText(
                            context,
                            "Students must use official college email ID",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button  // 🚫 STOP HERE
                    }

                    auth.createUserWithEmailAndPassword(e, p)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val firebaseUid = user?.uid ?: ""
                                val emailId = user?.email ?: ""
                                val role = Prefs.getUserRole(context) ?: ""

                                // ✅ Save first login flag
                                val sharedPreferences = context.getSharedPreferences("GoCabPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putBoolean("firstLogin_${firebaseUid}", true).apply()

                                // ✅ Save user info to Azure SQL via backend
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val selectedRole = Prefs.getUserRole(context) ?: "Student"

                                        Log.d("RegisterDebug", "➡ Selected role during signup: $selectedRole")

                                        val userResponse = RetrofitInstance.api.registerUser(
                                            UserRequest(firebaseUid, emailId, selectedRole)
                                        )

                                        Log.d("RegisterDebug", "Response code: ${userResponse.code()}")
                                        Log.d("RegisterDebug", "Response body: ${userResponse.body()}")
                                        Log.d("RegisterDebug", "Response error body: ${userResponse.errorBody()?.string()}")

                                        if (userResponse.isSuccessful) {
                                            Log.d("RegisterDebug", " Request succeeded")
                                        } else {
                                            Log.e("RegisterDebug", " Request failed")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RegisterDebug", " Exception while calling backend: ${e.message}", e)
                                    }
                                }
                                auth.currentUser?.sendEmailVerification()
                                auth.signOut()
                                Prefs.setDetailsFilled(context, false)
                                onSignupSuccess()

                            } else {
                                Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("Sign Up", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Login", color = Color.White)
            }
        }
    }

}

fun validatePassword(password: String): String? {
    if (password.length < 6) {
        return "! Password must be at least 6 characters long"
    }
    if (!password.any { it.isUpperCase() }) {
        return "! Password must contain at least 1 uppercase letter"
    }
    if (!password.any { it.isLowerCase() }) {
        return "! Password must contain at least 1 lowercase letter"
    }
    if (!password.any { it.isDigit() }) {
        return "! Password must contain at least 1 number"
    }
    if (!password.any { !it.isLetterOrDigit() }) {
        return "! Password must contain at least 1 special character"
    }
    return null //  Password is valid
}
@Composable
fun LoginScreen(
    onSignUpClicked: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onHelpClick: () -> Unit
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "Location granted")
            } else {
                Log.d("PERMISSION", "Location denied")
            }
        }
    val auth = FirebaseAuth.getInstance()
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.img_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
                .padding(top = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Text(
                text = "Forgot Password?",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        showForgotPasswordDialog = true
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val e = email.trim()
                    val p = password.trim()

                    if (e.isBlank() || p.isBlank()) {
                        Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.signInWithEmailAndPassword(e, p)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    val firebaseUid = user.uid

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val response =
                                                RetrofitInstance.api.getUserRole(mapOf("firebase_uid" to firebaseUid))
                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful) {
                                                    val actualRole =
                                                        response.body()?.user_type ?: ""

                                                    Prefs.setUserRole(context, actualRole)

                                                    val requiresVerification =
                                                        (actualRole == "Student" || actualRole == "Driver")

                                                    if (requiresVerification && !user.isEmailVerified) {
                                                        Toast.makeText(
                                                            context,
                                                            "Please verify your email before logging in.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        FirebaseAuth.getInstance().signOut()
                                                        return@withContext
                                                    }

                                                    //  Navigate by role
                                                    when (actualRole) {

                                                        "Student" -> {
                                                            Toast.makeText(context, "Welcome Student!", Toast.LENGTH_SHORT).show()
                                                            val studentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                                                            SelectedRideHolder.studentEmail = studentEmail
                                                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                            val sharedPreferences = context.getSharedPreferences("GoCabPrefs", Context.MODE_PRIVATE)
                                                            val isFirstLogin = sharedPreferences.getBoolean("firstLogin_${userId}", false)
                                                            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                                            prefs.edit().putString("email", studentEmail).apply()

                                                            if (isFirstLogin) {
                                                                // Navigate to personal details only once
                                                                (context as ComponentActivity).runOnUiThread {
                                                                    MainScreenNavigationHelper.navigateTo(Screen.PERSONAL_DETAILS)
                                                                }
                                                                // Mark it as completed
                                                                sharedPreferences.edit().putBoolean("firstLogin_${userId}", false).apply()
                                                            } else {
                                                                // From 2nd login onwards → home screen
                                                                (context as ComponentActivity).runOnUiThread {
                                                                    MainScreenNavigationHelper.navigateTo(Screen.HOME)
                                                                }
                                                            }
                                                        }

                                                        "Driver" -> {
                                                            Toast.makeText(context, "Welcome Driver!", Toast.LENGTH_SHORT).show()

                                                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                            val sharedPreferences = context.getSharedPreferences("GoCabPrefs", Context.MODE_PRIVATE)
                                                            val isFirstLogin = sharedPreferences.getBoolean("firstLogin_${userId}", false)
                                                            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                                            prefs.edit().putString("email", driverEmail).apply()

                                                            if (isFirstLogin) {
                                                                // ✅ Show DriverPersonalScreen only once
                                                                (context as ComponentActivity).runOnUiThread {
                                                                    MainScreenNavigationHelper.navigateTo(Screen.DRIVER_PERSONAL_DETAILS)
                                                                }
                                                                sharedPreferences.edit().putBoolean("firstLogin_${userId}", false).apply()
                                                            } else {
                                                                // ✅ From next time onwards → Driver Home
                                                                (context as ComponentActivity).runOnUiThread {
                                                                    MainScreenNavigationHelper.navigateTo(Screen.DRIVER_HOME)
                                                                }
                                                            }
                                                        }

                                                        "MaintenanceTeam" -> {
                                                            Toast.makeText(
                                                                context,
                                                                "Welcome Maintenance Team!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            Prefs.setDetailsFilled(context, true)
                                                            (context as ComponentActivity).runOnUiThread {
                                                                MainScreenNavigationHelper.navigateTo(Screen.MAINTENANCE_HOME)
                                                            }
                                                        }


                                                        "Administration" -> {
                                                            val adminEmail = user.email ?: ""

                                                            Toast.makeText(context, "Welcome Administration!", Toast.LENGTH_SHORT).show()
                                                            Prefs.setDetailsFilled(context, true)

                                                            onLoginSuccess(adminEmail)   // yahin se email MainScreen ko jayega

                                                            MainScreenNavigationHelper.navigateTo(Screen.COLLEGE_HOME)
                                                        }

                                                        else -> {
                                                            Toast.makeText(
                                                                context,
                                                                "Invalid role access!",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            FirebaseAuth.getInstance().signOut()
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error fetching user role",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("RoleCheck", "Error fetching user role", e)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to verify role",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Login Failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("Login", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onSignUpClicked) {
                Text("Don't have an account? Sign Up", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Need Help & Support?",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onHelpClick() }
                    .padding(8.dp)
            )
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSend = { enteredEmail ->
                if (enteredEmail.isBlank()) {
                    Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show()
                    return@ForgotPasswordDialog
                }

                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(enteredEmail.trim())
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Password reset link sent to your email",
                            Toast.LENGTH_LONG
                        ).show()
                        showForgotPasswordDialog = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            e.message ?: "Something went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        )
    }
}
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your registered email")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSend(email) }) {
                Text("Send Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center content vertically too
    ) {
        // Background Image (img_4)
        Image(
            painter = painterResource(id = R.drawable.img_1),
            contentDescription = "Role Selection Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Slight dark overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )

        // Foreground content (Text and Buttons)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Center buttons vertically
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp) // Add padding on sides
        ) {

            Text(
                text = "Enter as",
                fontSize = 42.sp, // Bada font size
                fontWeight = FontWeight.Bold,
                color = Color.White, // Safed rang background par dikhne ke liye
                modifier = Modifier.padding(bottom = 48.dp) // Buttons se thoda neeche
            )

            // Student Button
            RoleSelectionButtonWide( // Using the new reusable button
                text = "Student",
                icon = Icons.Filled.School,
                buttonColor = Color(0xFFFFFFFF), // Blue
                onClick = {
                    Prefs.setUserRole(context, "Student")
                    onRoleSelected("Student")
                }
            )

            Spacer(modifier = Modifier.height(30.dp)) // Spacing between buttons

            // Driver Button
            RoleSelectionButtonWide( // Using the new reusable button
                text = "Driver",
                icon = Icons.Filled.DirectionsCar,
                buttonColor = Color(0xFFFFFFFF), // Green
                onClick = {
                    Prefs.setUserRole(context, "Driver")
                    onRoleSelected("Driver")
                }
            )

            Spacer(modifier = Modifier.height(30.dp)) // Spacing between buttons

            // Others Button (Now Filled)
            RoleSelectionButtonWide( // Using the new reusable button
                text = "Others",
                icon = Icons.Filled.Person,
                buttonColor = Color(0xFFFFFFFF), // Neutral color
                onClick = {
                    Prefs.setUserRole(context, "Others")
                    currentScreen.value = Screen.LOGIN
                }
            )
        }
    }
}

// Reusable Composable for WIDER filled buttons with image background
@Composable
fun RoleSelectionButtonWide(text: String, icon: ImageVector, buttonColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp), // Rounded corners
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color(0xFF3D2486) // Text/Icon color on button
        ),
        modifier = Modifier
            .fillMaxWidth(0.96f) // ✅ WIDER BUTTON (80% of screen width)
            .height(64.dp)      // ✅ Slightly Taller Button
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Center icon and text
        ) {
            Spacer(modifier = Modifier.width(12.dp)) // Add padding before icon
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(23.dp)) // ✅ Increase space between icon and text
            // Center the text horizontally within the remaining space
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { // CenterStart will align text nicely
                Text(
                    text = text,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp)) // Add padding after text
        }
    }
}

