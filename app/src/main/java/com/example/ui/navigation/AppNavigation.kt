package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.models.Language
import com.example.models.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.KrishiQueueViewModel

data class NavTabItem(
    val destination: AppDestination,
    val title: String,
    val hindiTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isHindi = currentLang == Language.HINDI
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val currentPushNotification by viewModel.currentPushNotification.collectAsState()
    val pushHistory by viewModel.pushHistory.collectAsState()

    // Dialog states
    val tokenQrBooking by viewModel.showTokenQrDialog.collectAsState()
    val receiptBooking by viewModel.showReceiptDialog.collectAsState()
    val showNotifs by viewModel.showNotificationsSheet.collectAsState()

    // Dynamic Navigation Tabs based on Role
    val tabs = remember(currentRole, isHindi) {
        when (currentRole) {
            UserRole.FARMER -> listOf(
                NavTabItem(AppDestination.LANDING_HERO, "Home", "होम", Icons.Filled.Home, Icons.Outlined.Home),
                NavTabItem(AppDestination.FARMER_DASHBOARD, "Dashboard", "डैशबोर्ड", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                NavTabItem(AppDestination.BOOK_SLOT, "Book Slot", "स्लॉट बुक", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                NavTabItem(AppDestination.LIVE_QUEUE, "Live Queue", "लाइव कतार", Icons.Filled.OnlinePrediction, Icons.Outlined.OnlinePrediction),
                NavTabItem(AppDestination.CENTRES_MAP, "Mandis", "मंडियां", Icons.Filled.Storefront, Icons.Outlined.Storefront),
                NavTabItem(AppDestination.HISTORY_RECEIPTS, "Receipts", "रसीदें", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
                NavTabItem(AppDestination.FARMER_PROFILE, "Profile", "प्रोफाइल", Icons.Filled.Person, Icons.Outlined.Person)
            )
            UserRole.OPERATOR -> listOf(
                NavTabItem(AppDestination.OPERATOR_DESK, "Operator Desk", "संचालक डेस्क", Icons.Filled.Badge, Icons.Outlined.Badge),
                NavTabItem(AppDestination.LIVE_QUEUE, "Live Queue", "लाइव कतार", Icons.Filled.OnlinePrediction, Icons.Outlined.OnlinePrediction),
                NavTabItem(AppDestination.CENTRES_MAP, "Centres", "केंद्र", Icons.Filled.Storefront, Icons.Outlined.Storefront),
                NavTabItem(AppDestination.FARMER_PROFILE, "Settings", "सेटिंग्स", Icons.Filled.Settings, Icons.Outlined.Settings)
            )
            UserRole.CENTRE_MANAGER, UserRole.DISTRICT_ADMIN -> listOf(
                NavTabItem(AppDestination.ADMIN_ANALYTICS, "Analytics", "विश्लेषण", Icons.Filled.Assessment, Icons.Outlined.Assessment),
                NavTabItem(AppDestination.LIVE_QUEUE, "Live Queue", "लाइव कतार", Icons.Filled.OnlinePrediction, Icons.Outlined.OnlinePrediction),
                NavTabItem(AppDestination.CENTRES_MAP, "Centres", "केंद्र", Icons.Filled.Storefront, Icons.Outlined.Storefront),
                NavTabItem(AppDestination.FARMER_PROFILE, "Settings", "सेटिंग्स", Icons.Filled.Settings, Icons.Outlined.Settings)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintLight)
                                .border(1.dp, ForestGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_krishi_logo),
                                contentDescription = "KrishiQueue Logo",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KrishiQueue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = ForestGreenPrimary
                            )
                            Text(
                                text = if (isHindi) "उपभोक्ता मामले विभाग (DoCA)" else "DoCA • Smart Procurement",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoilBrown
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher (EN / HI)
                    LanguageSwitcherPill(
                        currentLanguage = currentLang,
                        onLanguageChange = { viewModel.setLanguage(it) }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Krishi Sahayak Voice Chatbot Top Action Button
                    IconButton(
                        onClick = { viewModel.toggleHelpChat(true) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MintLight)
                                .border(1.dp, ForestGreenPrimary.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Help Chatbot",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Notification Bell with unread badge
                    IconButton(
                        onClick = { viewModel.toggleNotifications(true) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = HarvestGold,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Role Switcher Dropdown
                    RoleSelectorDropdown(
                        currentRole = currentRole,
                        onRoleSelected = { viewModel.setRole(it) },
                        isHindi = isHindi
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = ForestGreenPrimary
                ),
                modifier = Modifier.shadow(1.5.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ForestGreenPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentDest == tab.destination
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(tab.destination) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isHindi) tab.hindiTitle else tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreenPrimary,
                            selectedTextColor = ForestGreenPrimary,
                            indicatorColor = MintLight,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        interactionSource = interactionSource
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 640.dp)
        ) {
            AnimatedContent(
                targetState = currentDest,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "navScreenAnim"
            ) { dest ->
                when (dest) {
                    AppDestination.LANDING_HERO -> LandingAndHeroScreen(viewModel = viewModel)
                    AppDestination.FARMER_DASHBOARD -> FarmerDashboardScreen(viewModel = viewModel)
                    AppDestination.BOOK_SLOT -> SlotBookingScreen(viewModel = viewModel)
                    AppDestination.LIVE_QUEUE -> LiveQueueScreen(viewModel = viewModel)
                    AppDestination.CENTRES_MAP -> ProcurementCentresScreen(viewModel = viewModel)
                    AppDestination.HISTORY_RECEIPTS -> HistoryAndReceiptsScreen(viewModel = viewModel)
                    AppDestination.OPERATOR_DESK -> OperatorConsoleScreen(viewModel = viewModel)
                    AppDestination.ADMIN_ANALYTICS -> AdminAnalyticsScreen(viewModel = viewModel)
                    AppDestination.FARMER_PROFILE -> FarmerProfileScreen(viewModel = viewModel)
                }
            }

            // Top Floating Heads-Up Push Notification System
            HeadsUpPushNotification(
                notification = currentPushNotification,
                isHindi = isHindi,
                onActionClick = { viewModel.handlePushNotificationAction(it) },
                onDismiss = { viewModel.dismissPushNotification() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(99f)
            )
        }
    }

    // Modal Overlays
    if (tokenQrBooking != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showTokenQr(null) },
            title = null,
            text = {
                TokenQrCard(
                    booking = tokenQrBooking!!,
                    isHindi = isHindi,
                    onTrackQueueClick = {
                        viewModel.showTokenQr(null)
                        viewModel.navigateTo(AppDestination.LIVE_QUEUE)
                    }
                )
            },
            confirmButton = {
                KrishiPrimaryButton(
                    text = if (isHindi) "बंद करें" else "Close",
                    onClick = { viewModel.showTokenQr(null) },
                    height = 38.dp
                )
            }
        )
    }

    if (receiptBooking != null) {
        ReceiptViewerDialog(
            booking = receiptBooking!!,
            isHindi = isHindi,
            onDismiss = { viewModel.showReceipt(null) }
        )
    }

    if (showNotifs) {
        NotificationsSheetDialog(
            notifications = notifications,
            pushHistory = pushHistory,
            onTriggerPush = { viewModel.triggerMockPush(it) },
            isHindi = isHindi,
            onDismiss = { viewModel.toggleNotifications(false) }
        )
    }

    // Krishi Sahayak Help Chatbot Dialog (Voice & Read-Out Enabled, Room DB Persisted)
    val showChat by viewModel.showHelpChatDialog.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val farmerProfile by viewModel.farmerProfile.collectAsState()

    if (showChat) {
        KrishiHelpChatDialog(
            chatMessages = chatMessages,
            farmerProfile = farmerProfile,
            currentLanguage = currentLang,
            onSendMessage = { query, isVoice, onBotResp ->
                viewModel.sendChatMessage(query, isVoice, onBotResp)
            },
            onClearChat = { viewModel.clearChatMessages() },
            onDismiss = { viewModel.toggleHelpChat(false) }
        )
    }
}

