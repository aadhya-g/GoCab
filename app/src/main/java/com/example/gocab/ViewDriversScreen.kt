@file:OptIn(ExperimentalMaterial3Api::class)
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.gocab.model.AdminDriver

@Composable
fun AViewDriversScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val viewModel: AdminDriversViewModel = viewModel()

    val drivers by viewModel.drivers.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadDrivers("")
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 BACKGROUND
        Image(
            painter = painterResource(id = R.drawable.img_7),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {


            // 🔍 SEARCH BAR (PREMIUM)
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.loadDrivers(it)
                },
                label = { Text("Search by name or licence") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F3F6),
                    unfocusedContainerColor = Color(0xFFF1F3F6),
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                drivers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers found", color = Color.White)
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(drivers) { driver ->
                            DriverItemUI(
                                driver = driver,
                                onVerify = { viewModel.verifyDriver(driver.D_eid) },
                                onReject = { viewModel.rejectDriver(driver.D_eid) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DriverItemUI(
    driver: AdminDriver,
    onVerify: () -> Unit,
    onReject: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
       // elevation = CardDefaults.cardElevation(8.dp)
    ) {


        Column(modifier = Modifier.padding(16.dp)) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)   // 🔥 important
                ) {
                    Text(
                        driver.D_name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF3F51B5)
                    )
                    Text("Licence: ${driver.D_licence_no}")
                    Text("Email: ${driver.D_eid}")
                }

                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Verify") },
                            onClick = {
                                expanded = false
                                actionType = "verify"
                                showDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Reject") },
                            onClick = {
                                expanded = false
                                actionType = "reject"
                                showDialog = true
                            }
                        )
                    }
                }
            }

        }
    }

    // 🔹 Dialog same as before
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (actionType == "verify") onVerify() else onReject()
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            },
            title = { Text("Confirmation") },
            text = { Text("Are you sure you want to $actionType this driver?") }
        )
    }
}


/*
@Composable
fun AViewDriversScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminDriversViewModel = viewModel()

    val drivers by viewModel.drivers.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var search by remember { mutableStateOf("") }

    // 🔹 Load all drivers first time
    LaunchedEffect(Unit) {
        viewModel.loadDrivers("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drivers List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔍 SEARCH BAR
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.loadDrivers(it)   // search by name or licence
                },
                label = { Text("Search by driver name or licence") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                drivers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers found")
                    }
                }

                else -> {
                    LazyColumn {

                        items(drivers) { driver ->
                            DriverItem(
                                driver = driver,
                                onVerify = { viewModel.verifyDriver(driver.D_eid) },
                                onReject = { viewModel.rejectDriver(driver.D_eid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverItem(
    driver: AdminDriver,
    onVerify: () -> Unit,
    onReject: () -> Unit,

    ) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text("Name: ${driver.D_name}", fontWeight = FontWeight.Bold)
                    Text("Licence No: ${driver.D_licence_no}")
                    Text("Email: ${driver.D_eid}")
                }

                // 🔹 3 Dot Menu
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Verify") },
                            onClick = {
                                expanded = false
                                actionType = "verify"
                                showDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Reject") },
                            onClick = {
                                expanded = false
                                actionType = "reject"
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 🔹 Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {

                    if (actionType == "verify") {
                        onVerify()
                    } else {
                        onReject()
                    }

                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            },
            title = { Text("Confirmation") },
            text = {
                Text("Are you sure you want to $actionType this driver?")
            }
        )
    }
}

*/

//ViewDriversScreen.kt
/*@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gocab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.model.AdminDriver

@Composable
fun AViewDriversScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminDriversViewModel = viewModel()

    val drivers by viewModel.drivers.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var search by remember { mutableStateOf("") }

    // 🔹 Load all drivers first time
    LaunchedEffect(Unit) {
        viewModel.loadDrivers("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drivers List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔍 SEARCH BAR
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.loadDrivers(it)   // search by name or licence
                },
                label = { Text("Search by driver name or licence") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                drivers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers found")
                    }
                }

                else -> {
                    LazyColumn {
                        items(drivers) { driver ->
                            DriverItem(driver)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverItem(driver: AdminDriver) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${driver.D_name}", fontWeight = FontWeight.Bold)
            Text("Licence No: ${driver.D_licence_no}")
            Text("Email: ${driver.D_eid}")
        }
    }
}
//ViewDriversScreen.kt

 */


/*
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.gocab
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.model.AdminDriver

@Composable
fun AViewDriversScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminDriversViewModel = viewModel()

    val drivers by viewModel.drivers.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var search by remember { mutableStateOf("") }

    // 🔹 Load all drivers first time
    LaunchedEffect(Unit) {
        viewModel.loadDrivers("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drivers List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔍 SEARCH BAR
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.loadDrivers(it)   // search by name or licence
                },
                label = { Text("Search by driver name or licence") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                drivers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No drivers found")
                    }
                }

                else -> {
                    LazyColumn {

                        items(drivers) { driver ->
                            DriverItem(
                                driver = driver,
                                onVerify = { viewModel.verifyDriver(driver.D_eid) },
                                onReject = { viewModel.rejectDriver(driver.D_eid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverItem(
    driver: AdminDriver,
    onVerify: () -> Unit,
    onReject: () -> Unit,

    ) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text("Name: ${driver.D_name}", fontWeight = FontWeight.Bold)
                    Text("Licence No: ${driver.D_licence_no}")
                    Text("Email: ${driver.D_eid}")
                }

                // 🔹 3 Dot Menu
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Verify") },
                            onClick = {
                                expanded = false
                                actionType = "verify"
                                showDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Reject") },
                            onClick = {
                                expanded = false
                                actionType = "reject"
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 🔹 Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {

                    if (actionType == "verify") {
                        onVerify()
                    } else {
                        onReject()
                    }

                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            },
            title = { Text("Confirmation") },
            text = {
                Text("Are you sure you want to $actionType this driver?")
            }
        )
    }
}*/
//ViewDriversScreen.kt

