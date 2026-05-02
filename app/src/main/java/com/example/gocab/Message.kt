package com.example.gocab
data class Message(
    val sender: String = "",
    val senderName: String = "",   //  IMPORTANT
    val text: String = "",
    val timestamp: Long = 1770000000000
)