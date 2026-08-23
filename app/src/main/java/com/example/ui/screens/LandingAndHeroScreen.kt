package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.models.Language
import com.example.models.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.KrishiQueueViewModel

@Composable
fun LandingAndHeroScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val isSimRunning by viewModel.isSimulationRunning.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Hero Header Banner
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Government DoCA pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MintLight,
                            border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "उपभोक्ता मामले विभाग (DoCA)" else "Dept. of Consumer Affairs (DoCA)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Simulation live badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSimRunning) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSimRunning) ForestGreenPrimary else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isSimRunning) (if (isHindi) "लाइव इंजन" else "Live Engine") else "Paused",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimRunning) ForestGreenPrimary else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Headlines
                    Text(
                        text = if (isHindi) "अपना स्लॉट जानें।\nअपनी कतार जानें।\nखरीद स्थिति जानें।"
                               else "Know your slot.\nKnow your queue.\nKnow your procurement status.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = ForestGreenPrimary,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "कृषि-क्यू किसानों को स्लॉट बुक करने और वास्तविक समय में अपनी कतार की स्थिति ट्रैक करने में मदद करता है — जिससे अनावश्यक प्रतीक्षा और अनिश्चितता समाप्त होती है।"
                               else "KrishiQueue helps farmers register, book procurement slots and track their position in real time — reducing unnecessary waiting and uncertainty.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary CTAs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KrishiPrimaryButton(
                            text = if (isHindi) "स्लॉट बुक करें" else "Book Slot",
                            icon = Icons.Default.CalendarToday,
                            onClick = { viewModel.navigateTo(AppDestination.BOOK_SLOT) },
                            modifier = Modifier.weight(1f)
                        )

                        KrishiSecondaryButton(
                            text = if (isHindi) "टोकन ट्रैक करें" else "Track Token",
                            icon = Icons.Default.OnlinePrediction,
                            onClick = { viewModel.navigateTo(AppDestination.LIVE_QUEUE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Interactive 3D / Isometric Canvas Hero
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (isHindi) "3D डिजिटल खरीद यार्ड पूर्वावलोकन" else "3D Digital Procurement Yard Preview",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                IsometricScene3D(
                    currentlyServing = "CP-098",
                    activeTokens = listOf("CP-098", "CP-099", "CP-100", "CP-104"),
                    onNodeClicked = { viewModel.navigateTo(AppDestination.LIVE_QUEUE) }
                )
            }
        }

        // 3. Impact & National Metric Tickers
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (isHindi) "राष्ट्रीय खरीद आंकड़े (DoCA)" else "National Procurement Metrics (DoCA)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = if (isHindi) "औसत प्रतीक्षा कमी" else "Wait Reduced",
                        value = "68%",
                        subtitle = if (isHindi) "12 घंटे से 45 मिनट" else "From 12h to 45m",
                        icon = Icons.Default.TrendingDown,
                        iconTint = ForestGreenPrimary,
                        iconBg = MintLight,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = if (isHindi) "आज की खरीद" else "Procured Today",
                        value = "28,450 Qtl",
                        subtitle = if (isHindi) "100% डीबीटी भुगतान" else "100% Direct DBT",
                        icon = Icons.Default.Agriculture,
                        iconTint = HarvestGold,
                        iconBg = GoldLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = if (isHindi) "सक्रिय खरीद केंद्र" else "Active Mandis",
                        value = "480+",
                        subtitle = if (isHindi) "रीयल-टाइम कतार" else "Real-time Live",
                        icon = Icons.Default.Storefront,
                        iconTint = StatusInfo,
                        iconBg = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = if (isHindi) "पंजीकृत किसान" else "Registered",
                        value = "1.2 Lakh",
                        subtitle = if (isHindi) "पारदर्शी प्रक्रिया" else "Zero Intermediaries",
                        icon = Icons.Default.People,
                        iconTint = StatusPurple,
                        iconBg = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. "How It Works" 6-Step Visual Workflow
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (isHindi) "यह कैसे काम करता है?" else "How KrishiQueue Works",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                val steps = listOf(
                    Triple("1", if (isHindi) "पंजीकरण व सत्यापन" else "1. Register & Verify", if (isHindi) "मोबाइल ओटीपी और भूमि विवरण के साथ 1 मिनट में प्रोफाइल बनाएं" else "Quick 1-min sign up with Mobile OTP & land records"),
                    Triple("2", if (isHindi) "मंडी व स्लॉट चुनें" else "2. Select Mandi & Slot", if (isHindi) "अपनी नजदीकी मंडी और सुविधाजनक 1 घंटे का समय चुनें" else "Choose nearest centre and convenient hourly slot"),
                    Triple("3", if (isHindi) "डिजिटल टोकन प्राप्त करें" else "3. Get Digital Token QR", if (isHindi) "सुरक्षित टोकन नंबर और क्यूआर कोड तुरंत प्राप्त करें" else "Instant verified token pass with dynamic reference"),
                    Triple("4", if (isHindi) "लाइव कतार ट्रैक करें" else "4. Monitor Live Queue", if (isHindi) "घर बैठे देखें कि अभी कौन सा नंबर चल रहा है" else "Watch live counter movements from home on mobile"),
                    Triple("5", if (isHindi) "समय पर पहुंचे" else "5. Arrive On Turn Alert", if (isHindi) "जब आपका नंबर पास हो, तभी मंडी पहुंचे और कतार से बचें" else "Get smart push & SMS alert when you are 3 positions away"),
                    Triple("6", if (isHindi) "खरीद व त्वरित डीबीटी" else "6. Instant DBT Payout", if (isHindi) "पारदर्शी वजन, नमी जांच और सीधे बैंक खाते में भुगतान" else "Transparent weighing & instant bank account credit")
                )

                steps.forEach { (number, title, desc) ->
                    Card(
                        shape = AppCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreenPrimary)
                            ) {
                                Text(
                                    text = number,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
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

        // 5. Quick Role Switcher Showcase Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight),
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "रोल-आधारित डेमो कंसोल" else "Role-Based Demonstration Modes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) "विभिन्न हितधारकों के दृष्टिकोण का अनुभव करने के लिए किसी भी रोल पर स्विच करें:"
                               else "Switch between portals to experience the end-to-end ecosystem:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KrishiPrimaryButton(
                            text = if (isHindi) "किसान" else "Farmer",
                            icon = Icons.Default.Agriculture,
                            onClick = { viewModel.setRole(UserRole.FARMER) },
                            height = 36.dp,
                            modifier = Modifier.weight(1f)
                        )
                        KrishiPrimaryButton(
                            text = if (isHindi) "संचालक" else "Operator",
                            icon = Icons.Default.Badge,
                            onClick = { viewModel.setRole(UserRole.OPERATOR) },
                            containerColor = SoilBrown,
                            height = 36.dp,
                            modifier = Modifier.weight(1f)
                        )
                        KrishiPrimaryButton(
                            text = if (isHindi) "प्रशासक" else "Admin",
                            icon = Icons.Default.AdminPanelSettings,
                            onClick = { viewModel.setRole(UserRole.CENTRE_MANAGER) },
                            containerColor = HarvestGold,
                            height = 36.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
