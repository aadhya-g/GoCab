
package com.example.gocab.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.R
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.ui.DataModels.DriverRideResponse
import com.example.gocab.ui.DataModels.JoinRequest
import com.example.gocab.ui.DataModels.UpdateRideRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun DriverRideRequestScreen() {

    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var rideList by remember { mutableStateOf<List<DriverRideResponse>>(emptyList()) }
    var joinList by remember { mutableStateOf<List<JoinRequest>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val rideResponse = RetrofitInstance.rideApi.getDriverRides(email)
            if (rideResponse.isSuccessful) {
                rideList = rideResponse.body() ?: emptyList()
            }

            val joinResponse = RetrofitInstance.rideApi.getJoinRequests(email)
            if (joinResponse.isSuccessful) {
                joinList = joinResponse.body() ?: emptyList()
            }

        } catch (e: Exception) {
            Log.e("DRIVER_FETCH", e.message ?: "")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 Background Image
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )

        // 🌫 Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.30f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // 🔷 Toggle Partition
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(55.dp)
                    .background(
                        Color(0xFF2A2A2A),
                        RoundedCornerShape(20.dp)
                    )
            ) {

                // INITIATE TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (pagerState.currentPage == 0) Color(0xFF3F51B5)
                            else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Initiate Ride",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // JOIN TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (pagerState.currentPage == 1) Color(0xFF3F51B5)
                            else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                   contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Join Ride",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔽 CONTENT SWITCH
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                when (page) {
                    0 -> InitiateRideList(rideList)
                    1 -> JoinRideList(joinList)
                }
            }
        }
    }
}
@Composable
fun InitiateRideList(rideList: List<DriverRideResponse>) {

    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var rideList by remember { mutableStateOf<List<DriverRideResponse>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var selectedRide by remember { mutableStateOf<DriverRideResponse?>(null) }
    var selectedAction by remember { mutableStateOf("") } // "Accepted" or "Rejected"
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.rideApi.getDriverRides(email)
            if (response.isSuccessful) {
                rideList = response.body() ?: emptyList()
            }
        } catch (_: Exception) { }
    }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(rideList) { ride ->

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        // 🔹 Route Section
                        Text(
                            " ${ride.initial_loc}  ➜  ${ride.final_loc}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )

                        Divider()

                        // 🔹 Date & Time
                        /*Text("📅 Date: ${ride.R_date}")*/
                            Text(" Date: ${ride.R_date.substringBefore("T")}")
                        /* Text(" Time: ${ride.R_timing}")*/
                        Text(
                            " Time: ${
                                ride.R_timing
                                    .substringAfter("T")
                                    .substringBefore(".")
                            }"
                        )

                        Divider()

                        // 🔹 Ride Info
                        Text(" Distance: ${ride.distance_km} km")
                        Text(
                            " Fare: ₹${ride.fare_amount}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            // ✅ ACCEPT BUTTON
                            Button(
                                onClick = {
                                    selectedRide = ride
                                    selectedAction = "Accepted"
                                    showDialog = true
                                },

                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text("Accept", color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // ❌ REJECT BUTTON
                            Button(onClick = {
                                selectedRide = ride
                                selectedAction = "Rejected"
                                showDialog = true
                            },

                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC62828)
                                )
                            ) {
                                Text("Reject", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        if (showDialog && selectedRide != null) {

            AlertDialog(
                onDismissRequest = { showDialog = false },

                title = {
                    Text("Confirm Ride 🚘")
                },

                text = {
                    Text("Are you sure you want to ${selectedAction.lowercase()} this ride?")
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false

                            scope.launch {
                                try {
                                    val response =
                                        RetrofitInstance.rideApi.updateRideStatus(
                                            UpdateRideRequest(
                                                rideId = selectedRide!!.R_id,
                                                driverId = email,
                                                status = "Accepted"
                                            )
                                        )

                                    if (response.isSuccessful) {
                                        rideList = rideList.filter {
                                            it.R_id != selectedRide!!.R_id
                                        }
                                    }

                                } catch (_: Exception) { }
                            }
                        }
                    ) {
                        Text("Yes", color = Color(0xFF2E7D32))
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("No", color = Color.Red)
                    }
                }
            )
        }
    }

@Composable
fun JoinRideList(joinList: List<JoinRequest>) {

    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var joinList by remember { mutableStateOf<List<JoinRequest>>(emptyList()) }
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var selectedJoin by remember { mutableStateOf<JoinRequest?>(null) }
    var selectedAction by remember { mutableStateOf("") } // Accepted / Rejected

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.rideApi.getJoinRequests(email)
            if (response.isSuccessful) {
                joinList = response.body() ?: emptyList()
            }
        } catch (_: Exception) { }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        items(joinList) { request ->

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // 🔹 Route Section (Same Style)
                    Text(
                        " ${request.Pickup_loc}  ➜  ${request.Drop_loc}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )

                    Divider()

                    // 🔹 Date
                    Text(" Date: ${request.R_date.take(10)}")

                    Divider()

                    // 🔹 Student Info
                    Text(" Student: ${request.S_email_id}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // ✅ ACCEPT BUTTON
                        Button(
                            onClick = {
                                selectedJoin = request
                                selectedAction = "Accepted"
                                showDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Text("Accept", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // ❌ REJECT BUTTON
                        Button(
                            onClick = {
                                selectedJoin = request
                                selectedAction = "Rejected"
                                showDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828)
                            )
                        ) {
                            Text("Reject", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 🔔 Confirmation Dialog
    if (showDialog && selectedJoin != null) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text("Confirm Join Request 🚘")
            },

            text = {
                Text("Are you sure you want to ${selectedAction.lowercase()} this join request?")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false

                        scope.launch {
                            try {

                                if (selectedAction == "Accepted") {

                                    RetrofitInstance.rideApi.acceptJoin(
                                        mapOf("requestId" to selectedJoin!!.id)
                                    )

                                } else {

                                    RetrofitInstance.rideApi.rejectJoin(
                                        mapOf("requestId" to selectedJoin!!.id)
                                    )
                                }

                                joinList = joinList.filter {
                                    it.id != selectedJoin!!.id
                                }

                            } catch (_: Exception) { }
                        }
                    }
                ) {
                    Text("Yes", color = Color(0xFF2E7D32))
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("No", color = Color.Red)
                }
            }
        )
    }
}

