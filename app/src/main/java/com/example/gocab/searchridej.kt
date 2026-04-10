package com.example.gocab

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.gocab.network.RetrofitClient
import com.example.gocab.util.JoinRideFilterData
import org.json.JSONObject
import java.util.Calendar

// ================= NAVIGATION =================

@Composable
fun JoinRideSearchNav1(
    onBackToHome: () -> Unit
){

    val navController = rememberNavController()

    var selectedFilters by remember {
        mutableStateOf(JoinRideFilterData())
    }

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {

        composable("search") {
            JoinRideSearchScreen1(
                navController = navController,
                onBack = onBackToHome,
                onApplyFiltersClick = {
                    navController.navigate("filters")
                }
            )
        }
        composable("results/{pickup}/{drop}/{date}") { backStackEntry ->

            val pickup = Uri.decode(backStackEntry.arguments?.getString("pickup") ?: "")
            val drop = Uri.decode(backStackEntry.arguments?.getString("drop") ?: "")
            val date = Uri.decode(backStackEntry.arguments?.getString("date") ?: "")

            SearchResultsScreen(
                pickup = pickup,
                drop = drop,
                date = date,
                filters = selectedFilters,
                navController = navController
            )
        }

//        composable("results/{pickup}/{drop}/{date}") { backStackEntry ->
//
//            val pickup = backStackEntry.arguments?.getString("pickup") ?: ""
//            val drop = backStackEntry.arguments?.getString("drop") ?: ""
//            val date = backStackEntry.arguments?.getString("date") ?: ""
//
//            SearchResultsScreen(
//                pickup = pickup,
//                drop = drop,
//                date = date,
//                filters = selectedFilters,
//                navController = navController
//            )
//        }

        composable("filters") {
            JoinRideFilterScreen(
                onApplyClick = { filters: JoinRideFilterData ->
                    selectedFilters = filters
                    navController.popBackStack()
                }
            )
        }

        composable("rideDetails/{rideId}") { backStackEntry ->
            val rideId = backStackEntry.arguments
                ?.getString("rideId")
                ?.toIntOrNull() ?: 0

            RideDetailsScreen(
                rideId = rideId,
                navController = navController
            )
        }
    }
}

