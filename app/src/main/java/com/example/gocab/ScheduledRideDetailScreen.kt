//ScheduledRideDetailedScreen.kt

package com.example.gocab

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledRideDetailScreen(
    rideId: Int,
    onBack: () -> Unit,
    onOpenChat: (Int) -> Unit,
    onHome: () -> Unit,
    onTrackRide: (Int) -> Unit
) {
    BackHandler {
        onHome()   // 👈 back = home
    }
    val context = LocalContext.current

    var rideData by remember { mutableStateOf<JSONObject?>(null) }
    var studentsList by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    /* val pickup = rideData!!.getString("Pickup_loc")
     val drop = rideData!!.getString("Drop_loc")
     val uri = "https://www.google.com/maps/dir/?api=1&origin=$pickup&destination=$drop"
     val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
     context.startActivity(intent)*/

    // 🔥 SAME API CALL
    LaunchedEffect(Unit) {
        fetchFullRideDetails(context, rideId) { ride, students ->
            rideData = ride
            studentsList = students
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scheduled Ride Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
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
                painter = painterResource(id = R.drawable.img_7),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(), alpha = .85f,
                contentScale = ContentScale.Crop
            )

            // 🌫 Dark Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            if (rideData == null) {
                Text("Error loading data")
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        // 🔹 DRIVER SECTION
                        Text(
                            "Driver Information",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            )
                        ) {

                            val verifiedCount = rideData!!.optInt("verifiedCount", 0)

                            Column(
                                modifier = Modifier.padding(18.dp),
                                //   verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Text(
                                        "Name - ${rideData!!.getString("D_name")}",
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (verifiedCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Verified,
                                            null,
                                            tint = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                if (verifiedCount > 0) {
                                    Text(
                                        "Verified by $verifiedCount colleges",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp
                                    )
                                }

                                DetailRoww("Phone - ${rideData!!.getString("D_phone_no")}")
                                DetailRoww("Gender - ${rideData!!.getString("D_gender")}")
                                DetailRoww("City - ${rideData!!.getString("current_city")}")
                            }
                        }

                        // 🔹 CAR SECTION
                        Text(
                            "Car Details",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {

                                DetailRoww("Car - ${rideData!!.getString("C_name")}")
                                DetailRoww("Number - ${rideData!!.getString("C_number")}")
                                DetailRoww("Model - ${rideData!!.getString("C_model")}")
                                DetailRoww("AC/NAC - ${rideData!!.getString("C_ac_nac")}")
                            }
                        }

                        // 🔹 STUDENTS
                        Text(
                            "Students Joined",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E5F5)
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
/*
                                studentsList.forEach { student ->

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        // 👤 Avatar Circle
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    Color(0xFF4169E1),
                                                    shape = RoundedCornerShape(50)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.getString("S_name").first().toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {

                                            Text(
                                                text = student.getString("S_name"),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )

                                            Text(
                                                text = student.optString("College_name",""),
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )

                                            Text(
                                                text = "${student.optString("course","")} • ${student.optString("branch","")} • ${student.optString("year","")} Year",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }*/
                                studentsList.forEach { student ->

                                    // 🔥 NEW DATA (coming from backend)
                                    val pickup = student.optString("student_pickup", "N/A")
                                    val drop = student.optString("student_drop", "N/A")
                                    val fare = student.optString("fare_per_student", "0")

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        // 👤 Avatar Circle
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    Color(0xFF4169E1),
                                                    shape = RoundedCornerShape(50)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.getString("S_name").first().toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {

                                            Text(
                                                text = student.getString("S_name"),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )

                                            Text(
                                                text = student.optString("College_name",""),
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )

                                            Text(
                                                text = "${student.optString("course","")} • ${student.optString("branch","")} • ${student.optString("year","")} Year",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )

                                            // 🔥 NEW: ROUTE
                                            Text(
                                                text = "Route: $pickup → $drop",
                                                fontSize = 12.sp,
                                                color = Color(0xFF1E88E5)
                                            )

                                            // 🔥 NEW: FARE
                                            Text(
                                                text = "Pays: ₹$fare",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 🔥 ACTION BUTTONS
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onOpenChat(rideId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D245B)
                            )
                        ) {
                            Text("Open Chat", color = Color.White)
                        }

                        //  Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onTrackRide(rideId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D245B)
                            )
                        ) {
                            Text(" Track Ride", color = Color.White)
                        }


                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}