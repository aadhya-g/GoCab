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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gocab.ui.ConfirmRideScreen
import com.example.gocab.ui.DriverListScreen
import com.example.gocab.util.FilterData

// ---------------- Search + Filters Navigation ----------------
@Composable
fun SearchRideNavScreen(onBackToHome: () -> Unit) {

    val navController = rememberNavController()

    // ✅ STORE FILTERS HERE
    var selectedFilters by remember { mutableStateOf(FilterData()) }

    NavHost(navController = navController, startDestination = "search") {

        // 🔍 SEARCH SCREEN
        composable("search") {
            SearchRideScreen(
                onBackToHome = onBackToHome,
                onApplyFiltersClick = {
                    navController.navigate("filters")
                },
                onSearchClick = { pickup, drop ->
                    navController.navigate("drivers/$pickup/$drop")
                }
            )
        }

        // 🚗 DRIVER LIST
        composable("drivers/{pickup}/{drop}") { backStackEntry ->

            val pickup = backStackEntry.arguments?.getString("pickup") ?: ""
            val drop = backStackEntry.arguments?.getString("drop") ?: ""

            DriverListScreen(
                pickup = pickup,
                drop = drop,
                filters = selectedFilters,   // ✅ FIX HERE
                navController = navController
            )
        }

        // ✅ FILTER SCREEN
        composable("filters") {
            FiltersScreen(
                onApplyClick = { filters ->
                    selectedFilters = filters   // ✅ SAVE FILTERS
                    navController.popBackStack() // go back
                }
            )
        }

        composable("confirmRide") {
            ConfirmRideScreen(navController = navController)
        }

        composable("rideSuccess") {
            RideSuccessScreen(navController = navController)
        }
    }
}
/*
@Composable
fun SearchRideNavScreen(onBackToHome: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {

        composable("search") {
            SearchRideScreen(
                onBackToHome = onBackToHome,
                onApplyFiltersClick = { navController.navigate("filters") },
                onSearchClick = { pickup, drop ->
                    navController.navigate("drivers/$pickup/$drop")
                }
            )
        }

        composable("drivers/{pickup}/{drop}") { backStackEntry ->
            DriverListScreen(

                pickup = backStackEntry.arguments?.getString("pickup") ?: "",
                drop = backStackEntry.arguments?.getString("drop") ?: "",
                navController = navController
            )
        }
        /*composable("confirmRide") {
            ConfirmRideScreen(
                driverEmail = SelectedRideHolder.driverEmail,
                pickup = SelectedRideHolder.pickup,
                drop = SelectedRideHolder.drop,
                distanceKm = SelectedRideHolder.distanceKm,
                fare = SelectedRideHolder.fare,
                customerId = SelectedRideHolder.customerId,
                studentEmail = SelectedRideHolder.studentEmail,
                onRideConfirmed = {
                    navController.navigate("search") {
                        popUpTo("search") { inclusive = true }
                    }
                }
            )
        }*/
        composable("confirmRide") {
            ConfirmRideScreen(
                navController = navController
            )
        }
        // 👇 ADD THIS HERE
        composable("rideSuccess") {
            RideSuccessScreen(navController = navController)
        }

        composable("filters") {
            FiltersScreen(
                onApplyClick = { navController.navigate("search") }
            )
        }
    }
}*/


// ---------------- SearchRideScreen ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRideScreen(
    onBackToHome: () -> Unit,
    onApplyFiltersClick: () -> Unit,
    onSearchClick: (String, String) -> Unit   // 👈 ADD THIS
)

{
    // --- Handle physical back button ---
    BackHandler {
        onBackToHome()
    }

    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }

    AppScaffold(
        title = "Search Ride",
        onProfile = { },
        onHistory = { /* open ride history */ },
        onScheduledRides = { /* open scheduled rides */ },
        onLogout = { /* logout logic */ }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 🌄 Background Image
            Image(
                painter = painterResource(id = R.drawable.img10),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🧾 Semi-transparent overlay for contrast
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
                /*Text(
                    text = "Search Ride",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )*/
                Spacer(modifier = Modifier.height(20.dp))

                // Card for Input Fields
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
                            placeholder = { Text("Enter Pickup Point") },
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
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name (optional)") },
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
                                    if (pickup.isNotBlank() && drop.isNotBlank()) {
                                        onSearchClick(pickup, drop)
                                    }
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
                                Text("Apply Filters",fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}






/*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gocab.ui.ConfirmRideScreen
import com.example.gocab.ui.DriverListScreen

// ---------------- Search + Filters Navigation ----------------
@Composable
fun SearchRideNavScreen(onBackToHome: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {

        composable("search") {
            SearchRideScreen(
                onBackToHome = onBackToHome,
                onApplyFiltersClick = { navController.navigate("filters") },
                onSearchClick = { pickup, drop ->
                    navController.navigate("drivers/$pickup/$drop")
                }
            )
        }

        composable("drivers/{pickup}/{drop}") { backStackEntry ->
            DriverListScreen(

                pickup = backStackEntry.arguments?.getString("pickup") ?: "",
                drop = backStackEntry.arguments?.getString("drop") ?: "",
                navController = navController
            )
        }
        */
/*composable("confirmRide") {
            ConfirmRideScreen(
                driverEmail = SelectedRideHolder.driverEmail,
                pickup = SelectedRideHolder.pickup,
                drop = SelectedRideHolder.drop,
                distanceKm = SelectedRideHolder.distanceKm,
                fare = SelectedRideHolder.fare,
                customerId = SelectedRideHolder.customerId,
                studentEmail = SelectedRideHolder.studentEmail,
                onRideConfirmed = {
                    navController.navigate("search") {
                        popUpTo("search") { inclusive = true }
                    }
                }
            )
        }*//*

        composable("confirmRide") {
            ConfirmRideScreen(
                navController = navController
            )
        }
        // 👇 ADD THIS HERE
        composable("rideSuccess") {
            RideSuccessScreen(navController = navController)
        }

        composable("filters") {
            FiltersScreen(
                onApplyClick = { navController.navigate("search") }
            )
        }
    }
}


// ---------------- SearchRideScreen ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRideScreen(
    onBackToHome: () -> Unit,
    onApplyFiltersClick: () -> Unit,
    onSearchClick: (String, String) -> Unit   // 👈 ADD THIS
)

{
    // --- Handle physical back button ---
    BackHandler {
        onBackToHome()
    }

    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }

    AppScaffold(
        title = "Search Ride",
        onProfile = { },
        onHistory = { */
/* open ride history *//*
 },
        onScheduledRides = { */
/* open scheduled rides *//*
 },
        onLogout = { */
/* logout logic *//*
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
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🧾 Semi-transparent overlay for contrast
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
                    text = "Search Ride",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Card for Input Fields
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
                            placeholder = { Text("Enter Pickup Point") },
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
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name (optional)") },
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
                                    if (pickup.isNotBlank() && drop.isNotBlank()) {
                                        onSearchClick(pickup, drop)
                                    }
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
            }
        }
    }
}


*/
