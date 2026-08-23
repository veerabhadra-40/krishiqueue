package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Booking
import com.example.models.BookingStatus
import com.example.models.Language
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun HistoryAndReceiptsScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val bookings by viewModel.bookings.collectAsState()
    val farmerProfile by viewModel.farmerProfile.collectAsState()

    val myHistory = bookings.filter {
        it.farmerId == farmerProfile.id && it.status == BookingStatus.COMPLETED
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
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
                        text = if (isHindi) "खरीद इतिहास व डिजिटल रसीदें" else "Procurement History & Receipts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "पारदर्शी वजन, नमी रिपोर्ट और प्रत्यक्ष बैंक हस्तांतरण (DBT) भुगतान पर्चियां:"
                               else "Verified weighment slips, moisture readings & Direct Benefit Transfer records:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalEarned = myHistory.sumOf { it.totalAmountPaid ?: 0.0 }
                    val totalWeight = myHistory.sumOf { it.actualWeightQuintals ?: 0.0 }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = if (isHindi) "कुल डीबीटी भुगतान" else "Total DBT Payout",
                            value = "₹${String.format("%,.0f", totalEarned)}",
                            subtitle = if (isHindi) "100% बैंक में जमा" else "Direct to Bank Account",
                            icon = Icons.Default.AccountBalance,
                            iconTint = ForestGreenPrimary,
                            iconBg = MintLight,
                            modifier = Modifier.weight(1f)
                        )

                        MetricStatCard(
                            title = if (isHindi) "कुल खरीद मात्रा" else "Total Procured",
                            value = "${String.format("%.1f", totalWeight)} Qtl",
                            subtitle = if (isHindi) "सरकारी एमएसपी दर" else "At Official MSP",
                            icon = Icons.Default.Scale,
                            iconTint = HarvestGold,
                            iconBg = GoldLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // List of Completed Slips
        items(myHistory) { item ->
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Token ${item.tokenNumber}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ForestGreenPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = if (isHindi) "भुगतान पूर्ण (DBT)" else "DBT Transferred",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.centreName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Date: ${item.bookingDate} • Receipt: ${item.receiptId ?: "RCP-2026"}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slip Breakdown Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MintLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) item.cropType.hindiName else item.cropType.englishName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Weight: ${item.actualWeightQuintals ?: item.expectedQuantityQuintals} Qtl • Moisture: ${item.moisturePercentage ?: 10.5}%",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${String.format("%,.2f", item.totalAmountPaid ?: 0.0)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestGreenPrimary
                                )
                                Text(
                                    text = "@ ₹${String.format("%,.0f", item.cropType.mspPerQuintal)}/Qtl",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    KrishiSecondaryButton(
                        text = if (isHindi) "डिजिटल रसीद देखें / डाउनलोड" else "View & Download Verified Slip",
                        icon = Icons.Default.Download,
                        onClick = { viewModel.showReceipt(item) },
                        modifier = Modifier.fillMaxWidth(),
                        height = 40.dp
                    )
                }
            }
        }
    }
}
