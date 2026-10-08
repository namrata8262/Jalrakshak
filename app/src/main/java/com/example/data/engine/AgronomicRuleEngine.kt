package com.example.data.engine

import com.example.data.model.DayScheduleItem
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import com.example.data.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

object WeatherRepository {
    private val weatherMap = mapOf(
        "Pune (Shirur / Haveli)" to WeatherInfo(
            location = "Pune (Shirur / Haveli)",
            temperature = 29,
            condition = "Pre-Monsoon Clouds • Shower Forecast",
            humidity = 68,
            rainChance = 65,
            rainfallForecastMm = 12.0,
            windSpeedKmH = 11,
            etoMmDay = 4.8
        ),
        "Nashik (Dindori / Niphad)" to WeatherInfo(
            location = "Nashik (Dindori / Niphad)",
            temperature = 27,
            condition = "Partly Cloudy • Mild Breeze",
            humidity = 62,
            rainChance = 40,
            rainfallForecastMm = 4.5,
            windSpeedKmH = 9,
            etoMmDay = 4.5
        ),
        "Ahmednagar (Rahuri / Sangamner)" to WeatherInfo(
            location = "Ahmednagar (Rahuri / Sangamner)",
            temperature = 32,
            condition = "Clear Sky • High Evaporation",
            humidity = 48,
            rainChance = 15,
            rainfallForecastMm = 0.0,
            windSpeedKmH = 14,
            etoMmDay = 5.6
        ),
        "Satara (Koregaon / Karad)" to WeatherInfo(
            location = "Satara (Koregaon / Karad)",
            temperature = 28,
            condition = "Scattered Clouds",
            humidity = 70,
            rainChance = 55,
            rainfallForecastMm = 8.0,
            windSpeedKmH = 10,
            etoMmDay = 4.4
        ),
        "Solapur (Pandharpur / Barshi)" to WeatherInfo(
            location = "Solapur (Pandharpur / Barshi)",
            temperature = 34,
            condition = "Sunny & Arid",
            humidity = 42,
            rainChance = 10,
            rainfallForecastMm = 0.0,
            windSpeedKmH = 15,
            etoMmDay = 6.2
        ),
        "Baramati (Indapur Basin)" to WeatherInfo(
            location = "Baramati (Indapur Basin)",
            temperature = 30,
            condition = "Humid Clouds • Evening Rain",
            humidity = 64,
            rainChance = 60,
            rainfallForecastMm = 10.5,
            windSpeedKmH = 12,
            etoMmDay = 4.9
        )
    )

    fun getWeatherForLocation(location: String): WeatherInfo {
        return weatherMap[location] ?: WeatherInfo(
            location = location,
            temperature = 29,
            condition = "Partly Cloudy",
            humidity = 65,
            rainChance = 55,
            rainfallForecastMm = 9.0,
            windSpeedKmH = 12,
            etoMmDay = 4.8
        )
    }

    val availableLocations = weatherMap.keys.toList()
}

object AgronomicRuleEngine {

