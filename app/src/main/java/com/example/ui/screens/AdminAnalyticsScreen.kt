package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CentreAnalytics
import com.example.models.Language
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun AdminAnalyticsScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val centres by viewModel.centres.collectAsState()
    val selectedCentreId by viewModel.selectedCentreId.collectAsState()

    val currentCentre = centres.firstOrNull { it.id == selectedCentreId } ?: centres.firstOrNull()
    val analytics = remember(selectedCentreId) { viewModel.getCentreAnalytics() }

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMsg by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "मंडी प्रबंधन व विश्लेषण डैशबोर्ड" else "Centre Management & Analytics",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }

                        KrishiPrimaryButton(
                            text = if (isHindi) "घोषणा जारी करें" else "Broadcast",
                            icon = Icons.Default.Campaign,
                            onClick = { showBroadcastDialog = true },
                            containerColor = HarvestGold,
                            height = 36.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentCentre?.name} (${currentCentre?.district}, ${currentCentre?.state})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Real-Time KPI Cards Grid
        item {
            Text(
                text = if (isHindi) "आज की प्रमुख परिचालन मेट्रिक्स" else "Today's Operational Metrics",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (isHindi) "कुल टोकन बुक" else "Total Bookings",
                    value = "${analytics.totalBookingsToday}",
                    subtitle = if (isHindi) "आज का कोटा" else "Today's Quota",
                    icon = Icons.Default.ConfirmationNumber,
                    iconTint = ForestGreenPrimary,
                    iconBg = MintLight,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = if (isHindi) "खरीद पूर्ण" else "Completed",
                    value = "${analytics.completedCount}",
                    subtitle = if (isHindi) "100% डीबीटी जारी" else "Receipts Issued",
                    icon = Icons.Default.CheckCircle,
                    iconTint = StatusSuccess,
                    iconBg = Color(0xFFDCFCE7),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = if (isHindi) "कुल आवक मात्रा" else "Volume Procured",
                    value = "${String.format("%,.0f", analytics.totalProcuredQuintals)} Qtl",
                    subtitle = if (isHindi) "गेहूं, धान व सरसों" else "Wheat, Paddy, Mustard",
                    icon = Icons.Default.Scale,
                    iconTint = HarvestGold,
                    iconBg = GoldLight,
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = if (isHindi) "औसत प्रतीक्षा" else "Avg Wait Time",
                    value = "${analytics.avgWaitTimeMinutes} mins",
                    subtitle = if (isHindi) "68% समय बचत" else "Down from 12 hrs",
                    icon = Icons.Default.Timer,
                    iconTint = StatusInfo,
                    iconBg = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Hourly Throughput Chart (Arrivals vs Completions)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "प्रति घंटे आवक व निपटान चार्ट" else "Hourly Inflow vs Completion Rate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "हरा = पूर्ण खरीद, नारंगी = किसान आवक" else "Green = Procured, Orange = Arrivals",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Jetpack Compose Bar Chart
                    HourlyBarChartCanvas(
                        arrivals = analytics.hourlyArrivals.map { it.count },
                        completions = analytics.hourlyCompletions.map { it.count },
                        labels = analytics.hourlyArrivals.map { it.hourLabel }
                    )
                }
            }
        }
    }

    // Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Text(
                    text = if (isHindi) "मंडी घोषणा प्रसारित करें" else "Broadcast Announcement to Farmers",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text(if (isHindi) "घोषणा शीर्षक" else "Title") },
                        placeholder = { Text("e.g. Counter 4 Operational") },
                        shape = AppButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastMsg,
                        onValueChange = { broadcastMsg = it },
                        label = { Text(if (isHindi) "संदेश विवरण" else "Message") },
                        placeholder = { Text("Enter detailed instruction...") },
                        minLines = 3,
                        shape = AppButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                KrishiPrimaryButton(
                    text = if (isHindi) "प्रसारित करें" else "Publish Alert",
                    onClick = {
                        if (broadcastTitle.isNotBlank()) {
                            viewModel.broadcastAnnouncement(
                                title = broadcastTitle,
                                hindiTitle = broadcastTitle,
                                message = broadcastMsg,
                                hindiMessage = broadcastMsg,
                                isUrgent = isUrgent
                            )
                            showBroadcastDialog = false
                        }
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun HourlyBarChartCanvas(
    arrivals: List<Int>,
    completions: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        val barCount = arrivals.size
        val groupWidth = w / barCount
        val barWidth = groupWidth * 0.32f
        val maxVal = 40f

        for (i in 0 until barCount) {
            val gx = i * groupWidth + groupWidth * 0.15f
            val arrH = (arrivals[i] / maxVal) * (h - 20f)
            val compH = (completions[i] / maxVal) * (h - 20f)

            // Arrival bar (Orange)
            drawRoundRect(
                color = SaffronOrange,
                topLeft = Offset(gx, h - 20f - arrH),
                size = Size(barWidth, arrH),
                cornerRadius = CornerRadius(3f, 3f)
            )

            // Completion bar (Green)
            drawRoundRect(
                color = ForestGreenPrimary,
                topLeft = Offset(gx + barWidth + 3f, h - 20f - compH),
                size = Size(barWidth, compH),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }

        // Base baseline line
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(0f, h - 20f),
            end = Offset(w, h - 20f),
            strokeWidth = 1.5f
        )
    }
}