// ================= SEARCH SCREEN =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRideSearchScreen1(
    navController: NavController,
    onBack: () -> Unit,
    onApplyFiltersClick: () -> Unit,


){

    BackHandler { onBack() }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var travelDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Join Ride", color = Color.White) },

                // 🔥 BACK BUTTON ADD HERE
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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

            Image(
                painter = painterResource(id = R.drawable.img11),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                /* Text(
                text = "Join Ride",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )*/

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(modifier = Modifier.padding(20.dp)) {

                        OutlinedTextField(
                            value = pickup,
                            onValueChange = { pickup = it },
                            label = { Text("Pickup Location") },
                            placeholder = { Text("Enter Pickup") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = drop,
                            onValueChange = { drop = it },
                            label = { Text("Drop Location") },
                            placeholder = { Text("Enter Destination") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = travelDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date of Travel") },
                            placeholder = { Text("Select Date") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            travelDate = "%04d-%02d-%02d".format(y, m + 1, d)
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            /*Button(
                                onClick = {
                                    navController.navigate("results/$pickup/$drop/$travelDate")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Search")
                            }*/
                            Button(
                                onClick = {
                                    if (pickup.isBlank() || drop.isBlank() || travelDate.isBlank()) {
                                        Log.d("NAV", "Empty fields")
                                        return@Button
                                    }

                                    val encodedPickup = Uri.encode(pickup)
                                    val encodedDrop = Uri.encode(drop)
                                    val encodedDate = Uri.encode(travelDate)

                                    navController.navigate("results/$encodedPickup/$encodedDrop/$encodedDate")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Search")
                            }

                            OutlinedButton(
                                onClick = onApplyFiltersClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Apply Filters",
                                    fontSize = 12.sp   // 👈 tumne bola tha chota
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
// ================= API =================

    fun searchExistingRides(
        context: Context,
        pickup: String,
        drop: String,
        date: String,
        onResult: (List<RideSearchResult>) -> Unit
    ) {
        val url = RetrofitClient.BASE_URL + "api/ride/search-existing"
        val jsonBody = JSONObject().apply {
            put("pickupCity", pickup)
            put("dropCity", drop)
            put("date", date)
        }
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                val ridesArray = response.getJSONArray("rides")
                val list = mutableListOf<RideSearchResult>()

                for (i in 0 until ridesArray.length()) {
                    val obj = ridesArray.getJSONObject(i)

                    list.add(
                        RideSearchResult(
                            R_id = obj.getInt("R_id"),
                            D_name = obj.getString("D_name"),
                            R_date = obj.getString("R_date"),
                            R_timing = obj.optString("R_timing"),
                            distance_km = obj.getDouble("distance_km"),
                            fare_amount = obj.getDouble("fare_amount"),
                            seats_left = obj.optInt("seats_left", 0),
                            fare_per_student = obj.optDouble("fare_per_student", 0.0),
                            verifiedCount = obj.optInt("verifiedCount", 0),
                            colleges = obj.optString("colleges"), // ✅ FIXED
                            year = obj.optString("years"),
                            branch = obj.optString("branches"),
                            course = obj.optString("courses"),
                            rating=obj.optDouble("rating")
                        )
                    )
                }

                onResult(list)
            },
            { error ->
                Log.e("API_ERROR", error.toString())
            }
        )

        Volley.newRequestQueue(context).add(request)
    }



/*
package com.example.gocab
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

// ---------------- Join Ride Search Screen ----------------
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun JoinRideSearchScreen1(navController: NavController,
                          onBack: () -> Unit,
                          onApplyFiltersClick: () -> Unit) {
    // --- Handle physical back button ---
    BackHandler {
        onBack()
    }
    AppScaffold(

        title = "Join Ride",
        onProfile = { */
/* navigate to profile *//*
 },
        onHistory = { */
/* open history *//*
 },
        onScheduledRides = { */
/* open scheduled rides *//*
 },
        onLogout = { */
/* logout logic *//*
 }

    ) { paddingValues ->

        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        var pickup by remember { mutableStateOf("") }
        var drop by remember { mutableStateOf("") }
        var driverName by remember { mutableStateOf("") }
        var travelDate by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.img_5),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Ride",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        OutlinedTextField(
                            value = pickup,
                            onValueChange = { pickup = it },
                            label = { Text("Pickup Location") },
                            placeholder = { Text("Enter Pickup") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = drop,
                            onValueChange = { drop = it },
                            label = { Text("Drop Location") },
                            placeholder = { Text("Enter Destination") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        OutlinedTextField(
                            value = travelDate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Date of Travel") },
                            placeholder = { Text("Select Date") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            travelDate = "%04d-%02d-%02d".format(
                                                year,
                                                month + 1,
                                                dayOfMonth
                                            )
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
                        */
/*OutlinedTextField(
                            value = travelDate,
                            onValueChange = { travelDate = it },
                            label = { Text("Date of Travel") },
                            placeholder = { Text("Enter Date (YYYY-MM-DD)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )*//*


                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name (Optional)") },
                            placeholder = { Text("Enter Driver Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("results/$pickup/$drop/$travelDate")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Search")
                            }
                            OutlinedButton(
                                onClick = onApplyFiltersClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply Filters")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))


            }
        }
    }
}


// ---------------- NavHost for Join Ride Search ----------------
@Composable
fun JoinRideSearchNav1(onBackToHome: () -> Unit) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {
        */
/*composable("chat/{rideId}") { backStackEntry ->

            val rideId = backStackEntry.arguments?.getString("rideId") ?: ""

            GroupChatScreen(rideId = rideId)
        }*//*

        composable("search") {
            JoinRideSearchScreen1(
                navController = navController,
                onBack = onBackToHome,
                onApplyFiltersClick = {
                    navController.navigate("filters")
                }
            )
        }

        composable("results/{pickup}/{drop}/{date}") { backStackEntry ->

            val pickup = backStackEntry.arguments?.getString("pickup") ?: ""
            val drop = backStackEntry.arguments?.getString("drop") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""

            SearchResultsScreen(
                pickup = pickup,
                drop = drop,
                date = date,
                navController = navController
            )
        }

        // 🔥 THIS MUST BE INSIDE NavHost
        composable("rideDetails/{rideId}") { backStackEntry ->

            val rideId = backStackEntry.arguments
                ?.getString("rideId")
                ?.toIntOrNull() ?: 0

            RideDetailsScreen(
                rideId = rideId,
                navController = navController
            )
        }
    }
}
fun searchExistingRides(
    context: Context,
    pickup: String,
    drop: String,
    date: String,
    onResult: (List<RideSearchResult>) -> Unit
) {
    val url = "http://172.30.14.204:5000/api/ride/search-existing"

    val jsonBody = JSONObject().apply {
        put("pickupCity", pickup)
        put("dropCity", drop)
        put("date", date)
    }

    val request = JsonObjectRequest(
        Request.Method.POST,
        url,
        jsonBody,
        { response ->
            val ridesArray = response.getJSONArray("rides")
            val list = mutableListOf<RideSearchResult>()

            for (i in 0 until ridesArray.length()) {
                val obj = ridesArray.getJSONObject(i)

                list.add(
                    RideSearchResult(
                        R_id = obj.getInt("R_id"),
                        D_name = obj.getString("D_name"),
                        R_date = obj.getString("R_date"),
                        R_timing = obj.optString("R_timing"),
                        distance_km = obj.getDouble("distance_km"),
                        fare_amount = obj.getDouble("fare_amount"),
                        seats_left = obj.optInt("seats_left", 0),
                        fare_per_student = obj.optDouble("fare_per_student", 0.0),
                        verifiedCount = obj.optInt("verifiedCount", 0),
                        College_name = obj.optString("College_name"),
                        year = obj.optString("year"),
                        branch = obj.optString("branch"),
                        course = obj.optString("course")
                    )
                )
            }

            onResult(list)
        },
        { error ->
            Log.e("API_ERROR", error.toString())
        }
    )

    Volley.newRequestQueue(context).add(request)
}

*/

/*
package com.example.gocab
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

// ---------------- Join Ride Search Screen ----------------
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun JoinRideSearchScreen1(navController: NavController,
                          onBack: () -> Unit,
                          onApplyFiltersClick: () -> Unit)
{
    // --- Handle physical back button ---
    BackHandler {
        onBack()
    }
    AppScaffold(

        title = "Join Ride",
        onProfile = { */
/* navigate to profile *//*
 },
        onHistory = { */
/* open history *//*
 },
        onScheduledRides = { */
/* open scheduled rides *//*
 },
        onLogout = { */
/* logout logic *//*
 }

    ) { paddingValues ->
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        var pickup by remember { mutableStateOf("") }
        var drop by remember { mutableStateOf("") }
        var driverName by remember { mutableStateOf("") }
        var travelDate by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.img_5),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Ride",
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        OutlinedTextField(
                            value = pickup,
                            onValueChange = { pickup = it },
                            label = { Text("Pickup Location") },
                            placeholder = { Text("Enter Pickup") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = drop,
                            onValueChange = { drop = it },
                            label = { Text("Drop Location") },
                            placeholder = { Text("Enter Destination") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )


                        OutlinedTextField(
                            value = travelDate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Date of Travel") },
                            placeholder = { Text("Select Date") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            travelDate = "%04d-%02d-%02d".format(
                                                year,
                                                month + 1,
                                                dayOfMonth
                                            )
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
                        */
/*OutlinedTextField(
                            value = travelDate,
                            onValueChange = { travelDate = it },
                            label = { Text("Date of Travel") },
                            placeholder = { Text("Enter Date (YYYY-MM-DD)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)

                        )


                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name (Optional)") },
                            placeholder = { Text("Enter Driver Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("results/$pickup/$drop/$travelDate")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Search")
                            }
                            OutlinedButton(
                                onClick = onApplyFiltersClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Apply Filters")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))


            }
        }
    }
}


// ---------------- NavHost for Join Ride Search ----------------
@Composable
fun JoinRideSearchNav1(onBackToHome: () -> Unit) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {

        composable("search") {
            JoinRideSearchScreen1(
                navController = navController,
                onBack = onBackToHome,
                onApplyFiltersClick = {
                    navController.navigate("filters")
                }
            )
        }

        composable("results/{pickup}/{drop}/{date}") { backStackEntry ->

            val pickup = backStackEntry.arguments?.getString("pickup") ?: ""
            val drop = backStackEntry.arguments?.getString("drop") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""

            SearchResultsScreen(
                pickup = pickup,
                drop = drop,
                date = date,
                navController = navController
            )
        }

        // 🔥 THIS MUST BE INSIDE NavHost
        composable("rideDetails/{rideId}") { backStackEntry ->

            val rideId = backStackEntry.arguments
                ?.getString("rideId")
                ?.toIntOrNull() ?: 0

            RideDetailsScreen(
                rideId = rideId,
                navController = navController
            )
        }
    }
}
fun searchExistingRides(
    context: Context,
    pickup: String,
    drop: String,
    date: String,
    onResult: (List<RideSearchResult>) -> Unit
) {
    val url = "http://10.136.253.204:5000/api/ride/search-existing"

    val jsonBody = JSONObject().apply {
        put("pickupCity", pickup)
        put("dropCity", drop)
        put("date", date)
    }

    val request = JsonObjectRequest(
        Request.Method.POST,
        url,
        jsonBody,
        { response ->
            val ridesArray = response.getJSONArray("rides")
            val list = mutableListOf<RideSearchResult>()

            for (i in 0 until ridesArray.length()) {
                val obj = ridesArray.getJSONObject(i)

                list.add(
                    RideSearchResult(
                        R_id = obj.getInt("R_id"),
                        D_name = obj.getString("D_name"),
                        R_date = obj.getString("R_date"),
                        distance_km = obj.getDouble("distance_km"),
                        fare_amount = obj.getDouble("fare_amount"),
                        verifiedCount = obj.getInt("verifiedCount")
                    )
                )
            }

            onResult(list)
        },
        { error ->
            Log.e("API_ERROR", error.toString())
        }
    )

    Volley.newRequestQueue(context).add(request)
}*/
