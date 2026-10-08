package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppNavigationBar
import com.example.ui.components.JalRakshakTopBar
import com.example.ui.screens.*
import com.example.ui.theme.JalRakshakTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JalRakshakViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JalRakshakTheme {
                JalRakshakApp()
            }
        }
    }
}

@Composable
fun JalRakshakApp(
    viewModel: JalRakshakViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val farmProfile by viewModel.farmProfile.collectAsStateWithLifecycle()
    val currentRecommendation by viewModel.currentRecommendation.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
    val saveSuccessMessage by viewModel.saveSuccessMessage.collectAsStateWithLifecycle()
    val savedPlans by viewModel.savedRecommendations.collectAsStateWithLifecycle()

    // Handle back navigation
    BackHandler(enabled = currentScreen != AppScreen.LANDING) {
        when (currentScreen) {
            AppScreen.DASHBOARD -> viewModel.navigateTo(AppScreen.LANDING)
            AppScreen.RECOMMENDATION -> viewModel.navigateTo(AppScreen.DASHBOARD)
            AppScreen.CALENDAR -> viewModel.navigateTo(AppScreen.RECOMMENDATION)
            AppScreen.IMPACT -> viewModel.navigateTo(AppScreen.LANDING)
            AppScreen.PITCH -> viewModel.navigateTo(AppScreen.LANDING)
            AppScreen.LANDING -> { /* Do nothing, default back handles exit */ }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            JalRakshakTopBar(
                currentScreen = currentScreen,
                isDemoMode = isDemoMode,
                onNavigateBack = if (currentScreen != AppScreen.LANDING) {
                    {
                        when (currentScreen) {
                            AppScreen.DASHBOARD -> viewModel.navigateTo(AppScreen.LANDING)
                            AppScreen.RECOMMENDATION -> viewModel.navigateTo(AppScreen.DASHBOARD)
                            AppScreen.CALENDAR -> viewModel.navigateTo(AppScreen.RECOMMENDATION)
                            AppScreen.IMPACT -> viewModel.navigateTo(AppScreen.LANDING)
                            AppScreen.PITCH -> viewModel.navigateTo(AppScreen.LANDING)
                            AppScreen.LANDING -> {}
                        }
                    }
                } else null,
                onNavigateTo = { viewModel.navigateTo(it) },
                onToggleDemoMode = { viewModel.toggleDemoMode() }
            )
        },
        bottomBar = {
            AppNavigationBar(
                currentScreen = currentScreen,
                onNavigateTo = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LANDING -> {
                    LandingScreen(
                        onTryDemo = {
                            viewModel.loadDemoFarm()
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        },
                        onOpenPitch = {
                            viewModel.navigateTo(AppScreen.PITCH)
                        },
                        onNavigateTo = { viewModel.navigateTo(it) }
                    )
                }

                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        farmProfile = farmProfile,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        isDemoMode = isDemoMode,
                        savedPlans = savedPlans,
                        onUpdateProfile = { viewModel.updateFarmProfile(it) },
                        onLoadDemoFarm = { viewModel.loadDemoFarm() },
                        onGeneratePlan = { viewModel.generateIrrigationPlan() },
                        onOpenSavedPlan = { viewModel.openSavedPlan(it) },
                        onDeleteSavedPlan = { viewModel.deleteSavedPlan(it) }
                    )
                }

                AppScreen.RECOMMENDATION -> {
                    RecommendationScreen(
                        recommendation = currentRecommendation,
                        saveSuccessMessage = saveSuccessMessage,
                        onSavePlan = { viewModel.saveCurrentPlan() },
                        onRegeneratePlan = { viewModel.generateIrrigationPlan() },
                        onSharePlan = { viewModel.sharePlanSummary() },
                        onViewCalendar = { viewModel.navigateTo(AppScreen.CALENDAR) },
                        onNewAnalysis = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                    )
                }

                AppScreen.CALENDAR -> {
                    CalendarScreen(
                        recommendation = currentRecommendation,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.RECOMMENDATION) }
                    )
                }

                AppScreen.IMPACT -> {
                    ImpactScreen()
                }

                AppScreen.PITCH -> {
                    PitchScreen(
                        onStartLiveDemo = {
                            viewModel.loadDemoFarm()
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        }
                    )
                }
            }
        }
    }
}
