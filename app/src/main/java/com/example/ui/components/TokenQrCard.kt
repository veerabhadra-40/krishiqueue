package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Booking
import com.example.ui.theme.*

@Composable
fun TokenQrCard(
    booking: Booking,
    isHindi: Boolean = false,
    onTrackQueueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Official Emblem banner
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
                            .background(ForestGreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "उपभोक्ता मामले विभाग (DoCA)" else "Dept of Consumer Affairs (DoCA)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = if (isHindi) "स्मार्ट किसान खरीद पास" else "Smart Farmer Procurement Pass",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                StatusBadge(status = booking.status, isHindi = isHindi)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Token & QR Code Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MintLight)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = if (isHindi) "टोकन संख्या" else "DIGITAL TOKEN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoilBrown,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = booking.tokenNumber,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = ForestGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ref: ${booking.bookingReference}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                // Visual QR Code Graphic
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    QrCodeMatrixCanvas(ref = booking.bookingReference)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ticket Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) "खरीद केंद्र" else "Procurement Centre",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = booking.centreName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                    Text(
                        text = if (isHindi) "तारीख व समय" else "Date & Slot",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${booking.bookingDate}\n${booking.timeSlot}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crop & Qty details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) booking.cropType.hindiName else booking.cropType.englishName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${booking.expectedQuantityQuintals} Qtl (~₹${String.format("%,.0f", booking.expectedQuantityQuintals * booking.cropType.mspPerQuintal)})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dashed Divider
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Queue Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = if (isHindi) "वर्तमान कतार स्थिति" else "Live Queue Position",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (booking.queuePosition == 0) (if (isHindi) "अभी काउंटर पर" else "At Counter")
                               else "${booking.queuePosition} " + (if (isHindi) "किसान आगे" else "Ahead (~${booking.estimatedWaitMinutes}m wait)"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.queuePosition <= 2) HarvestGold else ForestGreenPrimary
                    )
                }

                KrishiPrimaryButton(
                    text = if (isHindi) "लाइव कतार देखें" else "Track Live Queue",
                    icon = Icons.Default.OnlinePrediction,
                    onClick = onTrackQueueClick,
                    height = 38.dp
                )
            }
        }
    }
}

@Composable
fun QrCodeMatrixCanvas(
    ref: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val matrixSize = 7
        val cellSize = w / (matrixSize + 4)

        // Draw Corner Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(Offset(0f, 0f), cellSize * 3)
        drawFinderPattern(Offset(w - cellSize * 3, 0f), cellSize * 3)
        drawFinderPattern(Offset(0f, h - cellSize * 3), cellSize * 3)

        // Pseudo-random deterministic QR payload matrix
        val hash = ref.hashCode()
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Avoid corners
                if ((r < 3 && c < 3) || (r < 3 && c >= matrixSize - 3) || (r >= matrixSize - 3 && c < 3)) {
                    continue
                }
                val isFilled = ((hash shr ((r * 3 + c) % 31)) and 1) == 1
                if (isFilled) {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset((c + 2) * cellSize, (r + 2) * cellSize),
                        size = Size(cellSize * 0.9f, cellSize * 0.9f),
                        cornerRadius = CornerRadius(1.5f, 1.5f)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(topLeft: Offset, sizePx: Float) {
    // Outer black box
    drawRoundRect(
        color = Color.Black,
        topLeft = topLeft,
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Inner white gap
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(topLeft.x + sizePx * 0.2f, topLeft.y + sizePx * 0.2f),
        size = Size(sizePx * 0.6f, sizePx * 0.6f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    // Center black dot
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(topLeft.x + sizePx * 0.35f, topLeft.y + sizePx * 0.35f),
        size = Size(sizePx * 0.3f, sizePx * 0.3f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}
