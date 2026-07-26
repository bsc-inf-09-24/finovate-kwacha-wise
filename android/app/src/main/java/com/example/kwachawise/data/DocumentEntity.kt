package com.example.kwachawise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val startDate: Long,
    val endDate: Long
)
