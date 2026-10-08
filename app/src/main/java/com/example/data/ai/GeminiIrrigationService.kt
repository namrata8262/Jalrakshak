package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.engine.AgronomicRuleEngine
import com.example.data.engine.WeatherRepository
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiIrrigationService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getIrrigationRecommendation(
        profile: FarmProfile,
        isDemoMode: Boolean = false
    ): Result<IrrigationRecommendation> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if real key is configured
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiService", "Gemini API key is unconfigured or placeholder. Using AgronomicRuleEngine fallback.")
            val fallback = AgronomicRuleEngine.generateRecommendation(profile, isAiGenerated = false, isDemoMode = isDemoMode)
            return@withContext Result.success(fallback)
        }

        val weather = WeatherRepository.getWeatherForLocation(profile.location)

        val systemPrompt = """
            You are 'JalRakshak AI', a premier agricultural hydrologist and smart irrigation expert tailored for small & medium farmers in Maharashtra, India.
            You must calculate precision irrigation advice based on FAO-56 Penman-Monteith crop evapotranspiration, soil moisture dynamics, and local rain forecasts.
            Always output ONLY a valid JSON object matching the requested schema. No conversational preamble.
        """.trimIndent()

        val userPrompt = """
            Analyze the following farm parameters and local weather:
            - Crop: ${profile.cropType} (${profile.growthStage})
            - Location: ${profile.location}
            - Soil: ${profile.soilType}
            - Land Size: ${profile.landSizeAcres} Acres
            - Current Soil Moisture: ${profile.soilMoisture}
            - Irrigation System: ${profile.irrigationMethod}
            - Water Source: ${profile.waterSource}
            - Live Weather: ${weather.temperature}°C, ${weather.condition}, Humidity ${weather.humidity}%, Rain Chance ${weather.rainChance}%, Forecast Rain: ${weather.rainfallForecastMm} mm, ETo: ${weather.etoMmDay} mm/day.

            Provide an optimal smart irrigation recommendation in strictly valid JSON format:
            {
              "decision": "SKIP_TODAY_WATER_TOMORROW" or "WATER_TODAY" or "SKIP_TODAY_RAIN",
              "decisionBadge": "SKIP TODAY • WATER TOMORROW" or "IRRIGATE TODAY",
              "headline": "concise 1-sentence decision for the farmer",
              "recommendedWaterLiters": 14500,
              "recommendedWaterMm": 5.8,
              "recommendedRunTimeHours": 2.5,
              "bestTimeWindow": "Tomorrow 06:00 AM – 08:30 AM",
              "riskLevel": "LOW" or "MEDIUM" or "HIGH",
              "cropHealthAlert": "Specific crop disease or physiological stress alert (e.g. blossom end rot, fungal blast, root asphyxiation)",
              "scientificReasoning": "Detailed scientific explanation referencing FAO-56 ETc, Kc, soil retention, and why skipping/watering today saves water & electricity",
              "waterSavedPercentage": 37,
              "nextSteps": [
                "Step 1 action item",
                "Step 2 action item",
                "Step 3 action item"
              ]
            }
        """.trimIndent()

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", "$systemPrompt\n\n$userPrompt") })
                }
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            }
            put("generationConfig", generationConfig)
        }

        val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(requestUrl)
            .post(requestJson.toString().toRequestBody(mediaType))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiService", "Gemini API responded with status ${response.code}. Falling back to rule engine.")
                val fallback = AgronomicRuleEngine.generateRecommendation(profile, isAiGenerated = false, isDemoMode = isDemoMode)
                return@withContext Result.success(fallback)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                val fallback = AgronomicRuleEngine.generateRecommendation(profile, isAiGenerated = false, isDemoMode = isDemoMode)
                return@withContext Result.success(fallback)
            }

            val parsedObj = JSONObject(text)
            val decision = parsedObj.optString("decision", "SKIP_TODAY_WATER_TOMORROW")
            val decisionBadge = parsedObj.optString("decisionBadge", "SKIP TODAY • WATER TOMORROW")
            val headline = parsedObj.optString("headline", "Hold irrigation today due to high rain probability; water tomorrow morning via drip.")
            val waterLiters = parsedObj.optInt("recommendedWaterLiters", 14500)
            val waterMm = parsedObj.optDouble("recommendedWaterMm", 5.8)
            val runTimeHours = parsedObj.optDouble("recommendedRunTimeHours", 2.5)
            val bestWindow = parsedObj.optString("bestTimeWindow", "Tomorrow 06:00 AM – 08:30 AM")
            val riskLevel = parsedObj.optString("riskLevel", "LOW")
            val cropAlert = parsedObj.optString("cropHealthAlert", "Blossom set stage: avoid waterlogging before rain to protect against collar rot.")
            val reasoning = parsedObj.optString("scientificReasoning", "Calculated via FAO-56 Penman-Monteith method. Soil retention and incoming precipitation satisfy immediate crop transpiration.")
            val savedPct = parsedObj.optInt("waterSavedPercentage", 36).coerceIn(20, 50)

            val stepsArray = parsedObj.optJSONArray("nextSteps")
            val nextSteps = mutableListOf<String>()
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    nextSteps.add(stepsArray.optString(i))
                }
            }
            if (nextSteps.isEmpty()) {
                nextSteps.add("Check drip filter mesh and pressure gauge.")
                nextSteps.add("Verify power schedule with MSEDCL agriculture feeder.")
                nextSteps.add("Perform soil ball test at 15cm depth tomorrow morning.")
            }

            val fallbackEngine = AgronomicRuleEngine.generateRecommendation(profile, isAiGenerated = true, isDemoMode = isDemoMode)

            val fullRecommendation = fallbackEngine.copy(
                decision = decision,
                decisionBadge = decisionBadge,
                headline = headline,
                recommendedWaterLiters = waterLiters,
                recommendedWaterMm = waterMm,
                recommendedRunTimeHours = runTimeHours,
                bestTimeWindow = bestWindow,
                riskLevel = riskLevel,
                cropHealthAlert = cropAlert,
                scientificReasoning = reasoning,
                nextSteps = nextSteps,
                waterSavedPercentage = savedPct,
                isAiGenerated = true,
                isDemoMode = isDemoMode
            )

            Result.success(fullRecommendation)
        } catch (e: Exception) {
            Log.e("GeminiService", "Network/Gemini error: ${e.message}. Using fallback engine.")
            val fallback = AgronomicRuleEngine.generateRecommendation(profile, isAiGenerated = false, isDemoMode = isDemoMode)
            Result.success(fallback)
        }
    }
}
