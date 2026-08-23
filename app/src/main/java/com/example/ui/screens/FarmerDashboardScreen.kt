package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.KrishiQueueViewModel
import java.util.Locale

@Composable
fun FarmerDashboardScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val farmer by viewModel.farmerProfile.collectAsState()
    val activeBooking by viewModel.activeFarmerBooking.collectAsState()
    val activeAlert by viewModel.activeAlert.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val centres by viewModel.centres.collectAsState()
    val selectedCentreId by viewModel.selectedCentreId.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()
    val isSimRunning by viewModel.isSimulationRunning.collectAsState()

    val currentCentre = remember(centres, selectedCentreId) {
        centres.firstOrNull { it.id == selectedCentreId } ?: centres.firstOrNull()
    }

    // Filter bookings for current centre
    val centreBookings = remember(allBookings, selectedCentreId) {
        allBookings.filter { it.centreId == (currentCentre?.id ?: "centre-krn-01") }
    }

    val activeQueue = remember(centreBookings) {
        centreBookings.filter {
            it.status in listOf(
                BookingStatus.IN_PROCESS,
                BookingStatus.CALLED,
                BookingStatus.ARRIVED,
                BookingStatus.WAITING,
                BookingStatus.BOOKED
            )
        }.sortedWith(compareBy<Booking> {
            when (it.status) {
                BookingStatus.IN_PROCESS -> 0
                BookingStatus.CALLED -> 1
                BookingStatus.ARRIVED -> 2
                BookingStatus.WAITING -> 3
                BookingStatus.BOOKED -> 4
                else -> 5
            }
        }.thenBy { it.queuePosition })
    }

    val completedBookings = remember(centreBookings) {
        centreBookings.filter { it.status == BookingStatus.COMPLETED }
    }

    // Procurement calculations
    val totalProcuredQtl = remember(completedBookings) {
        completedBookings.sumOf { it.actualWeightQuintals ?: it.expectedQuantityQuintals } + 2840.0
    }
    val targetCapacityQtl = currentCentre?.dailyCapacityQuintals ?: 4500.0
    val progressFraction = (totalProcuredQtl / targetCapacityQtl).toFloat().coerceIn(0f, 1f)

    val totalPayoutLakhs = remember(completedBookings) {
        val payout = completedBookings.sumOf {
            it.totalAmountPaid ?: (it.expectedQuantityQuintals * it.cropType.mspPerQuintal)
        } + 6450000.0
        payout / 100000.0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Profile & Center Switcher
        item {
            FarmerHeaderCard(
                farmer = farmer,
                currentCentre = currentCentre,
                centres = centres,
                onSelectCentre = { viewModel.selectCentre(it) },
                isHindi = isHindi
            )
        }

        // 2. Real-Time Simulation & Alert Banner
        if (activeAlert != null) {
            item {
                ActiveAlertBanner(
                    alertMessage = activeAlert ?: "",
                    onDismiss = { viewModel.clearAlert() }
                )
            }
        }

        // 2b. Mock Push Notification Simulator Bar
        item {
            MockPushNotificationSimulatorBar(
                onTriggerPush = { viewModel.triggerMockPush(it) },
                isHindi = isHindi
            )
        }

        // 3. Main Procurement Dashboard: TOTAL PROCUREMENT VOLUME CARDS
        item {
            TotalProcurementVolumeSection(
                totalProcuredQtl = totalProcuredQtl,
                targetCapacityQtl = targetCapacityQtl,
                progressFraction = progressFraction,
                totalPayoutLakhs = totalPayoutLakhs,
                currentCentre = currentCentre,
                completedCount = completedBookings.size + 28,
                isHindi = isHindi
            )
        }

        // 4. Main Procurement Dashboard: ACTIVE FARMER QUEUES SUMMARY
        item {
            ActiveFarmerQueuesSection(
                activeQueue = activeQueue,
                currentCentre = currentCentre,
                isSimRunning = isSimRunning,
                onToggleSim = { viewModel.toggleSimulation() },
                onAdvanceQueue = { viewModel.triggerManualQueueAdvance() },
                isHindi = isHindi
            )
        }

        // 5. Main Procurement Dashboard: INDIVIDUAL WAIT TIMES BREAKDOWN
        item {
            IndividualWaitTimesSection(
                activeQueue = activeQueue,
                currentFarmerId = farmer.id,
                currentCentre = currentCentre,
                isHindi = isHindi
            )
        }

        // 6. Active Farmer Token / Next Procurement Card (if ongoing slot exists)
        item {
            ActiveTokenSpotlightCard(
                booking = activeBooking,
                isHindi = isHindi,
                onTrackQueue = { viewModel.navigateTo(AppDestination.LIVE_QUEUE) },
                onShowQr = { viewModel.showTokenQr(activeBooking) },
                onBookSlot = { viewModel.navigateTo(AppDestination.BOOK_SLOT) }
            )
        }

        // 7. Quick Action Navigation Grid
        item {
            QuickActionsGrid(
                onNavigate = { viewModel.navigateTo(it) },
                onOpenHelpChat = { viewModel.toggleHelpChat(true) },
                isHindi = isHindi
            )
        }

        // 8. Procurement Announcements & Mandi Guidelines
        item {
            AnnouncementsSection(
                announcements = announcements,
                isHindi = isHindi
            )
        }
    }
}

