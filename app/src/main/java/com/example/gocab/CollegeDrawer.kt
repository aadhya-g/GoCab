package com.example.gocab

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CollegeDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onViewStudents: () -> Unit,
    onViewDrivers: () -> Unit,
    //onVerifyDriver: () -> Unit,
    onScheduledRides: () -> Unit,
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
                .background(Color(0xFF4169E1))
        ) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "🏫 College Admin",
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, bottom = 30.dp)
                )

                DrawerItem1("View Students", Icons.Filled.Person, onViewStudents)
                DrawerItem1("View Drivers", Icons.Filled.DirectionsCar, onViewDrivers)
                //DrawerItem1("Verify Driver", Icons.Filled.Verified, onVerifyDriver)
                DrawerItem1("Scheduled Rides", Icons.Filled.DateRange, onScheduledRides)
               // DrawerItem1("Ride Alerts", Icons.Filled.Notifications, onRideAlerts)
                DrawerItem1("Logout", Icons.Filled.ExitToApp) {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem1(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White)
    }
}