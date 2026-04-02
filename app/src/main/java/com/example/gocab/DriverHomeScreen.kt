package com.example.gocab
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.ui.screens.DriverRideRequestScreen
import com.example.gocab.viewmodel.DriverProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun DriverHomeScreen(
    onRideRequests: () -> Unit = {},
    onConfirmedRides: () -> Unit = {}
) {


    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ Background image (natural color)
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Welcome, Driver 👋",
                fontSize = 32.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What would you like to do?",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🚖 View Ride Requests Button
            Button(
                onClick = onRideRequests,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Ride Requests", color = Color.Black,fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💰 View Confirmed Ride Button
            Button(
                onClick = onConfirmedRides,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                )
            ) {
                Icon(
                    Icons.Filled.Money,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmed Rides", color = Color.White,fontSize = 18.sp)
            }

            // ✅ Push quote to bottom
            Spacer(modifier = Modifier.weight(1f))

            // 💰 Driver earning quote
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.35f) // 🔥 TRANSPARENT
                ),
                elevation = CardDefaults.cardElevation(0.dp) // optional: cleaner look
            ) {
                Text(
                    text = "Every ride increases your income. More rides, more earnings! 💰🚖",
                    fontSize = 18.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(
                            top = 13.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp
                        )
                )
            }

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAppContainer(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onLogout: () -> Unit,
    selectedRideId: Int?,                  // ✅ ADD
    onRideSelected: (Int) -> Unit         // ✅ ADD

) {
    val currentScreenState = MainScreenNavigationHelper.currentScreen

    var isEditing by remember { mutableStateOf(false) } // ✅ ADD THIS
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { newScreen ->
                    onScreenChange(newScreen)
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                val title = when (currentScreen) {
                    Screen.DRIVER_HOME -> "Driver Dashboard"
                    Screen.DRIVER_PROFILE -> "My Profile"
                    Screen.RIDE_REQUESTS -> "New Ride Requests"
                    Screen.CONFIRMED_RIDES -> "My Rides"
                 //  Screen.MONTHLY_EARNINGS -> "My Earnings"
                    else -> "GoCab Driver"
                }
                CenterAlignedTopAppBar(
                    title = { Text(title, color = Color.White, fontSize = 22.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.DRIVER_PROFILE) {
                            IconButton(onClick = { isEditing = !isEditing }) {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                    //contentDescription = if (isEditing) "Close Edit" else "Edit Profile",
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Log.d("SCREEN_DEBUG", "Current screen = $currentScreen, rideId = $selectedRideId")
                when (currentScreen) {
                    Screen.DRIVER_HOME -> DriverHomeScreen(
                        onRideRequests = { onScreenChange(Screen.RIDE_REQUESTS) },
                        onConfirmedRides = { onScreenChange(Screen.CONFIRMED_RIDES) }
                    )
                    Screen.CONFIRMED_RIDES -> {

                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }

                        val driverId = FirebaseAuth.getInstance().currentUser?.email ?: ""

                        ConfirmedRidesScreen(
                            driverId = driverId,
                            onRideClick = { rideId ->
                                onRideSelected(rideId)   // ✅ instead of local state
                                onScreenChange(Screen.DRIVER_RIDE_DETAIL)
                            }
                        )
                    }
                    /*Screen.CONFIRMED_RIDES -> {

                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }

                        val driverId = FirebaseAuth.getInstance().currentUser?.email ?: ""


                        ConfirmedRidesScreen(
                            driverId = driverId,
                            onRideClick = { rideId ->
                                selectedRideId = rideId
                                onScreenChange(Screen.DRIVER_RIDE_DETAIL)
                            }
                        )
                    }*/
                    /*Screen.CHAT_SCREEN -> {

                        if (selectedRideId == null) {
                            Text("No Ride Selected")   // debug
                        } else {
                            GroupChatScreen(
                                rideId = selectedRideId!!,
                                onHome = { onScreenChange(Screen.DRIVER_RIDE_DETAIL) },
                                onBack = { onScreenChange(Screen.DRIVER_RIDE_DETAIL) }
                            )
                        }
                    }*/
                    Screen.DRIVER_PROFILE -> {
                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }
                        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                        if (firebaseUid != null) {
                            val driverProfileViewModel: DriverProfileViewModel = viewModel()

                            DriverProfileScreen(
                                onLogout = onLogout,
                                firebase_uid = firebaseUid,
                                viewModel = driverProfileViewModel,
                                isEditing = isEditing,
                                onEditToggle = { isEditing = it }
                            )

                        } else {
                            // safety fallback
                            onScreenChange(Screen.LOGIN)
                        }
                    }
/*
                    Screen.DRIVER_RIDE_DETAIL -> {

                        if (selectedRideId != null) {

                            DriverRideDetailScreen(
                                rideId = selectedRideId!!,
                                onBack = { onScreenChange(Screen.CONFIRMED_RIDES) },
                                onOpenChat = { rideId ->
                                    selectedRideId = rideId
                                    //currentScreenState.value = Screen.CHAT_SCREEN
                                    onScreenChange(Screen.CHAT_SCREEN)
                                }
                            )
                        }
                    }*/
                    /*Screen.DRIVER_RIDE_DETAIL -> {

                        if (selectedRideId != null) {

                            DriverRideDetailScreen(
                                rideId = selectedRideId!!,
                                onBack = { onScreenChange(Screen.CONFIRMED_RIDES) },
                                onOpenChat = { rideId ->
                                    selectedRideId = rideId
                                    onScreenChange(Screen.CHAT_SCREEN)   // ✅ ONLY THIS
                                }
                            )
                        }
                    }*/
                    Screen.DRIVER_RIDE_DETAIL -> {

                        if (selectedRideId == null) {

                            Text("No Ride Selected ❌")   // 👈 debug

                        } else {

                            DriverRideDetailScreen(
                                rideId = selectedRideId!!,
                                onBack = { onScreenChange(Screen.CONFIRMED_RIDES) },
                                onOpenChat = { rideId ->
                                    onRideSelected(rideId)
                                    onScreenChange(Screen.CHAT_SCREEN)
                                }
                            )
                        }
                    }
                    Screen.RIDE_REQUESTS -> {

                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }

                        // Pass logged-in driver ID here
                        //val driverId = loggedInDriverId   // 🔥 Use your actual stored driver ID
                        val driverId = FirebaseAuth.getInstance().currentUser?.email ?: ""
                        //DriverRideRequestScreen(driverId = driverId)
                        DriverRideRequestScreen()
                    }

                    else -> DriverHomeScreen()
                }
            }
        }
    }
}
@Composable
fun DrawerContent(
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF3F51B5))
        ) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "🚖 GoCab Driver",
                    fontSize = 26.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, bottom = 30.dp)
                )

                DrrawerItem("My Profile", Icons.Filled.Person) { onNavigate(Screen.DRIVER_PROFILE) }
                DrrawerItem("Ride Requests", Icons.Filled.DirectionsCar) { onNavigate(Screen.RIDE_REQUESTS) }
                DrrawerItem("Confirmed Rides", Icons.Filled.CheckCircle) { onNavigate(Screen.CONFIRMED_RIDES) }
               // DrrawerItem("Monthly Earnings", Icons.Filled.Money) { onNavigate(Screen.MONTHLY_EARNINGS) }
                DrrawerItem("Logout", Icons.AutoMirrored.Filled.ExitToApp) { onLogout() }
            }
        }
    }
}
@Composable
fun DrrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}



