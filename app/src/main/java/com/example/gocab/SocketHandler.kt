package com.example.gocab

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketHandler {

    private lateinit var socket: Socket

    fun setSocket() {
        socket = IO.socket("http://10.77.144.204:5000")
    }

    fun establishConnection() {
        if (::socket.isInitialized) {
            socket.connect()
        }
    }

    fun closeConnection() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
    fun joinRide(rideId: Int) {
        if (::socket.isInitialized && socket.connected()) {
            socket.emit("join_ride", rideId)
        }
    }

    fun sendDriverLocation(data: JSONObject) {

        Log.d("SOCKET_DEBUG", "Location send called")

        if (::socket.isInitialized) {
            Log.d("SOCKET_DEBUG", "Socket initialized")
        } else {
            Log.d("SOCKET_DEBUG", "Socket NOT initialized")
        }

        if (::socket.isInitialized && socket.connected()) {

            Log.d("SOCKET_DEBUG", "Socket connected, sending data")

            socket.emit("driver_location", data)

        } else {

            Log.d("SOCKET_DEBUG", "Socket NOT connected")

        }
    }
    fun listenDriverLocation(callback: (Double, Double) -> Unit) {

        socket.on("driver_location_update") { args ->

            val data = args[0] as JSONObject
            val lat = data.getDouble("lat")
            val lng = data.getDouble("lng")

            callback(lat, lng)
        }
    }


}