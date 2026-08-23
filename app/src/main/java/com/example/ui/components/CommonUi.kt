package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.BookingStatus
import com.example.models.CentreStatus
import com.example.models.CropType
import com.example.models.Language
import com.example.models.UserRole
import com.example.ui.theme.*

// Standard rectangular button with rounded edges
val AppButtonShape = RoundedCornerShape(10.dp)
val AppCardShape = RoundedCornerShape(14.dp)
val AppBadgeShape = RoundedCornerShape(8.dp)

@Composable
fun KrishiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = ForestGreenPrimary,
    contentColor: Color = Color.White,
    height: Dp = 48.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBg by animateColorAsState(
        targetValue = when {
            !enabled -> containerColor.copy(alpha = 0.5f)
            isPressed -> containerColor.copy(alpha = 0.85f)
            isHovered -> ForestGreenLight
            else -> containerColor
        },
        animationSpec = tween(150),
        label = "btnBg"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedBg,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isHovered) 4.dp else 1.5.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        modifier = modifier
            .height(height)
            .defaultMinSize(minWidth = 110.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KrishiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color = ForestGreenPrimary,
    contentColor: Color = ForestGreenPrimary,
    height: Dp = 48.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = AppButtonShape,
        border = BorderStroke(1.5.dp, if (isHovered) ForestGreenLight else borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            containerColor = if (isHovered) MintLight.copy(alpha = 0.5f) else Color.Transparent
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .height(height)
            .defaultMinSize(minWidth = 100.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: BookingStatus,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        BookingStatus.BOOKED -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.ARRIVED -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.WAITING -> Triple(Color(0xFFFFEDD5), Color(0xFFC2410C), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.CALLED -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.IN_PROCESS -> Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.COMPLETED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.NO_SHOW -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), if (isHindi) status.hindiName else status.displayName)
        BookingStatus.CANCELLED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), if (isHindi) status.hindiName else status.displayName)
    }

    Surface(
        color = bgColor,
        shape = AppBadgeShape,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CentreStatusBadge(
    status: CentreStatus,
    isHindi: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        CentreStatus.OPEN -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.CheckCircle)
        CentreStatus.BUSY -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.AccessTime)
        CentreStatus.PAUSED -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.PauseCircle)
        CentreStatus.CLOSED -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = AppBadgeShape,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isHindi) status.hindiName else status.displayName,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = ForestGreenPrimary,
    iconBg: Color = MintLight,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        shape = AppCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isHovered) ForestGreenLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 3.dp else 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ForestGreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LanguageSwitcherPill(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(3.dp)
        ) {
            Language.entries.forEach { lang ->
                val isSelected = lang == currentLanguage
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                        .clickable { onLanguageChange(lang) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = lang.nativeName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSelectorDropdown(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = AppButtonShape,
            color = MintLight,
            border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = when (currentRole) {
                        UserRole.FARMER -> Icons.Default.Agriculture
                        UserRole.OPERATOR -> Icons.Default.Badge
                        UserRole.CENTRE_MANAGER -> Icons.Default.Storefront
                        UserRole.DISTRICT_ADMIN -> Icons.Default.AdminPanelSettings
                    },
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHindi) currentRole.hindiName else currentRole.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isHindi) role.hindiName else role.displayName,
                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                            color = if (role == currentRole) ForestGreenPrimary else TextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (role) {
                                UserRole.FARMER -> Icons.Default.Agriculture
                                UserRole.OPERATOR -> Icons.Default.Badge
                                UserRole.CENTRE_MANAGER -> Icons.Default.Storefront
                                UserRole.DISTRICT_ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = null,
                            tint = if (role == currentRole) ForestGreenPrimary else TextMuted
                        )
                    },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}
