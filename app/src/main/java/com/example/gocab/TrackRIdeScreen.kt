//Trackridescreen

package com.example.gocab.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.gocab.SocketHandler
import com.example.gocab.utils.getAddressFromLatLng
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import kotlinx.coroutines.tasks.await

@Composable
fun TrackRideScreen(
    rideId: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {

    BackHandler { onHome() }

    val context = LocalContext.current

    // 🔹 STATES
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var driverMarker by remember { mutableStateOf<Marker?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }

    var driverLat by remember { mutableStateOf(0.0) }
    var driverLng by remember { mutableStateOf(0.0) }

    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }

    var driverAddress by remember { mutableStateOf("Locating driver...") }

    // 🔹 LOCATION CLIENT
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // 🔹 USER LOCATION FETCH
    LaunchedEffect(Unit) {if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return@LaunchedEffect
    }
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                userLat = it.latitude
                userLng = it.longitude
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔹 OSMDROID CONFIG
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Driver near: $driverAddress",
            modifier = Modifier.padding(16.dp)
        )

        // 🔹 MAP VIEW
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val map = MapView(ctx)
                map.setMultiTouchControls(true)

                val controller = map.controller
                controller.setZoom(5.0)

                val startPoint = GeoPoint(28.6139, 77.2090) // India default
                controller.setCenter(startPoint)

                // 🚗 DRIVER MARKER
                val dMarker = Marker(map)
                dMarker.position = startPoint
                dMarker.title = "Driver"
                map.overlays.add(dMarker)
                driverMarker = dMarker

                // 🔵 USER MARKER (only if available)
                if (userLat != 0.0 && userLng != 0.0) {
                    val uMarker = Marker(map).apply {
                        position = GeoPoint(userLat, userLng)
                        title = "You"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    map.overlays.add(uMarker)
                    userMarker = uMarker
                }

                mapView = map
                map
            }
        )
    }

    // 🔹 SOCKET DRIVER LOCATION
    LaunchedEffect(Unit) {
        SocketHandler.joinRide(rideId)

        SocketHandler.listenDriverLocation { lat, lng ->
            Log.d("SOCKET", "Lat: $lat Lng: $lng")
            driverLat = lat
            driverLng = lng
        }
    }

    // 🔹 DRIVER LIVE UPDATE
    LaunchedEffect(driverLat, driverLng) {

        val point = GeoPoint(driverLat, driverLng)

        driverMarker?.position = point

        if (driverLat != 0.0 && driverLng != 0.0) {
            mapView?.controller?.setCenter(point)
        }

        mapView?.invalidate()

        driverAddress = getAddressFromLatLng(driverLat, driverLng)
    }

    // 🔹 USER LIVE UPDATE
    LaunchedEffect(userLat, userLng) {

        val point = GeoPoint(userLat, userLng)

        if (userMarker == null && mapView != null) {
            val uMarker = Marker(mapView)
            uMarker.position = point
            uMarker.title = "You"
            uMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            mapView?.overlays?.add(uMarker)
            userMarker = uMarker
        } else {
            userMarker?.position = point
        }

        mapView?.invalidate()
    }
}


/*
@Composable
fun TrackRideScreen(
    rideId: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,

    ) {
    BackHandler {
        onHome()   // 👈 back = home
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var driverAddress by remember { mutableStateOf("Locating driver...") }
    var driverLat by remember { mutableStateOf(0.0) }
    var driverLng by remember { mutableStateOf(0.0) }
    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(Unit) {
        try {
            val location = fusedLocationClient.lastLocation.await()

            location?.let {
                userLat = it.latitude
                userLng = it.longitude
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // 🔥 OSMDroid config (IMPORTANT)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Driver near: $driverAddress",
            modifier = Modifier.padding(16.dp)
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val map = MapView(ctx)
                map.setMultiTouchControls(true)

                val startPoint = GeoPoint(28.6139, 77.2090) // 👉 default India (Delhi)

                userMarker = Marker(map).apply {
                    position = GeoPoint(userLat, userLng)
                    title = "You"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }

                map.overlays.add(userMarker!!)

                val controller = map.controller
                controller.setZoom(15.0)
                controller.setCenter(startPoint)

                val driverMarker = Marker(map)
                driverMarker.position = startPoint
                driverMarker.title = "Driver Location"

                map.overlays.add(driverMarker)

                mapView = map
                marker = driverMarker

                map
            }
        )

        /*else {
            Text(
                text = "Getting driver location...",
                modifier = Modifier.padding(20.dp)
            )
        }*/

        // 🔌 SOCKET CONNECTION
        LaunchedEffect(Unit) {

            SocketHandler.joinRide(rideId)

            SocketHandler.listenDriverLocation { lat, lng ->
                Log.d("SOCKET", "Lat: $lat Lng: $lng")
                driverLat = lat
                driverLng = lng
            }
        }

        // 🔄 UPDATE DRIVER LOCATION LIVE
       // var isFirstLoad by remember { mutableStateOf(true) }

        LaunchedEffect(driverLat, driverLng) {

            val newPoint = GeoPoint(driverLat, driverLng)

            marker?.position = newPoint

            if (driverLat != 0.0 && driverLng != 0.0) {
                mapView?.controller?.setCenter(newPoint)
            }

            mapView?.invalidate()

            driverAddress = getAddressFromLatLng(driverLat, driverLng)
        }
    }
}
*/


