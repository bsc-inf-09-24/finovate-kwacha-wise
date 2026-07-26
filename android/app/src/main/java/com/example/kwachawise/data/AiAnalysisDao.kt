package com.example.kwachawise.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAnalysisDao {
    @Query("SELECT * FROM ai_analysis ORDER BY timestamp DESC")
    fun getAllAnalysis(): Flow<List<AiAnalysisEntity>>

    @Query("SELECT * FROM ai_analysis ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAnalysis(): Flow<AiAnalysisEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: AiAnalysisEntity)

    @Query("DELETE FROM ai_analysis")
    suspend fun clearHistory()
}
