package com.example.gocab

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gocab.util.JoinRideFilterData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    pickup: String,
    drop: String,
    date: String,
    filters: JoinRideFilterData,
    navController: NavController
) {
    val context = LocalContext.current
    var rideList by remember {
        mutableStateOf<List<RideSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    //  API CALL
    LaunchedEffect(pickup, drop, date) {
        if (pickup.isNotBlank() && drop.isNotBlank() && date.isNotBlank()) {
            isLoading = true
            searchExistingRides(context, pickup, drop, date) { result ->
                rideList = result
                isLoading = false
            }
        }
    }
    //  APPLY FILTER (same college )
    val filteredRides = rideList.filter { ride ->
        if (filters.sameCollegeOnly == true) {
            // If backend sends colleges string → check match
            ride.colleges?.contains(filters.userCollege ?: "", ignoreCase = true) == true
        } else {
            true
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Available Rides", color = Color.White) },
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

            // 🌄 Background
            Image(
                painter = painterResource(id = R.drawable.img_7),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.85f,
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                when {

                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Searching rides...", color = Color.White)
                        }
                    }

                    filteredRides.isEmpty() -> {
                        Text(
                            text = "No rides available",
                            color = Color.White
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            items(filteredRides) { ride ->

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.95f)
                                    )
                                ) {

                                    Column(
                                        modifier = Modifier.padding(18.dp)
                                    ) {

                                        // 🔹 Driver Name
                                        Text(
                                            text = ride.D_name,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color(0xFF4169E1)
                                        )

                                        // 🔹 Verified Badge
                                        if (ride.verifiedCount > 0) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Verified by ${ride.verifiedCount} colleges",
                                                    color = Color(0xFF2E7D32),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(" Date: ${ride.R_date.substring(0, 10)}")

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            " Time: ${
                                                ride.R_timing
                                                    .substringAfter("T")
                                                    .substringBefore(".")
                                            }"
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(" Distance: ${ride.distance_km} km")

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = " Fare per seat: ₹${ride.fare_per_student}",
                                            color = Color(0xFF3F51B5),

                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = " Seats Left: ${ride.seats_left}",
                                            color = Color(0xFF2E7D32),

                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = " Rating: ${ride.rating ?: 0.0}⭐",
                                            //fontSize = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                Log.d("DEBUG_RIDE", "Ride ID clicked: ${ride.R_id}")
                                                navController.navigate("rideDetails/${ride.R_id}")
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4169E1)
                                            )
                                        ) {
                                            Text(
                                                text = "Request Ride",
                                                fontSize = 17.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
