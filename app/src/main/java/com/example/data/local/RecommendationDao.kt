package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM saved_recommendations ORDER BY timestamp DESC")
    fun getAllRecommendations(): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM saved_recommendations WHERE id = :id LIMIT 1")
    suspend fun getRecommendationById(id: String): RecommendationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(entity: RecommendationEntity)

    @Query("DELETE FROM saved_recommendations WHERE id = :id")
    suspend fun deleteRecommendationById(id: String)

    @Query("DELETE FROM saved_recommendations")
    suspend fun clearAll()
}
