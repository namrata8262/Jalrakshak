package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.model.DayScheduleItem
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "saved_recommendations")
data class RecommendationEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val cropType: String,
    val location: String,
    val soilType: String,
    val landSizeAcres: Double,
    val soilMoisture: String,
    val growthStage: String,
    val irrigationMethod: String,
    val waterSource: String,
    val decision: String,
    val decisionBadge: String,
    val headline: String,
    val recommendedWaterLiters: Int,
    val recommendedWaterMm: Double,
    val recommendedRunTimeHours: Double,
    val bestTimeWindow: String,
    val weatherSummary: String,
    val riskLevel: String,
    val cropHealthAlert: String,
    val scientificReasoning: String,
    val nextStepsJson: String,
    val traditionalWaterLiters: Int,
    val waterSavedPercentage: Int,
    val electricitySavedHours: Double,
    val costSavedInr: Int,
    val isAiGenerated: Boolean,
    val isDemoMode: Boolean,
    val sevenDayScheduleJson: String
) {
    fun toDomainModel(moshi: Moshi): IrrigationRecommendation {
        val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
        val stringAdapter = moshi.adapter<List<String>>(stringListType)
        val steps = try {
            stringAdapter.fromJson(nextStepsJson) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val scheduleListType = Types.newParameterizedType(List::class.java, DayScheduleItem::class.java)
        val scheduleAdapter = moshi.adapter<List<DayScheduleItem>>(scheduleListType)
        val schedule = try {
            scheduleAdapter.fromJson(sevenDayScheduleJson) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        return IrrigationRecommendation(
            id = id,
            timestamp = timestamp,
            farmProfile = FarmProfile(
                cropType = cropType,
                location = location,
                soilType = soilType,
                landSizeAcres = landSizeAcres,
                soilMoisture = soilMoisture,
                growthStage = growthStage,
                irrigationMethod = irrigationMethod,
                waterSource = waterSource
            ),
            decision = decision,
            decisionBadge = decisionBadge,
            headline = headline,
            recommendedWaterLiters = recommendedWaterLiters,
            recommendedWaterMm = recommendedWaterMm,
            recommendedRunTimeHours = recommendedRunTimeHours,
            bestTimeWindow = bestTimeWindow,
            weatherSummary = weatherSummary,
            riskLevel = riskLevel,
            cropHealthAlert = cropHealthAlert,
            scientificReasoning = scientificReasoning,
            nextSteps = steps,
            traditionalWaterLiters = traditionalWaterLiters,
            waterSavedPercentage = waterSavedPercentage,
            electricitySavedHours = electricitySavedHours,
            costSavedInr = costSavedInr,
            isAiGenerated = isAiGenerated,
            isDemoMode = isDemoMode,
            sevenDaySchedule = schedule
        )
    }

    companion object {
        fun fromDomainModel(model: IrrigationRecommendation, moshi: Moshi): RecommendationEntity {
            val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
            val stringAdapter = moshi.adapter<List<String>>(stringListType)
            val scheduleListType = Types.newParameterizedType(List::class.java, DayScheduleItem::class.java)
            val scheduleAdapter = moshi.adapter<List<DayScheduleItem>>(scheduleListType)

            return RecommendationEntity(
                id = model.id,
                timestamp = model.timestamp,
                cropType = model.farmProfile.cropType,
                location = model.farmProfile.location,
                soilType = model.farmProfile.soilType,
                landSizeAcres = model.farmProfile.landSizeAcres,
                soilMoisture = model.farmProfile.soilMoisture,
                growthStage = model.farmProfile.growthStage,
                irrigationMethod = model.farmProfile.irrigationMethod,
                waterSource = model.farmProfile.waterSource,
                decision = model.decision,
                decisionBadge = model.decisionBadge,
                headline = model.headline,
                recommendedWaterLiters = model.recommendedWaterLiters,
                recommendedWaterMm = model.recommendedWaterMm,
                recommendedRunTimeHours = model.recommendedRunTimeHours,
                bestTimeWindow = model.bestTimeWindow,
                weatherSummary = model.weatherSummary,
                riskLevel = model.riskLevel,
                cropHealthAlert = model.cropHealthAlert,
                scientificReasoning = model.scientificReasoning,
                nextStepsJson = stringAdapter.toJson(model.nextSteps),
                traditionalWaterLiters = model.traditionalWaterLiters,
                waterSavedPercentage = model.waterSavedPercentage,
                electricitySavedHours = model.electricitySavedHours,
                costSavedInr = model.costSavedInr,
                isAiGenerated = model.isAiGenerated,
                isDemoMode = model.isDemoMode,
                sevenDayScheduleJson = scheduleAdapter.toJson(model.sevenDaySchedule)
            )
        }
    }
}
