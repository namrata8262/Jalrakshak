package com.example.data.repository

import android.content.Context
import com.example.data.ai.GeminiIrrigationService
import com.example.data.engine.AgronomicRuleEngine
import com.example.data.local.JalRakshakDatabase
import com.example.data.local.RecommendationEntity
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IrrigationRepository(context: Context) {

    private val db = JalRakshakDatabase.getDatabase(context)
    private val dao = db.recommendationDao()
    private val aiService = GeminiIrrigationService()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    val savedRecommendations: Flow<List<IrrigationRecommendation>> =
        dao.getAllRecommendations().map { entities ->
            entities.map { it.toDomainModel(moshi) }
        }

    suspend fun generateRecommendation(
        profile: FarmProfile,
        isDemoMode: Boolean
    ): Result<IrrigationRecommendation> {
        return aiService.getIrrigationRecommendation(profile, isDemoMode)
    }

    suspend fun saveRecommendation(recommendation: IrrigationRecommendation) {
        val entity = RecommendationEntity.fromDomainModel(recommendation, moshi)
        dao.insertRecommendation(entity)
    }

    suspend fun getRecommendationById(id: String): IrrigationRecommendation? {
        val entity = dao.getRecommendationById(id)
        return entity?.toDomainModel(moshi)
    }

    suspend fun deleteRecommendation(id: String) {
        dao.deleteRecommendationById(id)
    }

    fun getDemoFarmProfile(): FarmProfile {
        return FarmProfile(
            cropType = "Tomato (Abhinav Hybrid)",
            location = "Pune (Shirur / Haveli)",
            soilType = "Black Cotton (Regur)",
            landSizeAcres = 2.5,
            soilMoisture = "Dry (20-35%)",
            growthStage = "Flowering & Fruit Set",
            irrigationMethod = "Drip Irrigation (Thibak)",
            waterSource = "Borewell + Farm Pond"
        )
    }

    fun getInitialDemoRecommendation(): IrrigationRecommendation {
        return AgronomicRuleEngine.generateRecommendation(
            profile = getDemoFarmProfile(),
            isAiGenerated = true,
            isDemoMode = true
        )
    }
}
