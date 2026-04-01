package com.example.gocab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.viewmodel.UpcomingRideViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledRidesScreen1(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onRideHistory: () -> Unit,
    onHome: () -> Unit,
    // onOpenChat: (Int) -> Unit,
    onRideClick: (Int) -> Unit,
    viewModel: UpcomingRideViewModel = viewModel()
) {

    val studentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var isLoaded by remember { mutableStateOf(false) }

    // 🔙 Handle phone back
    BackHandler {
        onHome()   // 👈 back = home
    }

    LaunchedEffect(Unit) {
        if (!isLoaded) {
            viewModel.loadRides(studentEmail)
            isLoaded = true
        }
    }

    Scaffold(
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
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

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

                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                viewModel.rides.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Upcoming Rides 🚘",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        items(viewModel.rides) { ride ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRideClick(ride.R_id)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                )

                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    // 🔹 Route
                                    Text(
                                        //text = "${ride.Pickup_loc} ➜ ${ride.Drop_loc}",
                                        text = "${ride.pickup_city} ➜ ${ride.drop_city}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3F51B5)
                                    )
// 🔹 Date + Time


                                    Text(
                                        text = " Date: ${ride.R_date.substringBefore("T")}",

                                        )


                                    /*Text(text = "🕒 ${
                                            ride.R_timing
                                                ?.substringAfter("T")
                                                ?.substringBefore(".") ?: "--"
                                        }", fontSize = 13.sp, color = Color.DarkGray)
                                    }*/

                                    // 🔹 Status Chip (CONFIRMED)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Confirmed",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
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
}

/*
package com.example.gocab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.viewmodel.UpcomingRideViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledRidesScreen1(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onRideHistory: () -> Unit,
    onHome: () -> Unit,
   // onOpenChat: (Int) -> Unit,
    onRideClick: (Int) -> Unit,
    viewModel: UpcomingRideViewModel = viewModel()
) {

    val studentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var isLoaded by remember { mutableStateOf(false) }

    // 🔙 Handle phone back
    BackHandler {
        onHome()   // 👈 back = home
    }

    LaunchedEffect(Unit) {
        if (!isLoaded) {
            viewModel.loadRides(studentEmail)
            isLoaded = true
        }
    }

    Scaffold(
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
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

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

                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                viewModel.rides.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Upcoming Rides 🚘",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        items(viewModel.rides) { ride ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRideClick(ride.R_id)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                )

                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    // 🔹 Route
                                    Text(
                                        text = "${ride.Pickup_loc} ➜ ${ride.Drop_loc}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3F51B5)
                                    )
// 🔹 Date + Time


                                    Text(
                                        text = "📅 ${ride.R_date.substringBefore("T")}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )

                                    */
/*Text(text = "🕒 ${
                                            ride.R_timing
                                                ?.substringAfter("T")
                                                ?.substringBefore(".") ?: "--"
                                        }", fontSize = 13.sp, color = Color.DarkGray)
                                    }
                                    // 🔹 Status Chip (CONFIRMED)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF2E7D32).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Confirmed",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
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
}
*/

/*@Composable
fun GroupChatScreen(rideId: String) {

    val user = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    LaunchedEffect(Unit) {
        listenMessages(rideId) {
            messages = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1C2E))
    ) {

        // 🔹 Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->

                val isMe = msg.sender == user

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {

                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .background(
                                if (isMe) Color(0xFF3F51B5) else Color.DarkGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {

                        Column {

                            Text(
                                text = if (isMe) "You" else "Them",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )

                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 🔹 Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("type a message...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(25.dp)),
                shape = RoundedCornerShape(25.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (message.isNotEmpty()) {
                        sendMessage(
                            rideId,
                            user,
                            user.substringBefore("@"),  // 👈 NAME
                            message
                        )
                        message = ""
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                )
            ) {
                Text("Send", color = Color.White)
            }
        }
    }
}*/

/*
//ScheduledRidesScreen.kt
package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.viewmodel.UpcomingRideViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ScheduledRidesScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onRideHistory: () -> Unit,
    onHome: () -> Unit,
    viewModel: UpcomingRideViewModel = viewModel()
) {
    AppScaffold(
        title = "Scheduled Rides",
        onLogout = onLogout,
        onProfile = onProfile,
        onHistory=onRideHistory
    ) { padding ->
        val studentEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        LaunchedEffect(Unit) {
            viewModel.loadRides(studentEmail)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Background image FIRST
            Image(
                painter = painterResource(id = R.drawable.img_1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier

                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                when {

                    viewModel.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading...", color = Color.White)
                        }
                    }

                    viewModel.rides.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Upcoming Rides", color = Color.White)
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.rides) { ride ->

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {

                                        // Driver Name + Badge
                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Text(
                                                text = ride.D_name,
                                                fontSize = 18.sp
                                            )

                                            if (ride.verifiedCount > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E7D32)
                                                )
                                            }
                                        }

                                        if (ride.verifiedCount > 0) {
                                            Text(
                                                text = "Verified by ${ride.verifiedCount} colleges",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text("Pickup: ${ride.Pickup_loc}")
                                        Text("Drop: ${ride.Drop_loc}")
                                        Text("Date: ${ride.R_date}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }}


*/