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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun SearchRideNavScreen(
    onBackToHome: () -> Unit,
){
    val navController = rememberNavController()
    var selectedFilters by remember { mutableStateOf(FilterData()) }
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchRideScreen(
                onBackToHome = onBackToHome,
                onApplyFiltersClick = { navController.navigate("filters") },
                onSearchClick = { pickup, drop ->
                    navController.navigate("drivers/$pickup/$drop")
                },

            )
        }
        composable("drivers/{pickup}/{drop}") { backStackEntry ->
            val pickup = backStackEntry.arguments?.getString("pickup") ?: ""
            val drop = backStackEntry.arguments?.getString("drop") ?: ""
            DriverListScreen(
                pickup = pickup,
                drop = drop,
                filters = selectedFilters,
                navController = navController
            )
        }
        composable("filters") {
            FiltersScreen(
                onApplyClick = { filters ->
                    selectedFilters = filters
                    navController.popBackStack()
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

// ---------------- SearchRideScreen ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRideScreen(
    onBackToHome: () -> Unit,
    onApplyFiltersClick: () -> Unit,
    onSearchClick: (String, String) -> Unit,
)
{
    BackHandler {onBackToHome()}
    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search Ride", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBackToHome() }) {
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
            // Background Image
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