// ----------------------------------------------------------------------------
// 1. Farmer Header & Mandi Switcher
// ----------------------------------------------------------------------------
@Composable
private fun FarmerHeaderCard(
    farmer: FarmerProfile,
    currentCentre: ProcurementCentre?,
    centres: List<ProcurementCentre>,
    onSelectCentre: (String) -> Unit,
    isHindi: Boolean
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MintLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) "नमस्ते, ${farmer.fullName}" else "Welcome, ${farmer.fullName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${farmer.village}, ${farmer.district} • Reg: ${farmer.landRecordNo}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KYC VERIFIED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Mandi Selector Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = HarvestGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "खरीद मंडी केंद्र:" else "Mandi Hub:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Box {
                    Surface(
                        shape = AppButtonShape,
                        color = MintLight,
                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = (if (isHindi) currentCentre?.hindiName else currentCentre?.name)
                                    ?: "Select Centre",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        centres.forEach { centre ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = if (isHindi) centre.hindiName else centre.name,
                                            fontWeight = if (centre.id == currentCentre?.id) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = if (centre.id == currentCentre?.id) ForestGreenPrimary else TextPrimary
                                        )
                                        Text(
                                            text = "${centre.district}, ${centre.state} • ${centre.activeCounters} Counters",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (centre.id == currentCentre?.id) ForestGreenPrimary else TextMuted
                                    )
                                },
                                onClick = {
                                    onSelectCentre(centre.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 2. Active Alert Banner
// ----------------------------------------------------------------------------
@Composable
private fun ActiveAlertBanner(
    alertMessage: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
        border = BorderStroke(1.2.dp, HarvestGold),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFDE68A))
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = SoilBrown,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = alertMessage,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SoilBrown,
                modifier = Modifier.weight(1f),
                lineHeight = 16.sp
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = SoilBrown,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. Section: TOTAL PROCUREMENT VOLUME (Material 3 Cards)
// ----------------------------------------------------------------------------
@Composable
private fun TotalProcurementVolumeSection(
    totalProcuredQtl: Double,
    targetCapacityQtl: Double,
    progressFraction: Float,
    totalPayoutLakhs: Double,
    currentCentre: ProcurementCentre?,
    completedCount: Int,
    isHindi: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(600),
        label = "procurementProgress"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MintLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "कुल खरीद मात्रा व प्रगति" else "Total Procurement Volume",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = AppBadgeShape,
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (isHindi) "आज की प्रगति" else "Today's Intake",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Metrics 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total Volume (Quintals & MT)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MintLight,
                    border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "कुल तौल मात्रा" else "TOTAL VOLUME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.ENGLISH, "%,.1f Qtl", totalProcuredQtl),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = String.format(Locale.ENGLISH, "≈ %,.1f Metric Tonnes", totalProcuredQtl / 10.0),
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Card 2: DBT Payout Disbursed
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldLight,
                    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "डीबीटी भुगतान" else "DBT DISBURSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown
                            )
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = HarvestGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.ENGLISH, "₹%.2f Lakh", totalPayoutLakhs),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = HarvestGold
                        )
                        Text(
                            text = if (isHindi) "100% प्रत्यक्ष बैंक अंतरण" else "Direct Bank Transfer",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Capacity Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "दैनिक क्षमता लक्ष्य:" else "Daily Capacity Target:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${String.format(Locale.ENGLISH, "%.0f", totalProcuredQtl)} / ${String.format(Locale.ENGLISH, "%.0f", targetCapacityQtl)} Qtl (${(progressFraction * 100).toInt()}%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ForestGreenPrimary,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Crop-wise Breakdown Mini Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropVolumePill(
                    cropName = if (isHindi) "गेहूं (Wheat)" else "Wheat (MSP ₹2,275)",
                    volume = "2,150 Qtl",
                    share = "68%",
                    color = ForestGreenPrimary,
                    bgColor = MintLight,
                    modifier = Modifier.weight(1f)
                )
                CropVolumePill(
                    cropName = if (isHindi) "सरसों (Mustard)" else "Mustard (MSP ₹5,650)",
                    volume = "630 Qtl",
                    share = "20%",
                    color = HarvestGold,
                    bgColor = GoldLight,
                    modifier = Modifier.weight(1f)
                )
                CropVolumePill(
                    cropName = if (isHindi) "धान (Paddy)" else "Paddy (MSP ₹2,300)",
                    volume = "400 Qtl",
                    share = "12%",
                    color = StatusInfo,
                    bgColor = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CropVolumePill(
    cropName: String,
    volume: String,
    share: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = cropName,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = SoilBrown,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = volume,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = share,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 4. Section: ACTIVE FARMER QUEUES SUMMARY (Material 3 Cards & Live Simulation)
// ----------------------------------------------------------------------------
@Composable
private fun ActiveFarmerQueuesSection(
    activeQueue: List<Booking>,
    currentCentre: ProcurementCentre?,
    isSimRunning: Boolean,
    onToggleSim: () -> Unit,
    onAdvanceQueue: () -> Unit,
    isHindi: Boolean
) {
    val inProcessCount = activeQueue.count { it.status == BookingStatus.IN_PROCESS }
    val calledCount = activeQueue.count { it.status == BookingStatus.CALLED }
    val waitingCount = activeQueue.count { it.status == BookingStatus.WAITING || it.status == BookingStatus.ARRIVED }
    val bookedCount = activeQueue.count { it.status == BookingStatus.BOOKED }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with Live Sim Controller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleAlt,
                            contentDescription = null,
                            tint = HarvestGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "सक्रिय किसान कतार सारांश" else "Active Farmer Queues Summary",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentCentre?.activeCounters ?: 4} ${if (isHindi) "कांटे/काउंटर चालू हैं" else "Weighbridge Counters Active"}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Sim Controls Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = AppButtonShape,
                        color = if (isSimRunning) MintLight else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isSimRunning) ForestGreenPrimary.copy(alpha = 0.4f) else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { onToggleSim() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSimRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isSimRunning) ForestGreenPrimary else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSimRunning) "LIVE" else "PAUSED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimRunning) ForestGreenPrimary else TextMuted
                            )
                        }
                    }

                    Surface(
                        shape = AppButtonShape,
                        color = MintLight,
                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onAdvanceQueue() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Advance Next",
                            tint = ForestGreenPrimary,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Status Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QueueStateCard(
                    title = if (isHindi) "तौल जारी" else "In-Process",
                    count = inProcessCount,
                    token = activeQueue.firstOrNull { it.status == BookingStatus.IN_PROCESS }?.tokenNumber ?: "CP-098",
                    color = StatusPurple,
                    bgColor = Color(0xFFEDE9FE),
                    modifier = Modifier.weight(1f)
                )
                QueueStateCard(
                    title = if (isHindi) "बुलाया गया" else "Called",
                    count = calledCount,
                    token = activeQueue.firstOrNull { it.status == BookingStatus.CALLED }?.tokenNumber ?: "CP-099",
                    color = Color(0xFFDC2626),
                    bgColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f)
                )
                QueueStateCard(
                    title = if (isHindi) "कतार में" else "In Queue",
                    count = waitingCount,
                    token = "${waitingCount} Farmers",
                    color = HarvestGold,
                    bgColor = GoldLight,
                    modifier = Modifier.weight(1f)
                )
                QueueStateCard(
                    title = if (isHindi) "स्लॉट बुक" else "Booked",
                    count = bookedCount,
                    token = "${bookedCount} Today",
                    color = StatusInfo,
                    bgColor = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Currently Serving Ticker Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MintLight,
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ForestGreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isHindi) "वर्तमान में सेवा जारी (कांटा 1 & 2)" else "Now Serving at Weighbridges",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                            val servingTokens = activeQueue.filter { it.status in listOf(BookingStatus.IN_PROCESS, BookingStatus.CALLED) }
                                .joinToString(", ") { "${it.tokenNumber} (C-${it.counterNumber})" }
                            Text(
                                text = if (servingTokens.isNotEmpty()) servingTokens else "CP-098 (C-1), CP-099 (C-2)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = SoilBrown
                            )
                        }
                    }

                    Text(
                        text = "${activeQueue.size} in queue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueStateCard(
    title: String,
    count: Int,
    token: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SoilBrown,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = token,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ----------------------------------------------------------------------------
// 5. Section: INDIVIDUAL WAIT TIMES BREAKDOWN (Material 3 Cards)
// ----------------------------------------------------------------------------
@Composable
private fun IndividualWaitTimesSection(
    activeQueue: List<Booking>,
    currentFarmerId: String,
    currentCentre: ProcurementCentre?,
    isHindi: Boolean
) {
    var showAllItems by remember { mutableStateOf(false) }
    val displayList = remember(activeQueue, showAllItems) {
        if (showAllItems) activeQueue else activeQueue.take(6)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F2FE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = StatusInfo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "व्यक्तिगत प्रतीक्षा समय (लाइव कतार)" else "Individual Wait Times Breakdown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "औसत समय: ~7 मिनट प्रति ट्रैक्टर ट्रॉली" else "Avg Service Speed: ~7m per Trolley",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = AppBadgeShape,
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "SLA: < 45m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Column Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "टोकन / किसान" else "Token / Farmer",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = if (isHindi) "फसल" else "Crop",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = if (isHindi) "कांटा" else "Counter",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = if (isHindi) "प्रतीक्षा समय" else "Wait Time",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of upcoming individual tokens with estimated wait times
            displayList.forEachIndexed { index, booking ->
                val isCurrentFarmer = booking.farmerId == currentFarmerId
                val isFirst = index == 0

                val (rowBg, borderColor) = when {
                    isCurrentFarmer -> Pair(MintLight, ForestGreenPrimary)
                    isFirst -> Pair(GoldLight.copy(alpha = 0.5f), HarvestGold.copy(alpha = 0.4f))
                    else -> Pair(Color.Transparent, Color.Transparent)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = rowBg,
                    border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        // Token & Farmer Name
                        Column(modifier = Modifier.weight(1.3f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = booking.tokenNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isCurrentFarmer) ForestGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrentFarmer) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ForestGreenPrimary
                                    ) {
                                        Text(
                                            text = "YOU",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = booking.farmerName,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Crop & Quantity
                        Column(modifier = Modifier.weight(0.9f)) {
                            Text(
                                text = booking.cropType.englishName.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${booking.expectedQuantityQuintals.toInt()} Qtl",
                                fontSize = 9.5.sp,
                                color = TextMuted
                            )
                        }

                        // Counter #
                        Text(
                            text = "Counter ${booking.counterNumber}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SoilBrown,
                            modifier = Modifier.weight(0.8f)
                        )

                        // Wait Time Badge
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            val (waitText, waitColor) = when {
                                booking.status == BookingStatus.IN_PROCESS -> Pair("0 min (At Desk)", StatusPurple)
                                booking.status == BookingStatus.CALLED -> Pair("~3-5 mins", Color(0xFFDC2626))
                                booking.estimatedWaitMinutes <= 15 -> Pair("~${booking.estimatedWaitMinutes} mins", ForestGreenPrimary)
                                booking.estimatedWaitMinutes <= 35 -> Pair("~${booking.estimatedWaitMinutes} mins", HarvestGold)
                                else -> Pair("~${booking.estimatedWaitMinutes} mins", StatusInfo)
                            }

                            Text(
                                text = waitText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = waitColor
                            )
                            Text(
                                text = when (booking.status) {
                                    BookingStatus.IN_PROCESS -> "Weighing"
                                    BookingStatus.CALLED -> "Called"
                                    BookingStatus.ARRIVED -> "At Gate"
                                    BookingStatus.WAITING -> "Pos #${booking.queuePosition}"
                                    BookingStatus.BOOKED -> "Booked"
                                    else -> ""
                                },
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            if (activeQueue.size > 6) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { showAllItems = !showAllItems },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (showAllItems) (if (isHindi) "कम दिखाएं" else "Show Less")
                        else (if (isHindi) "सभी ${activeQueue.size} किसान कतार देखें" else "View All ${activeQueue.size} Farmers in Queue"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Icon(
                        imageVector = if (showAllItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 6. Section: ACTIVE FARMER TOKEN SPOTLIGHT CARD
// ----------------------------------------------------------------------------
@Composable
private fun ActiveTokenSpotlightCard(
    booking: Booking?,
    isHindi: Boolean,
    onTrackQueue: () -> Unit,
    onShowQr: () -> Unit,
    onBookSlot: () -> Unit
) {
    if (booking != null) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MintLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "आपका सक्रिय खरीद स्लॉट" else "Your Active Procurement Slot",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }
                    StatusBadge(status = booking.status, isHindi = isHindi)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Big Highlight Boxes: Token / Queue Pos / Est Wait
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Token
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintLight,
                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHindi) "टोकन" else "TOKEN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown
                            )
                            Text(
                                text = booking.tokenNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ForestGreenPrimary
                            )
                        }
                    }

                    // Queue Position
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldLight,
                        border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHindi) "कतार स्थिति" else "QUEUE POS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown
                            )
                            Text(
                                text = if (booking.queuePosition == 0) (if (isHindi) "काउंटर पर" else "At Desk") else "#${booking.queuePosition}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = HarvestGold
                            )
                        }
                    }

                    // Estimated Wait
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, StatusInfo.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHindi) "अनुमानित प्रतीक्षा" else "EST. WAIT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = if (booking.queuePosition == 0) "0 min" else "~${booking.estimatedWaitMinutes}m",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = StatusInfo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Centre & Slot Info
                Text(
                    text = booking.centreName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${booking.bookingDate} • ${booking.timeSlot} • ${booking.cropType.englishName} (${booking.expectedQuantityQuintals} Qtl)",
                    fontSize = 11.5.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stage Timeline Tracker
                LiveQueueStageTracker(status = booking.status, isHindi = isHindi)

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KrishiPrimaryButton(
                        text = if (isHindi) "लाइव कतार ट्रैक करें" else "Track Live Queue",
                        icon = Icons.Default.OnlinePrediction,
                        onClick = onTrackQueue,
                        modifier = Modifier.weight(1f)
                    )

                    KrishiSecondaryButton(
                        text = if (isHindi) "डिजिटल पास" else "QR Pass",
                        icon = Icons.Default.QrCode,
                        onClick = onShowQr,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    } else {
        // Empty State: Prompt to Book Slot
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MintLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isHindi) "कोई सक्रिय खरीद स्लॉट नहीं है" else "No Active Procurement Slot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "अपनी फसल की सरकारी दर (MSP) पर बिक्री के लिए बिना कतार लगे डिजिटल स्लॉट बुक करें"
                    else "Book a hassle-free digital procurement token at official MSP rates without waiting in physical mandi queues.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                KrishiPrimaryButton(
                    text = if (isHindi) "नया स्लॉट बुक करें" else "Book a Procurement Slot",
                    icon = Icons.Default.Add,
                    onClick = onBookSlot
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 7. Section: QUICK ACTIONS GRID
// ----------------------------------------------------------------------------
@Composable
private fun QuickActionsGrid(
    onNavigate: (AppDestination) -> Unit,
    onOpenHelpChat: () -> Unit,
    isHindi: Boolean
) {
    Text(
        text = if (isHindi) "त्वरित सुविधाएं" else "Quick Portal Actions",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Krishi Sahayak Voice Help Chatbot Feature Card
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MintLight),
        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenHelpChat)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ForestGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isHindi) "कृषि सहायक (Voice Chatbot)" else "Krishi Sahayak (Voice Chatbot)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HarvestGold
                        ) {
                            Text(
                                text = "24/7 AI",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isHindi) "एमएसपी, स्लॉट व कतार स्थिति बोलकर पूछें व सुनें" else "Instant voice Q&A for MSP, slots & queue times",
                        fontSize = 10.5.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, ForestGreenPrimary.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionTile(
            title = if (isHindi) "स्लॉट बुकिंग" else "Book Slot",
            subtitle = if (isHindi) "नया टोकन" else "New Token",
            icon = Icons.Default.CalendarMonth,
            containerColor = MintLight,
            iconTint = ForestGreenPrimary,
            onClick = { onNavigate(AppDestination.BOOK_SLOT) },
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            title = if (isHindi) "खरीद केंद्र" else "Mandis Map",
            subtitle = if (isHindi) "नजदीकी केंद्र" else "Find Centres",
            icon = Icons.Default.Storefront,
            containerColor = GoldLight,
            iconTint = HarvestGold,
            onClick = { onNavigate(AppDestination.CENTRES_MAP) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionTile(
            title = if (isHindi) "रसीदें व इतिहास" else "History & Slips",
            subtitle = if (isHindi) "डीबीटी भुगतान" else "Past Receipts",
            icon = Icons.Default.ReceiptLong,
            containerColor = Color(0xFFE0F2FE),
            iconTint = StatusInfo,
            onClick = { onNavigate(AppDestination.HISTORY_RECEIPTS) },
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            title = if (isHindi) "किसान प्रोफाइल" else "Farm Profile",
            subtitle = if (isHindi) "Room DB सहेजा" else "Room DB Profile",
            icon = Icons.Default.Badge,
            containerColor = Color(0xFFEDE9FE),
            iconTint = StatusPurple,
            onClick = { onNavigate(AppDestination.FARMER_PROFILE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = AppCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clip(AppCardShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 8. Section: OFFICIAL MANDI ANNOUNCEMENTS
// ----------------------------------------------------------------------------
@Composable
private fun AnnouncementsSection(
    announcements: List<Announcement>,
    isHindi: Boolean
) {
    Text(
        text = if (isHindi) "मंडी सूचनाएं व घोषणाएं" else "Procurement Centre Announcements",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))

    announcements.forEach { ann ->
        Card(
            shape = AppCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (ann.isUrgent) Color(0xFFFEE2E2) else MintLight)
                ) {
                    Icon(
                        imageVector = if (ann.isUrgent) Icons.Default.PriorityHigh else Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (ann.isUrgent) Color(0xFFDC2626) else ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) ann.hindiTitle else ann.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ann.timestamp,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) ann.hindiMessage else ann.message,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
