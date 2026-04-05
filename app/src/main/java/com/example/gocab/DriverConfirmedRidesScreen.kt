package com.example.gocab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.app.ActivityCompat
import com.example.gocab.network.RetrofitClient
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.ui.DataModels.DriverRideResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/* ---------------- TIME MERGE FUNCTION ---------------- */

fun getRideTimeInMillis(dateIso: String, timeIso: String): Long {
    return try {
        val utcFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        val parsedDateUtc = utcFormat.parse(dateIso)
        val parsedTimeUtc = utcFormat.parse(timeIso)

        val localCalendar = Calendar.getInstance()
        localCalendar.time = parsedDateUtc!!

        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.time = parsedTimeUtc!!

        localCalendar.set(Calendar.HOUR_OF_DAY, utcCalendar.get(Calendar.HOUR_OF_DAY))
        localCalendar.set(Calendar.MINUTE, utcCalendar.get(Calendar.MINUTE))
        localCalendar.set(Calendar.SECOND, 0)

        localCalendar.timeInMillis

    } catch (e: Exception) {
        0L
    }
}

/* ---------------- SCREEN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ConfirmedRidesScreen(
    driverId: String,
    onRideClick: (Int) -> Unit   // 🔥 ADD THIS
){

    var confirmedRides by remember { mutableStateOf<List<DriverRideResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    /* -------- CONNECT SOCKET WHEN SCREEN OPENS -------- */

    LaunchedEffect(Unit) {
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
    }

    /* -------- CLEANUP WHEN SCREEN CLOSES -------- */

    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            SocketHandler.closeConnection()
        }
    }

    /* -------- START RIDE -------- */

    fun startRide(rideId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.startRide(rideId)

                if (response.isSuccessful) {
                    SocketHandler.joinRide(rideId)

                    withContext(Dispatchers.Main) {
                        confirmedRides = confirmedRides.map {
                            if (it.R_id == rideId)
                                it.copy(R_status = "Started")
                            else it
                        }
                    }

                    withContext(Dispatchers.Main) {
                        startLocationUpdates(
                            rideId,
                            fusedLocationClient,
                            context
                        ) { callback ->
                            locationCallback = callback
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d("START_RIDE", e.localizedMessage ?: "Error")
            }
        }
    }

    /* -------- END RIDE -------- */

    fun endRide(rideId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.endRide(rideId)

                if (response.isSuccessful) {

                    withContext(Dispatchers.Main) {
                        confirmedRides = confirmedRides.map {
                            if (it.R_id == rideId)
                                it.copy(R_status = "Completed")
                            else it
                        }
                    }

                    locationCallback?.let {
                        fusedLocationClient.removeLocationUpdates(it)
                    }
                }

            } catch (e: Exception) {
                Log.d("END_RIDE", e.localizedMessage ?: "Error")
            }
        }
    }


    /* LOAD RIDES */
    LaunchedEffect(Unit) {

        isLoading = true

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getConfirmedRides(driverId)
            }

            if (response.isSuccessful && response.body() != null) {

                val rides = response.body()!!

                Log.d("RIDES_DEBUG", "DriverId: $driverId")
                Log.d("RIDES_DEBUG", "Total rides: ${rides.size}")

                rides.forEach {
                    Log.d(
                        "RIDES_DEBUG",
                        "Ride: ${it.R_id} | ${it.R_date} | ${it.R_timing} | ${it.R_status}"
                    )
                }

                confirmedRides = rides

            } else {
                errorMessage = "Failed: ${response.code()}"
                Log.e("API_ERROR", "Error body: ${response.errorBody()?.string()}")
            }

        } catch (e: Exception) {

            Log.e("API_EXCEPTION", e.message.toString())

            errorMessage = e.message ?: "Server error"

        } finally {
            isLoading = false
        }
    }
    /*   LaunchedEffect(Unit) {
        try {
            val response: Response<List<DriverRideResponse>> =
                RetrofitInstance.api.getConfirmedRides(driverId)

                        if (response.isSuccessful) {
                            confirmedRides = response.body() ?: emptyList()
                        }
            if (response.isSuccessful) {
                val rides = response.body() ?: emptyList()

                Log.d("RIDES_DEBUG", "DriverId: $driverId")
                Log.d("RIDES_DEBUG", "Total rides: ${rides.size}")
                rides.forEach {
                    Log.d("RIDES_DEBUG", "Ride: ${it.R_id} | ${it.R_date} | ${it.R_timing} | ${it.R_status}")
                }

                confirmedRides = rides
            }
            else {
                errorMessage = "Failed to load rides"
            }

        } catch (e: Exception) {
            errorMessage = "Server error"
        } finally {
            isLoading = false
        }
    }*/

    /*Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Scheduled Rides", color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { onHome() }) {
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
    ) { padding ->*/
    /* UI */
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        //  Background Image
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // 🌫 Dark Overlay
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

                errorMessage != null -> Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )

                confirmedRides.isEmpty() -> Text(
                    text = "No Confirmed Rides Yet ",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(confirmedRides) { ride ->

                            val rideTimeMillis =
                                getRideTimeInMillis(ride.R_date, ride.R_timing)

                            val currentTime = System.currentTimeMillis()
                            val threeHoursInMillis = 3 * 60 * 60 * 1000

                            val isStartEnabled =
                                (rideTimeMillis - currentTime) in 0..threeHoursInMillis &&
                                        ride.R_status == "Accepted"
                          //  var selectedRideId by remember { mutableStateOf(0) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRideClick(ride.R_id)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {

                                    //  Route Highlight
                                    Text(
                                        text = " ${ride.pickup_city} ➜ ${ride.drop_city}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF3F51B5)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                  //  Text(" Date: ${ride.R_date}")
                                    Text(
                                        text = " Date: ${ride.R_date.substringBefore("T")}",

                                    )
                                    Text(
                                        text = " Time: ${ride.R_timing.substringAfter("T").substring(0,5)}",

                                    )
                                    //Text(" Time: ${ride.R_timing}")
                                    Text(" Distance: ${ride.distance_km} km")

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = " Fare: ₹${ride.fare_amount}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3F51B5)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    //  Status Badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = ride.R_status,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    //  Start Button
                                    if (ride.R_status == "Accepted") {
                                        Button(
                                            onClick = { startRide(ride.R_id) },
                                            enabled = isStartEnabled,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF3F51B5)
                                            )
                                        ) {
                                            Text("Start Ride ", color = Color.White)
                                        }
                                    }

                                    //  End Button
                                    if (ride.R_status == "Started") {
                                        Button(
                                            onClick = { endRide(ride.R_id) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD32F2F)
                                            )
                                        ) {
                                            Text("End Ride ", color = Color.White)
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
    /* ---------------- LOCATION FUNCTION ---------------- */

    fun startLocationUpdates(
        rideId: Int,
        fusedLocationClient: FusedLocationProviderClient,
        context: android.content.Context,
        onCallbackReady: (LocationCallback) -> Unit
    ) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .setWaitForAccurateLocation(true)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d("GPS_TEST", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    val data = JSONObject()
                    data.put("rideId", rideId)
                    data.put("latitude", location.latitude)
                    data.put("longitude", location.longitude)

                    SocketHandler.sendDriverLocation(data)
                }
            }
        }

        onCallbackReady(callback)

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
    }


