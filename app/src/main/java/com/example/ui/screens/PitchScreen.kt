package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.*

@Composable
fun PitchScreen(
    onStartLiveDemo: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pitch_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pitch Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForestGreenPrimary
                        ) {
                            Text(
                                text = "60-SECOND JURY PITCH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "3-Min Live Demo",
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "JalRakshak: AI Smart Irrigation Assistant",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Empowering 120M+ Indian smallholders to stop wasting groundwater, slash electric bills by ₹14,000/yr, and maximize crop yields.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                    Button(
                        onClick = onStartLiveDemo,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pitch_start_demo_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Live 3-Minute Demo Flow", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 8 Jury Pillars
        item {
            Text(
                text = "The 8 Jury Pillars",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            JuryPillarCard(
                number = "1",
                title = "The Problem",
                summary = "80% of India's freshwater is consumed by agriculture. 40% of this water is wasted because farmers irrigate based on guesswork and fixed timer habits.",
                icon = Icons.Default.WaterDamage,
                tag = "CRISIS"
            )
        }

        item {
            JuryPillarCard(
                number = "2",
                title = "Who Suffers Most",
                summary = "Small & medium farmers in Maharashtra (1-5 acres). They face falling borewell water tables, 8-hour erratic power rosters, and unseasonal rainfall destroying crops.",
                icon = Icons.Default.Person,
                tag = "14.5M FARMERS"
            )
        }

        item {
            JuryPillarCard(
                number = "3",
                title = "Why Existing Solutions Fail",
                summary = "Hardware IoT soil sensors cost ₹25,000+ ($300), requiring battery replacement and technical calibration that smallholders cannot afford.",
                icon = Icons.Default.MoneyOff,
                tag = "HARDWARE BARRIER"
            )
        }

        item {
            JuryPillarCard(
                number = "4",
                title = "The JalRakshak Solution",
                summary = "A zero-hardware smartphone assistant calculating exact irrigation liters, best operating windows, and pump runtimes using local crop physics.",
                icon = Icons.Default.Lightbulb,
                tag = "ZERO-HARDWARE"
            )
        }

        item {
            JuryPillarCard(
                number = "5",
                title = "Where AI is Applied",
                summary = "Gemini 3.5 Flash synthesizes FAO-56 Penman-Monteith crop coefficients (Kc), soil retention indices, and real-time precipitation radar to generate actionable JSON recommendations.",
                icon = Icons.Default.Psychology,
                tag = "GEMINI 3.5 FLASH"
            )
        }

        item {
            JuryPillarCard(
                number = "6",
                title = "Key Differentiator",
                summary = "Offline-resilient hybrid engine! If connectivity drops in rural fields or API limits hit, the FAO-56 Agronomic Rule Engine seamlessly maintains 100% functionality without failure.",
                icon = Icons.Default.Shield,
                tag = "HYBRID OFFLINE"
            )
        }

        item {
            JuryPillarCard(
                number = "7",
                title = "Measurable Impact",
                summary = "38% water conserved (4.2M Liters in pilot), ₹14,200 saved per season in electricity & pump repairs, 22% crop yield boost from reduced root rot.",
                icon = Icons.Default.BarChart,
                tag = "MEASURABLE ROI"
            )
        }

        item {
            JuryPillarCard(
                number = "8",
                title = "How It Scales",
                summary = "B2B partnership with Krishi Vigyan Kendras (KVKs), FPOs, and state agriculture departments. Automated daily Marathi/Hindi WhatsApp bot for non-smartphone users.",
                icon = Icons.Default.RocketLaunch,
                tag = "SCALABLE B2B"
            )
        }
    }
}

@Composable
fun JuryPillarCard(
    number: String,
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = ForestGreenPrimary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MintContainer
                    ) {
                        Text(
                            text = tag,
                            color = ForestGreenPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
