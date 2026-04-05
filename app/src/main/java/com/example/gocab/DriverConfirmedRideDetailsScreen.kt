//DriverRideDetailsScreen

package com.example.gocab

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DriverRideDetailScreen(
    rideId: Int,
    onBack: () -> Unit,
    onOpenChat: (Int) -> Unit
) {
    //var currentScreen by remember { mutableStateOf(Screen.DRIVER_HOME) }
    var rideData by remember { mutableStateOf<JSONObject?>(null) }
    var studentsList by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    //var selectedRideId: Int?   // nullable
    val context = LocalContext.current

    BackHandler {
        onBack()   // 👈 back = home
    }
    // 🔥 API CALL (same as tera previous screen)
    LaunchedEffect(rideId) {
        fetchFullRideDetails(context, rideId) { ride, students ->
            rideData = ride
            studentsList = students
            isLoading = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 Background Image
        Image(
            painter = painterResource(id = R.drawable.img_7),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // 🌫 Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when {

                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )

                rideData == null -> Text(
                    "Error loading data",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {

                    Column {

                        // 🚗 HEADER
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Ride ID: $rideId",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Students Joined",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 👥 STUDENTS LIST
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(studentsList) { student ->

                                if (student.optString("Ride_status") != "Cancelled") {

                                    val name = student.optString("S_name", "")
                                    val pickup = student.optString("student_pickup", "")
                                    val drop = student.optString("student_drop", "")
                                    val fare = student.optString("fare_per_student", "0")
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.95f)
                                        ),
                                        elevation = CardDefaults.cardElevation(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                // 👤 Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .background(
                                                            Color(0xFF3F51B5),
                                                            shape = RoundedCornerShape(50)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = name.firstOrNull()?.toString()
                                                            ?: "?",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {

                                                    // 👤 Name
                                                    Text(
                                                        text = name,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 16.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    // 📍 Route
                                                    Text(
                                                        text = "$pickup ➜ $drop",
                                                        fontSize = 13.sp,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // 💰 Fare BELOW
                                            Text(
                                                text = "Fare: ₹$fare",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                fontSize = 15.sp
                                            )


                                        }

                                    }
                                }


                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // 💬 CHAT BUTTON
                        Button(
                            onClick = {
                                Log.d("DRIVER_RIDE", "Opening chat rideId = $rideId")
                                onOpenChat(rideId)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3F51B5)
                            )
                        ) {
                            Text("Open Chat  ", color = Color.White)
                        }

                    }
                }
            }
        }
    }
}




/*
package com.example.gocab

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DriverRideDetailScreen(
    rideId: Int,
    onBack: () -> Unit,
    onOpenChat: (Int) -> Unit
    ) {
    //var currentScreen by remember { mutableStateOf(Screen.DRIVER_HOME) }
    var rideData by remember { mutableStateOf<JSONObject?>(null) }
    var studentsList by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    //var selectedRideId: Int?   // nullable
    val context = LocalContext.current

    BackHandler {
        onBack()   // 👈 back = home
    }
    // 🔥 API CALL (same as tera previous screen)
    LaunchedEffect(rideId) {
        fetchFullRideDetails(context, rideId) { ride, students ->
            rideData = ride
            studentsList = students
            isLoading = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 Background Image
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // 🌫 Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when {

                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )

                rideData == null -> Text(
                    "Error loading data",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {

                    Column {

                        // 🚗 HEADER
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Ride ID: $rideId",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Students Joined",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 👥 STUDENTS LIST
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(studentsList) { student ->

                                val name = student.optString("S_name", "")
                                val pickup = student.optString("student_pickup", "")
                                val drop = student.optString("student_drop", "")
                                val fare = student.optString("fare_per_student", "0")
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.95f)
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            // 👤 Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .background(
                                                        Color(0xFF3F51B5),
                                                        shape = RoundedCornerShape(50)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = name.firstOrNull()?.toString() ?: "?",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {

                                                // 👤 Name
                                                Text(
                                                    text = name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // 📍 Route
                                                Text(
                                                    text = "$pickup ➜ $drop",
                                                    fontSize = 13.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // 💰 Fare BELOW
                                        Text(
                                            text = "Fare: ₹$fare",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            fontSize = 15.sp
                                        )


                                    }

                                }

                                */
/*Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.95f)
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        // 👤 Circle Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(
                                                    Color(0xFF3F51B5),
                                                    shape = RoundedCornerShape(50)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name.firstOrNull()?.toString() ?: "?",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {

                                            // 👤 Name
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // 📍 Route
                                            Text(
                                                text = "$pickup ➜ $drop",
                                                fontSize = 13.sp,
                                                color = Color.DarkGray
                                            )
                                        }

                                        // 💰 Fare Badge
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "₹$fare",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }*//*

                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // 💬 CHAT BUTTON
                        Button(
                            onClick = {
                                Log.d("DRIVER_RIDE", "Opening chat rideId = $rideId")
                                onOpenChat(rideId)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D245B)
                            )
                        ) {
                            Text("Open Chat  ", color = Color.White)
                        }

                    }
                    }
                }
            }
        }
    }


   */
/* Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        when {

            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            rideData == null -> Text(
                "Error loading data",
                modifier = Modifier.align(Alignment.Center)
            )

            else -> {

                Column {

                    // ✅ RIDE ID
                    Text(
                        text = "Ride ID: $rideId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // ✅ STUDENTS LIST
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(studentsList) { student ->

                            val name = student.optString("S_name", "")
                            val pickup = student.optString("student_pickup", "")
                            val drop = student.optString("student_drop", "")
                            val fare = student.optString("fare_per_student", "0")

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {

                                    // 👤 NAME
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 📍 ROUTE
                                    Text(
                                        text = "$pickup ➜ $drop",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 💰 FARE
                                    Text(
                                        text = "₹$fare",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
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
*/

