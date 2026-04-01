package com.example.gocab.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
suspend fun getAddressFromLatLng(lat: Double, lng: Double): String {

    return withContext(Dispatchers.IO) {

        try {

            val url = URL(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng"
            )

            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "GoCabApp") // 🔥 IMPORTANT

            val response = connection.getInputStream()
                .bufferedReader()
                .use { it.readText() }

            val json = JSONObject(response)

            val addressObj = json.getJSONObject("address")

            val road = addressObj.optString("road", "")
            val suburb = addressObj.optString("suburb", "")
            val city = addressObj.optString("city", "")
            val state = addressObj.optString("state", "")

            listOf(road, suburb, city, state)
                .filter { it.isNotEmpty() }
                .joinToString(", ")

        } catch (e: Exception) {

            "Unknown location"

        }

    }
}
/*
suspend fun getAddressFromLatLng(lat: Double, lng: Double): String {

    return withContext(Dispatchers.IO) {

        try {

            val url =
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng"

            val response = URL(url).readText()

            val json = JSONObject(response)

            json.getString("display_name")

        } catch (e: Exception) {

            "Unknown location"

        }

    }
}*/