package com.example.gocab

//import androidx.compose.foundation.layout.weight
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onHome: () -> Unit

) {
    BackHandler {
        onHome()   // 👈 back = home
    }
    val context = LocalContext.current
    var showChatDialog by remember { mutableStateOf(false) }
    var userMessage by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Confirm Ride", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
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

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // Search Bar
            item {
                var searchText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search your issue") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // FAQs
            item {
                SectionTitle("FAQs")
                Spacer(modifier = Modifier.height(8.dp))

                HelpItem("How to book a ride?")
                HelpItem("How to cancel a ride?")

                HelpItem("Lost & Found Assistance")

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Safety Section
            item {
                SectionTitle("Safety & Emergency")
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    SafetyButton("SOS", Color.Red) {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:112")
                        context.startActivity(intent)
                    }

                    SafetyButton("Safety Tips", Color(0xFF2196F3)) {}
                }



                Spacer(modifier = Modifier.height(20.dp))
            }

            // Contact Section
            item {
                SectionTitle("Contact Us")
                Spacer(modifier = Modifier.height(8.dp))

                ContactItem(Icons.Default.Call, "Call Us +91 9876543210") {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:9876543210")
                    context.startActivity(intent)
                }

                ContactItem(Icons.Default.Chat, "Chat with Support") {showChatDialog = true}

                ContactItem(Icons.Default.Email, "Email Us support@gocab.com") {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:support@gocab.com")
                    context.startActivity(intent)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Policies
            item {
                SectionTitle("Terms & Policies")
                Spacer(modifier = Modifier.height(8.dp))

                HelpItem("Cancellation Policy")
                HelpItem("Privacy Policy")
                HelpItem("University Email Login Policy")
            }
        }
    }
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChatDialog = false
                        userMessage = ""
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Chat with Support") },
            text = {
                Column {
                    Text("Describe your issue below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        placeholder = { Text("Type your message...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}
@Composable
fun HelpItem(text: String) {

    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                modifier = Modifier.weight(1f),   // 🔥 pushes arrow to extreme right
                fontSize = 16.sp
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }

    if (showDialog) {

        when (text) {

            // 🔹 BOOK RIDE
            "How to book a ride?" -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("OK") }
                    },
                    title = { Text("How to Book a Ride") },
                    text = {
                        Text(
                            "1️⃣ Login to your GoCab account.\n\n" +
                                    "2️⃣ Tap on 'Book Ride' from Home Screen.\n\n" +
                                    "3️⃣ Enter pickup & destination location.\n\n" +
                                    "4️⃣ Select suitable driver/ride.\n\n" +
                                    "5️⃣ Confirm your ride request.\n\n" +
                                    "6️⃣ Wait for driver confirmation.\n\n" +
                                    "🎉 Enjoy your safe ride!"
                        )
                    }
                )
            }

            // 🔹 CANCEL RIDE
            "How to cancel a ride?" -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("OK") }
                    },
                    title = { Text("How to Cancel a Ride") },
                    text = {
                        Text(
                            "1️⃣ Go to 'Ride History'.\n\n" +
                                    "2️⃣ Select the upcoming ride.\n\n" +
                                    "3️⃣ Tap on 'Cancel Ride'.\n\n" +
                                    "4️⃣ Confirm cancellation.\n\n" +
                                    "⚠ Cancellation charges may apply depending on timing."
                        )
                    }
                )
            }

            // 🔹 LOST & FOUND
            "Lost & Found Assistance" -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("OK") }
                    },
                    title = { Text("Lost & Found Assistance") },
                    text = {
                        Text(
                            "👜 If you left something in the vehicle:\n\n" +
                                    "1️⃣ Go Cab is not responsible for whatever item you have lost in the cab.\n\n" +

                                    "📞 If you want to recollect the item contact that driver"
                        )
                    }
                )
            }

            "Privacy Policy" -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("OK") }
                    },
                    title = { Text("Privacy Policy") },
                    text = {
                        Text(
                            "🔐 Privacy Policy:\n\n" +
                                    "• GoCab collects only necessary information (name, email, ride data).\n\n" +
                                    "• Your university email is used for authentication purposes.\n\n" +
                                    "• We do NOT share your personal data with third parties.\n\n" +
                                    "• Ride details are stored securely for safety and record keeping.\n\n"

                        )
                    }
                )
            }

            // 🔹 UNIVERSITY POLICY
            "University Email Login Policy" -> {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { Text("OK") }
                    },
                    title = { Text("University Email Login Policy") },
                    text = {
                        Text(
                            "• Students and Admins must login using official university email.\n\n" +
                                    "• Student & Admin email domains must match.\n\n" +
                                    "Example:\nstudent@abcuniversity.edu\nadmin@abcuniversity.edu"
                        )
                    }
                )
            }
        }
    }
}
//@Composable
//fun SafetyButton(text: String, color: Color, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .weight(1f)
//            .height(80.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .fillMaxSize()
//                .background(color.copy(alpha = 0.1f))
//        ) {
//            Text(
//                text = text,
//                color = color,
//                fontWeight = FontWeight.Bold
//            )
//        }
//    }
//}
@Composable
fun RowScope.SafetyButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.1f))
        ) {
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text)
        }
    }
}//HelpScreen.kt