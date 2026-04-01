//AdminScreen.kt
package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gocab.network.RetrofitInstance

// ---------------- VIEW STUDENTS ----------------
/*
@Composable
fun AdminStudentsPlaceholder(
    navController: NavController,
    adminEmail: String
) {
    ViewStudentsaScreen(
        adminEmail = adminEmail,
        onBack = { navController.popBackStack() }
    )
}
*/



// ---------------- VIEW DRIVERS ----------------

// ---------------- SCHEDULED RIDES ----------------
@Composable
fun ScheduledRidesScreen(
    navController: NavController,
    adminEmail: String
) {

    var groupedRides by remember { mutableStateOf<List<RideGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {

            val api = RetrofitInstance.api
            val response = api.getTodaysRides(adminEmail)

            if (response.success) {
                groupedRides = groupRides(response.rides)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background
        Image(
            painter = painterResource(id = R.drawable.img_7),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Scheduled Rides",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2A5E)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn {
                    items(groupedRides) { ride ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {

                            Column(modifier = Modifier.padding(12.dp)) {

                                Text("Ride ID: ${ride.R_id}")
                                Text("From: ${ride.initial_loc}")
                                Text("To: ${ride.final_loc}")
                                Text("Time: ${ride.R_timing}")

                                Text(
                                    text = ride.R_status,
                                    color = if (ride.R_status == "Completed") Color.Green else Color.Blue
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Students (${ride.students.size})")

                                ride.students.forEach {
                                    Text("• $it")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------- COMMON TEMPLATE ----------------
@Composable
fun AdminCommonScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.img_7),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2A5E)
            )


            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Admin Panel",
                fontSize = 18.sp,
                color = Color.DarkGray
            )
        }
    }
}
//admin

data class AdminRideResponse(
    val success: Boolean,
    val rides: List<Ride>
)
// ---------------- DATA MODELS ----------------

data class Ride(
    val R_id: Int,
    val S_name: String,
    val R_timing: String,
    val R_status: String,
    val initial_loc: String,
    val final_loc: String
)

data class RideGroup(
    val R_id: Int,
    val R_timing: String,
    val R_status: String,
    val initial_loc: String,
    val final_loc: String,
    val students: List<String>
)




// ---------------- GROUP FUNCTION ----------------

fun groupRides(rides: List<Ride>): List<RideGroup> {
    return rides
        .groupBy { it.R_id }
        .map { (rideId, rideList) ->

            val first = rideList.first()

            RideGroup(
                R_id = rideId,
                R_timing = first.R_timing,
                R_status = first.R_status,
                initial_loc = first.initial_loc,
                final_loc = first.final_loc,
                students = rideList.map { it.S_name }.distinct()
            )
        }
}


/*
package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------- VIEW STUDENTS ----------------
*/
/*@Composable
fun AdminStudentsPlaceholder(
    navController: NavController,
    adminEmail: String
) {
    ViewStudentsScreen(
        adminEmail = adminEmail,
        onBack = { navController.popBackStack() }
    )
}*//*


// ---------------- VIEW DRIVERS ----------------

// ---------------- SCHEDULED RIDES ----------------
@Composable
fun ScheduledRidesScreen(onBack: () -> Unit) {
    AdminCommonScreen(title = "Scheduled Rides")
}

// ---------------- RIDE ALERTS ----------------
*/
/*@Composable
fun RideAlertsScreen(onBack: () -> Unit) {
    AdminCommonScreen(title = "Ride Alerts")
}*//*


// ---------------- COMMON TEMPLATE ----------------
@Composable
fun AdminCommonScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2A5E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Admin Panel",
                fontSize = 18.sp,
                color = Color.DarkGray
            )
        }
    }
}
//admin*/