/*@Composable
fun ConfirmedRidesScreen(driverId: String) {

    var confirmedRides by remember { mutableStateOf<List<DriverRideResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    *//* -------- CONNECT SOCKET WHEN SCREEN OPENS -------- *//*

    LaunchedEffect(Unit) {
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
    }

    *//* -------- CLEANUP WHEN SCREEN CLOSES -------- *//*

    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            SocketHandler.closeConnection()
        }
    }

    *//* -------- START RIDE -------- *//*

    fun startRide(rideId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.startRide(rideId)

                if (response.isSuccessful) {
                    SocketHandler.joinRide(rideId)

                    withContext(Dispatchers.Main) {
                        confirmedRides = confirmedRides.map {
                            if (it.R_id == rideId)
                                it.copy(R_status = "Started")
                            else it
                        }
                    }

                    withContext(Dispatchers.Main) {
                        startLocationUpdates(
                            rideId,
                            fusedLocationClient,
                            context
                        ) { callback ->
                            locationCallback = callback
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d("START_RIDE", e.localizedMessage ?: "Error")
            }
        }
    }

    *//* -------- END RIDE -------- *//*

    fun endRide(rideId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.endRide(rideId)

                if (response.isSuccessful) {

                    withContext(Dispatchers.Main) {
                        confirmedRides = confirmedRides.map {
                            if (it.R_id == rideId)
                                it.copy(R_status = "Completed")
                            else it
                        }
                    }

                    locationCallback?.let {
                        fusedLocationClient.removeLocationUpdates(it)
                    }
                }

            } catch (e: Exception) {
                Log.d("END_RIDE", e.localizedMessage ?: "Error")
            }
        }
    }

    *//* -------- LOAD CONFIRMED RIDES -------- *//*

    LaunchedEffect(Unit) {
        try {
            val response: Response<List<DriverRideResponse>> =
                RetrofitInstance.api.getConfirmedRides(driverId)
            *//*
                        if (response.isSuccessful) {
                            confirmedRides = response.body() ?: emptyList()
                        }*//*
            if (response.isSuccessful) {
                val rides = response.body() ?: emptyList()

                Log.d("RIDES_DEBUG", "DriverId: $driverId")
                Log.d("RIDES_DEBUG", "Total rides: ${rides.size}")
                rides.forEach {
                    Log.d("RIDES_DEBUG", "Ride: ${it.R_id} | ${it.R_date} | ${it.R_timing} | ${it.R_status}")
                }

                confirmedRides = rides
            }
            else {
                errorMessage = "Failed to load rides"
            }

        } catch (e: Exception) {
            errorMessage = "Server error"
        } finally {
            isLoading = false
        }
    }

    *//* -------- UI -------- *//*

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 Background
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )

        // 🌫 Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.30f))
        )
        when {

            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            errorMessage != null -> Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )

            confirmedRides.isEmpty() -> Text(
                text = "No Confirmed Rides Yet",
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            else -> {
                LazyColumn { modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)

                    items(confirmedRides) { ride ->

                        val rideTimeMillis =
                            getRideTimeInMillis(ride.R_date, ride.R_timing)

                        val currentTime = System.currentTimeMillis()
                        val threeHoursInMillis = 3 * 60 * 60 * 1000

                        val isStartEnabled =
                            (rideTimeMillis - currentTime) in 0..threeHoursInMillis &&
                                    ride.R_status == "Accepted"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Text("Pickup: ${ride.initial_loc}", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Drop: ${ride.final_loc}")
                                Text("Date: ${ride.R_date}")
                                Text("Time: ${ride.R_timing}")
                                Text("Fare: ₹${ride.fare_amount}")
                                Text("Distance: ${ride.distance_km} km")

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Status: ${ride.R_status}",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (ride.R_status == "Accepted") {
                                    Button(
                                        onClick = { startRide(ride.R_id) },
                                        enabled = isStartEnabled,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start Ride")
                                    }
                                }

                                if (ride.R_status == "Started") {
                                    Button(
                                        onClick = { endRide(ride.R_id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("End Ride")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}*/





