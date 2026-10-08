package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.WeatherRepository
import com.example.data.model.FarmProfile
import com.example.data.model.IrrigationRecommendation
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    farmProfile: FarmProfile,
    isLoading: Boolean,
    errorMessage: String?,
    isDemoMode: Boolean,
    savedPlans: List<IrrigationRecommendation>,
    onUpdateProfile: (FarmProfile) -> Unit,
    onLoadDemoFarm: () -> Unit,
    onGeneratePlan: () -> Unit,
    onOpenSavedPlan: (IrrigationRecommendation) -> Unit,
    onDeleteSavedPlan: (String) -> Unit
) {
    val weather = remember(farmProfile.location) {
        WeatherRepository.getWeatherForLocation(farmProfile.location)
    }

    val crops = listOf(
        "Tomato (Hybrid)",
        "Onion (Nashik Red)",
        "Sugarcane (Co 86032)",
        "Cotton (Bt Hybrid)",
        "Soybean (JS 335)",
        "Pomegranate (Bhagwa)",
        "Wheat (Lokwan)"
    )

    val locations = WeatherRepository.availableLocations

    val soils = listOf(
        "Black Cotton (Regur)",
        "Red Loamy",
        "Sandy Loam",
        "Clay Soil",
        "Alluvial Silt"
    )

    val moistureLevels = listOf(
        "Very Dry (<20%)",
        "Dry (20-35%)",
        "Optimal (40-60%)",
        "Wet/Saturated (>65%)"
    )

    val growthStages = listOf(
        "Seedling / Germination",
        "Vegetative Growth",
        "Flowering & Fruit Set",
        "Maturity / Harvesting"
    )

    val irrigationMethods = listOf(
        "Drip Irrigation (Thibak)",
        "Sprinkler Irrigation",
        "Furrow / Flood Irrigation"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Demo Action Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MintContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreenLight.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hackathon Quick Demo",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                        Text(
                            text = "Pune Tomato Farm (2.5 Acres, Drip)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnMintContainer
                        )
                    }

                    Button(
                        onClick = onLoadDemoFarm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        modifier = Modifier.testTag("load_demo_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load Demo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Local Weather & Agrometeorology Panel
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Microclimate & Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = weather.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (weather.rainChance > 50) WaterBlueContainer else AmberContainer
                        ) {
                            Text(
                                text = "${weather.temperature}°C • ${weather.condition}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (weather.rainChance > 50) OnWaterContainer else OnAmberContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherMetric(
                            label = "Rain Probability",
                            value = "${weather.rainChance}%",
                            icon = Icons.Default.CloudQueue,
                            color = WaterBlueSecondary
                        )
                        WeatherMetric(
                            label = "Rain Forecast",
                            value = "${weather.rainfallForecastMm} mm",
                            icon = Icons.Default.Thunderstorm,
                            color = WaterBlueSecondary
                        )
                        WeatherMetric(
                            label = "Humidity",
                            value = "${weather.humidity}%",
                            icon = Icons.Default.WaterDrop,
                            color = ForestGreenPrimary
                        )
                        WeatherMetric(
                            label = "ET Rate (ETo)",
                            value = "${weather.etoMmDay} mm/d",
                            icon = Icons.Default.WbSunny,
                            color = SunGoldTertiary
                        )
                    }
                }
            }
        }

        // Form Title
        item {
            Text(
                text = "Farm Profile & Crop Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Crop Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Crop Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(crops) { crop ->
                            val isSelected = farmProfile.cropType == crop
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(cropType = crop)) },
                                label = { Text(crop, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Location Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Location / Agricultural Basin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locations) { loc ->
                            val isSelected = farmProfile.location == loc
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(location = loc)) },
                                label = { Text(loc, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Soil Type & Land Size
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Soil Type & Land Area",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(soils) { soil ->
                            val isSelected = farmProfile.soilType == soil
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(soilType = soil)) },
                                label = { Text(soil, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Land Area (Acres)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${farmProfile.landSizeAcres} Acres (~${(farmProfile.landSizeAcres * 4047).toInt()} m²)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val newSize = (farmProfile.landSizeAcres - 0.5).coerceAtLeast(0.5)
                                    onUpdateProfile(farmProfile.copy(landSizeAcres = (newSize * 10).toInt() / 10.0))
                                }
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Acres")
                            }
                            Text(
                                text = "${farmProfile.landSizeAcres}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = {
                                    val newSize = (farmProfile.landSizeAcres + 0.5).coerceAtMost(25.0)
                                    onUpdateProfile(farmProfile.copy(landSizeAcres = (newSize * 10).toInt() / 10.0))
                                }
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Acres")
                            }
                        }
                    }
                }
            }
        }

        // Soil Moisture State & Crop Growth Stage
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "4. Current Soil Moisture Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moistureLevels) { moist ->
                            val isSelected = farmProfile.soilMoisture == moist
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(soilMoisture = moist)) },
                                label = { Text(moist, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "5. Crop Growth Stage",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(growthStages) { stage ->
                            val isSelected = farmProfile.growthStage == stage
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(growthStage = stage)) },
                                label = { Text(stage, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Irrigation System
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "6. Irrigation System",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(irrigationMethods) { method ->
                            val isSelected = farmProfile.irrigationMethod == method
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateProfile(farmProfile.copy(irrigationMethod = method)) },
                                label = { Text(method, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Main Action Button
        item {
            Button(
                onClick = onGeneratePlan,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_plan_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Calculating Crop Kc & Rainfall...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Irrigation Plan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Recent Saved Plans Section
        if (savedPlans.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Recent Saved Plans (${savedPlans.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    savedPlans.take(3).forEach { plan ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSavedPlan(plan) }
                                .testTag("saved_plan_item_${plan.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${plan.farmProfile.cropType} (${plan.farmProfile.landSizeAcres} Ac)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${plan.formattedDate} • ${plan.recommendedWaterLiters} L",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MintContainer
                                    ) {
                                        Text(
                                            text = plan.decisionBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onOpenSavedPlan(plan) },
                                        modifier = Modifier.testTag("open_saved_plan_${plan.id}")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open Plan", tint = ForestGreenPrimary)
                                    }
                                    IconButton(
                                        onClick = { onDeleteSavedPlan(plan.id) },
                                        modifier = Modifier.testTag("delete_saved_plan_${plan.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
