package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Booking
import com.example.models.BookingStatus
import com.example.models.Language
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun LiveQueueScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val centres by viewModel.centres.collectAsState()
    val selectedCentreId by viewModel.selectedCentreId.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val farmerProfile by viewModel.farmerProfile.collectAsState()
    val isSimRunning by viewModel.isSimulationRunning.collectAsState()

    val currentCentre = centres.firstOrNull { it.id == selectedCentreId } ?: centres.firstOrNull()
    val centreBookings = bookings.filter { it.centreId == (currentCentre?.id ?: "") }
    val activeQueue = centreBookings.filter {
        it.status in listOf(BookingStatus.IN_PROCESS, BookingStatus.CALLED, BookingStatus.ARRIVED, BookingStatus.WAITING, BookingStatus.BOOKED)
    }.sortedBy { it.tokenNumber }

    val myBooking = centreBookings.firstOrNull { it.farmerId == farmerProfile.id && it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
    val currentlyServing = activeQueue.firstOrNull { it.status == BookingStatus.IN_PROCESS } ?: activeQueue.firstOrNull()

    val peopleAhead = if (myBooking != null) {
        val myIdx = activeQueue.indexOfFirst { it.id == myBooking.id }
        if (myIdx >= 0) myIdx else myBooking.queuePosition
    } else 0

    // Pulsing visual effect for currently serving
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Queue Centre Header
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
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "रीयल-टाइम कतार ट्रैकर" else "Real-Time Live Queue",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }

                        // Simulation toggle button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSimRunning) MintLight else Color(0xFFF3F4F6),
                            border = BorderStroke(1.dp, if (isSimRunning) ForestGreenPrimary else Color.LightGray),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.toggleSimulation() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSimRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isSimRunning) ForestGreenPrimary else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSimRunning) (if (isHindi) "स्वतः अपडेट चालू" else "Auto-Tick ON") else "Auto-Tick OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimRunning) ForestGreenPrimary else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentCentre?.name ?: "Central Mandi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${currentCentre?.address} • 4 Active Weighing Desks",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. High-Visibility Currently Serving & Your Token Display
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CURRENTLY SERVING
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintLight)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (isHindi) "वर्तमान सेवारत" else "CURRENTLY SERVING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentlyServing?.tokenNumber ?: "CP-098",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Desk ${currentlyServing?.counterNumber ?: 1} (Weighbridge)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // YOUR TOKEN (CP-104)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldLight)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (isHindi) "आपका टोकन" else "YOUR TOKEN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = myBooking?.tokenNumber ?: "CP-104",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = HarvestGold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (myBooking?.status == BookingStatus.CALLED) (if (isHindi) "बुलाया गया!" else "CALLED!")
                                       else if (peopleAhead == 0) (if (isHindi) "आपकी बारी" else "Your Turn")
                                       else "$peopleAhead " + (if (isHindi) "किसान आगे" else "Ahead"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (myBooking?.status == BookingStatus.CALLED) Color(0xFFDC2626) else HarvestGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Metric Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "अनुमानित प्रतीक्षा: ~${maxOf(0, peopleAhead * 6)} मिनट"
                                       else "Estimated Waiting Time: ~${maxOf(0, peopleAhead * 6)} mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Demo step advance trigger
                        Surface(
                            shape = AppButtonShape,
                            color = ForestGreenPrimary,
                            modifier = Modifier
                                .clip(AppButtonShape)
                                .clickable { viewModel.triggerManualQueueAdvance() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isHindi) "कतार आगे बढ़ाएं (डेमो)" else "Step Next (Demo)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2b. Mock Push Notification Simulator Bar
        item {
            MockPushNotificationSimulatorBar(
                onTriggerPush = { viewModel.triggerMockPush(it) },
                isHindi = isHindi
            )
        }

        // 3. Live Token Stream Line
        item {
            Text(
                text = if (isHindi) "लाइव टोकन कतार सूची" else "Live Yard Queue Stream",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isHindi) "मंडी में उपस्थित सभी किसानों की पारदर्शी क्रम सूची:"
                       else "Transparent FIFO queue of all active farmer tokens:",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        itemsIndexed(activeQueue) { index, booking ->
            val isMyToken = booking.farmerId == farmerProfile.id
            val isBeingServed = booking.status == BookingStatus.IN_PROCESS
            val isCalled = booking.status == BookingStatus.CALLED

            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isMyToken -> GoldLight
                        isBeingServed -> MintLight
                        isCalled -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    if (isMyToken || isBeingServed || isCalled) 1.5.dp else 1.dp,
                    when {
                        isMyToken -> HarvestGold
                        isBeingServed -> ForestGreenPrimary
                        isCalled -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Position circle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isBeingServed -> ForestGreenPrimary
                                        isCalled -> Color(0xFFDC2626)
                                        isMyToken -> HarvestGold
                                        else -> Color(0xFFE2E8F0)
                                    }
                                )
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBeingServed || isCalled || isMyToken) Color.White else TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = booking.tokenNumber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        isMyToken -> HarvestGold
                                        isBeingServed -> ForestGreenPrimary
                                        isCalled -> Color(0xFFDC2626)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (isMyToken) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = HarvestGold
                                    ) {
                                        Text(
                                            text = if (isHindi) "आप (YOU)" else "YOU",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${booking.farmerName} • ${booking.cropType.englishName} (${booking.expectedQuantityQuintals} Qtl)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    StatusBadge(status = booking.status, isHindi = isHindi)
                }
            }
        }
    }
}
