package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Booking
import com.example.ui.theme.*

@Composable
fun ReceiptViewerDialog(
    booking: Booking,
    isHindi: Boolean = false,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "GOVERNMENT OF INDIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Dept. of Consumer Affairs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = "Official Weighment & DBT Slip",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MintLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(10.dp))

                    ReceiptRow(label = "Receipt No:", value = booking.receiptId ?: "RCP-2026-883921")
                    ReceiptRow(label = "Token Ref:", value = booking.tokenNumber)
                    ReceiptRow(label = "Farmer Name:", value = booking.farmerName)
                    ReceiptRow(label = "Centre / Mandi:", value = booking.centreName)
                    ReceiptRow(label = "Procurement Date:", value = booking.bookingDate)
                    ReceiptRow(label = "Commodity:", value = booking.cropType.englishName)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    ReceiptRow(label = "Actual Net Weight:", value = "${booking.actualWeightQuintals ?: booking.expectedQuantityQuintals} Quintals", isBold = true)
                    ReceiptRow(label = "Moisture Reading:", value = "${booking.moisturePercentage ?: 10.5}% (Standard <= 12%)")
                    ReceiptRow(label = "Government MSP Rate:", value = "₹${String.format("%,.0f", booking.cropType.mspPerQuintal)} / Qtl")

                    Spacer(modifier = Modifier.height(10.dp))

                    // Final Amount Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "TOTAL DBT PAYMENT APPROVED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = "₹${String.format("%,.2f", booking.totalAmountPaid ?: (booking.expectedQuantityQuintals * booking.cropType.mspPerQuintal))}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = "Directly credited to Aadhaar-linked Bank Account",
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            KrishiPrimaryButton(
                text = if (isHindi) "बंद करें" else "Close",
                onClick = onDismiss,
                height = 38.dp
            )
        }
    )
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = TextPrimary
        )
    }
}
