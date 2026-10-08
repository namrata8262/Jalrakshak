package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.WeatherRepository
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import com.example.data.model.WeatherInfo
import com.example.data.repository.IrrigationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LANDING,
    DASHBOARD,
    RECOMMENDATION,
    CALENDAR,
    IMPACT,
    PITCH
}

class JalRakshakViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IrrigationRepository(application)

    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _farmProfile = MutableStateFlow(repository.getDemoFarmProfile())
    val farmProfile: StateFlow<FarmProfile> = _farmProfile.asStateFlow()

    private val _currentRecommendation = MutableStateFlow<IrrigationRecommendation?>(null)
    val currentRecommendation: StateFlow<IrrigationRecommendation?> = _currentRecommendation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _saveSuccessMessage = MutableStateFlow<String?>(null)
    val saveSuccessMessage: StateFlow<String?> = _saveSuccessMessage.asStateFlow()

    val savedRecommendations: StateFlow<List<IrrigationRecommendation>> =
        repository.savedRecommendations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentWeather: StateFlow<WeatherInfo> = MutableStateFlow(
        WeatherRepository.getWeatherForLocation(farmProfile.value.location)
    )

    init {
        // Preload demo recommendation for instantaneous demo flow
        _currentRecommendation.value = repository.getInitialDemoRecommendation()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _errorMessage.value = null
    }

    fun updateFarmProfile(newProfile: FarmProfile) {
        _farmProfile.value = newProfile
    }

    fun loadDemoFarm() {
        val demo = repository.getDemoFarmProfile()
        _farmProfile.value = demo
        _isDemoMode.value = true
        _errorMessage.value = null
    }

    fun toggleDemoMode() {
        _isDemoMode.value = !_isDemoMode.value
    }

    fun generateIrrigationPlan() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // Short realistic processing delay to show AI animation
            delay(1200)

            val result = repository.generateRecommendation(_farmProfile.value, _isDemoMode.value)
            result.onSuccess { rec ->
                _currentRecommendation.value = rec
                _isLoading.value = false
                _currentScreen.value = AppScreen.RECOMMENDATION
            }.onFailure { err ->
                _isLoading.value = false
                _errorMessage.value = err.message ?: "Failed to generate plan. Please try again."
            }
        }
    }

    fun saveCurrentPlan() {
        val rec = _currentRecommendation.value ?: return
        viewModelScope.launch {
            repository.saveRecommendation(rec)
            _saveSuccessMessage.value = "Irrigation Plan saved successfully!"
            delay(3000)
            _saveSuccessMessage.value = null
        }
    }

    fun deleteSavedPlan(id: String) {
        viewModelScope.launch {
            repository.deleteRecommendation(id)
        }
    }

    fun openSavedPlan(recommendation: IrrigationRecommendation) {
        _farmProfile.value = recommendation.farmProfile
        _currentRecommendation.value = recommendation
        _currentScreen.value = AppScreen.RECOMMENDATION
    }

    fun sharePlanSummary() {
        val rec = _currentRecommendation.value ?: return
        val context = getApplication<Application>()
        val shareText = """
            🌾 JalRakshak Smart Irrigation Plan
            Crop: ${rec.farmProfile.cropType} (${rec.farmProfile.landSizeAcres} Acres)
            Location: ${rec.farmProfile.location}
            
            ⚡ Decision: ${rec.decisionBadge}
            💧 Water Volume: ${rec.recommendedWaterLiters} Liters (${rec.recommendedWaterMm} mm)
            ⏰ Best Window: ${rec.bestTimeWindow}
            🚜 Pump Runtime: ${rec.recommendedRunTimeHours} Hours (5HP Drip)
            
            🌧️ Weather Advice: ${rec.weatherSummary}
            🛡️ Crop Alert: ${rec.cropHealthAlert}
            💰 Water Saved: ${rec.waterSavedPercentage}% (₹${rec.costSavedInr} saved)
            
            Powered by JalRakshak AI
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "JalRakshak Irrigation Plan - ${rec.farmProfile.cropType}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share Irrigation Plan via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
