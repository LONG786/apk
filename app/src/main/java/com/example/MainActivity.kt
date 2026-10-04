package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.dialogs.BurnConfirmDialog
import com.example.ui.dialogs.CustomAddressDialog
import com.example.ui.dialogs.QrCodeDialog
import com.example.ui.dialogs.SendCustomMailDialog
import com.example.ui.dialogs.SimulateMailDialog
import com.example.ui.screens.EmailDetailScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.MailboxesScreen
import com.example.ui.screens.PrivacyShieldScreen
import com.example.ui.screens.SavedOldMailScreen
import com.example.ui.theme.GmailBlue
import com.example.ui.theme.GmailGreen
import com.example.ui.theme.GmailRed
import com.example.ui.theme.GmailYellow
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Gmail10pApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gmail10pApp(viewModel: MainViewModel) {
    val activeMailbox by viewModel.activeMailbox.collectAsStateWithLifecycle()
    val allMailboxes by viewModel.allMailboxes.collectAsStateWithLifecycle()
    val emails by viewModel.emailsForActiveMailbox.collectAsStateWithLifecycle()
    val starredEmails by viewModel.starredEmails.collectAsStateWithLifecycle()
    val filteredOldEmails by viewModel.filteredOldEmails.collectAsStateWithLifecycle()
    val oldMailboxFilter by viewModel.oldMailboxFilter.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalEmailsCount.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedEmail by viewModel.selectedEmail.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val autoRefreshEnabled by viewModel.autoRefreshEnabled.collectAsStateWithLifecycle()

    // Dialog states
    val showCustomAddressDialog by viewModel.showCustomAddressDialog.collectAsStateWithLifecycle()
    val showSimulateDialog by viewModel.showSimulateDialog.collectAsStateWithLifecycle()
    val showSendCustomDialog by viewModel.showSendCustomDialog.collectAsStateWithLifecycle()
    val showQrDialog by viewModel.showQrDialog.collectAsStateWithLifecycle()
    val showBurnConfirmDialog by viewModel.showBurnConfirmDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back button for screens
    if (currentScreen !is ScreenDestination.Inbox) {
        BackHandler {
            viewModel.navigateTo(ScreenDestination.Inbox)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen !is ScreenDestination.EmailDetail) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GmailRed,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gmail10p",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GmailBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "@gmail10p.com",
                                    color = GmailBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setShowSimulateDialog(true) },
                            modifier = Modifier.testTag("top_bar_simulate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Simulate mail",
                                tint = Color(0xFFF59E0B)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.manualRefresh() },
                            modifier = Modifier.testTag("top_bar_refresh_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh inbox"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen !is ScreenDestination.EmailDetail) {
                val unreadEmails = emails.count { !it.isRead }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // Inbox tab
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Inbox,
                        onClick = { viewModel.navigateTo(ScreenDestination.Inbox) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadEmails > 0) {
                                        Badge { Text(unreadEmails.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen is ScreenDestination.Inbox) Icons.Filled.Email else Icons.Outlined.Email,
                                    contentDescription = "Inbox"
                                )
                            }
                        },
                        label = { Text("Inbox") },
                        modifier = Modifier.testTag("nav_inbox")
                    )

                    // Saved & Old Mail tab
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Starred,
                        onClick = { viewModel.navigateTo(ScreenDestination.Starred) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (starredEmails.isNotEmpty()) {
                                        Badge { Text(starredEmails.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen is ScreenDestination.Starred) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = "Saved and Old Mail"
                                )
                            }
                        },
                        label = { Text("Saved & Old") },
                        modifier = Modifier.testTag("nav_saved_old")
                    )

                    // Mailboxes tab
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.Mailboxes,
                        onClick = { viewModel.navigateTo(ScreenDestination.Mailboxes) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.Mailboxes) Icons.Filled.AlternateEmail else Icons.Outlined.AlternateEmail,
                                contentDescription = "Mailboxes"
                            )
                        },
                        label = { Text("Aliases") },
                        modifier = Modifier.testTag("nav_mailboxes")
                    )

                    // Privacy Shield tab
                    NavigationBarItem(
                        selected = currentScreen is ScreenDestination.PrivacyShield,
                        onClick = { viewModel.navigateTo(ScreenDestination.PrivacyShield) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen is ScreenDestination.PrivacyShield) Icons.Filled.Shield else Icons.Outlined.Shield,
                                contentDescription = "Privacy Shield"
                            )
                        },
                        label = { Text("Shield") },
                        modifier = Modifier.testTag("nav_shield")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is ScreenDestination.Inbox -> {
                    InboxScreen(
                        mailbox = activeMailbox,
                        emails = emails,
                        searchQuery = searchQuery,
                        isRefreshing = isRefreshing,
                        autoRefreshEnabled = autoRefreshEnabled,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onNewRandom = { viewModel.generateNewRandomAddress() },
                        onCustomAddress = { viewModel.setShowCustomAddressDialog(true) },
                        onShowQr = { viewModel.setShowQrDialog(true) },
                        onBurnMailbox = { viewModel.setShowBurnConfirmDialog(true) },
                        onManualRefresh = { viewModel.manualRefresh() },
                        onToggleAutoRefresh = { viewModel.toggleAutoRefresh() },
                        onSimulateMail = { viewModel.setShowSimulateDialog(true) },
                        onSendTestMail = { viewModel.setShowSendCustomDialog(true) },
                        onClearAllEmails = { viewModel.deleteAllEmailsForActive() },
                        onEmailClick = { viewModel.openEmailDetail(it) },
                        onToggleStar = { viewModel.toggleStar(it) },
                        onViewOldMail = { viewModel.navigateTo(ScreenDestination.Starred) }
                    )
                }

                is ScreenDestination.Starred -> {
                    SavedOldMailScreen(
                        starredEmails = starredEmails,
                        historicalEmails = filteredOldEmails,
                        allMailboxes = allMailboxes,
                        selectedFilterAddress = oldMailboxFilter,
                        onSelectFilterAddress = { viewModel.setOldMailFilter(it) },
                        onEmailClick = { viewModel.openEmailDetail(it) },
                        onToggleStar = { viewModel.toggleStar(it) },
                        onSwitchMailbox = { viewModel.switchToMailbox(it) }
                    )
                }

                is ScreenDestination.Mailboxes -> {
                    MailboxesScreen(
                        mailboxes = allMailboxes,
                        onSelectMailbox = { viewModel.switchToMailbox(it) },
                        onNewRandom = { viewModel.generateNewRandomAddress() },
                        onCustomAddress = { viewModel.setShowCustomAddressDialog(true) },
                        onBurnMailbox = { viewModel.burnActiveMailbox() }
                    )
                }

                is ScreenDestination.PrivacyShield -> {
                    PrivacyShieldScreen(
                        totalEmailsReceived = totalCount,
                        autoRefreshEnabled = autoRefreshEnabled,
                        onToggleAutoRefresh = { viewModel.toggleAutoRefresh() }
                    )
                }

                is ScreenDestination.EmailDetail -> {
                    EmailDetailScreen(
                        email = selectedEmail,
                        onBack = { viewModel.navigateTo(ScreenDestination.Inbox) },
                        onToggleStar = { viewModel.toggleStar(it) },
                        onDelete = { viewModel.deleteEmail(it) }
                    )
                }
            }
        }
    }

    // Dialogs
    val activeAddress = activeMailbox?.address ?: "loading@gmail10p.com"

    if (showCustomAddressDialog) {
        CustomAddressDialog(
            onDismiss = { viewModel.setShowCustomAddressDialog(false) },
            onConfirm = { username -> viewModel.createCustomAddress(username) }
        )
    }

    if (showSimulateDialog) {
        SimulateMailDialog(
            activeAddress = activeAddress,
            onDismiss = { viewModel.setShowSimulateDialog(false) },
            onSelectTemplate = { template -> viewModel.triggerSimulation(template) }
        )
    }

    if (showSendCustomDialog) {
        SendCustomMailDialog(
            activeAddress = activeAddress,
            onDismiss = { viewModel.setShowSendCustomDialog(false) },
            onSend = { sender, email, subject, body ->
                viewModel.sendCustomTestMail(sender, email, subject, body)
            }
        )
    }

    if (showQrDialog) {
        QrCodeDialog(
            emailAddress = activeAddress,
            onDismiss = { viewModel.setShowQrDialog(false) }
        )
    }

    if (showBurnConfirmDialog) {
        BurnConfirmDialog(
            address = activeAddress,
            onDismiss = { viewModel.setShowBurnConfirmDialog(false) },
            onConfirmBurn = { viewModel.burnActiveMailbox() }
        )
    }
}