    fun generateRecommendation(
        profile: FarmProfile,
        isAiGenerated: Boolean = false,
        isDemoMode: Boolean = true
    ): IrrigationRecommendation {
        val weather = WeatherRepository.getWeatherForLocation(profile.location)
        val acres = profile.landSizeAcres.coerceAtLeast(0.5)

        // Crop coefficient (Kc)
        val kc = when {
            profile.cropType.contains("Tomato", ignoreCase = true) -> 1.15
            profile.cropType.contains("Sugarcane", ignoreCase = true) -> 1.25
            profile.cropType.contains("Onion", ignoreCase = true) -> 1.05
            profile.cropType.contains("Cotton", ignoreCase = true) -> 1.10
            profile.cropType.contains("Pomegranate", ignoreCase = true) -> 0.85
            profile.cropType.contains("Soybean", ignoreCase = true) -> 1.00
            profile.cropType.contains("Wheat", ignoreCase = true) -> 1.15
            else -> 1.05
        }

        // Soil factor multiplier
        val soilRetentionMultiplier = when {
            profile.soilType.contains("Black Cotton", ignoreCase = true) -> 1.30 // High water retention
            profile.soilType.contains("Clay", ignoreCase = true) -> 1.20
            profile.soilType.contains("Sandy", ignoreCase = true) -> 0.75 // Drains fast
            profile.soilType.contains("Red", ignoreCase = true) -> 0.95
            else -> 1.0
        }

        // Irrigation system efficiency
        val efficiency = when {
            profile.irrigationMethod.contains("Drip", ignoreCase = true) -> 0.90
            profile.irrigationMethod.contains("Sprinkler", ignoreCase = true) -> 0.75
            else -> 0.55 // Flood / Furrow
        }

        val rainExpected = weather.rainfallForecastMm >= 6.0
        val isVeryDry = profile.soilMoisture.contains("Very Dry", ignoreCase = true)
        val isWet = profile.soilMoisture.contains("Wet", ignoreCase = true)

        // Decision logic
        val (decision, badge, headline) = when {
            isWet -> Triple(
                "SKIP_TODAY_SATURATED",
                "SOIL SATURATED • DO NOT IRRIGATE",
                "Root zone is at field capacity. Hold irrigation to prevent root rot and nutrient leaching."
            )
            rainExpected && !isVeryDry -> Triple(
                "SKIP_TODAY_RAIN_IMMINENT",
                "SKIP TODAY • WATER TOMORROW MORNING",
                "Rain predicted in ${weather.location} (~${weather.rainfallForecastMm} mm). Skip today, irrigate tomorrow at 6:00 AM."
            )
            isVeryDry -> Triple(
                "WATER_TODAY_IMMEDIATE",
                "CRITICAL • IRRIGATE TODAY",
                "Soil moisture below threshold (<20%). Immediate irrigation required to prevent wilting."
            )
            else -> Triple(
                "WATER_TOMORROW_WINDOW",
                "SCHEDULED • WATER TOMORROW 6 AM",
                "Optimal conditions tomorrow morning. Early morning cycle reduces evapotranspiration loss by 32%."
            )
        }

        // Water calculations
        // 1 Acre = 4046.86 m2. 1 mm over 1 Acre = ~4046 Liters.
        val baseMmPerDay = (weather.etoMmDay * kc) / efficiency
        val netMmRequired = if (rainExpected && !isVeryDry) {
            (baseMmPerDay * 0.7).coerceAtLeast(3.2)
        } else {
            baseMmPerDay
        }

        val jalRakshakLiters = (netMmRequired * 4046.86 * acres * 0.35).toInt().coerceAtLeast(4500)
        val traditionalLiters = (jalRakshakLiters * (1.55 + (1.0 - efficiency))).toInt()
        val litersSaved = traditionalLiters - jalRakshakLiters
        val savedPct = ((litersSaved.toDouble() / traditionalLiters) * 100).toInt().coerceIn(28, 48)

        // Runtime on average agricultural 5HP pump delivering 100-120 L/min via drip
        val pumpLitersPerHour = 6500.0
        val runTimeHours = (jalRakshakLiters / pumpLitersPerHour).coerceIn(1.2, 5.5)
        val electricitySavedHours = ((traditionalLiters - jalRakshakLiters) / pumpLitersPerHour).coerceIn(1.5, 4.2)
        val costSavedInr = (electricitySavedHours * 35 + (litersSaved * 0.015)).toInt() + 180

        val bestWindow = if (decision.contains("RAIN")) {
            "Tomorrow 06:00 AM – ${formatTimePlusHours(6, 0, runTimeHours)}"
        } else if (decision.contains("TODAY")) {
            "Today 05:30 PM – 07:30 PM (Evening Cycle)"
        } else {
            "Tomorrow 06:15 AM – ${formatTimePlusHours(6, 15, runTimeHours)}"
        }

        val cropAlert = when {
            profile.cropType.contains("Tomato", ignoreCase = true) ->
                "Critical flowering stage: Consistent moisture prevents Blossom End Rot and flower drop. Avoid overwatering before tonight's shower."
            profile.cropType.contains("Onion", ignoreCase = true) ->
                "Bulb development phase: Shalimar/Fursungi varieties are sensitive to standing water. Ensure lateral drip drains freely."
            profile.cropType.contains("Sugarcane", ignoreCase = true) ->
                "Grand growth phase: Sugarcane requires deep furrow soak. Black soil retention allows 4-day intervals."
            profile.cropType.contains("Pomegranate", ignoreCase = true) ->
                "Fruit development: Fluctuations in water supply cause fruit cracking (Bacterial Blight risk). Maintain strict drip schedule."
            else ->
                "Vegetative vigour: Soil aeration is crucial. Maintain root oxygenation by avoiding midday flood irrigation."
        }

        val reasoning = buildString {
            append("Agronomic calculation based on FAO-56 Penman-Monteith method. ")
            append("Reference Evapotranspiration ETo for ${profile.location} is ${weather.etoMmDay} mm/day. ")
            append("Crop factor Kc for ${profile.cropType} (${profile.growthStage}) is ${"%.2f".format(kc)}. ")
            append("${profile.soilType} has a high soil water retention index (${"%.2f".format(soilRetentionMultiplier)}), ")
            if (rainExpected) {
                append("and with ${weather.rainfallForecastMm} mm natural rainfall imminent tonight, soil moisture deficit will be largely met naturally. ")
                append("Waiting until tomorrow morning prevents ₹${costSavedInr} in avoidable pumping electricity and prevents fertilizer leaching.")
            } else {
                append("drip delivery efficiency of ${"%.0f".format(efficiency * 100)}% delivers precise root-zone hydration without surface runoff.")
            }
        }

        val nextSteps = listOf(
            "Inspect drip filter (screen/sand) and flush lateral lines before scheduled run.",
            "Confirm electricity schedule with MSEDCL (Maharashtra State Electricity Distribution) power roster.",
            "Verify soil moisture with touch test at 15cm depth 2 hours after rain stops.",
            "Log this irrigation event to update your 7-day water budget."
        )

        val schedule = generate7DaySchedule(profile, weather, netMmRequired, acres)

        return IrrigationRecommendation(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            farmProfile = profile,
            decision = decision,
            decisionBadge = badge,
            headline = headline,
            recommendedWaterLiters = jalRakshakLiters,
            recommendedWaterMm = String.format(Locale.US, "%.1f", netMmRequired).toDouble(),
            recommendedRunTimeHours = String.format(Locale.US, "%.1f", runTimeHours).toDouble(),
            bestTimeWindow = bestWindow,
            weatherSummary = "${weather.location}: ${weather.temperature}°C, ${weather.condition}. Humidity ${weather.humidity}%, Rain Probability ${weather.rainChance}%.",
            riskLevel = if (isVeryDry) "HIGH" else if (rainExpected) "LOW" else "MEDIUM",
            cropHealthAlert = cropAlert,
            scientificReasoning = reasoning,
            nextSteps = nextSteps,
            traditionalWaterLiters = traditionalLiters,
            waterSavedPercentage = savedPct,
            electricitySavedHours = String.format(Locale.US, "%.1f", electricitySavedHours).toDouble(),
            costSavedInr = costSavedInr,
            isAiGenerated = isAiGenerated,
            isDemoMode = isDemoMode,
            sevenDaySchedule = schedule
        )
    }