/*
package com.example.gocab
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.viewmodel.DriverProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun DriverHomeScreen(
    onRideRequests: () -> Unit = {},
    onMonthlyEarnings: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ Background image (natural color)
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Welcome, Driver 👋",
                fontSize = 32.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What would you like to do?",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🚖 View Ride Requests Button
            Button(
                onClick = onRideRequests,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                )
            ) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Ride Requests", color = Color.Black,fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💰 View Earnings Button
            Button(
                onClick = onMonthlyEarnings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                )
            ) {
                Icon(
                    Icons.Filled.Money,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Earnings", color = Color.White,fontSize = 18.sp)
            }

            // ✅ Push quote to bottom
            Spacer(modifier = Modifier.weight(1f))

            // 💰 Driver earning quote
            Text(
                text = "Every ride increases your income. More rides, more earnings! 💰🚖",
                fontSize = 18.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    )
            )

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverAppContainer(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) } // ✅ ADD THIS
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { newScreen ->
                    onScreenChange(newScreen)
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                val title = when (currentScreen) {
                    Screen.DRIVER_HOME -> "Driver Dashboard"
                    Screen.DRIVER_PROFILE -> "My Profile"
                    Screen.RIDE_REQUESTS -> "New Ride Requests"
                    Screen.CONFIRMED_RIDES -> "My Rides"
                    Screen.MONTHLY_EARNINGS -> "My Earnings"
                    else -> "GoCab Driver"
                }
                CenterAlignedTopAppBar(
                    title = { Text(title, color = Color.White, fontSize = 22.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.DRIVER_PROFILE) {
                            IconButton(onClick = { isEditing = !isEditing }) {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                    //contentDescription = if (isEditing) "Close Edit" else "Edit Profile",
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    Screen.DRIVER_HOME -> DriverHomeScreen(
                        onRideRequests = { onScreenChange(Screen.RIDE_REQUESTS) },
                        onMonthlyEarnings = { onScreenChange(Screen.MONTHLY_EARNINGS) }
                    )

                    Screen.DRIVER_PROFILE -> {
                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }
                        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                        if (firebaseUid != null) {
                            val driverProfileViewModel: DriverProfileViewModel = viewModel()

                            DriverProfileScreen(
                                onLogout = onLogout,
                                firebase_uid = firebaseUid,
                                viewModel = driverProfileViewModel,
                                isEditing = isEditing,
                                onEditToggle = { isEditing = it }
                            )

                        } else {
                            // safety fallback
                            onScreenChange(Screen.LOGIN)
                        }
                    }


                    Screen.RIDE_REQUESTS -> {
                        BackHandler { onScreenChange(Screen.DRIVER_HOME) }
                        RideRequestScreen(
                            onAccept = {
                                Toast.makeText(context, "Ride Accepted!", Toast.LENGTH_SHORT).show()
                                onScreenChange(Screen.DRIVER_HOME)
                            },
                            onReject = {
                                Toast.makeText(context, "Ride Rejected!", Toast.LENGTH_SHORT).show()
                                onScreenChange(Screen.DRIVER_HOME)
                            }
                        )
                    }

                    else -> DriverHomeScreen()
                }
            }
        }
    }
}
@Composable
fun DrawerContent(
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF3F51B5))
        ) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "🚖 GoCab Driver",
                    fontSize = 26.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, bottom = 30.dp)
                )

                DrrawerItem("My Profile", Icons.Filled.Person) { onNavigate(Screen.DRIVER_PROFILE) }
                DrrawerItem("Ride Requests", Icons.Filled.DirectionsCar) { onNavigate(Screen.RIDE_REQUESTS) }
                DrrawerItem("Confirmed Rides", Icons.Filled.CheckCircle) { onNavigate(Screen.CONFIRMED_RIDES) }
                DrrawerItem("Monthly Earnings", Icons.Filled.Money) { onNavigate(Screen.MONTHLY_EARNINGS) }
                DrrawerItem("Logout", Icons.AutoMirrored.Filled.ExitToApp) { onLogout() }
            }
        }
    }
}
@Composable
fun DrrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}
*/
