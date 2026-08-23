package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.models.PushNotificationItem
import com.example.models.PushNotificationType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HeadsUpPushNotification(
    notification: PushNotificationItem?,
    isHindi: Boolean = false,
    onActionClick: (PushNotificationItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timerProgress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(notification?.id) {
        if (notification != null) {
            timerProgress = 1f
            val totalSteps = 80
            val stepDelay = 100L // 8.0 seconds total display time
            for (i in totalSteps downTo 0) {
                delay(stepDelay)
                timerProgress = i.toFloat() / totalSteps
            }
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        if (notification != null) {
            val isHighPriority = notification.type == PushNotificationType.NEXT_IN_LINE ||
                    notification.type == PushNotificationType.TOKEN_CALLED

            val borderColor = when (notification.type) {
                PushNotificationType.TOKEN_CALLED -> Color(0xFFDC2626)
                PushNotificationType.NEXT_IN_LINE -> HarvestGold
                PushNotificationType.PROCUREMENT_COMPLETED -> ForestGreenPrimary
                else -> ForestGreenPrimary.copy(alpha = 0.5f)
            }

            val headerBg = when (notification.type) {
                PushNotificationType.TOKEN_CALLED -> Color(0xFFFEE2E2)
                PushNotificationType.NEXT_IN_LINE -> GoldLight
                PushNotificationType.PROCUREMENT_COMPLETED -> MintLight
                else -> Color(0xFFF1F5F9)
            }

            // Outer floating card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = borderColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // System Notification Channel Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerBg)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ForestGreenPrimary)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_krishi_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KRISHIQUEUE • PUSH & SMS ALERT",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = SoilBrown,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Sound pulse indicator
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Alert Sound",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isHindi) "अभी" else "now",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Main Content Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Visual Icon Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (notification.type) {
                                        PushNotificationType.TOKEN_CALLED -> Color(0xFFFEE2E2)
                                        PushNotificationType.NEXT_IN_LINE -> GoldLight
                                        PushNotificationType.PROCUREMENT_COMPLETED -> MintLight
                                        else -> Color(0xFFE0F2FE)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (notification.type) {
                                        PushNotificationType.TOKEN_CALLED -> Color(0xFFDC2626)
                                        PushNotificationType.NEXT_IN_LINE -> HarvestGold
                                        PushNotificationType.PROCUREMENT_COMPLETED -> ForestGreenPrimary
                                        else -> StatusInfo
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = when (notification.type) {
                                    PushNotificationType.TOKEN_CALLED -> Icons.Default.Campaign
                                    PushNotificationType.NEXT_IN_LINE -> Icons.Default.Bolt
                                    PushNotificationType.PROCUREMENT_COMPLETED -> Icons.Default.CheckCircle
                                    PushNotificationType.PROCUREMENT_STARTED -> Icons.Default.Scale
                                    PushNotificationType.GATE_CHECKIN -> Icons.Default.QrCodeScanner
                                    else -> Icons.Default.NotificationsActive
                                },
                                contentDescription = null,
                                tint = when (notification.type) {
                                    PushNotificationType.TOKEN_CALLED -> Color(0xFFDC2626)
                                    PushNotificationType.NEXT_IN_LINE -> HarvestGold
                                    PushNotificationType.PROCUREMENT_COMPLETED -> ForestGreenPrimary
                                    else -> StatusInfo
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title, Body & Token Tag
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isHindi) notification.hindiTitle else notification.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MintLight,
                                    border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = notification.tokenNumber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = ForestGreenPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = if (isHindi) notification.hindiBody else notification.body,
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ SMS sent to +91 98765 43210",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )

                                Surface(
                                    shape = AppButtonShape,
                                    color = ForestGreenPrimary,
                                    modifier = Modifier.clickable { onActionClick(notification) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (isHindi) notification.hindiActionText else notification.actionText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Auto-Dismiss Timeout Line
                    LinearProgressIndicator(
                        progress = { timerProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = ForestGreenPrimary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}
