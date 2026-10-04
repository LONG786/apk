package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EmailEntity
import com.example.data.MailRepository
import com.example.data.MailboxEntity
import com.example.util.EmailGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Inbox : ScreenDestination()
    object Starred : ScreenDestination()
    object Mailboxes : ScreenDestination()
    object PrivacyShield : ScreenDestination()
    data class EmailDetail(val emailId: Long) : ScreenDestination()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = AppDatabase.getDatabase(application)
    private val repository: MailRepository = MailRepository(database.mailboxDao(), database.emailDao())

    val activeMailbox: StateFlow<MailboxEntity?> = repository.activeMailbox
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMailboxes: StateFlow<List<MailboxEntity>> = repository.allMailboxes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val starredEmails: StateFlow<List<EmailEntity>> = repository.starredEmails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistoricalEmails: StateFlow<List<EmailEntity>> = repository.allHistoricalEmails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalEmailsCount: StateFlow<Int> = repository.totalReceivedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _oldMailboxFilter = MutableStateFlow<String?>(null)
    val oldMailboxFilter = _oldMailboxFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Inbox)
    val currentScreen = _currentScreen.asStateFlow()

    private val _selectedEmail = MutableStateFlow<EmailEntity?>(null)
    val selectedEmail = _selectedEmail.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _autoRefreshEnabled = MutableStateFlow(true)
    val autoRefreshEnabled = _autoRefreshEnabled.asStateFlow()

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    // Dialog flags
    private val _showCustomAddressDialog = MutableStateFlow(false)
    val showCustomAddressDialog = _showCustomAddressDialog.asStateFlow()

    private val _showSimulateDialog = MutableStateFlow(false)
    val showSimulateDialog = _showSimulateDialog.asStateFlow()

    private val _showSendCustomDialog = MutableStateFlow(false)
    val showSendCustomDialog = _showSendCustomDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog = _showQrDialog.asStateFlow()

    private val _showBurnConfirmDialog = MutableStateFlow(false)
    val showBurnConfirmDialog = _showBurnConfirmDialog.asStateFlow()

    private var autoRefreshJob: Job? = null

    val emailsForActiveMailbox: StateFlow<List<EmailEntity>> = activeMailbox
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.address == new.address }
        .flatMapLatest { mailbox ->
            repository.getEmailsForAddress(mailbox.address)
        }
        .combine(_searchQuery) { emails, query ->
            if (query.isBlank()) {
                emails
            } else {
                emails.filter {
                    it.subject.contains(query, ignoreCase = true) ||
                    it.senderName.contains(query, ignoreCase = true) ||
                    it.senderEmail.contains(query, ignoreCase = true) ||
                    it.previewText.contains(query, ignoreCase = true) ||
                    (it.extractedOtp?.contains(query, ignoreCase = true) == true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredOldEmails: StateFlow<List<EmailEntity>> = repository.allHistoricalEmails
        .combine(_oldMailboxFilter) { list, filter ->
            if (filter == null) list else list.filter { it.mailboxAddress == filter }
        }
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.subject.contains(query, ignoreCase = true) ||
                    it.senderName.contains(query, ignoreCase = true) ||
                    it.senderEmail.contains(query, ignoreCase = true) ||
                    it.previewText.contains(query, ignoreCase = true) ||
                    it.mailboxAddress.contains(query, ignoreCase = true) ||
                    (it.extractedOtp?.contains(query, ignoreCase = true) == true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setOldMailFilter(address: String?) {
        _oldMailboxFilter.value = address
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultIfNeeded()
        }
        startBackgroundPolling()
    }

    private fun startBackgroundPolling() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(10000L)
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun openEmailDetail(email: EmailEntity) {
        _selectedEmail.value = email
        _currentScreen.value = ScreenDestination.EmailDetail(email.id)
        viewModelScope.launch {
            repository.markEmailAsRead(email.id)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAutoRefresh() {
        _autoRefreshEnabled.value = !_autoRefreshEnabled.value
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(600)
            _isRefreshing.value = false
            _uiEvents.emit("Inbox refreshed")
        }
    }

    fun generateNewRandomAddress() {
        viewModelScope.launch {
            val created = repository.createNewMailbox()
            _uiEvents.emit("New address created: ${created.address}")
        }
    }

    fun createCustomAddress(username: String) {
        viewModelScope.launch {
            val created = repository.createNewMailbox(username)
            _showCustomAddressDialog.value = false
            _uiEvents.emit("Switched to ${created.address}")
        }
    }

    fun switchToMailbox(address: String) {
        viewModelScope.launch {
            repository.switchToMailbox(address)
            _currentScreen.value = ScreenDestination.Inbox
            _uiEvents.emit("Active: $address")
        }
    }

    fun burnActiveMailbox() {
        val address = activeMailbox.value?.address ?: return
        viewModelScope.launch {
            repository.burnMailbox(address)
            _showBurnConfirmDialog.value = false
            _uiEvents.emit("Mailbox burned & wiped permanently")
        }
    }

    fun toggleStar(email: EmailEntity) {
        viewModelScope.launch {
            val newStarred = !email.isStarred
            repository.toggleStarEmail(email.id, newStarred)
            if (_selectedEmail.value?.id == email.id) {
                _selectedEmail.value = _selectedEmail.value?.copy(isStarred = newStarred)
            }
            val msg = if (newStarred) "Saved to Starred" else "Removed from Starred"
            _uiEvents.emit(msg)
        }
    }

    fun deleteEmail(id: Long) {
        viewModelScope.launch {
            repository.deleteEmail(id)
            if (_currentScreen.value is ScreenDestination.EmailDetail) {
                _currentScreen.value = ScreenDestination.Inbox
            }
            _uiEvents.emit("Email deleted")
        }
    }

    fun deleteAllEmailsForActive() {
        val address = activeMailbox.value?.address ?: return
        viewModelScope.launch {
            repository.deleteAllEmails(address)
            _uiEvents.emit("All emails cleared for $address")
        }
    }

    fun triggerSimulation(template: EmailGenerator.Template? = null) {
        val address = activeMailbox.value?.address ?: return
        viewModelScope.launch {
            val email = repository.simulateIncomingEmail(address, template)
            _showSimulateDialog.value = false
            _uiEvents.emit("📬 New mail arrived from ${email.senderName}!")
        }
    }

    fun sendCustomTestMail(sender: String, email: String, subject: String, body: String) {
        val address = activeMailbox.value?.address ?: return
        viewModelScope.launch {
            val newMail = repository.sendCustomTestEmail(address, sender, email, subject, body)
            _showSendCustomDialog.value = false
            _uiEvents.emit("📬 Test email delivered from ${newMail.senderName}")
        }
    }

    // Dialog toggles
    fun setShowCustomAddressDialog(show: Boolean) { _showCustomAddressDialog.value = show }
    fun setShowSimulateDialog(show: Boolean) { _showSimulateDialog.value = show }
    fun setShowSendCustomDialog(show: Boolean) { _showSendCustomDialog.value = show }
    fun setShowQrDialog(show: Boolean) { _showQrDialog.value = show }
    fun setShowBurnConfirmDialog(show: Boolean) { _showBurnConfirmDialog.value = show }
}
