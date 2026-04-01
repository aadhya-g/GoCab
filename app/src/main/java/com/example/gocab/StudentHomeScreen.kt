package com.example.gocab

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// 🎨 GoCab Theme Colors
val PrimaryYellow = Color(0xFFFFC107)
val PrimaryBlue = Color(0xFF3F51B5)
val DarkText = Color(0xFF1E2A5E)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentScreen: () -> Screen,
    onBookRide: () -> Unit,
    onViewRides: () -> Unit,
    onLogout: () -> Unit,          // ✅ Logout callback
    onProfile: () -> Unit = {},
    onHistory: () -> Unit = {},
  //  onHelp:() ->Unit={},
    onScheduledRides: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
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
                        .background(Color(0xFF4169E1)) // Blue background
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = "🚗 GoCab",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 20.dp, bottom = 30.dp)
                        )

                        // ✅ Working drawer items
                        DrawerItem("My Profile", Icons.Filled.Person) { onProfile() }
                        DrawerItem("Ride History", Icons.Filled.List) { onHistory() } // ✅ Changed ListAlt → List
                        DrawerItem("Scheduled Rides", Icons.Filled.DateRange) { onScheduledRides() }
                        /*DrawerItem("Help & Support", Icons.Filled.Help) {
                            scope.launch {
                                drawerState.close()
                                MainScreenNavigationHelper.currentScreen.value = Screen.HELP
                            }
                        }*/
                        DrawerItem("Logout", Icons.Filled.ExitToApp) {
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
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "GoCab",
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
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF4169E1)
                    )
                )
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    // Background image
                    Image(
                        painter = painterResource(id = R.drawable.img_6),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.05f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(90.dp))

                        Text(
                            text = "Welcome, Student 👋",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "What would you like to do?",
                            fontSize = 18.sp,
                            color = DarkText
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // 🔶 Book Ride Button (Primary)
                        Button(
                            onClick = onBookRide,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Text(
                                text = "🚕  Book Ride",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }


                        Spacer(modifier = Modifier.height(18.dp))

                        // 🔷 View Rides Button (Secondary)
                        Button(
                            onClick = onViewRides,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Text(
                                text = "📋  View Rides",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 🧾 Bottom Info Card (FIXES READABILITY ISSUE)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.35f) // 🔥 TRANSPARENT
                            ),
                            elevation = CardDefaults.cardElevation(0.dp) // optional: cleaner look
                        ) {
                            Text(
                                text = "Enjoy safe and secure rides with your chosen driver and ride mate 🚘",
                                fontSize = 16.sp,
                                color = DarkText,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }

                    }
                }
            }

        )
    }
}

@Composable
fun DrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = Color.White, fontSize = 16.sp)
    }
}