/*
package com.example.gocab.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gocab.SocketHandler
import com.example.gocab.utils.getAddressFromLatLng
import com.google.android.gms.maps.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@Composable
fun TrackRideScreen(
    rideId: Int,
    onBack: () -> Unit
) {

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var driverAddress by remember { mutableStateOf("Locating driver...") }

    // 👇 DRIVER LOCATION STATE
    var driverLat by remember { mutableStateOf(19.0760) }
    var driverLng by remember { mutableStateOf(72.8777) }

    */
/*
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->

            val map = MapView(context)
            map.setMultiTouchControls(true)

            val startPoint = GeoPoint(driverLat, driverLng)

            val controller = map.controller
            controller.setZoom(15.0)
            controller.setCenter(startPoint)

            val driverMarker = Marker(map)
            driverMarker.position = startPoint
            driverMarker.title = "Driver Location"

            map.overlays.add(driverMarker)

            mapView = map
            marker = driverMarker

            map
        }
    )*//*

    Column(modifier = Modifier.fillMaxSize()) {

        androidx.compose.material3.Text(
            text = "Driver near: $driverAddress",
            modifier = Modifier.padding(16.dp)
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->

                val map = MapView(context)
                map.setMultiTouchControls(true)

                val startPoint = GeoPoint(driverLat, driverLng)

                val controller = map.controller
                controller.setZoom(15.0)
                controller.setCenter(startPoint)

                val driverMarker = Marker(map)
                driverMarker.position = startPoint
                driverMarker.title = "Driver Location"

                map.overlays.add(driverMarker)

                mapView = map
                marker = driverMarker

                map
            }
        )
    }

    // 👇 RECEIVE DRIVER LOCATION FROM SOCKET
    LaunchedEffect(Unit) {

        SocketHandler.joinRide(rideId)

        SocketHandler.listenDriverLocation { lat, lng ->

            driverLat = lat
            driverLng = lng

        }
    }

    // 👇 UPDATE MARKER WHEN LOCATION CHANGES
    */
/*
    LaunchedEffect(driverLat, driverLng) {

        val newPoint = GeoPoint(driverLat, driverLng)

        marker?.position = newPoint

        mapView?.controller?.setCenter(newPoint)

        mapView?.invalidate()
    }*//*

    LaunchedEffect(driverLat, driverLng) {

        val newPoint = GeoPoint(driverLat, driverLng)

        marker?.position = newPoint

        mapView?.controller?.setCenter(newPoint)

        mapView?.invalidate()

        //driverAddress = getAddressFromLatLng(driverLat, driverLng)
        if (driverAddress == "Locating driver...") {
            driverAddress = getAddressFromLatLng(driverLat, driverLng)
        }
    }
}
*/


/*
@Composable
fun TrackRideScreen(
    rideId: Int,
    onBack: () -> Unit
) {

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->

            val map = MapView(context)
            map.setMultiTouchControls(true)

            val startPoint = GeoPoint(19.0760, 72.8777)

            val controller = map.controller
            controller.setZoom(15.0)
            controller.setCenter(startPoint)

            val driverMarker = Marker(map)
            driverMarker.position = startPoint
            driverMarker.title = "Driver Location"

            map.overlays.add(driverMarker)

            mapView = map
            marker = driverMarker

            map
        }
    )

    // 👇 ADD THIS PART HERE
    LaunchedEffect(Unit) {

        SocketHandler.listenDriverLocation { lat, lng ->

            marker?.position = GeoPoint(lat, lng)

            mapView?.controller?.setCenter(GeoPoint(lat, lng))

            mapView?.invalidate()
        }
    }
}

 */