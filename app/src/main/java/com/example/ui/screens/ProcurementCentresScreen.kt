package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun ProcurementCentresScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val centres by viewModel.centres.collectAsState()
    val searchQuery by viewModel.centreSearchQuery.collectAsState()
    val selectedCropFilter by viewModel.cropFilter.collectAsState()

    val filteredCentres = centres.filter { centre ->
        (searchQuery.isEmpty() || centre.name.contains(searchQuery, ignoreCase = true) || centre.district.contains(searchQuery, ignoreCase = true) || centre.state.contains(searchQuery, ignoreCase = true)) &&
        (selectedCropFilter == null || centre.cropsAccepted.contains(selectedCropFilter))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter Header
        item {
            Text(
                text = if (isHindi) "सरकारी खरीद केंद्र व मंडियां" else "Procurement Centres & Mandis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isHindi) "नजदीकी केंद्र चुनें, कतार देखें और तुरंत स्लॉट बुक करें:"
                       else "Find nearest government yards, view live capacity & book slots:",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setCentreSearchQuery(it) },
                placeholder = { Text(if (isHindi) "मंडी नाम या जिले से खोजें..." else "Search by Mandi, District or State...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreenPrimary) },
                shape = AppButtonShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Crop Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCropFilter == null,
                        onClick = { viewModel.setCropFilter(null) },
                        label = { Text(if (isHindi) "सभी फसलें" else "All Crops") },
                        shape = AppButtonShape
                    )
                }
                items(CropType.entries.toList()) { crop ->
                    FilterChip(
                        selected = selectedCropFilter == crop,
                        onClick = { viewModel.setCropFilter(if (selectedCropFilter == crop) null else crop) },
                        label = { Text(if (isHindi) crop.hindiName else crop.englishName.split(" ").first()) },
                        shape = AppButtonShape
                    )
                }
            }
        }

        // Centre Cards List
        items(filteredCentres) { centre ->
            Card(
                shape = AppCardShape,
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
                        Text(
                            text = if (isHindi) centre.hindiName else centre.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        CentreStatusBadge(status = centre.status, isHindi = isHindi)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = centre.address,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Strip (Queue Length, Avg Wait, Active Counters)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MintLight)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "वर्तमान कतार" else "Current Queue",
                                fontSize = 10.sp,
                                color = SoilBrown
                            )
                            Text(
                                text = "${centre.currentQueueLength} " + (if (isHindi) "किसान" else "Farmers"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }

                        Column {
                            Text(
                                text = if (isHindi) "अनुमानित प्रतीक्षा" else "Est. Wait",
                                fontSize = 10.sp,
                                color = SoilBrown
                            )
                            Text(
                                text = "~${centre.avgWaitMinutes} mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGold
                            )
                        }

                        Column {
                            Text(
                                text = if (isHindi) "सक्रिय कांटे" else "Active Desks",
                                fontSize = 10.sp,
                                color = SoilBrown
                            )
                            Text(
                                text = "${centre.activeCounters} Counters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KrishiPrimaryButton(
                            text = if (isHindi) "स्लॉट बुक करें" else "Book Slot Here",
                            icon = Icons.Default.CalendarMonth,
                            onClick = {
                                viewModel.setBookingCentre(centre)
                                viewModel.setBookingStep(2)
                                viewModel.navigateTo(AppDestination.BOOK_SLOT)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        KrishiSecondaryButton(
                            text = if (isHindi) "लाइव कतार" else "Live Queue",
                            icon = Icons.Default.OnlinePrediction,
                            onClick = {
                                viewModel.selectCentre(centre.id)
                                viewModel.navigateTo(AppDestination.LIVE_QUEUE)
                            },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }
    }
}
