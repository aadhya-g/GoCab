package com.example.gocab
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MViewDriverScreen(
    viewModel: MaintenanceDriversViewModel = viewModel(),
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    val drivers = viewModel.drivers.collectAsState().value
    var searchQuery by remember { mutableStateOf("") }

    val filteredDrivers = drivers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.licenceNumber.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDrivers()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 BACKGROUND
        Image(
            painter = painterResource(id = R.drawable.img13),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🌫 OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                   // .padding(padding)
                    .padding(16.dp)
            ) {

                // 🔍 PREMIUM SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search driver") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFF1F3F6),
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredDrivers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers found", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredDrivers) { driver ->
                            DriverCardUI(driver)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DriverCardUI(driver: DriverMaintenance) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = driver.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Email: ${driver.email}")
            Text("Licence: ${driver.licenceNumber}")
        }
    }
}









/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MViewDriverScreen(
    viewModel: MaintenanceDriversViewModel = viewModel()
) {
    val drivers = viewModel.drivers.collectAsState().value
    var searchQuery by remember { mutableStateOf("") }

    val filteredDrivers = drivers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery) ||
                it.licenceNumber.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDrivers()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.img_7),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.25f
        )

        Scaffold(
            containerColor = Color.Transparent,
            /*topBar = {
                TopAppBar(
                    title = { Text("View Drivers") }
                )
            }*/
        ) { padding ->

            // ⚠️ Column is IMPORTANT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // 🔍 SEARCH BAR (NOW VISIBLE)
                SearchBar(
                    hint = "Search driver...",
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDrivers) { driver ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Name: ${driver.name}", fontWeight = FontWeight.Bold)
                                Text("Email: ${driver.email}")
                                Text("Licence: ${driver.licenceNumber}")
                            }
                        }
                    }
                }
            }
        }
    }
}*/