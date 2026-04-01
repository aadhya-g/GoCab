package com.example.gocab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp(
    adminEmail: String
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ✅ TITLE STATE (MAIN CHANGE)
    var title by remember { mutableStateOf("College Admin") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CollegeDrawer(
                drawerState = drawerState,
                scope = scope,

                onViewStudents = {
                    title = "Students List"
                    navController.navigate("students/$adminEmail")
                },

                onViewDrivers = {
                    title = "Drivers List"
                    navController.navigate("drivers")
                },

                onScheduledRides = {
                    title = "Scheduled Rides"
                    navController.navigate("scheduled_rides")
                },



                onLogout = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }

    ) {
        Scaffold(
            topBar = {
                if (currentRoute != "login") {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(title, color = Color.White) // ✅ dynamic title
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        title = "College Admin" // ✅ reset

                        CollegeHomeScreen(
                            onAViewStudents = {
                                title = "Students List"
                                navController.navigate("students/$adminEmail")
                            },
                            onViewDrivers = {
                                title = "Drivers List"
                                navController.navigate("drivers")
                            },
                            onScheduledRides = {
                                title = "Scheduled Rides"
                                navController.navigate("scheduled_rides")
                            },

                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )

                    }

                    composable("students/{adminEmail}") { backStackEntry ->
                        val adminEmail =
                            backStackEntry.arguments?.getString("adminEmail") ?: ""

                        ViewStudentsScreen(
                            adminEmail = adminEmail,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("drivers") {
                        AViewDriversScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("scheduled_rides") {
                        ScheduledRidesScreen(
                            navController = navController,
                            adminEmail = adminEmail
                        )
                    }



                    composable("login") {
                        LoginScreen(
                            onSignUpClicked = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onHelpClick = {
                                navController.navigate("help")
                            }
                        )
                    }
                }
            }
        }
    }
}//CollegeApp.kt



/*
package com.example.gocab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp(
    adminEmail: String
)
{
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CollegeDrawer(
                drawerState = drawerState,
                scope = scope,
                onViewStudents = {
                    navController.navigate("students/$adminEmail")
                    scope.launch { drawerState.close() }
                }
                ,
                onViewDrivers = {
                    navController.navigate("drivers")
                    scope.launch { drawerState.close() }
                },
                onScheduledRides = {
                    navController.navigate("scheduled_rides")
                    scope.launch { drawerState.close() }
                },
                onRideAlerts = {
                    navController.navigate("alerts")
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }

                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != "login") {
                    CenterAlignedTopAppBar(
                        title = { Text("College Admin", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        CollegeHomeScreen(
                            onViewStudents = { navController.navigate("students/$adminEmail") },
                            onViewDrivers = { navController.navigate("drivers") },

                            onScheduledRides = { navController.navigate("scheduled_rides") },
                            onRideAlerts = { navController.navigate("alerts") },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("students/{adminEmail}") { backStackEntry ->
                        val adminEmail = backStackEntry.arguments?.getString("adminEmail") ?: ""

                        ViewStudentsScreen(
                            adminEmail = adminEmail,
                            onBack = { navController.popBackStack() }
                        )
                    }



                    composable("drivers") {
                        AViewDriversScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // ✅ FIXED: renamed composable
                    composable("scheduled_rides") {
                        ScheduledRidesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                   */
/* composable("alerts") {
                        RideAlertsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }*//*


                    composable("login") {
                        LoginScreen(
                            onSignUpClicked = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onHelpClick = {        // 👈 ADD THIS
                                navController.navigate("help")
                            } //collegeApp.kt
                        )
                    }
                }
            }
        }
    }
}
*/
/*
package com.example.gocab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp(
    adminEmail: String
)
{
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CollegeDrawer(
                drawerState = drawerState,
                scope = scope,
                onViewStudents = {
                    navController.navigate("students/$adminEmail")
                    scope.launch { drawerState.close() }
                }
                ,
                onViewDrivers = {
                    navController.navigate("drivers")
                    scope.launch { drawerState.close() }
                },
                onScheduledRides = {
                    navController.navigate("scheduled_rides")
                    scope.launch { drawerState.close() }
                },
                onRideAlerts = {
                    navController.navigate("alerts")
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }

                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != "login") {
                    CenterAlignedTopAppBar(
                        title = { Text("College Admin", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFF4169E1)
                        )
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        CollegeHomeScreen(
                            onViewStudents = { navController.navigate("students/$adminEmail") },
                            onViewDrivers = { navController.navigate("drivers") },

                            onScheduledRides = { navController.navigate("scheduled_rides") },
                            onRideAlerts = { navController.navigate("alerts") },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("students/{adminEmail}") { backStackEntry ->
                        val adminEmail = backStackEntry.arguments?.getString("adminEmail") ?: ""

                        ViewStudentsScreen(
                            adminEmail = adminEmail,
                            onBack = { navController.popBackStack() }
                        )
                    }



                    composable("drivers") {
                        ViewDriversScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // ✅ FIXED: renamed composable
                    composable("scheduled_rides") {
                        ScheduledRidesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("alerts") {
                        RideAlertsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            onSignUpClicked = {
                                navController.navigate("signup")
                            },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideAlertsScreen(onBack: () -> Boolean) {
    TODO("Not yet implemented")
}*/

