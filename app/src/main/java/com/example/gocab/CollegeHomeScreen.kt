package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ------------------- HOME SCREEN -------------------
@Composable
fun CollegeHomeScreen(
    onAViewStudents: () -> Unit,
    onViewDrivers: () -> Unit,
    onScheduledRides: () -> Unit,
    onLogout: () -> Unit,
    //onVerifyDriver: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 🔹 Background image
        Image(
            painter = painterResource(id = R.drawable.img_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 Title
            Text(
                text = "Welcome, Admin 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2A5E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text ="Manage your college rides efficiently",
                fontSize = 18.sp,
                color = Color(0xFF1E2A5E)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 🔹 ONLY 2 BUTTONS
            AdminDashboardButton(
                text = "Scheduled Rides",
                icon = Icons.Default.DateRange,
                bgColor = Color(0xFFFFC107),
                onClick = onScheduledRides
            )

            AdminDashboardButton(
                text = "View Driver",
                icon = Icons.Default.Notifications,
                bgColor = Color(0xFF3F51B5),
                onClick = onViewDrivers
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Stay updated. Keep rides smooth 🚕📢",
                fontSize = 18.sp,
                color = Color.DarkGray
            )
        }
    }
}
@Composable
fun AdminDashboardButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(15.dp))
        Text(text, fontSize = 16.sp, color = Color.White)
    }
}
//CollegeHomeScreen.kt