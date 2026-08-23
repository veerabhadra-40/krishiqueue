package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.KrishiDatabase
import com.example.models.*
import com.example.repository.KrishiQueueRepository
import com.example.ui.components.KrishiChatKnowledgeBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AppDestination {
    LANDING_HERO,
    FARMER_DASHBOARD,
    BOOK_SLOT,
    LIVE_QUEUE,
    CENTRES_MAP,
    HISTORY_RECEIPTS,
    OPERATOR_DESK,
    ADMIN_ANALYTICS,
    FARMER_PROFILE
}

class KrishiQueueViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = KrishiDatabase.getDatabase(application)
    private val repository = KrishiQueueRepository(database.krishiDao())

    // Current Navigation Screen
    private val _currentDestination = MutableStateFlow(AppDestination.LANDING_HERO)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Language & Role from Repository
    val currentLanguage: StateFlow<Language> = repository.currentLanguage
    val currentRole: StateFlow<UserRole> = repository.currentRole
    val farmerProfile: StateFlow<FarmerProfile> = repository.farmerProfile
    val profileSaveStatus: StateFlow<String?> = repository.profileSaveStatus
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
    val centres: StateFlow<List<ProcurementCentre>> = repository.centres
    val selectedCentreId: StateFlow<String> = repository.selectedCentreId
    val bookings: StateFlow<List<Booking>> = repository.bookings
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val announcements: StateFlow<List<Announcement>> = repository.announcements
    val isSimulationRunning: StateFlow<Boolean> = repository.isSimulationRunning
    val activeAlert: StateFlow<String?> = repository.activeAlert
    val currentPushNotification: StateFlow<PushNotificationItem?> = repository.currentPushNotification
    val pushHistory: StateFlow<List<PushNotificationItem>> = repository.pushHistory

    // Help Chat Dialog State
    private val _showHelpChatDialog = MutableStateFlow(false)
    val showHelpChatDialog: StateFlow<Boolean> = _showHelpChatDialog.asStateFlow()

    // Active farmer booking (if any ongoing)
    val activeFarmerBooking: StateFlow<Booking?> = combine(bookings, farmerProfile) { allBookings, farmer ->
        allBookings.firstOrNull {
            it.farmerId == farmer.id &&
            it.status in listOf(BookingStatus.BOOKED, BookingStatus.ARRIVED, BookingStatus.WAITING, BookingStatus.CALLED, BookingStatus.IN_PROCESS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Booking Wizard State
    private val _bookingStep = MutableStateFlow(1)
    val bookingStep: StateFlow<Int> = _bookingStep.asStateFlow()

    private val _selectedBookingCentre = MutableStateFlow<ProcurementCentre?>(null)
    val selectedBookingCentre: StateFlow<ProcurementCentre?> = _selectedBookingCentre.asStateFlow()

    private val _selectedBookingDate = MutableStateFlow("24 August 2026")
    val selectedBookingDate: StateFlow<String> = _selectedBookingDate.asStateFlow()

    private val _selectedTimeSlot = MutableStateFlow<TimeSlot?>(null)
    val selectedTimeSlot: StateFlow<TimeSlot?> = _selectedTimeSlot.asStateFlow()

    private val _selectedCrop = MutableStateFlow(CropType.WHEAT)
    val selectedCrop: StateFlow<CropType> = _selectedCrop.asStateFlow()

    private val _enteredQuantity = MutableStateFlow("120")
    val enteredQuantity: StateFlow<String> = _enteredQuantity.asStateFlow()

    private val _lastGeneratedBooking = MutableStateFlow<Booking?>(null)
    val lastGeneratedBooking: StateFlow<Booking?> = _lastGeneratedBooking.asStateFlow()

    // Dialogs & Overlays
    private val _showTokenQrDialog = MutableStateFlow<Booking?>(null)
    val showTokenQrDialog: StateFlow<Booking?> = _showTokenQrDialog.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow<Booking?>(null)
    val showReceiptDialog: StateFlow<Booking?> = _showReceiptDialog.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _showAnnouncementDialog = MutableStateFlow(false)
    val showAnnouncementDialog: StateFlow<Boolean> = _showAnnouncementDialog.asStateFlow()

    // Centre Search & Filter
    private val _centreSearchQuery = MutableStateFlow("")
    val centreSearchQuery: StateFlow<String> = _centreSearchQuery.asStateFlow()

    private val _cropFilter = MutableStateFlow<CropType?>(null)
    val cropFilter: StateFlow<CropType?> = _cropFilter.asStateFlow()

    // Mouse pointer reactive coordinates for 3D hero tilt
    private val _pointerX = MutableStateFlow(0f)
    val pointerX: StateFlow<Float> = _pointerX.asStateFlow()

    private val _pointerY = MutableStateFlow(0f)
    val pointerY: StateFlow<Float> = _pointerY.asStateFlow()

    init {
        // Default selected booking centre
        _selectedBookingCentre.value = repository.centres.value.firstOrNull()
        _selectedTimeSlot.value = repository.getTimeSlotsForDate("centre-krn-01", "24 August 2026").firstOrNull { it.isAvailable }
    }

    // Helper for bilingual translation
    fun tr(en: String, hi: String): String {
        return if (currentLanguage.value == Language.HINDI) hi else en
    }

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
    }

    fun setLanguage(lang: Language) {
        repository.setLanguage(lang)
    }

    fun setRole(role: UserRole) {
        repository.setRole(role)
        when (role) {
            UserRole.FARMER -> _currentDestination.value = AppDestination.FARMER_DASHBOARD
            UserRole.OPERATOR -> _currentDestination.value = AppDestination.OPERATOR_DESK
            UserRole.CENTRE_MANAGER, UserRole.DISTRICT_ADMIN -> _currentDestination.value = AppDestination.ADMIN_ANALYTICS
        }
    }

    fun selectCentre(centreId: String) {
        repository.selectCentre(centreId)
        _selectedBookingCentre.value = repository.centres.value.firstOrNull { it.id == centreId }
    }

    fun toggleSimulation() {
        repository.toggleSimulation()
    }

    fun triggerManualQueueAdvance() {
        repository.advanceQueue(selectedCentreId.value)
    }

    fun updatePointer(x: Float, y: Float) {
        _pointerX.value = x
        _pointerY.value = y
    }

    // Booking Wizard Methods
    fun setBookingStep(step: Int) {
        _bookingStep.value = step
    }

    fun setBookingCentre(centre: ProcurementCentre) {
        _selectedBookingCentre.value = centre
        repository.selectCentre(centre.id)
    }

    fun setBookingDate(date: String) {
        _selectedBookingDate.value = date
    }

    fun setTimeSlot(slot: TimeSlot) {
        _selectedTimeSlot.value = slot
    }

    fun setCrop(crop: CropType) {
        _selectedCrop.value = crop
    }

    fun setQuantity(qty: String) {
        _enteredQuantity.value = qty
    }

    fun getSlotsForDate(date: String): List<TimeSlot> {
        val centreId = _selectedBookingCentre.value?.id ?: "centre-krn-01"
        return repository.getTimeSlotsForDate(centreId, date)
    }

    fun confirmBooking(): Booking? {
        val centre = _selectedBookingCentre.value ?: repository.centres.value.firstOrNull() ?: return null
        val slot = _selectedTimeSlot.value ?: return null
        val qty = _enteredQuantity.value.toDoubleOrNull() ?: 100.0

        val booking = repository.createBooking(
            centreId = centre.id,
            date = _selectedBookingDate.value,
            timeSlot = slot.timeRange,
            cropType = _selectedCrop.value,
            quantity = qty
        )

        _lastGeneratedBooking.value = booking
        _bookingStep.value = 4
        return booking
    }

    // Dialogs
    fun showTokenQr(booking: Booking?) {
        _showTokenQrDialog.value = booking
    }

    fun showReceipt(booking: Booking?) {
        _showReceiptDialog.value = booking
    }

    fun toggleNotifications(show: Boolean) {
        _showNotificationsSheet.value = show
        if (show) repository.markNotificationsAsRead()
    }

    fun toggleAnnouncementDialog(show: Boolean) {
        _showAnnouncementDialog.value = show
    }

    fun clearAlert() {
        repository.clearAlert()
    }

    // Operator Desk
    fun operatorCallToken(bookingId: String, counter: Int = 1) {
        repository.operatorCallToken(bookingId, counter)
    }

    fun operatorMarkArrived(bookingId: String) {
        repository.operatorMarkArrived(bookingId)
    }

    fun operatorStartProcurement(bookingId: String) {
        repository.operatorStartProcurement(bookingId)
    }

    fun operatorCompleteProcurement(bookingId: String, weight: Double, moisture: Double) {
        repository.operatorCompleteProcurement(bookingId, weight, moisture)
    }

    fun operatorMarkNoShow(bookingId: String) {
        repository.operatorMarkNoShow(bookingId)
    }

    fun operatorSkipToken(bookingId: String) {
        repository.operatorSkipToken(bookingId)
    }

    // Manager / Admin
    fun getCentreAnalytics(): CentreAnalytics {
        return repository.getCentreAnalytics(selectedCentreId.value)
    }

    fun broadcastAnnouncement(title: String, hindiTitle: String, message: String, hindiMessage: String, isUrgent: Boolean) {
        repository.broadcastAnnouncement(title, hindiTitle, message, hindiMessage, isUrgent)
        _showAnnouncementDialog.value = false
    }

    fun setCentreSearchQuery(query: String) {
        _centreSearchQuery.value = query
    }

    fun setCropFilter(crop: CropType?) {
        _cropFilter.value = crop
    }

    // Push Notification Actions & Mock Testing
    fun dismissPushNotification() {
        repository.dismissPushNotification()
    }

    fun triggerMockPush(type: PushNotificationType) {
        repository.triggerMockPush(type)
    }

    fun handlePushNotificationAction(item: PushNotificationItem) {
        repository.dismissPushNotification()
        when (item.destination) {
            "LIVE_QUEUE" -> _currentDestination.value = AppDestination.LIVE_QUEUE
            "HISTORY_RECEIPTS" -> _currentDestination.value = AppDestination.HISTORY_RECEIPTS
            "CENTRES_MAP" -> _currentDestination.value = AppDestination.CENTRES_MAP
            "BOOK_SLOT" -> _currentDestination.value = AppDestination.BOOK_SLOT
            else -> _currentDestination.value = AppDestination.FARMER_DASHBOARD
        }
    }

    // Farmer Profile Room DB Persistence
    fun saveFarmerProfile(profile: FarmerProfile) {
        repository.updateFarmerProfile(profile)
    }

    fun clearProfileSaveStatus() {
        repository.clearProfileSaveStatus()
    }

    // Help Chatbot Management
    fun toggleHelpChat(show: Boolean) {
        _showHelpChatDialog.value = show
    }

    fun sendChatMessage(
        query: String,
        isVoice: Boolean = false,
        onResponse: (KrishiChatKnowledgeBase.BotResponse) -> Unit = {}
    ) {
        if (query.isBlank()) return
        val response = repository.sendChatMessage(query, isVoice)
        onResponse(response)
    }

    fun clearChatMessages() {
        repository.clearChatMessages()
    }
}

