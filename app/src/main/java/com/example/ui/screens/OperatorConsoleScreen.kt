package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun OperatorConsoleScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val centres by viewModel.centres.collectAsState()
    val selectedCentreId by viewModel.selectedCentreId.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    val currentCentre = centres.firstOrNull { it.id == selectedCentreId } ?: centres.firstOrNull()
    val centreBookings = bookings.filter { it.centreId == (currentCentre?.id ?: "") }
    val activeQueue = centreBookings.filter {
        it.status in listOf(BookingStatus.IN_PROCESS, BookingStatus.CALLED, BookingStatus.ARRIVED, BookingStatus.WAITING, BookingStatus.BOOKED)
    }.sortedBy { it.tokenNumber }

    val currentActiveBooking = activeQueue.firstOrNull { it.status == BookingStatus.IN_PROCESS || it.status == BookingStatus.CALLED }
        ?: activeQueue.firstOrNull()

    var showCompleteDialog by remember { mutableStateOf(false) }
    var completeWeightInput by remember { mutableStateOf("118.5") }
    var completeMoistureInput by remember { mutableStateOf("10.8") }
    var selectedCounter by remember { mutableStateOf(1) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Operator Desk Header
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
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "खरीद संचालक डेस्क (ऑपरेटर)" else "Procurement Operator Console",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintLight
                        ) {
                            Text(
                                text = "Desk #$selectedCounter",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentCentre?.name ?: "Central Mandi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time Token Calling & Weighbridge Verification System",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Active Serving Token Action Center
        item {
            if (currentActiveBooking != null) {
                val active = currentActiveBooking
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) "सक्रिय प्रक्रमण टोकन" else "ACTIVE SERVING TOKEN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown,
                                letterSpacing = 0.5.sp
                            )
                            StatusBadge(status = active.status, isHindi = isHindi)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintLight)
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = active.tokenNumber,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForestGreenPrimary
                                )
                                Text(
                                    text = "${active.farmerName} • ${active.farmerMobile}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isHindi) active.cropType.hindiName else active.cropType.englishName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Est: ${active.expectedQuantityQuintals} Qtl",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Operator Quick Control Buttons Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KrishiPrimaryButton(
                                text = if (isHindi) "टोकन बुलाएं" else "Call Token",
                                icon = Icons.Default.Campaign,
                                onClick = { viewModel.operatorCallToken(active.id, selectedCounter) },
                                containerColor = HarvestGold,
                                modifier = Modifier.weight(1f),
                                height = 40.dp
                            )

                            KrishiPrimaryButton(
                                text = if (isHindi) "उपस्थित दर्ज" else "Mark Arrived",
                                icon = Icons.Default.Done,
                                onClick = { viewModel.operatorMarkArrived(active.id) },
                                containerColor = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f),
                                height = 40.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KrishiPrimaryButton(
                                text = if (isHindi) "खरीद शुरू करें" else "Start Inspection",
                                icon = Icons.Default.Scale,
                                onClick = { viewModel.operatorStartProcurement(active.id) },
                                containerColor = Color(0xFF7C3AED),
                                modifier = Modifier.weight(1f),
                                height = 40.dp
                            )

                            KrishiPrimaryButton(
                                text = if (isHindi) "खरीद पूर्ण करें" else "Complete Weighment",
                                icon = Icons.Default.CheckCircle,
                                onClick = {
                                    completeWeightInput = "${active.expectedQuantityQuintals}"
                                    showCompleteDialog = true
                                },
                                containerColor = ForestGreenPrimary,
                                modifier = Modifier.weight(1f),
                                height = 40.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KrishiSecondaryButton(
                                text = if (isHindi) "अनुपस्थित" else "No Show",
                                icon = Icons.Default.PersonOff,
                                onClick = { viewModel.operatorMarkNoShow(active.id) },
                                borderColor = Color(0xFFDC2626),
                                contentColor = Color(0xFFDC2626),
                                modifier = Modifier.weight(1f),
                                height = 36.dp
                            )

                            KrishiSecondaryButton(
                                text = if (isHindi) "छोड़ें (Skip)" else "Skip Token",
                                icon = Icons.Default.SkipNext,
                                onClick = { viewModel.operatorSkipToken(active.id) },
                                borderColor = SoilBrown,
                                contentColor = SoilBrown,
                                modifier = Modifier.weight(1f),
                                height = 36.dp
                            )
                        }
                    }
                }
            }
        }

        // Live Mandi Queue List for Operator
        item {
            Text(
                text = if (isHindi) "आज की पूरी कतार (${activeQueue.size} किसान प्रतीक्षारत)" else "Today's Mandi Queue (${activeQueue.size} Farmers Waiting)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(activeQueue) { item ->
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.tokenNumber,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ForestGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.farmerName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${item.cropType.englishName} • ${item.expectedQuantityQuintals} Qtl • Slot: ${item.timeSlot}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    StatusBadge(status = item.status, isHindi = isHindi)
                }
            }
        }
    }

    // Modal Dialog: Complete Procurement & Generate Verified Receipt
    if (showCompleteDialog && currentActiveBooking != null) {
        val active = currentActiveBooking
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = if (isHindi) "खरीद पूर्ण करें व वजन पर्ची जारी करें" else "Complete Procurement & Generate Slip",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Token: ${active.tokenNumber} • Farmer: ${active.farmerName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )

                    OutlinedTextField(
                        value = completeWeightInput,
                        onValueChange = { completeWeightInput = it },
                        label = { Text(if (isHindi) "अंतिम वजन (क्विंटल)" else "Actual Weight (Quintals)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = AppButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = completeMoistureInput,
                        onValueChange = { completeMoistureInput = it },
                        label = { Text(if (isHindi) "नमी प्रतिशत (%)" else "Moisture Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = AppButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val w = completeWeightInput.toDoubleOrNull() ?: 0.0
                    val payout = w * active.cropType.mspPerQuintal
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DBT Payout: ₹${String.format("%,.2f", payout)} (@ ₹${String.format("%,.0f", active.cropType.mspPerQuintal)}/Qtl)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoilBrown,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                KrishiPrimaryButton(
                    text = if (isHindi) "रसीद जारी करें" else "Issue Slip & DBT",
                    onClick = {
                        val weight = completeWeightInput.toDoubleOrNull() ?: active.expectedQuantityQuintals
                        val moisture = completeMoistureInput.toDoubleOrNull() ?: 11.0
                        viewModel.operatorCompleteProcurement(active.id, weight, moisture)
                        showCompleteDialog = false
                    },
                    height = 40.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
