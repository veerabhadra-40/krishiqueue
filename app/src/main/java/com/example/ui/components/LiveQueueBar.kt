package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.BookingStatus
import com.example.ui.theme.*

@Composable
fun LiveQueueStageTracker(
    status: BookingStatus,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentStep = when (status) {
        BookingStatus.BOOKED -> 1
        BookingStatus.ARRIVED -> 2
        BookingStatus.WAITING -> 2
        BookingStatus.CALLED -> 3
        BookingStatus.IN_PROCESS -> 4
        BookingStatus.COMPLETED -> 5
        else -> 1
    }

    val stages = listOf(
        Triple(1, if (isHindi) "स्लॉट बुक" else "Slot Booked", Icons.Default.EventAvailable),
        Triple(2, if (isHindi) "उपस्थिति" else "Gate Arrival", Icons.Default.DirectionsCar),
        Triple(3, if (isHindi) "बुलाया गया" else "Token Called", Icons.Default.NotificationsActive),
        Triple(4, if (isHindi) "जांच व तौल" else "Inspection & Weight", Icons.Default.Scale),
        Triple(5, if (isHindi) "खरीद पूर्ण" else "DBT Payment", Icons.Default.CheckCircle)
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isHindi) "प्रोक्योरमेंट प्रगति चरण" else "Procurement Progress Timeline",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { index, (stepNumber, label, icon) ->
                    val isDone = currentStep > stepNumber
                    val isCurrent = currentStep == stepNumber
                    val isFuture = currentStep < stepNumber

                    val circleColor = when {
                        isDone -> ForestGreenPrimary
                        isCurrent -> HarvestGold
                        else -> Color(0xFFE2E8F0)
                    }

                    val iconTint = when {
                        isDone || isCurrent -> Color.White
                        else -> Color(0xFF94A3B8)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) HarvestGold else if (isDone) ForestGreenPrimary else TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp
                        )
                    }

                    if (index < stages.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(2.5.dp)
                                .background(if (currentStep > stepNumber) ForestGreenPrimary else Color(0xFFE2E8F0))
                                .offset(y = (-10).dp)
                        )
                    }
                }
            }
        }
    }
}
