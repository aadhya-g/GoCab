package com.example.gocab

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.ui.DataModels.RideHistory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RideHistoryScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onScheduledRides: () -> Unit,
    onHome: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {

    BackHandler { onHome() }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)
    val rides = viewModel.rides
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(rides) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

        if (rides.isEmpty()) {
            viewModel.fetchRideHistory(email)
        }

        if (rides.isNotEmpty()) {
            isLoading = false   // ✅ tabhi band hoga jab data aayega
        }
    }

    AppScaffold(
        title = "Ride History",
        onLogout = onLogout,
        onProfile = onProfile,
        onScheduledRides = onScheduledRides
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🌄 BACKGROUND
            Image(
                painter = painterResource(id = R.drawable.img11),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )

            // 🌫 OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            when {

                // 🔄 LOADING (jab tak data nahi aaya)
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            CircularProgressIndicator(color = Color.White)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Loading rides...", color = Color.White)
                        }
                    }
                }

                // ❌ EMPTY (real empty case)
                rides.isEmpty() -> {
                    Text("No Completed Rides", color = Color.White)
                }

                // ✅ DATA
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(rides) { ride ->
                            RideHistoryItem(ride, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("rating_prefs", Context.MODE_PRIVATE)

    var rating by remember { mutableStateOf(0f) }

    // 🔥 LOAD SAVED STATE (PERSISTENT)
    var isSubmitted by remember {
        mutableStateOf(prefs.getBoolean("rated_${ride.R_id}", false))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            // 🚗 ROUTE
            Text(
                text = "${ride.initial_loc} ➜ ${ride.final_loc}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Driver: ${ride.driver_name}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(10.dp))

            // ⭐ RATING
            Text("Rate your ride:", fontWeight = FontWeight.SemiBold)

            Row {
                (1..5).forEach { i ->
                    Text(
                        text = if (i <= rating) "⭐" else "☆",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(enabled = !isSubmitted) {
                                rating = i.toFloat()
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔘 BUTTON
            Button(
                onClick = {
                    if (rating > 0f) {

                        viewModel.submitRating(ride, rating)

                        isSubmitted = true

                        // 🔥 SAVE PERMANENTLY
                        prefs.edit()
                            .putBoolean("rated_${ride.R_id}", true)
                            .apply()
                    }

                },
                enabled = !isSubmitted && rating > 0f,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted) Color.Gray else Color(0xFF3F51B5)
                )
            ) {
                Text("Submit Rating", color = Color.White)
            }

            // ✅ MESSAGE
            if (isSubmitted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You already rated this ride ✅",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/*

@Composable
fun RideHistoryScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onScheduledRides: () -> Unit,
    onHome: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {

    BackHandler { onHome() }

    val rides = viewModel.rides

    LaunchedEffect(Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModel.fetchRideHistory(email)
    }

    AppScaffold(
        title = "Ride History",
        onLogout = onLogout,
        onProfile = onProfile,
        onScheduledRides = onScheduledRides
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🌄 BACKGROUND IMAGE
            Image(
                painter = painterResource(id = R.drawable.img_6), // 🔥 change if needed
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )

            // 🌫 DARK OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            if (rides.isEmpty()) {

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No Completed Rides ",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(rides) { ride ->
                        RideHistoryItem(ride, viewModel)
                    }
                }
            }
        }
    }
}
@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    var rating by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
       // elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            // 🚗 ROUTE
            Text(
                text = "${ride.initial_loc} ➜ ${ride.final_loc}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Driver: ${ride.driver_name}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(10.dp))

            // ⭐ RATING UI
            Text("Rate your ride:", fontWeight = FontWeight.SemiBold)

            Row {
                (1..5).forEach { i ->
                    Text(
                        text = if (i <= rating) "⭐" else "☆",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { rating = i.toFloat() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.submitRating(ride, rating)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                )
            ) {
                Text("Submit Rating", color = Color.White)
            }
        }
    }
}*/


/*

@Composable
fun RideHistoryScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onScheduledRides: () -> Unit,
    onHome: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {

    BackHandler {
        onHome()   // 👈 back = home
    }

    val rides = viewModel.rides
    // 🔥 ADD THIS LINE HERE
    println("UI RIDES SIZE = ${rides.size}")

    LaunchedEffect(Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModel.fetchRideHistory(email)
    }

    AppScaffold(
        title = "Ride History",
        onLogout = onLogout,
        onProfile = onProfile,
        onScheduledRides = onScheduledRides
        // 🔥 remove onHome if error
    ) { padding ->

        if (rides.isEmpty()) {
            Column(
                modifier = Modifier

                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No Completed Rides Yet 🚫")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(rides) { ride ->
                    //RideHistoryItem(ride)
                    RideHistoryItem(ride, viewModel)
                }
            }
        }
    }
}

@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    var rating by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Ride ID: ${ride.R_id}")
            Text("Driver: ${ride.driver_name}")

            //  Text("Rating: ⭐ ${ride.D_avg_rating ?: "Not Rated"}")

            Text("From: ${ride.initial_loc}")
            Text("To: ${ride.final_loc}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(8.dp))

            // ⭐ STAR SELECTOR
            Row {
                (1..5).forEach { i ->
                    Text(
                        text = if (i <= rating) "⭐" else "☆",
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                rating = i.toFloat()
                            }
                    )
                }
            }

            // 🚀 SUBMIT BUTTON

            Button(
                onClick = {
                    println("BUTTON CLICKED 🚀")
                    viewModel.submitRating(ride, rating)
                }
            ) {
                Text("Submit Rating")
            }
        }
    }
}

*/

/*

@Composable
fun RideHistoryScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onScheduledRides: () -> Unit,
    onHome: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {

    BackHandler { onHome() }

    val rides = viewModel.rides
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModel.fetchRideHistory(email)
        isLoading = false
    }

    AppScaffold(
        title = "Ride History",
        onLogout = onLogout,
        onProfile = onProfile,
        onScheduledRides = onScheduledRides
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🌄 BACKGROUND
            Image(
                painter = painterResource(id = R.drawable.img_6),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )

            // 🌫 OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            when {

                // 🔄 LOADING
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            CircularProgressIndicator(color = Color.White)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Loading rides...", color = Color.White)
                        }
                    }
                }

                // ❌ EMPTY
                rides.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No Completed Rides 🚫", color = Color.White)
                    }
                }

                // ✅ DATA
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(rides) { ride ->
                            RideHistoryItem(ride, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    var rating by remember { mutableStateOf(0f) }
    var isSubmitted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            // 🚗 ROUTE
            Text(
                text = "${ride.initial_loc} ➜ ${ride.final_loc}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Driver: ${ride.driver_name}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(10.dp))

            // ⭐ RATING
            Text("Rate your ride:", fontWeight = FontWeight.SemiBold)

            Row {
                (1..5).forEach { i ->
                    Text(
                        text = if (i <= rating) "⭐" else "☆",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(enabled = !isSubmitted) {
                                rating = i.toFloat()
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔘 BUTTON (ONLY THIS DISABLE HOGA)
            Button(
                onClick = {
                    if (rating > 0f) {
                        viewModel.submitRating(ride, rating)
                        isSubmitted = true
                    }
                },
                enabled = !isSubmitted && rating > 0f,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted) Color.Gray else Color(0xFF3F51B5)
                )
            ) {
                Text("Submit Rating", color = Color.White)
            }

            // ✅ MESSAGE (CARD KE ANDAR HI)
            if (isSubmitted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You already rated this ride ✅",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}



*/


/*
@Composable
fun RideHistoryScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onScheduledRides: () -> Unit,
    onHome: () -> Unit,
    viewModel: RideViewModel = viewModel()
) {

    BackHandler { onHome() }

    val rides = viewModel.rides

    LaunchedEffect(Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        viewModel.fetchRideHistory(email)
    }

    AppScaffold(
        title = "Ride History",
        onLogout = onLogout,
        onProfile = onProfile,
        onScheduledRides = onScheduledRides
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🌄 BACKGROUND IMAGE
            Image(
                painter = painterResource(id = R.drawable.img_6), // 🔥 change if needed
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.9f
            )

            // 🌫 DARK OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            if (rides.isEmpty()) {

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No Completed Rides 🚫",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(rides) { ride ->
                        RideHistoryItem(ride, viewModel)
                    }
                }
            }
        }
    }
}
@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    var rating by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
       // elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            // 🚗 ROUTE
            Text(
                text = "${ride.initial_loc} ➜ ${ride.final_loc}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Driver: ${ride.driver_name}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(10.dp))

            // ⭐ RATING UI
            Text("Rate your ride:", fontWeight = FontWeight.SemiBold)

            Row {
                (1..5).forEach { i ->
                    Text(
                        text = if (i <= rating) "⭐" else "☆",
                        fontSize = 22.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { rating = i.toFloat() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.submitRating(ride, rating)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                )
            ) {
                Text("Submit Rating", color = Color.White)
            }
        }
    }
}*/

/*
@Composable
fun RideHistoryItem(ride: RideHistory, viewModel: RideViewModel) {

    var rating by remember { mutableStateOf(0f) }
    var isSubmitted by remember { mutableStateOf(ride.alreadyRated) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("Ride ID: ${ride.R_id}")
            Text("Driver: ${ride.driver_name}")

            Text("Rating: ⭐ ${ride.D_avg_rating ?: "Not Rated"}")

            Text("From: ${ride.initial_loc}")
            Text("To: ${ride.final_loc}")
            Text("Date: ${ride.R_date.substringBefore("T")}")
            Text("Fare: ₹${ride.fare_amount}")

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ IF NOT RATED → SHOW UI
            if (!isSubmitted) {

                // ⭐ STAR SELECTOR
                Row {
                    (1..5).forEach { i ->
                        Text(
                            text = if (i <= rating) "⭐" else "☆",
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    rating = i.toFloat()
                                }
                        )
                    }
                }

                // 🚀 SUBMIT BUTTON

                Button(
                    onClick = {
                        if (rating > 0f) {
                            viewModel.submitRating(ride, rating)
                            isSubmitted = true   // ✅ hide after submit
                        }
                    }
                ) {
                    Text("Submit Rating")
                }


            } else {
                // ✅ AFTER RATING
                Text("You already rated this ride ✅")
            }
        }
    }
}

 */