package com.example.gocab

import android.content.Context
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
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailsScreen(
    rideId: Int,
    navController: NavController
) {

    val context = LocalContext.current
    val studentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var rideData by remember { mutableStateOf<JSONObject?>(null) }
    var studentsList by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var detailedPickup by remember { mutableStateOf("") }
    var detailedDrop by remember { mutableStateOf("") }
    var pickupCity by remember { mutableStateOf("") }
    var dropCity by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

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
        ){

            // 🌄 Background Image
            Image(
                painter = painterResource(id = R.drawable.img_6),
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
                return@Box
            }

            if (rideData == null) {
                Text(
                    "Failed to load ride details",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    //Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.92f)
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
                                color = Color(0xFF3F51B5),

                                )
                            /*Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)   // Light Blue
                                )
                            ) {
                                Column(

                                    modifier = Modifier.padding(18.dp),
                                    //verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DetailRoww("Name -  ${rideData!!.getString("D_name")}")
                                    DetailRoww("Phone - ${rideData!!.getString("D_phone_no")}")
                                    DetailRoww("Gender - ${rideData!!.getString("D_gender")}")
                                    DetailRoww("Rating - ${rideData!!.optString("D_avg_rating", "N/A")}")
                                    DetailRoww("City - ${rideData!!.getString("current_city")}")
                                }
                            }*/

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {

                                val verifiedCount = rideData!!.optInt("verifiedCount", 0)

                                Column(
                                    modifier = Modifier.padding(18.dp),
                                //    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {

                                    // 🔥 Name + Verified Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = "Name - ${rideData!!.getString("D_name")}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )

                                        if (verifiedCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))

                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // 🔥 Verified Text
                                    if (verifiedCount > 0) {
                                        Text(
                                            text = "Verified by $verifiedCount colleges",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 12.sp
                                        )
                                    }

                                    DetailRoww("Phone - ${rideData!!.getString("D_phone_no")}")
                                    DetailRoww("Gender - ${rideData!!.getString("D_gender")}")
                                    DetailRoww(
                                        "Rating - ${
                                            rideData!!.optString(
                                                "D_avg_rating",
                                                "N/A"
                                            )
                                        }"
                                    )
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
                                    containerColor = Color(0xFFE8F5E9)   // Light Green
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    //verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    DetailRoww("Car Name - ${rideData!!.getString("C_name")}")
                                    DetailRoww("Number - ${rideData!!.getString("C_number")}")
                                    DetailRoww("Model - ${rideData!!.getString("C_model")}")
                                    DetailRoww("AC/NAC - ${rideData!!.getString("C_ac_nac")}")
                                    DetailRoww("Seater - ${rideData!!.getString("C_seater")}")
                                }
                            }

                            /*Text(
                                "Car Information",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
                            )

                            Text("Car Name: ${rideData!!.getString("C_name")}")
                            Text("Number: ${rideData!!.getString("C_number")}")
                            Text("Model: ${rideData!!.getString("C_model")}")
                            Text("AC/NAC: ${rideData!!.getString("C_ac_nac")}")
                            Text("Seater: ${rideData!!.getString("C_seater")}")

                            Divider()*/

                            // 🔹 STUDENTS SECTION

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
                                    }
                                }
                            }
                            /*Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF3E5F5)
                                )
                            ) {
                                studentsList.forEach { student ->



                                        // 👤 Student Icon Circle
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
                                                text = student.getString("S_name").first()
                                                    .toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {

                                            Text(
                                                text = student.getString("S_name"),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            )

                                            Text(
                                                text = student.optString("College_name", ""),
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )

                                            Text(
                                                text = "${
                                                    student.optString(
                                                        "course",
                                                        ""
                                                    )
                                                } • ${
                                                    student.optString(
                                                        "branch",
                                                        ""
                                                    )
                                                } • ${student.optString("year", "")}",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                }
                            }*/

                            /*Text(
                                "Students Joined",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
                            )

                            studentsList.forEach { student ->
                                Text("• ${student.getString("S_name")}")
                            }

                            Divider()*/

                            // 🔹 FORM SECTION
                            Text(
                                "Request Details",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
                            )

                            OutlinedTextField(
                                value = detailedPickup,
                                onValueChange = { detailedPickup = it },
                                label = { Text("Detailed Pickup Location") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = detailedDrop,
                                onValueChange = { detailedDrop = it },
                                label = { Text("Detailed Drop Location") },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {  val studentEmail =
                                    FirebaseAuth.getInstance().currentUser?.email ?: ""
                                    requestRide(
                                        context = context,
                                        rideId = rideId,
                                        studentEmail = studentEmail,
                                        pickup = detailedPickup,
                                        drop = detailedDrop,
                                        pickupCity = pickupCity,
                                        dropCity = dropCity,
                                        date = rideData!!.getString("R_date")
                                    ) {
                                        Toast.makeText(
                                            context,
                                            "Ride Requested Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                       // navController.popBackStack()
                                        navController.navigate("home") {
                                            popUpTo("search") { inclusive = true }
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
                                    "Request Ride",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

        }
    }
}


@Composable
fun DetailRoww(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Black
        )
        Text(
            text = " ",
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}


fun fetchFullRideDetails(
    context: Context,
    rideId: Int,
    onResult: (JSONObject, List<JSONObject>) -> Unit
) {

    val url = "http://10.206.39.204:5000/api/ride/full-details/$rideId"

    val request = JsonObjectRequest(
        Request.Method.GET,
        url,
        null,
        { response ->

            val ride = response.getJSONObject("ride")
            val studentsArray = response.getJSONArray("students")

            val list = mutableListOf<JSONObject>()
            for (i in 0 until studentsArray.length()) {
                list.add(studentsArray.getJSONObject(i))
            }

            onResult(ride, list)
        },
        { error ->
            Log.e("DETAILS_ERROR", error.toString())
        }
    )

    Volley.newRequestQueue(context).add(request)
}
fun requestRide(
    context: Context,
    rideId: Int,
    studentEmail: String,
    pickup: String,
    drop: String,
    pickupCity: String,
    dropCity: String,
    date: String,
    onSuccess: () -> Unit
) {

    val url = "http://10.206.39.204:5000/api/ride/join"

    val jsonBody = JSONObject().apply {
        put("rideId", rideId)
        put("studentEmail", studentEmail)
        put("pickup", pickup)
        put("drop", drop)
        put("pickupCity", pickupCity)
        put("dropCity", dropCity)
        put("date", date)
    }

    val request = JsonObjectRequest(
        Request.Method.POST,
        url,
        jsonBody,
        {
            onSuccess()
        },
        { error ->
            Log.e("JOIN_ERROR", error.toString())
        }
    )

    Volley.newRequestQueue(context).add(request)
}