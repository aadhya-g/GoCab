package com.example.gocab.ui

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gocab.R
import com.example.gocab.model.RideRequest
import com.example.gocab.network.ApiClient
import com.example.gocab.network.DriverWithCarResponse
import com.example.gocab.util.SelectedRideHolder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmRideScreen(navController: NavController) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val calendar = Calendar.getInstance()
    var from by remember { mutableStateOf(SelectedRideHolder.pickup) }
    var to by remember { mutableStateOf(SelectedRideHolder.drop) }
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var driverData by remember { mutableStateOf<DriverWithCarResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var pickupCity by remember { mutableStateOf("") }
    var dropCity by remember { mutableStateOf("") }

    // 🔥 Fetch Driver Details
    LaunchedEffect(Unit) {
        try {
            Log.d("API_TEST", "Driver Email: ${SelectedRideHolder.driverEmail}")

            val response = ApiClient.instance
                .getDriverDetails(SelectedRideHolder.driverEmail)

            Log.d("API_TEST", "Response Code: ${response.code()}")

            if (response.isSuccessful) {
                driverData = response.body()
                Log.d("API_TEST", "Driver Data: $driverData")
            }
            else {
                Log.e("API_TEST", "Error Body: ${response.errorBody()}")
            }

        } catch (e: Exception) {
            Log.e("API_TEST", "Exception: ${e.message}")
        }

        isLoading = false
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Confirm Ride", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1)
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🌄 Background Image
            Image(
                painter = painterResource(id = R.drawable.img_6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.85f),
                contentScale = ContentScale.Crop
            )
            // 🌫 Optional Dark Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )

            // 🧾 Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    // elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {

                            if (driverData == null) {
                                Text(
                                    "Driver details not available ",
                                    color = Color.Red
                                )
                            } else {
                                val driver = driverData!!
                                // ================= DRIVER CARD =================
                                SectionTitle("Driver Details")
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE3F2FD)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Name", color = Color.Gray)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(driver.D_name)
                                                if (SelectedRideHolder.verifiedCount > 0) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Verified,
                                                        contentDescription = "Verified",
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (SelectedRideHolder.verifiedCount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                Text(
                                                    text = "Verified by ${SelectedRideHolder.verifiedCount} colleges",
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        InfoRow("Email", SelectedRideHolder.driverEmail)
                                        InfoRow("Phone", driver.D_phone_no)
                                        InfoRow("Rating", "⭐ ${driver.D_avg_rating}")
                                    }
                                }
                                Spacer(modifier = Modifier.height(0.dp))
                                // ================= CAR CARD =================
                                SectionTitle("Car Details")
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF1F8E9)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        InfoRow("Car Name", driver.C_name ?: "N/A")
                                        InfoRow("Car Number", driver.C_number ?: "N/A")
                                        InfoRow("Seats", driver.C_seater?.toString() ?: "N/A")
                                        InfoRow("Cost per Km", "₹${driver.cost_per_km}")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(0.dp))
                        // ================= RIDE DETAILS =================

                        SectionTitle("Ride Details")

                        OutlinedTextField(
                            value = from,
                            onValueChange = { from = it },
                            label = { Text("Pickup Location (Full Address)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = to,
                            onValueChange = { to = it },
                            label = { Text("Drop Location (Full Address)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = date,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Date") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),

                            trailingIcon = {
                                TextButton(onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            date = "$year-${month + 1}-$day"
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Pick Date"
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = pickupCity,
                            onValueChange = { pickupCity = it },
                            label = { Text("Pickup City") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = dropCity,
                            onValueChange = { dropCity = it },
                            label = { Text("Drop City") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Distance: ${SelectedRideHolder.distanceKm} km", fontSize = 18.sp,)
                        Text(
                            "Total Fare: ₹${SelectedRideHolder.fare}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val carId = SelectedRideHolder.carId

                                if (carId.isNullOrEmpty()) {
                                    Toast.makeText(context, "Car ID missing", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val studentEmail =
                                    FirebaseAuth.getInstance().currentUser?.email ?: ""

                                if (date.isBlank() || time.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Select date & time",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                val rideRequest = RideRequest(
                                    driverEmail = SelectedRideHolder.driverEmail,
                                    carId = carId,
                                    studentEmail = studentEmail,
                                    pickup = from,
                                    drop = to,
                                    pickupCity = pickupCity,      // 🔥 ADD THIS
                                    dropCity = dropCity,
                                    distanceKm = SelectedRideHolder.distanceKm,
                                    fare = SelectedRideHolder.fare,
                                    time = "$time:00",   // ✅ Fix SQL time issue
                                    date = date
                                )

                                scope.launch {
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            ApiClient.instance.createRide(rideRequest)
                                        }

                                        if (response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Ride Booked!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("rideSuccess")
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Ride failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    } catch (e: Exception) {
                                        Log.e("RIDE_ERROR", e.message.toString())
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3F51B5)
                            )
                        ) {
                            Text(
                                "Confirm Ride ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))

            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value ?: "N/A")
    }
}
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF3F51B5)
    )
}


