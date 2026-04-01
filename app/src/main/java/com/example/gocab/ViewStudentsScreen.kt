package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gocab.model.AdminStudent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStudentsScreen(
    adminEmail: String,
    onBack: () -> Unit
) {

    val viewModel: AdminStudentsViewModel = viewModel()

    val students by viewModel.students.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val search = remember { mutableStateOf("") }

    // 🔥 extract college name
    val collegeName = adminEmail.substringAfter("@").substringBefore(".")

    LaunchedEffect(Unit) {
        viewModel.setAdminEmail(adminEmail)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.img_7),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(), alpha = .85f,
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.30f))
        )

        Scaffold(
            containerColor = Color.Transparent,

        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {
                Text(
                    text = "$collegeName Admin",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // 🔍 SEARCH BAR
                OutlinedTextField(
                    value = search.value,
                    onValueChange = {
                        search.value = it
                        viewModel.loadStudents(it)
                    },
                    label = { Text("Search students", color = Color.White) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {

                    // 🔄 LOADING
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // ❌ EMPTY
                    students.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No students found 😕",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // ✅ LIST
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(students) { student ->
                                StudentCard1(student)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StudentCard1(student: AdminStudent) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = student.student_name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("ID: ${student.student_id}")
            Text("College: ${student.college_name}")
        }
    }
}


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStudentsScreen(
    adminEmail: String,
    onBack: () -> Unit
) {
    val viewModel: AdminStudentsViewModel = viewModel()

    val students by viewModel.students.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val search = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.setAdminEmail(adminEmail)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌄 BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.img_6), // change if needed
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🌫 DARK OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Students List",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                // 🔍 PREMIUM SEARCH BAR
                OutlinedTextField(
                    value = search.value,
                    onValueChange = {
                        search.value = it
                        viewModel.loadStudents(it)
                    },
                    label = { Text("Search by name or ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {

                    // 🔄 LOADING
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // ❌ EMPTY
                    students.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No students found 😕",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // ✅ LIST
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(students) { student ->
                                StudentCard1(student)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StudentCard1(student: AdminStudent) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = student.student_name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("ID: ${student.student_id}")
            Text("College: ${student.college_name}")
        }
    }
}*/



/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStudentsScreen(
    adminEmail: String,
    onBack: () -> Unit
) {
    val viewModel: AdminStudentsViewModel = viewModel()

    val students by viewModel.students.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val search = remember { mutableStateOf("") }

    // 🔥 STEP-4 (MOST IMPORTANT)
    // Screen open hote hi admin ka domain set karo
    LaunchedEffect(Unit) {
        viewModel.setAdminEmail(adminEmail)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students List") },
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

            // 🔍 Student search box
            OutlinedTextField(
                value = search.value,
                onValueChange = {
                    search.value = it
                    viewModel.loadStudents(it)   // name or ID
                },
                label = { Text("Search by student name or ID") },
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

                students.isEmpty() -> {
                    Text("No students found")
                }

                else -> {
                    LazyColumn {
                        items(students) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("ID: ${student.student_id}", fontWeight = FontWeight.Bold)
                                    Text("Name: ${student.student_name}")
                                    Text("College: ${student.college_name}")
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