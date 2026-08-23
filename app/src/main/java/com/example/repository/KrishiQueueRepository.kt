package com.example.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.FarmerProfileEntity
import com.example.data.local.KrishiDao
import com.example.models.*
import com.example.ui.components.KrishiChatKnowledgeBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class KrishiQueueRepository(
    private val krishiDao: KrishiDao? = null
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJob: Job? = null

    // App language
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    // Current user role
    private val _currentRole = MutableStateFlow(UserRole.FARMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Current logged in Farmer Profile
    private val _farmerProfile = MutableStateFlow(
        FarmerProfile(
            id = "FARMER-8842",
            fullName = "Rameshwar Singh",
            mobile = "+91 98765 43210",
            phone = "+91 98765 43210",
            email = "rameshwar.singh@krishi.gov.in",
            aadhaarLast4 = "8842",
            registrationId = "MFMB-2026-987410",
            preferredCentreId = "centre-krn-01",
            preferredCentreName = "Central Karnal Grain Mandi",
            state = "Haryana",
            district = "Karnal",
            block = "Nilokheri",
            village = "Taraori",
            landRecordNo = "HR-KRN-2024-88392",
            landAreaAcres = 6.5,
            primaryCrop = CropType.WHEAT,
            expectedQuantityQuintals = 120.0,
            bankName = "State Bank of India",
            bankAccountLast4 = "5412",
            ifscCode = "SBIN0001234",
            preferredLanguage = Language.ENGLISH,
            kycVerified = true
        )
    )
    val farmerProfile: StateFlow<FarmerProfile> = _farmerProfile.asStateFlow()

    // Room DB Profile Save Status
    private val _profileSaveStatus = MutableStateFlow<String?>(null)
    val profileSaveStatus: StateFlow<String?> = _profileSaveStatus.asStateFlow()

    // Help Chatbot Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    // Procurement Centres
    private val _centres = MutableStateFlow<List<ProcurementCentre>>(emptyList())
    val centres: StateFlow<List<ProcurementCentre>> = _centres.asStateFlow()

    // Selected Centre for booking/view
    private val _selectedCentreId = MutableStateFlow<String>("centre-krn-01")
    val selectedCentreId: StateFlow<String> = _selectedCentreId.asStateFlow()

    // All Active & History Bookings
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Real-time Simulation state
    private val _isSimulationRunning = MutableStateFlow(true)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    // Live Alert Banner (when farmer is near or called)
    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    // Mock Push Notification System (Heads-Up Alerts)
    private val _currentPushNotification = MutableStateFlow<PushNotificationItem?>(null)
    val currentPushNotification: StateFlow<PushNotificationItem?> = _currentPushNotification.asStateFlow()

    private val _pushHistory = MutableStateFlow<List<PushNotificationItem>>(emptyList())
    val pushHistory: StateFlow<List<PushNotificationItem>> = _pushHistory.asStateFlow()

    private var lastRecordedFarmerPosition: Int = 6
    private var lastRecordedFarmerStatus: BookingStatus = BookingStatus.WAITING

    init {
        seedInitialData()
        initRoomPersistence()
        startSimulation()
    }

    private fun initRoomPersistence() {
        if (krishiDao != null) {
            scope.launch {
                try {
                    // Check if profile exists in Room DB
                    val existing = krishiDao.getFarmerProfileDirect()
                    if (existing != null) {
                        _farmerProfile.value = existing.toDomainModel()
                    } else {
                        krishiDao.insertOrUpdateProfile(
                            FarmerProfileEntity.fromDomainModel(_farmerProfile.value)
                        )
                    }

                    // Observe Profile changes from Room DB
                    launch {
                        krishiDao.getFarmerProfile(_farmerProfile.value.id).collect { entity ->
                            if (entity != null) {
                                _farmerProfile.value = entity.toDomainModel()
                            }
                        }
                    }

                    // Observe Chat Messages from Room DB
                    launch {
                        krishiDao.getAllChatMessages().collect { msgs ->
                            if (msgs.isEmpty()) {
                                // Seed initial greeting into DB
                                val initialWelcome = ChatMessageEntity(
                                    sender = "BOT",
                                    text = "Namaste Kisan Bhai! 🙏 I am **Krishi Sahayak**, your 24/7 mandi procurement assistant. Ask me about MSP rates, moisture limits, slot booking, or DBT payments. Tap 🎙️ to speak!",
                                    hindiText = "नमस्ते किसान भाई! 🙏 मैं आपका **कृषि सहायक** हूँ। एमएसपी भाव, नमी मानक, स्लॉट बुकिंग या डीबीटी भुगतान से जुड़े सवाल पूछें। बोलकर पूछने के लिए 🎙️ दबाएं!",
                                    isVoice = false,
                                    category = "GENERAL"
                                )
                                krishiDao.insertChatMessage(initialWelcome)
                            } else {
                                _chatMessages.value = msgs
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("KrishiQueueRepository", "Room DB init error", e)
                }
            }
        } else {
            // In-memory fallback welcome message
            _chatMessages.value = listOf(
                ChatMessageEntity(
                    sender = "BOT",
                    text = "Namaste Kisan Bhai! 🙏 I am **Krishi Sahayak**, your 24/7 mandi procurement assistant. Ask me about MSP rates, moisture limits, slot booking, or DBT payments. Tap 🎙️ to speak!",
                    hindiText = "नमस्ते किसान भाई! 🙏 मैं आपका **कृषि सहायक** हूँ। एमएसपी भाव, नमी मानक, स्लॉट बुकिंग या डीबीटी भुगतान से जुड़े सवाल पूछें। बोलकर पूछने के लिए 🎙️ दबाएं!",
                    isVoice = false,
                    category = "GENERAL"
                )
            )
        }
    }

    private fun seedInitialData() {
        val initialCentres = listOf(
            ProcurementCentre(
                id = "centre-krn-01",
                code = "CP-KRN",
                name = "Central Karnal Grain Mandi",
                hindiName = "केंद्रीय करनाल अनाज मंडी",
                state = "Haryana",
                district = "Karnal",
                address = "GT Road, Sector 3, Karnal, Haryana 132001",
                latitude = 29.6857,
                longitude = 76.9905,
                status = CentreStatus.OPEN,
                dailyCapacityQuintals = 4500.0,
                bookedCapacityQuintals = 3200.0,
                totalTokensToday = 142,
                currentlyServingToken = "CP-098",
                currentQueueLength = 8,
                avgWaitMinutes = 35,
                activeCounters = 4,
                contactNumber = "+91 184 225 6789",
                cropsAccepted = listOf(CropType.WHEAT, CropType.PADDY, CropType.MUSTARD)
            ),
            ProcurementCentre(
                id = "centre-ldh-02",
                code = "CP-LDH",
                name = "Ludhiana APMC Procurement Hub",
                hindiName = "लुधियाना एपीएमसी खरीद केंद्र",
                state = "Punjab",
                district = "Ludhiana",
                address = "Gill Road, Near Grain Market, Ludhiana, Punjab 141003",
                latitude = 30.9010,
                longitude = 75.8573,
                status = CentreStatus.BUSY,
                dailyCapacityQuintals = 6000.0,
                bookedCapacityQuintals = 5400.0,
                totalTokensToday = 210,
                currentlyServingToken = "CP-185",
                currentQueueLength = 16,
                avgWaitMinutes = 55,
                activeCounters = 6,
                contactNumber = "+91 161 240 1920",
                cropsAccepted = listOf(CropType.WHEAT, CropType.PADDY, CropType.COTTON, CropType.MAIZE)
            ),
            ProcurementCentre(
                id = "centre-ind-03",
                code = "CP-IND",
                name = "Indore Laxmibai Nagar Mandi",
                hindiName = "इंदौर लक्ष्मीबाई नगर मंडी",
                state = "Madhya Pradesh",
                district = "Indore",
                address = "Sanwer Road, Laxmibai Nagar, Indore, MP 452006",
                latitude = 22.7533,
                longitude = 75.8937,
                status = CentreStatus.OPEN,
                dailyCapacityQuintals = 5000.0,
                bookedCapacityQuintals = 2800.0,
                totalTokensToday = 115,
                currentlyServingToken = "CP-082",
                currentQueueLength = 5,
                avgWaitMinutes = 20,
                activeCounters = 5,
                contactNumber = "+91 731 242 8844",
                cropsAccepted = listOf(CropType.WHEAT, CropType.SOYBEAN, CropType.GRAM_PULSES, CropType.MUSTARD)
            ),
            ProcurementCentre(
                id = "centre-vns-04",
                code = "CP-VNS",
                name = "Varanasi Krishak Seva Kendra",
                hindiName = "वाराणसी कृषक सेवा केंद्र",
                state = "Uttar Pradesh",
                district = "Varanasi",
                address = "Panchkoshi Road, Shivpur, Varanasi, UP 221003",
                latitude = 25.3572,
                longitude = 82.9698,
                status = CentreStatus.OPEN,
                dailyCapacityQuintals = 3500.0,
                bookedCapacityQuintals = 2100.0,
                totalTokensToday = 88,
                currentlyServingToken = "CP-064",
                currentQueueLength = 4,
                avgWaitMinutes = 18,
                activeCounters = 3,
                contactNumber = "+91 542 228 1190",
                cropsAccepted = listOf(CropType.WHEAT, CropType.PADDY, CropType.GRAM_PULSES)
            )
        )
        _centres.value = initialCentres

        val todayDate = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date())

        // Initial bookings including the farmer's active booking (Token A-104 / CP-104)
        val initialBookings = mutableListOf(
            // Currently serving
            Booking(
                id = "b-098",
                bookingReference = "KQ-2026-098",
                tokenNumber = "CP-098",
                farmerId = "FARMER-1021",
                farmerName = "Gurpreet Singh",
                farmerMobile = "+91 94160 11223",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "09:00 - 10:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 95.0,
                status = BookingStatus.IN_PROCESS,
                queuePosition = 0,
                counterNumber = 1,
                estimatedWaitMinutes = 0
            ),
            Booking(
                id = "b-099",
                bookingReference = "KQ-2026-099",
                tokenNumber = "CP-099",
                farmerId = "FARMER-1022",
                farmerName = "Baldev Ram",
                farmerMobile = "+91 98120 44556",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "09:00 - 10:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 80.0,
                status = BookingStatus.CALLED,
                queuePosition = 1,
                counterNumber = 2,
                estimatedWaitMinutes = 5
            ),
            Booking(
                id = "b-100",
                bookingReference = "KQ-2026-100",
                tokenNumber = "CP-100",
                farmerId = "FARMER-1023",
                farmerName = "Harbans Lal",
                farmerMobile = "+91 94660 77889",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 110.0,
                status = BookingStatus.ARRIVED,
                queuePosition = 2,
                counterNumber = 1,
                estimatedWaitMinutes = 12
            ),
            Booking(
                id = "b-101",
                bookingReference = "KQ-2026-101",
                tokenNumber = "CP-101",
                farmerId = "FARMER-1024",
                farmerName = "Sukhwinder Kaur",
                farmerMobile = "+91 98960 33445",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.MUSTARD,
                expectedQuantityQuintals = 65.0,
                status = BookingStatus.WAITING,
                queuePosition = 3,
                counterNumber = 3,
                estimatedWaitMinutes = 18
            ),
            Booking(
                id = "b-102",
                bookingReference = "KQ-2026-102",
                tokenNumber = "CP-102",
                farmerId = "FARMER-1025",
                farmerName = "Jagjit Singh",
                farmerMobile = "+91 94165 99887",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 140.0,
                status = BookingStatus.WAITING,
                queuePosition = 4,
                counterNumber = 1,
                estimatedWaitMinutes = 25
            ),
            Booking(
                id = "b-103",
                bookingReference = "KQ-2026-103",
                tokenNumber = "CP-103",
                farmerId = "FARMER-1026",
                farmerName = "Kuldeep Sharma",
                farmerMobile = "+91 98125 11335",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 90.0,
                status = BookingStatus.WAITING,
                queuePosition = 5,
                counterNumber = 2,
                estimatedWaitMinutes = 32
            ),
            // The Main Logged-in Farmer's Booking (A-104 / CP-104)
            Booking(
                id = "b-104",
                bookingReference = "KQ-2026-104",
                tokenNumber = "CP-104",
                farmerId = "FARMER-8842",
                farmerName = "Rameshwar Singh",
                farmerMobile = "+91 98765 43210",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 120.0,
                status = BookingStatus.WAITING,
                queuePosition = 6,
                counterNumber = 2,
                estimatedWaitMinutes = 40
            ),
            Booking(
                id = "b-105",
                bookingReference = "KQ-2026-105",
                tokenNumber = "CP-105",
                farmerId = "FARMER-1028",
                farmerName = "Manoj Kumar",
                farmerMobile = "+91 98765 12345",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = todayDate,
                timeSlot = "11:00 - 12:00 PM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 100.0,
                status = BookingStatus.BOOKED,
                queuePosition = 7,
                counterNumber = 4,
                estimatedWaitMinutes = 50
            ),
            // Past Completed Bookings for History & Receipts
            Booking(
                id = "b-hist-01",
                bookingReference = "KQ-2026-HIST-01",
                tokenNumber = "CP-042",
                farmerId = "FARMER-8842",
                farmerName = "Rameshwar Singh",
                farmerMobile = "+91 98765 43210",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = "15 August 2026",
                timeSlot = "09:00 - 10:00 AM",
                cropType = CropType.MUSTARD,
                expectedQuantityQuintals = 40.0,
                status = BookingStatus.COMPLETED,
                queuePosition = 0,
                counterNumber = 2,
                actualWeightQuintals = 41.2,
                moisturePercentage = 7.4,
                totalAmountPaid = 41.2 * 5650.0, // ₹2,32,780
                receiptId = "RCP-2026-883921"
            ),
            Booking(
                id = "b-hist-02",
                bookingReference = "KQ-2026-HIST-02",
                tokenNumber = "CP-019",
                farmerId = "FARMER-8842",
                farmerName = "Rameshwar Singh",
                farmerMobile = "+91 98765 43210",
                centreId = "centre-krn-01",
                centreName = "Central Karnal Grain Mandi",
                bookingDate = "10 April 2026",
                timeSlot = "10:00 - 11:00 AM",
                cropType = CropType.WHEAT,
                expectedQuantityQuintals = 110.0,
                status = BookingStatus.COMPLETED,
                queuePosition = 0,
                counterNumber = 1,
                actualWeightQuintals = 112.5,
                moisturePercentage = 11.2,
                totalAmountPaid = 112.5 * 2275.0, // ₹2,55,937.50
                receiptId = "RCP-2026-771204"
            )
        )
        _bookings.value = initialBookings

        _notifications.value = listOf(
            NotificationItem(
                title = "Slot Confirmed: Token CP-104",
                hindiTitle = "स्लॉट पुष्टि: टोकन CP-104",
                message = "Your Wheat procurement slot is confirmed for today 10:00-11:00 AM at Central Karnal Mandi.",
                hindiMessage = "आपकी गेहूं खरीद का स्लॉट आज सुबह 10:00-11:00 बजे केंद्रीय करनाल मंडी में पक्का हो गया है।",
                timestamp = "15m ago",
                type = "CONFIRMATION"
            ),
            NotificationItem(
                title = "Queue Movement Alert",
                hindiTitle = "कतार प्रगति सूचना",
                message = "Token CP-098 is now being serviced. There are 6 farmers ahead of your token CP-104.",
                hindiMessage = "टोकन CP-098 पर खरीद शुरू हो चुकी है। आपके टोकन CP-104 से पहले 6 किसान हैं।",
                timestamp = "5m ago",
                type = "ALERT"
            )
        )

        _announcements.value = listOf(
            Announcement(
                centreId = "centre-krn-01",
                title = "Fast-track Weighbridge Counter 4 Operational",
                hindiTitle = "फास्ट-ट्रैक तौल कांटा 4 चालू है",
                message = "Tractor-trolleys with pre-cleaned grain can utilize Counter 4 for instant weighment.",
                hindiMessage = "साफ अनाज वाली ट्रैक्टर-ट्रॉलियां त्वरित तौल के लिए काउंटर 4 का उपयोग कर सकती हैं।",
                timestamp = "Today 08:30 AM",
                isUrgent = false
            ),
            Announcement(
                centreId = "centre-krn-01",
                title = "Moisture Norms Update",
                hindiTitle = "नमी मानक सूचना",
                message = "As per DoCA guidelines, maximum allowed moisture for Wheat MSP is 12.0%.",
                hindiMessage = "उपभोक्ता मामले विभाग के दिशानिर्देशानुसार, गेहूं एमएसपी हेतु अधिकतम नमी 12.0% मान्य है।",
                timestamp = "Yesterday",
                isUrgent = false
            )
        )
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        _farmerProfile.update { it.copy(preferredLanguage = language) }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun selectCentre(centreId: String) {
        _selectedCentreId.value = centreId
    }

    fun toggleSimulation() {
        _isSimulationRunning.update { !it }
        if (_isSimulationRunning.value) {
            startSimulation()
        } else {
            simulationJob?.cancel()
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (true) {
                delay(8000) // Simulates live queue update every 8 seconds
                if (_isSimulationRunning.value) {
                    tickQueueSimulation()
                }
            }
        }
    }

    // Called when the queue naturally advances (or by operator)
    fun advanceQueue(centreId: String = "centre-krn-01") {
        val currentList = _bookings.value.filter { it.centreId == centreId }
        val activeQueue = currentList.filter {
            it.status in listOf(BookingStatus.BOOKED, BookingStatus.ARRIVED, BookingStatus.WAITING, BookingStatus.CALLED, BookingStatus.IN_PROCESS)
        }.sortedBy { it.tokenNumber }

        if (activeQueue.isEmpty()) return

        val inProcess = activeQueue.firstOrNull { it.status == BookingStatus.IN_PROCESS }
        val called = activeQueue.firstOrNull { it.status == BookingStatus.CALLED }
        val waiting = activeQueue.filter { it.status in listOf(BookingStatus.WAITING, BookingStatus.ARRIVED, BookingStatus.BOOKED) }

        _bookings.update { allBookings ->
            allBookings.map { booking ->
                if (booking.centreId != centreId) return@map booking

                when {
                    // Complete currently in-process
                    inProcess != null && booking.id == inProcess.id -> {
                        val delta = kotlin.random.Random.nextDouble(-2.0, 3.5)
                        val weight = booking.expectedQuantityQuintals + delta
                        val msp = booking.cropType.mspPerQuintal
                        booking.copy(
                            status = BookingStatus.COMPLETED,
                            actualWeightQuintals = weight,
                            moisturePercentage = 10.5,
                            totalAmountPaid = weight * msp,
                            receiptId = "RCP-2026-${(100000..999999).random()}",
                            queuePosition = 0,
                            estimatedWaitMinutes = 0
                        )
                    }
                    // Move called to in-process
                    called != null && booking.id == called.id -> {
                        booking.copy(
                            status = BookingStatus.IN_PROCESS,
                            queuePosition = 0,
                            estimatedWaitMinutes = 0
                        )
                    }
                    // Call next waiting token
                    waiting.isNotEmpty() && booking.id == waiting.first().id && called == null -> {
                        booking.copy(
                            status = BookingStatus.CALLED,
                            queuePosition = 1,
                            estimatedWaitMinutes = 3
                        )
                    }
                    // Advance remaining queue positions
                    waiting.isNotEmpty() -> {
                        val idx = waiting.indexOfFirst { it.id == booking.id }
                        if (idx >= 0) {
                            val newPos = maxOf(1, idx)
                            booking.copy(
                                queuePosition = newPos,
                                estimatedWaitMinutes = newPos * 7
                            )
                        } else booking
                    }
                    else -> booking
                }
            }
        }

        // Update centre stats
        val updatedCentreBookings = _bookings.value.filter { it.centreId == centreId && it.status in listOf(BookingStatus.CALLED, BookingStatus.IN_PROCESS) }
        val nextServing = updatedCentreBookings.firstOrNull()?.tokenNumber ?: "CP-105"

        _centres.update { list ->
            list.map { centre ->
                if (centre.id == centreId) {
                    val remainingWaiting = _bookings.value.count { it.centreId == centreId && it.status in listOf(BookingStatus.WAITING, BookingStatus.ARRIVED, BookingStatus.BOOKED) }
                    centre.copy(
                        currentlyServingToken = nextServing,
                        currentQueueLength = remainingWaiting,
                        avgWaitMinutes = maxOf(5, remainingWaiting * 6)
                    )
                } else centre
            }
        }

        // Check if our logged in farmer (CP-104) needs an alert & push notification!
        val myBooking = _bookings.value.firstOrNull { it.farmerId == _farmerProfile.value.id && it.status != BookingStatus.COMPLETED && it.status != BookingStatus.CANCELLED }
        if (myBooking != null) {
            val prevPos = lastRecordedFarmerPosition
            val prevStatus = lastRecordedFarmerStatus
            val newPos = myBooking.queuePosition
            val newStatus = myBooking.status

            lastRecordedFarmerPosition = newPos
            lastRecordedFarmerStatus = newStatus

            when {
                // 1. TOKEN IS CALLED
                newStatus == BookingStatus.CALLED && prevStatus != BookingStatus.CALLED -> {
                    _activeAlert.value = "🔔 Your Token ${myBooking.tokenNumber} has been CALLED! Proceed to Counter ${myBooking.counterNumber} now."
                    addNotification(
                        title = "Your Token ${myBooking.tokenNumber} is CALLED!",
                        hindiTitle = "आपका टोकन ${myBooking.tokenNumber} बुलाया गया है!",
                        message = "Please proceed immediately to Counter ${myBooking.counterNumber} at ${myBooking.centreName}.",
                        hindiMessage = "कृपया तुरंत ${myBooking.centreName} के काउंटर ${myBooking.counterNumber} पर पहुंचें।",
                        type = "CALL"
                    )
                    emitPushNotification(
                        PushNotificationItem(
                            type = PushNotificationType.TOKEN_CALLED,
                            title = "🔔 Token ${myBooking.tokenNumber} CALLED!",
                            hindiTitle = "🔔 टोकन ${myBooking.tokenNumber} बुलाया गया!",
                            body = "Proceed immediately to Weighbridge Counter ${myBooking.counterNumber} for electronic weighment.",
                            hindiBody = "इलेक्ट्रॉनिक तौल हेतु तुरंत तौल कांटा काउंटर ${myBooking.counterNumber} पर पहुंचें।",
                            tokenNumber = myBooking.tokenNumber,
                            queuePosition = 0,
                            counterNumber = myBooking.counterNumber,
                            actionText = "Go to Counter ${myBooking.counterNumber}",
                            hindiActionText = "काउंटर ${myBooking.counterNumber} पर जाएं",
                            destination = "LIVE_QUEUE"
                        )
                    )
                }

                // 2. IN PROCESS
                newStatus == BookingStatus.IN_PROCESS && prevStatus != BookingStatus.IN_PROCESS -> {
                    _activeAlert.value = "⚖️ Token ${myBooking.tokenNumber}: Weighment and Quality Inspection is currently in progress."
                    emitPushNotification(
                        PushNotificationItem(
                            type = PushNotificationType.PROCUREMENT_STARTED,
                            title = "⚖️ Inspection in Progress",
                            hindiTitle = "⚖️ तौल व जांच जारी",
                            body = "Gross trolley weighment and 12.0% moisture test active at Counter ${myBooking.counterNumber}.",
                            hindiBody = "ट्रैक्टर ट्रॉली तौल और 12.0% नमी परीक्षण काउंटर ${myBooking.counterNumber} पर जारी है।",
                            tokenNumber = myBooking.tokenNumber,
                            queuePosition = 0,
                            counterNumber = myBooking.counterNumber,
                            actionText = "View Live Status",
                            hindiActionText = "लाइव स्थिति देखें",
                            destination = "LIVE_QUEUE"
                        )
                    )
                }

                // 3. NEXT IN LINE (Position 1)
                newPos == 1 && (prevPos > 1 || prevStatus != BookingStatus.WAITING) -> {
                    _activeAlert.value = "⚡ YOU ARE NEXT IN QUEUE! Token ${myBooking.tokenNumber} has only 1 trolley ahead (~4 min)."
                    addNotification(
                        title = "You are NEXT in line! (1 ahead)",
                        hindiTitle = "आप कतार में अगले हैं! (1 आगे)",
                        message = "Your token ${myBooking.tokenNumber} is next. Please move your tractor-trolley to Mandi Gate 1.",
                        hindiMessage = "आपका टोकन ${myBooking.tokenNumber} अगला है। कृपया ट्रैक्टर मंडी गेट 1 पर ले जाएं।",
                        type = "ALERT"
                    )
                    emitPushNotification(
                        PushNotificationItem(
                            type = PushNotificationType.NEXT_IN_LINE,
                            title = "⚡ YOU ARE NEXT IN LINE!",
                            hindiTitle = "⚡ आप कतार में अगले हैं!",
                            body = "Token ${myBooking.tokenNumber}: Only 1 farmer ahead (~4 min wait). Move tractor to Mandi Gate 1 now.",
                            hindiBody = "टोकन ${myBooking.tokenNumber}: केवल 1 किसान आगे (~4 मिनट)। कृपया ट्रैक्टर मंडी गेट 1 पर लाएं।",
                            tokenNumber = myBooking.tokenNumber,
                            queuePosition = 1,
                            counterNumber = myBooking.counterNumber,
                            actionText = "Track Live Queue",
                            hindiActionText = "लाइव कतार देखें",
                            destination = "LIVE_QUEUE"
                        )
                    )
                }

                // 4. QUEUE MOVEMENT UPDATE (Position moved up)
                newPos < prevPos && newPos in 2..4 -> {
                    _activeAlert.value = "🚜 Queue Moving Forward: Token ${myBooking.tokenNumber} is now Position #$newPos (~${newPos * 6} mins)."
                    addNotification(
                        title = "Queue Moving: Position #$newPos",
                        hindiTitle = "कतार आगे बढ़ी: स्थान #$newPos",
                        message = "You are now $newPos position(s) away from weighment at ${myBooking.centreName}.",
                        hindiMessage = "आप ${myBooking.centreName} में तौल से केवल $newPos स्थान दूर हैं।",
                        type = "ALERT"
                    )
                    emitPushNotification(
                        PushNotificationItem(
                            type = PushNotificationType.QUEUE_UPDATE,
                            title = "🚜 Queue Update: Position #$newPos",
                            hindiTitle = "🚜 कतार अपडेट: स्थान #$newPos",
                            body = "Token ${myBooking.tokenNumber} moved up. $newPos farmers ahead (~${newPos * 6} min wait).",
                            hindiBody = "टोकन ${myBooking.tokenNumber} आगे बढ़ा। $newPos किसान आगे (~${newPos * 6} मिनट)।",
                            tokenNumber = myBooking.tokenNumber,
                            queuePosition = newPos,
                            counterNumber = myBooking.counterNumber,
                            actionText = "Track Queue",
                            hindiActionText = "कतार देखें",
                            destination = "LIVE_QUEUE"
                        )
                    )
                }
            }
        }
    }

    private fun tickQueueSimulation() {
        advanceQueue("centre-krn-01")
    }

    // Push Notification Emission & Triggers
    fun emitPushNotification(item: PushNotificationItem) {
        _currentPushNotification.value = item
        _pushHistory.update { listOf(item) + it.take(19) }
    }

    fun dismissPushNotification() {
        _currentPushNotification.value = null
    }

    fun triggerMockPush(type: PushNotificationType) {
        val myBooking = _bookings.value.firstOrNull { it.farmerId == _farmerProfile.value.id }
        val token = myBooking?.tokenNumber ?: "CP-104"
        val item = when (type) {
            PushNotificationType.NEXT_IN_LINE -> PushNotificationItem(
                type = PushNotificationType.NEXT_IN_LINE,
                title = "⚡ YOU ARE NEXT IN LINE!",
                hindiTitle = "⚡ आप कतार में अगले हैं!",
                body = "Token $token is next in queue. Only 1 trolley ahead (~4 mins). Please move vehicle to Gate 1.",
                hindiBody = "टोकन $token कतार में अगला है। केवल 1 ट्रॉली आगे (~4 मिनट)। कृपया वाहन गेट 1 पर लाएं।",
                tokenNumber = token,
                queuePosition = 1,
                counterNumber = 2,
                actionText = "Track Live Queue",
                hindiActionText = "लाइव कतार देखें",
                destination = "LIVE_QUEUE"
            )
            PushNotificationType.TOKEN_CALLED -> PushNotificationItem(
                type = PushNotificationType.TOKEN_CALLED,
                title = "🔔 Token $token CALLED!",
                hindiTitle = "🔔 टोकन $token बुलाया गया!",
                body = "Please proceed immediately to Weighbridge Counter 2 for grain unloading & weighment.",
                hindiBody = "कृपया अनाज अनलोडिंग व इलेक्ट्रॉनिक तौल हेतु तुरंत तौल काउंटर 2 पर पहुंचें।",
                tokenNumber = token,
                queuePosition = 0,
                counterNumber = 2,
                actionText = "Go to Counter 2",
                hindiActionText = "काउंटर 2 पर जाएं",
                destination = "LIVE_QUEUE"
            )
            PushNotificationType.QUEUE_UPDATE -> PushNotificationItem(
                type = PushNotificationType.QUEUE_UPDATE,
                title = "🚜 Queue Moving: Position #2",
                hindiTitle = "🚜 कतार आगे बढ़ी: स्थान #2",
                body = "Token $token is now 2 positions away. Estimated wait time ~12 mins at Karnal Mandi.",
                hindiBody = "टोकन $token अब 2 स्थान दूर है। करनाल मंडी में अनुमानित प्रतीक्षा समय ~12 मिनट।",
                tokenNumber = token,
                queuePosition = 2,
                counterNumber = 1,
                actionText = "View Yard Queue",
                hindiActionText = "यार्ड कतार देखें",
                destination = "LIVE_QUEUE"
            )
            PushNotificationType.PROCUREMENT_COMPLETED -> PushNotificationItem(
                type = PushNotificationType.PROCUREMENT_COMPLETED,
                title = "🌾 ₹2,76,412.50 Disbursed via DBT",
                hindiTitle = "🌾 ₹2,76,412.50 डीबीटी द्वारा हस्तांतरित",
                body = "Procurement of 121.5 Qtl Wheat successfully completed. Receipt RCP-2026-883921 generated.",
                hindiBody = "121.5 क्विंटल गेहूं की खरीद सफलतापूर्वक संपन्न। डिजिटल पावती RCP-2026-883921 जारी।",
                tokenNumber = token,
                queuePosition = 0,
                counterNumber = 2,
                actionText = "View DBT Slip",
                hindiActionText = "डीबीटी रसीद देखें",
                destination = "HISTORY_RECEIPTS"
            )
            PushNotificationType.GATE_CHECKIN -> PushNotificationItem(
                type = PushNotificationType.GATE_CHECKIN,
                title = "✅ Mandi Gate Entry Verified",
                hindiTitle = "✅ मंडी गेट प्रवेश सत्यापित",
                body = "Vehicle HR-05-AB-4920 checked-in at Karnal Mandi. Token $token placed in queue.",
                hindiBody = "वाहन HR-05-AB-4920 करनाल मंडी में दर्ज हुआ। टोकन $token कतार में सक्रिय।",
                tokenNumber = token,
                queuePosition = 3,
                counterNumber = 1,
                actionText = "View Gate Pass",
                hindiActionText = "गेट पास देखें",
                destination = "LIVE_QUEUE"
            )
            PushNotificationType.PROCUREMENT_STARTED -> PushNotificationItem(
                type = PushNotificationType.PROCUREMENT_STARTED,
                title = "⚖️ Weighment & Moisture Test Active",
                hindiTitle = "⚖️ तौल व नमी परीक्षण शुरू",
                body = "Trolley positioned on weighbridge. Moisture measured at 11.2% (Pass <12.0%).",
                hindiBody = "ट्रॉली तौल कांटे पर स्थित। नमी 11.2% मापी गई (मानक <12.0% उत्तीर्ण)।",
                tokenNumber = token,
                queuePosition = 0,
                counterNumber = 1,
                actionText = "Live Moisture Test",
                hindiActionText = "नमी रिपोर्ट देखें",
                destination = "LIVE_QUEUE"
            )
            PushNotificationType.MANDI_ADVISORY -> PushNotificationItem(
                type = PushNotificationType.MANDI_ADVISORY,
                title = "⚠️ Weather & Mandi Advisory",
                hindiTitle = "⚠️ मौसम एवं मंडी सलाह",
                body = "Cover grain trolleys with tarpaulin. Fast-track moisture counter 4 open.",
                hindiBody = "अनाज की ट्रॉलियों को तिरपाल से ढकें। फास्ट-ट्रैक काउंटर 4 चालू है।",
                tokenNumber = token,
                queuePosition = 3,
                counterNumber = 4,
                actionText = "Read Advisory",
                hindiActionText = "सलाह पढ़ें",
                destination = "CENTRES_MAP"
            )
        }
        emitPushNotification(item)
    }

    // Operator Controls
    fun operatorCallToken(bookingId: String, counter: Int = 1) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(status = BookingStatus.CALLED, counterNumber = counter, queuePosition = 1, estimatedWaitMinutes = 2)
                else it
            }
        }
        advanceQueue("centre-krn-01")
    }

    fun operatorMarkArrived(bookingId: String) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(status = BookingStatus.ARRIVED, checkInTime = System.currentTimeMillis())
                else it
            }
        }
    }

    fun operatorStartProcurement(bookingId: String) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(status = BookingStatus.IN_PROCESS, queuePosition = 0, estimatedWaitMinutes = 0)
                else it
            }
        }
    }

    fun operatorCompleteProcurement(bookingId: String, actualWeight: Double, moisture: Double) {
        val booking = _bookings.value.firstOrNull { it.id == bookingId } ?: return
        val msp = booking.cropType.mspPerQuintal
        val total = actualWeight * msp
        val receipt = "RCP-2026-${(100000..999999).random()}"

        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) {
                    it.copy(
                        status = BookingStatus.COMPLETED,
                        actualWeightQuintals = actualWeight,
                        moisturePercentage = moisture,
                        totalAmountPaid = total,
                        receiptId = receipt,
                        queuePosition = 0,
                        estimatedWaitMinutes = 0
                    )
                } else it
            }
        }

        if (booking.farmerId == _farmerProfile.value.id) {
            _activeAlert.value = "✅ Procurement Completed! Payment ₹${String.format(Locale.ENGLISH, "%,.2f", total)} approved via DBT."
            addNotification(
                title = "Procurement Complete: ₹${String.format(Locale.ENGLISH, "%,.2f", total)}",
                hindiTitle = "खरीद पूर्ण: ₹${String.format(Locale.ENGLISH, "%,.2f", total)}",
                message = "Weighment: ${actualWeight} Qtl. Moisture: ${moisture}%. Receipt: $receipt.",
                hindiMessage = "वजन: ${actualWeight} क्विंटल। नमी: ${moisture}%। रसीद: $receipt.",
                type = "CONFIRMATION"
            )
            emitPushNotification(
                PushNotificationItem(
                    type = PushNotificationType.PROCUREMENT_COMPLETED,
                    title = "🌾 ₹${String.format(Locale.ENGLISH, "%,.2f", total)} Disbursed via DBT",
                    hindiTitle = "🌾 ₹${String.format(Locale.ENGLISH, "%,.2f", total)} डीबीटी द्वारा हस्तांतरित",
                    body = "Procurement of ${actualWeight} Qtl ${booking.cropType.englishName} completed. Receipt $receipt generated.",
                    hindiBody = "${actualWeight} क्विंटल ${booking.cropType.hindiName} की खरीद संपन्न। डिजिटल पावती $receipt जारी।",
                    tokenNumber = booking.tokenNumber,
                    queuePosition = 0,
                    counterNumber = booking.counterNumber,
                    actionText = "View DBT Receipt",
                    hindiActionText = "डीबीटी रसीद देखें",
                    destination = "HISTORY_RECEIPTS"
                )
            )
        }
    }

    fun operatorMarkNoShow(bookingId: String) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(status = BookingStatus.NO_SHOW, queuePosition = 999)
                else it
            }
        }
    }

    fun operatorSkipToken(bookingId: String) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(queuePosition = it.queuePosition + 3, estimatedWaitMinutes = (it.queuePosition + 3) * 7)
                else it
            }
        }
    }

    // Farmer Slot Booking
    fun createBooking(
        centreId: String,
        date: String,
        timeSlot: String,
        cropType: CropType,
        quantity: Double
    ): Booking {
        val centre = _centres.value.firstOrNull { it.id == centreId } ?: _centres.value.first()
        val nextTokenNumber = "CP-${(106..199).random()}"
        val ref = "KQ-2026-${(1000..9999).random()}"

        val currentWaiting = _bookings.value.count { it.centreId == centreId && it.status in listOf(BookingStatus.WAITING, BookingStatus.BOOKED) }
        val position = currentWaiting + 1
        val waitEst = position * 7

        val newBooking = Booking(
            bookingReference = ref,
            tokenNumber = nextTokenNumber,
            farmerId = _farmerProfile.value.id,
            farmerName = _farmerProfile.value.fullName,
            farmerMobile = _farmerProfile.value.mobile,
            centreId = centre.id,
            centreName = centre.name,
            bookingDate = date,
            timeSlot = timeSlot,
            cropType = cropType,
            expectedQuantityQuintals = quantity,
            status = BookingStatus.BOOKED,
            queuePosition = position,
            estimatedWaitMinutes = waitEst
        )

        _bookings.update { listOf(newBooking) + it }

        addNotification(
            title = "Slot Booked: Token $nextTokenNumber",
            hindiTitle = "स्लॉट बुक हुआ: टोकन $nextTokenNumber",
            message = "Booking $ref for $date ($timeSlot) confirmed at ${centre.name}.",
            hindiMessage = "बुकिंग $ref दिनांक $date ($timeSlot) हेतु ${centre.name} में सुनिश्चित हुई।",
            type = "CONFIRMATION"
        )

        return newBooking
    }

    fun cancelBooking(bookingId: String) {
        _bookings.update { list ->
            list.map {
                if (it.id == bookingId) it.copy(status = BookingStatus.CANCELLED)
                else it
            }
        }
    }

    fun updateFarmerProfile(profile: FarmerProfile) {
        _farmerProfile.value = profile
        if (krishiDao != null) {
            scope.launch {
                try {
                    krishiDao.insertOrUpdateProfile(FarmerProfileEntity.fromDomainModel(profile))
                    val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date())
                    _profileSaveStatus.value = "Profile saved to local Room Database ($timeStr)"
                } catch (e: Exception) {
                    android.util.Log.e("KrishiQueueRepository", "Error saving profile to Room", e)
                    _profileSaveStatus.value = "Error saving to Room database: ${e.message}"
                }
            }
        } else {
            val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(Date())
            _profileSaveStatus.value = "Profile updated ($timeStr)"
        }
    }

    fun clearProfileSaveStatus() {
        _profileSaveStatus.value = null
    }

    // Help Chatbot Operations
    fun sendChatMessage(query: String, isVoice: Boolean = false): KrishiChatKnowledgeBase.BotResponse {
        val userMsg = ChatMessageEntity(
            sender = "USER",
            text = query,
            hindiText = query,
            timestamp = System.currentTimeMillis(),
            isVoice = isVoice
        )

        // Generate response using knowledge base & current farmer context
        val botResponse = KrishiChatKnowledgeBase.answerQuery(query, _farmerProfile.value)

        val botMsg = ChatMessageEntity(
            sender = "BOT",
            text = botResponse.englishText,
            hindiText = botResponse.hindiText,
            timestamp = System.currentTimeMillis() + 50,
            isVoice = false,
            category = botResponse.category
        )

        if (krishiDao != null) {
            scope.launch {
                try {
                    krishiDao.insertChatMessage(userMsg)
                    krishiDao.insertChatMessage(botMsg)
                } catch (e: Exception) {
                    android.util.Log.e("KrishiQueueRepository", "Error saving chat message to Room", e)
                }
            }
        } else {
            _chatMessages.update { it + listOf(userMsg, botMsg) }
        }

        return botResponse
    }

    fun clearChatMessages() {
        if (krishiDao != null) {
            scope.launch {
                try {
                    krishiDao.clearChatMessages()
                    val initialWelcome = ChatMessageEntity(
                        sender = "BOT",
                        text = "Namaste Kisan Bhai! 🙏 I am **Krishi Sahayak**, your 24/7 mandi procurement assistant. Ask me about MSP rates, moisture limits, slot booking, or DBT payments. Tap 🎙️ to speak!",
                        hindiText = "नमस्ते किसान भाई! 🙏 मैं आपका **कृषि सहायक** हूँ। एमएसपी भाव, नमी मानक, स्लॉट बुकिंग या डीबीटी भुगतान से जुड़े सवाल पूछें। बोलकर पूछने के लिए 🎙️ दबाएं!",
                        isVoice = false,
                        category = "GENERAL"
                    )
                    krishiDao.insertChatMessage(initialWelcome)
                } catch (e: Exception) {
                    android.util.Log.e("KrishiQueueRepository", "Error clearing chat in Room", e)
                }
            }
        } else {
            _chatMessages.value = listOf(
                ChatMessageEntity(
                    sender = "BOT",
                    text = "Namaste Kisan Bhai! 🙏 I am **Krishi Sahayak**, your 24/7 mandi procurement assistant. Ask me about MSP rates, moisture limits, slot booking, or DBT payments. Tap 🎙️ to speak!",
                    hindiText = "नमस्ते किसान भाई! 🙏 मैं आपका **कृषि सहायक** हूँ। एमएसपी भाव, नमी मानक, स्लॉट बुकिंग या डीबीटी भुगतान से जुड़े सवाल पूछें। बोलकर पूछने के लिए 🎙️ दबाएं!",
                    isVoice = false,
                    category = "GENERAL"
                )
            )
        }
    }

    fun addNotification(title: String, hindiTitle: String, message: String, hindiMessage: String, type: String = "ALERT") {
        val item = NotificationItem(
            title = title,
            hindiTitle = hindiTitle,
            message = message,
            hindiMessage = hindiMessage,
            timestamp = "Just now",
            type = type,
            isRead = false
        )
        _notifications.update { listOf(item) + it }
    }

    fun clearAlert() {
        _activeAlert.value = null
    }

    fun markNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun broadcastAnnouncement(title: String, hindiTitle: String, message: String, hindiMessage: String, isUrgent: Boolean) {
        val item = Announcement(
            centreId = _selectedCentreId.value,
            title = title,
            hindiTitle = hindiTitle,
            message = message,
            hindiMessage = hindiMessage,
            timestamp = "Today " + SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
            isUrgent = isUrgent
        )
        _announcements.update { listOf(item) + it }
    }

    fun getCentreAnalytics(centreId: String): CentreAnalytics {
        val centreBookings = _bookings.value.filter { it.centreId == centreId }
        val completed = centreBookings.filter { it.status == BookingStatus.COMPLETED }
        val inProcess = centreBookings.count { it.status == BookingStatus.IN_PROCESS || it.status == BookingStatus.CALLED }
        val waiting = centreBookings.count { it.status == BookingStatus.WAITING || it.status == BookingStatus.ARRIVED || it.status == BookingStatus.BOOKED }
        val noShow = centreBookings.count { it.status == BookingStatus.NO_SHOW }

        val totalProcured = completed.sumOf { it.actualWeightQuintals ?: it.expectedQuantityQuintals }
        val totalPayout = completed.sumOf { it.totalAmountPaid ?: (it.expectedQuantityQuintals * it.cropType.mspPerQuintal) }

        val hourlyArr = listOf(
            HourlyStat("08:00", 18),
            HourlyStat("09:00", 26),
            HourlyStat("10:00", 35),
            HourlyStat("11:00", 28),
            HourlyStat("12:00", 15),
            HourlyStat("13:00", 12),
            HourlyStat("14:00", 22)
        )

        val hourlyComp = listOf(
            HourlyStat("08:00", 14),
            HourlyStat("09:00", 24),
            HourlyStat("10:00", 30),
            HourlyStat("11:00", 25),
            HourlyStat("12:00", 16),
            HourlyStat("13:00", 11),
            HourlyStat("14:00", 19)
        )

        return CentreAnalytics(
            totalBookingsToday = centreBookings.size + 45,
            completedCount = completed.size + 28,
            inProcessCount = inProcess,
            waitingCount = waiting,
            noShowCount = noShow + 2,
            totalProcuredQuintals = totalProcured + 2840.0,
            totalPayoutDisbursed = totalPayout + 6450000.0,
            avgWaitTimeMinutes = 24,
            avgServiceTimeMinutes = 8,
            hourlyArrivals = hourlyArr,
            hourlyCompletions = hourlyComp
        )
    }

    fun getTimeSlotsForDate(centreId: String, dateString: String): List<TimeSlot> {
        return listOf(
            TimeSlot("slot-1", centreId, dateString, "08:00 - 09:00 AM", 25, 22, isAvailable = true),
            TimeSlot("slot-2", centreId, dateString, "09:00 - 10:00 AM", 30, 29, isAvailable = true),
            TimeSlot("slot-3", centreId, dateString, "10:00 - 11:00 AM", 30, 30, isAvailable = false), // Full
            TimeSlot("slot-4", centreId, dateString, "11:00 - 12:00 PM", 30, 24, isAvailable = true),
            TimeSlot("slot-5", centreId, dateString, "12:00 - 01:00 PM", 20, 14, isAvailable = true),
            TimeSlot("slot-6", centreId, dateString, "02:00 - 03:00 PM", 30, 18, isAvailable = true),
            TimeSlot("slot-7", centreId, dateString, "03:00 - 04:00 PM", 30, 12, isAvailable = true),
            TimeSlot("slot-8", centreId, dateString, "04:00 - 05:00 PM", 25, 8, isAvailable = true)
        )
    }
}