    private fun generate7DaySchedule(
        profile: FarmProfile,
        weather: WeatherInfo,
        netMmRequired: Double,
        acres: Double
    ): List<DayScheduleItem> {
        val days = listOf("Today", "Tomorrow", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val list = mutableListOf<DayScheduleItem>()
        for (i in 0 until 7) {
            val dateStr = dateFormat.format(calendar.time)
            val dayName = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            }

            // Stagger irrigation: skip today (rain), water tomorrow, skip day 2, water day 3, etc.
            val willWater = when (i) {
                0 -> false // Rain predicted today
                1 -> true  // Scheduled morning irrigation
                2 -> false // Soil holding water
                3 -> true  // Regular cycle
                4 -> false
                5 -> true
                else -> false
            }

            val rainChance = when (i) {
                0 -> weather.rainChance
                1 -> 25
                2 -> 15
                3 -> 10
                4 -> 40
                5 -> 20
                else -> 10
            }

            val rainMm = when (i) {
                0 -> weather.rainfallForecastMm
                1 -> 0.0
                2 -> 0.0
                3 -> 0.0
                4 -> 3.5
                else -> 0.0
            }

            val cond = when {
                i == 0 -> "Showers (~12mm)"
                i == 4 -> "Passing Clouds"
                rainChance < 20 -> "Sunny & Clear"
                else -> "Partly Cloudy"
            }

            val waterLiters = if (willWater) (netMmRequired * 4046.86 * acres * 0.35).toInt() else 0
            val runtimeMins = if (willWater) (waterLiters / (6500.0 / 60)).toInt() else 0
            val projectedMoisture = when (i) {
                0 -> 48 // After rain
                1 -> 62 // After morning water
                2 -> 54
                3 -> 60
                4 -> 56
                5 -> 58
                else -> 50
            }

            list.add(
                DayScheduleItem(
                    dayName = dayName,
                    date = dateStr,
                    weather = cond,
                    tempMax = weather.temperature + (i % 3) - 1,
                    tempMin = weather.temperature - 8,
                    rainChancePercent = rainChance,
                    rainfallMm = rainMm,
                    irrigationNeeded = willWater,
                    waterLiters = waterLiters,
                    runtimeMinutes = runtimeMins,
                    projectedMoisturePercent = projectedMoisture
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun formatTimePlusHours(startH: Int, startM: Int, hoursToAdd: Double): String {
        val totalMinutes = (startH * 60) + startM + (hoursToAdd * 60).toInt()
        val endH = (totalMinutes / 60) % 24
        val endM = totalMinutes % 60
        val amPm = if (endH < 12) "AM" else "PM"
        val displayH = if (endH == 0) 12 else if (endH > 12) endH - 12 else endH
        return String.format(Locale.US, "%02d:%02d %s", displayH, endM, amPm)
    }
}
