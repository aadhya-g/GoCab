package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.gocab.util.FilterData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(onApplyClick: (FilterData) -> Unit) {

    var driverRating by remember { mutableStateOf("Best Rated") }
    var carType by remember { mutableStateOf("Carrier") }
    var acOption by remember { mutableStateOf("AC") }
    var seats by remember { mutableStateOf("2 Seater") }
    var cost by remember { mutableStateOf("Low to High") }

    val ratings = listOf("Best Rated", "Average", "Low Rated")
    val carTypes = listOf("Carrier", "Non-Carrier")
    val acOptions = listOf("AC", "Non-AC")
    val seatOptions = listOf("2 Seater", "4 Seater", "6 Seater")
    val costOptions = listOf("Low to High", "High to Low")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Apply Filter", color = Color.White) },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1)
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // 🌄 Background Image
            Image(
                painter = painterResource(id = R.drawable.img11),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 🌫 Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {

                // 🔝 Title
                /*Text(
                    text = "Apply Filters",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 10.dp)
                )*/

                // 🎯 Main Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        FilterSection("Driver Rating", ratings, driverRating) {
                            driverRating = it
                        }

                        FilterSection("Car Type", carTypes, carType) {
                            carType = it
                        }

                        FilterSection("AC Options", acOptions, acOption) {
                            acOption = it
                        }

                        FilterSection("Seats", seatOptions, seats) {
                            seats = it
                        }

                        FilterSection("Cost", costOptions, cost) {
                            cost = it
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 🚀 Apply Button
                Button(
                    onClick = {
                        val filters = FilterData(
                            rating = when (driverRating) {
                                "Best Rated" -> "Best"
                                "Average" -> "Average"
                                "Low Rated" -> "Low"
                                else -> null
                            },
                            carType = carType,
                            acType = acOption,
                            seats = when (seats) {
                                "2 Seater" -> 2
                                "4 Seater" -> 4
                                "6 Seater" -> 6
                                else -> null
                            },
                            costOrder = when (cost) {
                                "Low to High" -> "LowToHigh"
                                "High to Low" -> "HighToLow"
                                else -> null
                            }
                        )

                        onApplyClick(filters)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107)
                    )
                ) {
                    Text(
                        "Apply Filters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
    @Composable
    fun FilterSection(
        title: String,
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {

            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->

                    val isSelected = option == selectedOption

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(
                                if (isSelected) Color(0xFF1976D2)
                                else Color(0xFFF1F3F6),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onOptionSelected(option) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }





/*
@Composable
        /*fun FiltersScreen(onApplyClick: () -> Unit)*/
fun FiltersScreen(onApplyClick: (FilterData) -> Unit){
    var driverRating by remember { mutableStateOf("Best Rated") }
    var carType by remember { mutableStateOf("Carrier") }
    var acOption by remember { mutableStateOf("AC") }
    var seats by remember { mutableStateOf("2 Seater") }
    var cost by remember { mutableStateOf("Low to High") }

    val ratings = listOf("Best Rated", "Average", "Low Rated")
    val carTypes = listOf("Carrier", "Non-Carrier")
    val acOptions = listOf("AC", "Non-AC")
    val seatOptions = listOf("2 Seater", "4 Seater", "6 Seater")
    val costOptions = listOf("Low to High", "High to Low")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.img11), // add your image in res/drawable folder
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Apply Filter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            FilterSection(
                title = "Driver Rating",
                options = ratings,
                selectedOption = driverRating,
                onOptionSelected = { driverRating = it }
            )

            FilterSection(
                title = "Car Type",
                options = carTypes,
                selectedOption = carType,
                onOptionSelected = { carType = it }
            )

            FilterSection(
                title = "AC Options",
                options = acOptions,
                selectedOption = acOption,
                onOptionSelected = { acOption = it }
            )

            FilterSection(
                title = "Seats",
                options = seatOptions,
                selectedOption = seats,
                onOptionSelected = { seats = it }
            )

            FilterSection(
                title = "Cost",
                options = costOptions,
                selectedOption = cost,
                onOptionSelected = { cost = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            /*
                        Button(
                            onClick = {
                                val filters = FilterData(
                                    driverRating,
                                    carType,
                                    acOption,
                                    seats,
                                    cost
                                )
                                onApplyClick(filters)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Apply Filters",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }*/
            Button(
                onClick = {
                    val filters = FilterData(
                        rating = when (driverRating) {
                            "Best Rated" -> "Best"
                            "Average" -> "Average"
                            "Low Rated" -> "Low"
                            else -> null
                        },
                        carType = carType,
                        acType = acOption,
                        seats = when (seats) {
                            "2 Seater" -> 2
                            "4 Seater" -> 4
                            "6 Seater" -> 6
                            else -> null
                        },
                        costOrder = when (cost) {
                            "Low to High" -> "LowToHigh"
                            "High to Low" -> "HighToLow"
                            else -> null
                        }
                    )

                    onApplyClick(filters)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}*/


/*

package com.example.gocab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FiltersScreen(onApplyClick: () -> Unit) {
    var driverRating by remember { mutableStateOf("Best Rated") }
    var carType by remember { mutableStateOf("Carrier") }
    var acOption by remember { mutableStateOf("AC") }
    var seats by remember { mutableStateOf("2 Seater") }
    var cost by remember { mutableStateOf("Low to High") }

    val ratings = listOf("Best Rated", "Average", "Low Rated")
    val carTypes = listOf("Carrier", "Non-Carrier")
    val acOptions = listOf("AC", "Non-AC")
    val seatOptions = listOf("2 Seater", "4 Seater", "6 Seater")
    val costOptions = listOf("Low to High", "High to Low")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.img_5), // add your image in res/drawable folder
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Apply Filter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            FilterSection(
                title = "Driver Rating",
                options = ratings,
                selectedOption = driverRating,
                onOptionSelected = { driverRating = it }
            )

            FilterSection(
                title = "Car Type",
                options = carTypes,
                selectedOption = carType,
                onOptionSelected = { carType = it }
            )

            FilterSection(
                title = "AC Options",
                options = acOptions,
                selectedOption = acOption,
                onOptionSelected = { acOption = it }
            )

            FilterSection(
                title = "Seats",
                options = seatOptions,
                selectedOption = seats,
                onOptionSelected = { seats = it }
            )

            FilterSection(
                title = "Cost",
                options = costOptions,
                selectedOption = cost,
                onOptionSelected = { cost = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply Filters", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}





*/
/*
package com.example.gocab

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gocab.R

@Composable
fun FiltersScreen(
    onApplyClick: @Composable (
        String,
        String,
        String,
        String,
        String
    ) -> Unit
)
{
    var driverRating by remember { mutableStateOf("Best Rated") }
    var carType by remember { mutableStateOf("Carrier") }
    var acOption by remember { mutableStateOf("AC") }
    var seats by remember { mutableStateOf("2 Seater") }
    var cost by remember { mutableStateOf("Low to High") }

    val ratings = listOf("Best Rated", "Average", "Low Rated")
    val carTypes = listOf("Carrier", "Non-Carrier")
    val acOptions = listOf("AC", "Non-AC")
    val seatOptions = listOf("2 Seater", "4 Seater", "6 Seater")
    val costOptions = listOf("Low to High", "High to Low")

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.img_4), // add your image in res/drawable folder
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Apply Filter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            FilterSection(
                title = "Driver Rating",
                options = ratings,
                selectedOption = driverRating,
                onOptionSelected = { driverRating = it }
            )

            FilterSection(
                title = "Car Type",
                options = carTypes,
                selectedOption = carType,
                onOptionSelected = { carType = it }
            )

            FilterSection(
                title = "AC Options",
                options = acOptions,
                selectedOption = acOption,
                onOptionSelected = { acOption = it }
            )

            FilterSection(
                title = "Seats",
                options = seatOptions,
                selectedOption = seats,
                onOptionSelected = { seats = it }
            )

            FilterSection(
                title = "Cost",
                options = costOptions,
                selectedOption = cost,
                onOptionSelected = { cost = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClick = {
                    onApplyClick(
                        driverRating,
                        carType,
                        acOption,
                        seats,
                        cost
                    )
                }
                ,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply Filters", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

*/

