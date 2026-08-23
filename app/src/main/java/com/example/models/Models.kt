package com.example.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी")
}

enum class UserRole(val displayName: String, val hindiName: String) {
    FARMER("Farmer", "किसान"),
    OPERATOR("Procurement Operator", "खरीद संचालक"),
    CENTRE_MANAGER("Centre Manager", "केंद्र प्रबंधक"),
    DISTRICT_ADMIN("District Admin", "जिला प्रशासक")
}

enum class BookingStatus(val displayName: String, val hindiName: String) {
    BOOKED("Booked", "बुक किया गया"),
    ARRIVED("Arrived", "उपस्थित"),
    WAITING("Waiting in Queue", "कतार में प्रतीक्षारत"),
    CALLED("Token Called", "टोकन बुलाया गया"),
    IN_PROCESS("In Process", "प्रक्रियाधीन"),
    COMPLETED("Completed", "खरीद पूर्ण"),
    NO_SHOW("No Show", "अनुपस्थित"),
    CANCELLED("Cancelled", "रद्द")
}

enum class CentreStatus(val displayName: String, val hindiName: String) {
    OPEN("Open", "खुला है"),
    BUSY("Heavy Queue", "अधिक भीड़"),
    PAUSED("Paused", "अस्थायी रुका"),
    CLOSED("Closed", "बंद")
}

enum class CropType(
    val englishName: String,
    val hindiName: String,
    val mspPerQuintal: Double,
    val icon: String = "🌾"
) {
    WHEAT("Wheat (गेहूं)", "गेहूं", 2275.0, "🌾"),
    PADDY("Paddy / Rice (धान)", "धान", 2300.0, "🌾"),
    MUSTARD("Mustard (सरसों)", "सरसों", 5650.0, "🌼"),
    GRAM_PULSES("Gram / Chana (चना)", "चना", 5440.0, "🌱"),
    COTTON("Cotton (कपास)", "कपास", 7121.0, "☁️"),
    MAIZE("Maize (मक्का)", "मक्का", 2090.0, "🌽"),
    SOYBEAN("Soybean (सोयाबीन)", "सोयाबीन", 4892.0, "🫘")
}

enum class PushNotificationType(
    val englishTitle: String,
    val hindiTitle: String,
    val priority: String
) {
    NEXT_IN_LINE("⚡ You Are Next in Queue!", "⚡ आप कतार में अगले हैं!", "HIGH"),
    TOKEN_CALLED("🔔 Token Called: Proceed to Counter", "🔔 टोकन बुलाया गया: काउंटर पर जाएं", "CRITICAL"),
    QUEUE_UPDATE("🚜 Live Queue Status Update", "🚜 लाइव कतार स्थिति अपडेट", "NORMAL"),
    GATE_CHECKIN("✅ Mandi Gate Entry Verified", "✅ मंडी गेट प्रवेश सत्यापित", "NORMAL"),
    PROCUREMENT_STARTED("⚖️ Weighment & Moisture Test Started", "⚖️ तौल और नमी परीक्षण शुरू", "HIGH"),
    PROCUREMENT_COMPLETED("🌾 Procurement & DBT Payment Done", "🌾 खरीद पूर्ण व डीबीटी भुगतान", "NORMAL"),
    MANDI_ADVISORY("⚠️ Mandi Yard Advisory", "⚠️ मंडी यार्ड सूचना", "LOW")
}

data class PushNotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val type: PushNotificationType,
    val title: String,
    val hindiTitle: String,
    val body: String,
    val hindiBody: String,
    val tokenNumber: String = "CP-104",
    val queuePosition: Int = 1,
    val counterNumber: Int = 1,
    val timestamp: String = "Just now",
    val actionText: String = "Track Live Queue",
    val hindiActionText: String = "लाइव कतार देखें",
    val destination: String = "LIVE_QUEUE",
    val deliveryChannel: String = "FCM Push + SMS (DoCA-KRISHI)",
    val receivedAtMillis: Long = System.currentTimeMillis()
)

data class FarmerProfile(
    val id: String = "FARMER-8842",
    val fullName: String = "Rameshwar Singh",
    val mobile: String = "+91 98765 43210",
    val phone: String = "+91 98765 43210",
    val email: String = "rameshwar.farmer@krishi.gov.in",
    val aadhaarLast4: String = "8842",
    val registrationId: String = "MFMB-2026-987410",
    val preferredCentreId: String = "centre-krn-01",
    val preferredCentreName: String = "Central Procurement Mandi - Karnal",
    val state: String = "Haryana",
    val district: String = "Karnal",
    val block: String = "Nilokheri",
    val village: String = "Taraori",
    val landRecordNo: String = "HR-KRN-2024-88392",
    val landAreaAcres: Double = 6.5,
    val primaryCrop: CropType = CropType.WHEAT,
    val expectedQuantityQuintals: Double = 120.0,
    val bankName: String = "State Bank of India",
    val bankAccountLast4: String = "5412",
    val ifscCode: String = "SBIN0001234",
    val preferredLanguage: Language = Language.ENGLISH,
    val kycVerified: Boolean = true
)

data class ProcurementCentre(
    val id: String,
    val code: String, // e.g. "CP-KRN"
    val name: String,
    val hindiName: String,
    val state: String,
    val district: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val status: CentreStatus,
    val dailyCapacityQuintals: Double,
    val bookedCapacityQuintals: Double,
    val totalTokensToday: Int,
    val currentlyServingToken: String,
    val currentQueueLength: Int,
    val avgWaitMinutes: Int,
    val activeCounters: Int,
    val contactNumber: String,
    val cropsAccepted: List<CropType>
)

data class TimeSlot(
    val id: String,
    val centreId: String,
    val dateString: String,
    val timeRange: String,
    val maxCapacitySlots: Int,
    val bookedSlots: Int,
    val isAvailable: Boolean = true
)

data class Booking(
    val id: String = UUID.randomUUID().toString(),
    val bookingReference: String,
    val tokenNumber: String, // e.g. "CP-104"
    val farmerId: String,
    val farmerName: String,
    val farmerMobile: String,
    val centreId: String,
    val centreName: String,
    val bookingDate: String,
    val timeSlot: String,
    val cropType: CropType,
    val expectedQuantityQuintals: Double,
    val status: BookingStatus,
    val queuePosition: Int,
    val counterNumber: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val checkInTime: Long? = null,
    val estimatedWaitMinutes: Int = 30,
    val actualWeightQuintals: Double? = null,
    val moisturePercentage: Double? = null,
    val totalAmountPaid: Double? = null,
    val receiptId: String? = null
)

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val hindiTitle: String,
    val message: String,
    val hindiMessage: String,
    val timestamp: String = "Just now",
    val type: String = "QUEUE_UPDATE", // "ALERT", "CALL", "CONFIRMATION"
    val isRead: Boolean = false
)

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val centreId: String,
    val title: String,
    val hindiTitle: String,
    val message: String,
    val hindiMessage: String,
    val timestamp: String,
    val isUrgent: Boolean = false
)

data class CentreAnalytics(
    val totalBookingsToday: Int,
    val completedCount: Int,
    val inProcessCount: Int,
    val waitingCount: Int,
    val noShowCount: Int,
    val totalProcuredQuintals: Double,
    val totalPayoutDisbursed: Double,
    val avgWaitTimeMinutes: Int,
    val avgServiceTimeMinutes: Int,
    val hourlyArrivals: List<HourlyStat>,
    val hourlyCompletions: List<HourlyStat>
)

data class HourlyStat(
    val hourLabel: String,
    val count: Int
)
