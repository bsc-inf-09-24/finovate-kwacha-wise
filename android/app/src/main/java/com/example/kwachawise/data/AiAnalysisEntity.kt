package com.example.kwachawise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_analysis")
data class AiAnalysisEntity(
    @PrimaryKey val id: String,
    val result: String,
    val timestamp: Long,
    val transactionHash: Int
)
