

package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceHomeScreens(
    //onViewStudents: () -> Unit,
    //onViewDrivers: () -> Unit,
   // onViewComplaints: () -> Unit,
    onLogout: () -> Unit,
    // onProfile: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("Home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF3F51B5))
                ) {//0xFF1E88E5
                    Column {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = "🧰 Maintenance",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 20.dp, bottom = 30.dp)
                        )

                        MDrawerItem("My Profile", Icons.Default.Person) {
                            selectedScreen = "Profile"
                            scope.launch { drawerState.close() }
                            //MaintenanceMyProfileScreen();
                            //onProfile()
                        }


                        MDrawerItem("View Students", Icons.Default.School) {
                            selectedScreen = "Students"
                            scope.launch { drawerState.close() }
                            //onViewStudents()
                        }

                        MDrawerItem("View Drivers", Icons.Default.DirectionsCar) {
                            selectedScreen = "Drivers"
                            scope.launch { drawerState.close() }
                            //onViewDrivers()
                        }



                        Spacer(modifier = Modifier.weight(1f))

                        MDrawerItem("Logout", Icons.Default.ExitToApp) {
                            scope.launch {
                                drawerState.close()
                                onLogout()
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = getMaintenanceTopBarTitle(selectedScreen),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                )
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                when (selectedScreen) {

                    // ✅ FRONT PAGE UI (SCREENSHOT STYLE)
                    "Home" -> {
                        Box(modifier = Modifier.fillMaxSize()) {

                            Image(
                                painter = painterResource(id = R.drawable.img_6),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Spacer(modifier = Modifier.height(60.dp))

                                Text(
                                    text = "Welcome Maintenance 👋",
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "What would you like to do?",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                HomeButton(
                                    text = "View Students",
                                    icon = Icons.Default.School,
                                    background = Color(0xFFFFC107),
                                    onClick = {
                                        selectedScreen = "Students"
                                    }
                                )

                                Spacer(modifier = Modifier.height(15.dp))

                                HomeButton(
                                    text = "View Drivers",
                                    icon = Icons.Default.DirectionsCar,
                                    background = Color(0xFF3F51B5),
                                    onClick = {
                                        selectedScreen = "Drivers"
                                    }
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = "Manage records easily & efficiently 🚀",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(bottom = 40.dp)
                                )
                            }
                        }
                    }

                    "Profile" -> {
                        MaintenanceMyProfileScreen(
                            firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            onBack = { selectedScreen = "Home" }
                        )
                    }



                    "Students" -> {
                        StudentsScreen(onBack = { selectedScreen = "Home" });
                    }

                    "Drivers" -> {
                        MViewDriverScreen(onBack = { selectedScreen = "Home" });
                    }


                }
            }
        }
    }
}

fun getMaintenanceTopBarTitle(screen: String): String {
    return when (screen) {
        "Home" -> "Maintenance Home"
        "Profile" -> "My Profile"
        "Students" -> "View Students"
        "Drivers" -> "View Drivers"

        else -> "Maintenance"
    }
}

@Composable
fun StudentsScreen(
    viewModel: MaintenanceStudentsViewModel<Any?> =
        viewModel(),
    onBack: () -> Unit
) {
    val students = viewModel.students.collectAsState().value

    // Fetch once when screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchStudents()
    }

    MaintenanceStudentsScreen(
        students = students,
        onBack = onBack
    )
}
@Composable
fun MDrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun HomeButton(
    text: String,
    icon: ImageVector,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(background, shape = MaterialTheme.shapes.large)
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = text, tint = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}