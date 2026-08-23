package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.PushNotificationType
import com.example.ui.theme.*

data class QuickPushSimAction(
    val type: PushNotificationType,
    val label: String,
    val hindiLabel: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun MockPushNotificationSimulatorBar(
    onTriggerPush: (PushNotificationType) -> Unit,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val actions = listOf(
        QuickPushSimAction(
            type = PushNotificationType.NEXT_IN_LINE,
            label = "⚡ You're Next",
            hindiLabel = "⚡ आप अगले हैं",
            icon = Icons.Default.Bolt,
            color = HarvestGold
        ),
        QuickPushSimAction(
            type = PushNotificationType.TOKEN_CALLED,
            label = "🔔 Token Called",
            hindiLabel = "🔔 टोकन बुलाया गया",
            icon = Icons.Default.Campaign,
            color = Color(0xFFDC2626)
        ),
        QuickPushSimAction(
            type = PushNotificationType.QUEUE_UPDATE,
            label = "🚜 Queue Moving",
            hindiLabel = "🚜 कतार आगे बढ़ी",
            icon = Icons.Default.FastForward,
            color = ForestGreenPrimary
        ),
        QuickPushSimAction(
            type = PushNotificationType.PROCUREMENT_COMPLETED,
            label = "🌾 ₹ DBT Payout",
            hindiLabel = "🌾 ₹ डीबीटी भुगतान",
            icon = Icons.Default.CheckCircle,
            color = ForestGreenPrimary
        ),
        QuickPushSimAction(
            type = PushNotificationType.PROCUREMENT_STARTED,
            label = "⚖️ Moisture Test",
            hindiLabel = "⚖️ नमी परीक्षण",
            icon = Icons.Default.Scale,
            color = Color(0xFF0284C7)
        ),
        QuickPushSimAction(
            type = PushNotificationType.MANDI_ADVISORY,
            label = "⚠️ Mandi Advisory",
            hindiLabel = "⚠️ मंडी सलाह",
            icon = Icons.Default.Warning,
            color = SoilBrown
        )
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(MintLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHindi) "मॉक पुश सूचना सिम्युलेटर" else "Mock Push Notification Simulator",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isHindi) "रीयल-टाइम कतार स्थिति अलर्ट टेस्ट करें" else "Test real-time farmer queue heads-up alerts",
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (expanded) (if (isHindi) "छिपाएं" else "Collapse") else (if (isHindi) "विस्तार" else "Expand"),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Pills Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(actions) { action ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = action.color.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, action.color.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTriggerPush(action.type) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = action.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isHindi) action.hindiLabel else action.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = action.color
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi)
                            "• कतार में जब किसान की बारी 1 स्थान दूर होती है तो स्वतः 'YOU ARE NEXT' पुश अलर्ट भेजा जाता है।\n• संचालक द्वारा टोकन बुलाए जाने पर काउंटर नंबर सहित हाई-प्रायोरिटी अलर्ट व एसएमएस प्राप्त होता है।"
                        else
                            "• Automatic trigger fires 'YOU ARE NEXT' alert when 1 trolley is ahead (~4 min SLA).\n• High-priority 'TOKEN CALLED' alert rings with counter number upon weighbridge allocation.",
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
