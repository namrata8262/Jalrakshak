package com.example

import com.example.data.engine.AgronomicRuleEngine
import com.example.data.engine.WeatherRepository
import com.example.data.model.FarmProfile
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testWeatherRepositoryPune() {
        val weather = WeatherRepository.getWeatherForLocation("Pune (Shirur / Haveli)")
        assertNotNull(weather)
        assertEquals("Pune (Shirur / Haveli)", weather.location)
        assertTrue(weather.rainChance > 50)
        assertTrue(weather.rainfallForecastMm > 5.0)
    }

    @Test
    fun testAgronomicRuleEngineRecommendation() {
        val profile = FarmProfile(
            cropType = "Tomato (Hybrid)",
            location = "Pune (Shirur / Haveli)",
            soilType = "Black Cotton (Regur)",
            landSizeAcres = 2.5,
            soilMoisture = "Dry (20-35%)",
            growthStage = "Flowering & Fruit Set",
            irrigationMethod = "Drip Irrigation (Thibak)"
        )
        val rec = AgronomicRuleEngine.generateRecommendation(profile)
        assertNotNull(rec)
        assertTrue(rec.recommendedWaterLiters > 5000)
        assertTrue(rec.waterSavedPercentage in 20..50)
        assertEquals(7, rec.sevenDaySchedule.size)
        assertTrue(rec.costSavedInr > 0)
        assertTrue(rec.decision.contains("RAIN") || rec.decision.contains("SKIP") || rec.decision.contains("WATER"))
    }
}
