package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FarmProfile(
    val cropType: String = "Tomato (Hybrid)",
    val location: String = "Pune (Shirur / Haveli)",
    val soilType: String = "Black Cotton (Regur)",
    val landSizeAcres: Double = 2.5,
    val soilMoisture: String = "Dry (20-35%)",
    val growthStage: String = "Flowering & Fruit Set",
    val irrigationMethod: String = "Drip Irrigation (Thibak)",
    val waterSource: String = "Borewell + Farm Pond"
)

data class DayScheduleItem(
    val dayName: String,
    val date: String,
    val weather: String,
    val tempMax: Int,
    val tempMin: Int,
    val rainChancePercent: Int,
    val rainfallMm: Double,
    val irrigationNeeded: Boolean,
    val waterLiters: Int,
    val runtimeMinutes: Int,
    val projectedMoisturePercent: Int
)

data class IrrigationRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val farmProfile: FarmProfile,
    val decision: String, // "SKIP_TODAY_WATER_TOMORROW", "WATER_TODAY", "SKIP_TODAY_RAIN", "MAINTAIN_MOISTURE"
    val decisionBadge: String, // e.g. "SKIP TODAY • WATER TOMORROW"
    val headline: String,
    val recommendedWaterLiters: Int,
    val recommendedWaterMm: Double,
    val recommendedRunTimeHours: Double,
    val bestTimeWindow: String,
    val weatherSummary: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val cropHealthAlert: String,
    val scientificReasoning: String,
    val nextSteps: List<String>,
    val traditionalWaterLiters: Int,
    val waterSavedPercentage: Int,
    val electricitySavedHours: Double,
    val costSavedInr: Int,
    val isAiGenerated: Boolean,
    val isDemoMode: Boolean,
    val sevenDaySchedule: List<DayScheduleItem>
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

data class WeatherInfo(
    val location: String,
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val rainChance: Int,
    val rainfallForecastMm: Double,
    val windSpeedKmH: Int,
    val etoMmDay: Double // Reference Evapotranspiration
)