/*
package com.example.gocab
// ----------- IMPORTS -----------
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import com.example.gocab.network.RetrofitInstance
import com.example.gocab.ui.DataModels.DriverRideResponse

// ----------- SCREEN -----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmedRidesScreen(
    driverId: String
) {

    val scope = rememberCoroutineScope()
    var confirmedRides by remember { mutableStateOf<List<DriverRideResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response =
                RetrofitInstance.api.getConfirmedRides(driverId)

            if (response.isSuccessful) {
                confirmedRides = response.body() ?: emptyList()
            } else {
                errorMessage = "Failed to load rides"
            }

        } catch (e: Exception) {
            errorMessage = "Server error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 Background
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )

        // 🌫 Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.30f))
        )

        when {

            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            confirmedRides.isEmpty() -> {
                Text(
                    text = "No Confirmed Rides Yet 🚘",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(confirmedRides) { ride ->

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                // 🔹 Route Highlight
                                Text(
                                    text = " ${ride.initial_loc}  ➜  ${ride.final_loc}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )

                                Divider()

                                // 🔹 Ride Details
                                Text(" Date: ${ride.R_date.substringBefore("T")}")
                                Text(
                                    " Time: ${
                                        ride.R_timing
                                            .substringAfter("T")
                                            .substringBefore(".")
                                    }"
                                )
                                Text(" Distance: ${ride.distance_km} km")

                                Text(
                                    " Fare: ₹${ride.fare_amount}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 🔹 Status Badge Style
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF2E7D32).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = ride.R_status,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold
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
