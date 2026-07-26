package com.example.kwachawise.models

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val isRead: Boolean = false
)
