package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ChatMessageEntity
import com.example.models.FarmerProfile
import com.example.models.Language
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishiHelpChatDialog(
    chatMessages: List<ChatMessageEntity>,
    farmerProfile: FarmerProfile,
    currentLanguage: Language,
    onSendMessage: (query: String, isVoice: Boolean, onBotResponse: (KrishiChatKnowledgeBase.BotResponse) -> Unit) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isHindi = currentLanguage == Language.HINDI

    val voiceHelper = remember { VoiceSpeechHelper(context) }
    val isSpeaking by voiceHelper.isSpeaking.collectAsState()
    val isListening by voiceHelper.isListening.collectAsState()
    val speechError by voiceHelper.speechError.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var autoReadOut by remember { mutableStateOf(false) }
    var currentlySpeakingMessageId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages change
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Clean up voice helper on dismiss
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.shutdown()
        }
    }

    val quickChips = if (isHindi) {
        listOf(
            "🌾 गेहूं का सरकारी एमएसपी?",
            "🌼 सरसों का भाव क्या है?",
            "📱 स्लॉट कैसे बुक करें?",
            "⚖️ फसल में नमी मानक?",
            "💳 डीबीटी भुगतान कब?",
            "📋 मंडी में जरूरी दस्तावेज?",
            "🚜 मेरी लाइव कतार स्थिति?"
        )
    } else {
        listOf(
            "🌾 Current Wheat MSP rate?",
            "🌼 Mustard MSP & moisture?",
            "📱 How to book a slot?",
            "⚖️ Permissible moisture limits?",
            "💳 When will I receive DBT?",
            "📋 Required mandi documents?",
            "🚜 Track my live queue?"
        )
    }

    Dialog(
        onDismissRequest = {
            voiceHelper.stopSpeaking()
            voiceHelper.stopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Voice Indicator & Preferred Language
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ForestGreenPrimary, ForestGreenDark)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isHindi) "कृषि सहायक (Kisan AI)" else "Krishi Sahayak (AI Bot)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = HarvestGold
                                    ) {
                                        Text(
                                            text = if (isHindi) "वॉयस सक्षम" else "VOICE AI",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isHindi) "एमएसपी, स्लॉट बुकिंग व कतार सहायता (Room DB)" else "24/7 Mandi Procurement Assistant (Room DB)",
                                    fontSize = 10.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Auto read-out toggle
                            IconButton(
                                onClick = {
                                    autoReadOut = !autoReadOut
                                    if (!autoReadOut && isSpeaking) {
                                        voiceHelper.stopSpeaking()
                                        currentlySpeakingMessageId = null
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (autoReadOut) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle auto readout",
                                    tint = if (autoReadOut) HarvestGold else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Clear chat
                            IconButton(onClick = onClearChat) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear Chat",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Close
                            IconButton(
                                onClick = {
                                    voiceHelper.stopSpeaking()
                                    voiceHelper.stopListening()
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Active Audio / Speaking Status Banner
                AnimatedVisibility(visible = isSpeaking || isListening) {
                    Surface(
                        color = if (isListening) Color(0xFFFEF3C7) else MintLight,
                        border = BorderStroke(1.dp, if (isListening) HarvestGold else ForestGreenPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulseAudioIndicator(isActive = true, color = if (isListening) HarvestGold else ForestGreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isListening)
                                        (if (isHindi) "🎙️ सुन रहे हैं... अपना सवाल बोलें" else "🎙️ Listening... Speak your question")
                                    else
                                        (if (isHindi) "🔊 उत्तर पढ़ा जा रहा है (Audio readout active)..." else "🔊 Reading response out loud..."),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isListening) Color(0xFF92400E) else ForestGreenPrimary
                                )
                            }

                            if (isSpeaking) {
                                TextButton(
                                    onClick = {
                                        voiceHelper.stopSpeaking()
                                        currentlySpeakingMessageId = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (isHindi) "रोकें (Stop)" else "Stop Audio", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Messages Feed
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(chatMessages) { message ->
                        ChatBubbleItem(
                            message = message,
                            isHindi = isHindi,
                            isCurrentlySpeaking = currentlySpeakingMessageId == message.id && isSpeaking,
                            onReadOut = { textToSpeak ->
                                if (currentlySpeakingMessageId == message.id && isSpeaking) {
                                    voiceHelper.stopSpeaking()
                                    currentlySpeakingMessageId = null
                                } else {
                                    currentlySpeakingMessageId = message.id
                                    voiceHelper.speak(textToSpeak, isHindi)
                                }
                            }
                        )
                    }
                }

                // Quick Prompt Suggestion Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = if (isHindi) "त्वरित सुझाव (Quick Questions):" else "Suggested Topics:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickChips) { chip ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MintLight,
                                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val cleanText = chip.substringAfter(" ").trim()
                                        inputText = ""
                                        onSendMessage(cleanText, false) { botResp ->
                                            if (autoReadOut) {
                                                voiceHelper.speak(
                                                    if (isHindi) botResp.hindiText.replace("*", "") else botResp.englishText.replace("*", ""),
                                                    isHindi
                                                )
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = chip,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Input Bar with Voice Recording (Microphone) & Send Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // Voice Recording Button
                    val micBackground = if (isListening) Color(0xFFDC2626) else MintLight
                    val micTint = if (isListening) Color.White else ForestGreenPrimary

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(micBackground)
                            .border(
                                1.5.dp,
                                if (isListening) Color(0xFFDC2626) else ForestGreenPrimary.copy(alpha = 0.4f),
                                CircleShape
                            )
                            .clickable {
                                if (isListening) {
                                    voiceHelper.stopListening()
                                } else {
                                    voiceHelper.stopSpeaking()
                                    voiceHelper.startListening(isHindi) { spokenText ->
                                        inputText = spokenText
                                        // Auto-submit recognized speech
                                        onSendMessage(spokenText, true) { botResp ->
                                            inputText = ""
                                            if (autoReadOut) {
                                                voiceHelper.speak(
                                                    if (isHindi) botResp.hindiText.replace("*", "") else botResp.englishText.replace("*", ""),
                                                    isHindi
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Record",
                            tint = micTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isHindi) "सवाल लिखें या 🎙️ बोलें..." else "Type question or tap 🎙️ to speak...",
                                fontSize = 12.5.sp,
                                color = TextMuted
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp, max = 90.dp),
                        trailingIcon = {
                            if (inputText.isNotBlank()) {
                                IconButton(onClick = { inputText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val query = inputText.trim()
                                inputText = ""
                                onSendMessage(query, false) { botResp ->
                                    if (autoReadOut) {
                                        voiceHelper.speak(
                                            if (isHindi) botResp.hindiText.replace("*", "") else botResp.englishText.replace("*", ""),
                                            isHindi
                                        )
                                    }
                                }
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) ForestGreenPrimary else Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageEntity,
    isHindi: Boolean,
    isCurrentlySpeaking: Boolean,
    onReadOut: (String) -> Unit
) {
    val isUser = message.sender == "USER"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    val timeString = timeFormat.format(Date(message.timestamp))

    val displayContent = if (isUser) {
        message.text
    } else {
        if (isHindi && message.hindiText.isNotBlank()) message.hindiText else message.text
    }

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MintLight)
                    .border(1.dp, ForestGreenPrimary.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) ForestGreenDark else MaterialTheme.colorScheme.outline
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (isUser && message.isVoice) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = HarvestGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isHindi) "वॉयस इनपुट (Voice)" else "Voice Question",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGold
                        )
                    }
                }

                Text(
                    text = displayContent,
                    fontSize = 12.5.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeString,
                        fontSize = 9.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else TextMuted
                    )

                    // Read Out Button for Bot Messages
                    if (!isUser) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrentlySpeaking) Color(0xFFFEF3C7) else MintLight,
                            border = BorderStroke(1.dp, if (isCurrentlySpeaking) HarvestGold else ForestGreenPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val cleanText = displayContent.replace("*", "").replace("#", "")
                                    onReadOut(cleanText)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlySpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Read out",
                                    tint = if (isCurrentlySpeaking) Color(0xFFDC2626) else ForestGreenPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isCurrentlySpeaking) (if (isHindi) "रोकें" else "Stop") else (if (isHindi) "सुनें" else "Read Out"),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentlySpeaking) Color(0xFFDC2626) else ForestGreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MintLight)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PulseAudioIndicator(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isActive) bar1 else 0.4f)
                .background(color, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isActive) bar2 else 0.7f)
                .background(color, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(if (isActive) bar3 else 0.5f)
                .background(color, RoundedCornerShape(2.dp))
        )
    }
}
