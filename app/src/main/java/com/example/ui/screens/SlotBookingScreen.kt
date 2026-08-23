package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun SlotBookingScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI

    val currentStep by viewModel.bookingStep.collectAsState()
    val centres by viewModel.centres.collectAsState()
    val selectedCentre by viewModel.selectedBookingCentre.collectAsState()
    val selectedDate by viewModel.selectedBookingDate.collectAsState()
    val selectedSlot by viewModel.selectedTimeSlot.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val quantity by viewModel.enteredQuantity.collectAsState()
    val lastBooking by viewModel.lastGeneratedBooking.collectAsState()

    val availableDates = listOf("24 August 2026", "25 August 2026", "26 August 2026", "27 August 2026")
    val timeSlots = remember(selectedCentre, selectedDate) {
        viewModel.getSlotsForDate(selectedDate)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step Indicator Progress Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    val stepTitles = listOf(
                        if (isHindi) "1. केंद्र" else "1. Centre",
                        if (isHindi) "2. समय" else "2. Slot",
                        if (isHindi) "3. फसल" else "3. Crop",
                        if (isHindi) "4. पास" else "4. Pass"
                    )

                    stepTitles.forEachIndexed { index, title ->
                        val stepNum = index + 1
                        val isActive = currentStep == stepNum
                        val isDone = currentStep > stepNum

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> ForestGreenPrimary
                                            isActive -> HarvestGold
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    )
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$stepNum",
                                        color = if (isActive) Color.White else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) HarvestGold else if (isDone) ForestGreenPrimary else TextMuted
                            )
                        }

                        if (index < stepTitles.size - 1) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // STEP 1: Select Procurement Centre
        if (currentStep == 1) {
            item {
                Text(
                    text = if (isHindi) "चरण 1: खरीद केंद्र (मंडी) चुनें" else "Step 1: Select Procurement Centre",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "अपनी नजदीकी सरकारी खरीद मंडी का चयन करें:" else "Choose your nearest government procurement yard:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            items(centres) { centre ->
                val isSelected = selectedCentre?.id == centre.id
                Card(
                    shape = AppCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MintLight else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppCardShape)
                        .clickable { viewModel.setBookingCentre(centre) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isHindi) centre.hindiName else centre.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            CentreStatusBadge(status = centre.status, isHindi = isHindi)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${centre.address}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = HarvestGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isHindi) "औसत प्रतीक्षा: ~${centre.avgWaitMinutes} मिनट" else "Avg Wait: ~${centre.avgWaitMinutes} mins",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = if (isHindi) "सक्रिय काउंटर: ${centre.activeCounters}" else "Active Desks: ${centre.activeCounters}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                KrishiPrimaryButton(
                    text = if (isHindi) "अगला: स्लॉट समय चुनें" else "Next: Select Date & Slot",
                    icon = Icons.Default.ArrowForward,
                    onClick = { viewModel.setBookingStep(2) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // STEP 2: Select Date & Time Slot
        if (currentStep == 2) {
            item {
                Text(
                    text = if (isHindi) "चरण 2: तारीख और समय स्लॉट चुनें" else "Step 2: Select Date & Time Slot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${selectedCentre?.name}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Date Picker Row
            item {
                Text(
                    text = if (isHindi) "खरीद की तारीख" else "Procurement Date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableDates.forEach { dateStr ->
                        val isDateSelected = selectedDate == dateStr
                        Surface(
                            shape = AppButtonShape,
                            color = if (isDateSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isDateSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clip(AppButtonShape)
                                .clickable { viewModel.setBookingDate(dateStr) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = dateStr.split(" ").firstOrNull() ?: "",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDateSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Aug",
                                    fontSize = 10.sp,
                                    color = if (isDateSelected) Color.White.copy(alpha = 0.8f) else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Time Slots Grid
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isHindi) "उपलब्ध 1 घंटे के समय स्लॉट" else "Available Hourly Slots",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                timeSlots.chunked(2).forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            val isSlotSelected = selectedSlot?.id == slot.id
                            val isFull = !slot.isAvailable

                            Card(
                                shape = AppCardShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isSlotSelected -> MintLight
                                        isFull -> Color(0xFFF3F4F6)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    if (isSlotSelected) 1.5.dp else 1.dp,
                                    when {
                                        isSlotSelected -> ForestGreenPrimary
                                        isFull -> Color(0xFFE5E7EB)
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(AppCardShape)
                                    .clickable(enabled = !isFull) { viewModel.setTimeSlot(slot) }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = slot.timeRange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFull) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isFull) (if (isHindi) "भर गया" else "Full")
                                                   else "${slot.maxCapacitySlots - slot.bookedSlots} " + (if (isHindi) "स्थान शेष" else "left"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isFull) Color(0xFFDC2626) else ForestGreenPrimary
                                        )
                                        if (isSlotSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = ForestGreenPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KrishiSecondaryButton(
                        text = if (isHindi) "पीछे" else "Back",
                        onClick = { viewModel.setBookingStep(1) },
                        modifier = Modifier.weight(0.4f)
                    )
                    KrishiPrimaryButton(
                        text = if (isHindi) "अगला: फसल विवरण" else "Next: Crop Details",
                        icon = Icons.Default.ArrowForward,
                        onClick = { viewModel.setBookingStep(3) },
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }

        // STEP 3: Crop & Quantity Selection
        if (currentStep == 3) {
            item {
                Text(
                    text = if (isHindi) "चरण 3: फसल प्रकार व अनुमानित मात्रा" else "Step 3: Crop Type & Quantity",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "सरकारी समर्थन मूल्य (MSP) और अनुमानित भुगतान की गणना करें:"
                           else "Check government Minimum Support Price (MSP) and expected direct payout:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Crop selector list
            items(CropType.entries.toList()) { crop ->
                val isCropSelected = selectedCrop == crop
                Card(
                    shape = AppCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCropSelected) MintLight else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (isCropSelected) 1.5.dp else 1.dp,
                        if (isCropSelected) ForestGreenPrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppCardShape)
                        .clickable { viewModel.setCrop(crop) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isCropSelected,
                                onClick = { viewModel.setCrop(crop) },
                                colors = RadioButtonDefaults.colors(selectedColor = ForestGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isHindi) crop.hindiName else crop.englishName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "MSP: ₹${String.format("%,.0f", crop.mspPerQuintal)} / Quintal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HarvestGold
                                )
                            }
                        }
                    }
                }
            }

            // Quantity Input Box
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isHindi) "अनुमानित मात्रा (क्विंटल में)" else "Expected Quantity (in Quintals)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { viewModel.setQuantity(it) },
                    placeholder = { Text("e.g. 120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = AppButtonShape,
                    trailingIcon = { Text("Qtl", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Instant estimated calculation card
                val qtyNum = quantity.toDoubleOrNull() ?: 0.0
                val totalMspPayout = qtyNum * selectedCrop.mspPerQuintal

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = AppCardShape,
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "अनुमानित कुल डीबीटी राशि" else "Estimated Direct Bank Transfer (DBT)",
                                fontSize = 11.sp,
                                color = SoilBrown
                            )
                            Text(
                                text = "₹${String.format("%,.2f", totalMspPayout)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = HarvestGold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = HarvestGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KrishiSecondaryButton(
                        text = if (isHindi) "पीछे" else "Back",
                        onClick = { viewModel.setBookingStep(2) },
                        modifier = Modifier.weight(0.4f)
                    )
                    KrishiPrimaryButton(
                        text = if (isHindi) "स्लॉट बुक करें व टोकन लें" else "Confirm & Generate Token",
                        icon = Icons.Default.CheckCircle,
                        onClick = { viewModel.confirmBooking() },
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }

        // STEP 4: Generated Digital Token Pass (Success!)
        if (currentStep == 4 && lastBooking != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHindi) "स्लॉट सफलतापूर्वक बुक हो गया!" else "Slot Successfully Confirmed!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = if (isHindi) "आपका डिजिटल खरीद टोकन तैयार है।" else "Your digital queue token is active and verifiable.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The Digital Token Card Pass
                TokenQrCard(
                    booking = lastBooking!!,
                    isHindi = isHindi,
                    onTrackQueueClick = { viewModel.navigateTo(AppDestination.LIVE_QUEUE) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KrishiSecondaryButton(
                        text = if (isHindi) "डैशबोर्ड पर जाएं" else "Farmer Home",
                        icon = Icons.Default.Home,
                        onClick = { viewModel.navigateTo(AppDestination.FARMER_DASHBOARD) },
                        modifier = Modifier.weight(1f)
                    )
                    KrishiPrimaryButton(
                        text = if (isHindi) "लाइव कतार देखें" else "Track Live Queue",
                        icon = Icons.Default.OnlinePrediction,
                        onClick = { viewModel.navigateTo(AppDestination.LIVE_QUEUE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
