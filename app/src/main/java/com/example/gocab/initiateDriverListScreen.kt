package com.example.gocab.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gocab.R
import com.example.gocab.util.FilterData
import com.example.gocab.util.SelectedRideHolder
import com.example.gocab.viewmodel.SearchRideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverListScreen(
    pickup: String,
    drop: String,
    filters: FilterData,   // ✅ ADDED
    navController: NavController,
    viewModel: SearchRideViewModel = viewModel()
) {
    val drivers = viewModel.drivers
    val isLoading = viewModel.isLoading
    val error = viewModel.error
    LaunchedEffect(Unit) {
        viewModel.searchRide(pickup, drop, filters)
    }
    //  APPLY FILTER HERE
    val filteredDrivers = drivers
        .filter { driver ->
            val rating = driver.rating ?: 0.0
            //  Rating filter
            val ratingMatch = when (filters.rating) {
                "Best" -> rating >= 4.0
                "Average" -> rating in 2.5..3.9
                "Low" -> rating < 2.5
                else -> true
            }
            //  Car type (skip for now if backend not giving it)
            val carMatch = filters.carType == null || true
            //  AC filter (skip if not available in Driver)
            val acMatch = filters.acType == null || true
            //  Seats filter (skip if not available)
            val seatMatch = filters.seats == null || true

            ratingMatch && carMatch && acMatch && seatMatch
        }
        .let { list ->
            //  Sorting by cost
            when (filters.costOrder) {
                "LowToHigh" -> list.sortedBy { it.fare }
                "HighToLow" -> list.sortedByDescending { it.fare }
                else -> list
            }
        }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Available Drivers", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Finding nearby drivers...", color = Color.White)
                        }
                    }
                    error != null -> {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    filteredDrivers.isEmpty() -> {
                        Text(
                            text = "No drivers match your filters",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredDrivers) { driver ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.95f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = driver.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color(0xFF4169E1)
                                            )
                                            if (driver.verifiedCount > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        if (driver.verifiedCount > 0) {
                                            Text(
                                                text = "Verified by ${driver.verifiedCount} colleges",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = " Rating: ${driver.rating ?: 0.0}⭐",
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = " Distance: ${driver.distanceKm} km",
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = " Fare: ₹${driver.fare}",
                                            color = Color(0xFF3F51B5)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                SelectedRideHolder.driverEmail = driver.driverEmail
                                                SelectedRideHolder.carId = driver.carId
                                                SelectedRideHolder.pickup = pickup
                                                SelectedRideHolder.drop = drop
                                                SelectedRideHolder.distanceKm = driver.distanceKm
                                                SelectedRideHolder.fare = driver.fare
                                                SelectedRideHolder.verifiedCount = driver.verifiedCount
                                                navController.navigate("confirmRide")
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

