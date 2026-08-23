package com.example.ui.components

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
import com.example.models.NotificationItem
import com.example.models.PushNotificationItem
import com.example.models.PushNotificationType
import com.example.ui.theme.*

@Composable
fun NotificationsSheetDialog(
    notifications: List<NotificationItem>,
    pushHistory: List<PushNotificationItem> = emptyList(),
    onTriggerPush: (PushNotificationType) -> Unit = {},
    isHindi: Boolean = false,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isHindi) "स्मार्ट कतार सूचनाएं" else "Live Queue Notifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintLight
                    ) {
                        Text(
                            text = "${notifications.size + pushHistory.size} Alerts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = ForestGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isHindi) "इन-ऐप (${notifications.size})" else "In-App (${notifications.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = if (isHindi) "पुश व एसएमएस" else "Push & SMS",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = if (isHindi) "सिम्युलेटर" else "Test Simulator",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        },
        text = {
            when (selectedTab) {
                0 -> {
                    // Standard In-App Notification Feed
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                shape = AppCardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (notif.type) {
                                                    "CALL" -> Color(0xFFFEE2E2)
                                                    "ALERT" -> Color(0xFFFEF3C7)
                                                    else -> MintLight
                                                }
                                            )
                                    ) {
                                        Icon(
                                            imageVector = when (notif.type) {
                                                "CALL" -> Icons.Default.Campaign
                                                "ALERT" -> Icons.Default.NotificationsActive
                                                else -> Icons.Default.CheckCircle
                                            },
                                            contentDescription = null,
                                            tint = when (notif.type) {
                                                "CALL" -> Color(0xFFDC2626)
                                                "ALERT" -> HarvestGold
                                                else -> ForestGreenPrimary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (isHindi) notif.hindiTitle else notif.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = notif.timestamp,
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isHindi) notif.hindiMessage else notif.message,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Push Notification History / Intercepted Payloads
                    if (pushHistory.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHindi) "कोई हालिया पुश अलर्ट नहीं" else "No recent push alerts received",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onTriggerPush(PushNotificationType.NEXT_IN_LINE) },
                                shape = AppButtonShape
                            ) {
                                Text(if (isHindi) "टेस्ट अलर्ट भेजें" else "Trigger Test Push Alert", fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pushHistory) { push ->
                                Card(
                                    shape = AppCardShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(
                                        1.dp,
                                        if (push.type == PushNotificationType.TOKEN_CALLED) Color(0xFFDC2626)
                                        else if (push.type == PushNotificationType.NEXT_IN_LINE) HarvestGold
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Sms,
                                                    contentDescription = null,
                                                    tint = ForestGreenPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = push.deliveryChannel,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoilBrown
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MintLight
                                            ) {
                                                Text(
                                                    text = push.tokenNumber,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = ForestGreenPrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = if (isHindi) push.hindiTitle else push.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (isHindi) push.hindiBody else push.body,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Quick Simulator Trigger Board
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isHindi) "पुश सूचना प्रकार चुनें:" else "Select notification scenario to simulate:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        MockPushNotificationSimulatorBar(
                            onTriggerPush = {
                                onTriggerPush(it)
                                onDismiss()
                            },
                            isHindi = isHindi
                        )
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
