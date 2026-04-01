package com.example.gocab

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/* ---------------- DATE HELPERS ---------------- */
fun getDateLabel(timestamp: Long): String {

    if (timestamp == 0L) return ""

    val tz = TimeZone.getTimeZone("Asia/Kolkata")

    val messageDate = Calendar.getInstance(tz).apply {
        timeInMillis = timestamp
    }

    val today = Calendar.getInstance(tz)

    val yesterday = Calendar.getInstance(tz).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale("en", "IN")).format(Date(timestamp))
    }
}
/*fun getDateLabel(timestamp: Long): String {
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(timestamp))
    }
}*/

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale("en", "IN"))
    sdf.timeZone = TimeZone.getTimeZone("Asia/Kolkata")   // 🇮🇳 INDIA TIME
    return sdf.format(Date(timestamp))
}

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    rideId: Int,
    onHome: () -> Unit,
    onBack: () -> Unit
) {

    val user = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val currentUser = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var userName by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
   // var userName by remember { mutableStateOf("User") }


    val listState = rememberLazyListState()
    LaunchedEffect(rideId) {
        Log.d("CHAT_DEBUG", "Driver RideId = $rideId")
        listenMessages(rideId) {
            Log.d("CHAT_DEBUG", "Messages size = ${it.size}")
            messages = it
        }
    }
    /* 🔥 GET NAME FROM API */
    LaunchedEffect(Unit) {
        try {
            // 🔥 TRY student first
            val studentRes = RetrofitInstance.api.getStudentProfile(uid)

            if (studentRes.isSuccessful && studentRes.body()?.data != null) {
                userName = studentRes.body()?.data?.S_name ?: ""
            } else {
                // 🔥 else driver
                val driverRes = RetrofitInstance.api.getDriverProfile(uid)
                if (driverRes.isSuccessful) {
                    userName = driverRes.body()?.data?.D_name ?: ""
                }
            }

        } catch (e: Exception) {
            userName = "User"
        }
    }
    /*LaunchedEffect(Unit) {
        try {
            val res = RetrofitInstance.api.getStudentProfile(uid)
            if (res.isSuccessful) {
                userName = res.body()?.data?.S_name ?: "User"
                Log.d("CHAT_NAME", userName)
            }
        } catch (e: Exception) {
            Log.e("CHAT_NAME", "Error fetching name")
        }
    }*/

    /* 🔥 LISTEN REALTIME MESSAGES */
    /*LaunchedEffect(rideId) {
        Log.d("CHAT_DEBUG", "RideId = $rideId")
        listenMessages(rideId) {
            Log.d("CHAT_DEBUG", "Messages size = ${it.size}")
            messages = it
        }
    }*/

    /* 🔥 AUTO SCROLL */
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler { onHome() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ride Chat", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
        ) {

            /* ---------------- CHAT LIST ---------------- */

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {

                val sortedMessages = messages.sortedBy { it.timestamp }

                itemsIndexed(sortedMessages) { index, msg ->

                    val currentDate = getDateLabel(msg.timestamp)
                    val previousDate = if (index > 0)
                        getDateLabel(sortedMessages[index - 1].timestamp)
                    else ""

                    /* 🔥 DATE SEPARATOR */
                    if (index == 0 || currentDate != previousDate) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDate,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    //val isMe = msg.sender == user
                    val isMe = msg.sender.trim().lowercase() == currentUser.trim().lowercase()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {

                        Column(
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {

                            // 👤 NAME
                            Text(
                                text = if (isMe) "You" else msg.senderName,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            // 💬 MESSAGE
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(
                                        if (isMe) Color(0xFF3F51B5)
                                        else Color(0xFFE4E6EB),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isMe) Color.White else Color.Black
                                )
                            }
                        }


                    }
                }
            }

            /* ---------------- INPUT ---------------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F3F6),
                        unfocusedContainerColor = Color(0xFFF1F3F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (message.isNotEmpty()) {
                            sendMessage(
                                rideId,
                                user,
                                userName,
                                message
                            )
                            message = ""
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                ) {
                    Text("Send", color = Color.White)
                }
            }
        }
    }
}
//------------------------------
fun sendMessage(
    rideId: Int,
    sender: String,
    senderName: String,
    text: String
) {
    val db = FirebaseDatabase.getInstance().reference

    val msgId = db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .push().key ?: return

    val message = Message(
        sender = sender,
        senderName = senderName,
        text = text,
        timestamp = System.currentTimeMillis()
    )

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .child(msgId)
        .setValue(message)
}

fun listenMessages(
    rideId: Int,
    onUpdate: (List<Message>) -> Unit
) {
    val db = FirebaseDatabase.getInstance().reference

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()

                for (snap in snapshot.children) {
                    val msg = snap.getValue(Message::class.java)
                    msg?.let { list.add(it) }
                }

                onUpdate(list.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}


/* ---------------- FIREBASE ---------------- */

/*
fun sendMessage(
    rideId: Int,
    sender: String,
    senderName: String,
    text: String
) {
    val db = FirebaseDatabase.getInstance().reference

    val msgId = db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .push().key ?: return

    val message = Message(sender, senderName, text, System.currentTimeMillis())

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .child(msgId)
        .setValue(message)
}
*/

/*fun listenMessages(
    rideId: Int,
    onUpdate: (List<Message>) -> Unit
) {
    val db = FirebaseDatabase.getInstance().reference

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()

                for (snap in snapshot.children) {
                    val msg = snap.getValue(Message::class.java)
                    msg?.let { list.add(it) }
                }

                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}*/

/*
package com.example.gocab
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getDateLabel(timestamp: Long): String {
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(timestamp))
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    rideId: String,
    onHome: () -> Unit,
    onBack: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var userName by remember { mutableStateOf("User") }

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitInstance.api.getStudentProfile(uid)

            if (res.isSuccessful) {
                userName = res.body()?.data?.S_name ?: "User"
            }
        } catch (e: Exception) {
            Log.e("CHAT_NAME", "Error fetching name")
        }
    }
    LaunchedEffect(rideId) {
        listenMessages(rideId) {
            messages = it
            Log.d("CHAT_LIST", "Messages size = ${it.size}")
        }
    }
    BackHandler {
        onHome()   // 👈 back = home
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Ride Chat", color = Color.White)
                },
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
    ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA))   // 🌞 LIGHT BACKGROUND
            ) {

                // 🔹 CHAT LIST
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    reverseLayout = true
                ) {
                    val sortedMessages = messages.sortedBy { it.timestamp }

                    itemsIndexed(sortedMessages) { index, msg ->
                        val currentDate = getDateLabel(msg.timestamp)

                        val previousDate = if (index > 0)
                            getDateLabel(sortedMessages[index - 1].timestamp)
                        else ""

                        if (index == 0 || currentDate != previousDate) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentDate,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .background(Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                        val isMe = msg.sender == user

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {

                            Column(
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {

                                // 👤 NAME
                                Text(
                                    text = if (isMe) "You" else msg.senderName,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                // 💬 MESSAGE BUBBLE
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .background(
                                            if (isMe) Color(0xFF3F51B5)
                                            else Color(0xFFE4E6EB),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (isMe) Color.White else Color.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 🔹 INPUT BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = {
                            Text("Type a message...", color = Color.Gray)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,

                            focusedContainerColor = Color(0xFFF1F3F6),
                            unfocusedContainerColor = Color(0xFFF1F3F6),

                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (message.isNotEmpty()) {
                                sendMessage(
                                    rideId,
                                    user,
                                    userName,
                                    message
                                )
                                message = ""
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3F51B5)
                        )
                    ) {
                        Text("Send", color = Color.White)
                    }
                }
            }

    }
}


fun sendMessage(rideId: String, sender: String, senderName: String, text: String) {

    val db = FirebaseDatabase.getInstance().reference

    val msgId = db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .push().key ?: return

    val message = Message(sender, senderName, text, System.currentTimeMillis())

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .child(msgId)
        .setValue(message)
}
fun listenMessages(rideId: String, onUpdate: (List<Message>) -> Unit) {

    val db = FirebaseDatabase.getInstance().reference

    db.child("chats")
        .child("ride_$rideId")
        .child("messages")
        .addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val list = mutableListOf<Message>()

                for (snap in snapshot.children) {
                    val msg = snap.getValue(Message::class.java)
                    msg?.let { list.add(it) }
                }

                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}

*/
