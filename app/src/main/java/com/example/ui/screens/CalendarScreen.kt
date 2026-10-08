package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayScheduleItem
import com.example.data.model.IrrigationRecommendation
import com.example.ui.theme.*

@Composable
fun CalendarScreen(
    recommendation: IrrigationRecommendation?,
    onNavigateBack: () -> Unit
) {
    val schedule = recommendation?.sevenDaySchedule ?: emptyList()
    val totalWater = schedule.sumOf { it.waterLiters }
    val totalMins = schedule.sumOf { it.runtimeMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("calendar_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "7-Day Irrigation Calendar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${recommendation?.farmProfile?.cropType ?: "Crop"} • ${recommendation?.farmProfile?.location ?: "Location"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MintContainer
                        ) {
                            Text(
                                text = "Adaptive Radar",
                                color = ForestGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("7-Day Water Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalWater Liters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WaterBlueSecondary)
                        }
                        Column {
                            Text("Total Pump Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.1f".format(totalMins / 60.0)} Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                        Column {
                            Text("Irrigation Cycles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${schedule.count { it.irrigationNeeded }} of 7 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SunGoldTertiary)
                        }
                    }
                }
            }
        }

        items(schedule) { dayItem ->
            DayScheduleCard(item = dayItem)
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ForestGreenPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Calendar automatically shifts forward whenever real-time rain exceeds 5mm, dynamically postponing next irrigation cycle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnMintContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DayScheduleCard(item: DayScheduleItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.dayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${item.date})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.irrigationNeeded) MintContainer else SurfaceVariantTint
                ) {
                    Text(
                        text = if (item.irrigationNeeded) "WATER • ${item.waterLiters} L" else if (item.rainfallMm > 0) "RAIN (${item.rainfallMm}mm)" else "REST DAY",
                        color = if (item.irrigationNeeded) ForestGreenPrimary else Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.rainfallMm > 0) Icons.Default.Thunderstorm else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = if (item.rainfallMm > 0) WaterBlueSecondary else SunGoldTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.weather} • ${item.tempMax}° / ${item.tempMin}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.irrigationNeeded) {
                    Text(
                        text = "Run: ${item.runtimeMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestGreenPrimary
                    )
                }
            }

            // Projected Moisture Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Projected Root Zone Moisture",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.projectedMoisturePercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.projectedMoisturePercent in 40..65) ForestGreenPrimary else WarningOrange
                    )
                }
                LinearProgressIndicator(
                    progress = { item.projectedMoisturePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (item.projectedMoisturePercent in 40..65) ForestGreenPrimary else WarningOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
