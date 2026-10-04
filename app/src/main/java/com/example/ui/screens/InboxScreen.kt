package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EmailEntity
import com.example.data.MailboxEntity
import com.example.ui.components.EmailItemCard
import com.example.ui.components.TimerBanner
import com.example.ui.theme.GmailBlue
import com.example.ui.theme.GmailGreen
import com.example.ui.theme.GmailRed

@Composable
fun InboxScreen(
    mailbox: MailboxEntity?,
    emails: List<EmailEntity>,
    searchQuery: String,
    isRefreshing: Boolean,
    autoRefreshEnabled: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onNewRandom: () -> Unit,
    onCustomAddress: () -> Unit,
    onShowQr: () -> Unit,
    onBurnMailbox: () -> Unit,
    onManualRefresh: () -> Unit,
    onToggleAutoRefresh: () -> Unit,
    onSimulateMail: () -> Unit,
    onSendTestMail: () -> Unit,
    onClearAllEmails: () -> Unit,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleStar: (EmailEntity) -> Unit,
    onViewOldMail: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val address = mailbox?.address ?: "loading@gmail10p.com"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("inbox_screen_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Address & Action Banner
        item {
            TimerBanner(
                mailbox = mailbox,
                onNewRandom = onNewRandom,
                onCustomAddress = onCustomAddress,
                onShowQr = onShowQr,
                onBurn = onBurnMailbox,
                onViewOldMail = onViewOldMail
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search emails, subjects, or OTP codes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("email_search_field")
            )
        }

        // Action Quick Bar (Simulate, Send Test, Auto-Refresh, Clear)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulate Mail Chip
                AssistChip(
                    onClick = onSimulateMail,
                    label = { Text("Simulate Mail", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("simulate_mail_chip")
                )

                // Send Test Mail Chip
                AssistChip(
                    onClick = onSendTestMail,
                    label = { Text("Send Test") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = GmailBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("send_test_mail_chip")
                )

                // Manual Refresh Chip
                AssistChip(
                    onClick = onManualRefresh,
                    label = {
                        if (isRefreshing) {
                            Text("Refreshing...")
                        } else {
                            Text("Refresh")
                        }
                    },
                    leadingIcon = {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("manual_refresh_chip")
                )

                // Auto Refresh Toggle
                FilterChip(
                    selected = autoRefreshEnabled,
                    onClick = onToggleAutoRefresh,
                    label = {
                        Text(if (autoRefreshEnabled) "Auto-check: ON" else "Auto-check: OFF")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (autoRefreshEnabled) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("auto_refresh_chip")
                )

                if (emails.isNotEmpty()) {
                    AssistChip(
                        onClick = onClearAllEmails,
                        label = { Text("Clear All") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = GmailRed,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("clear_all_chip")
                    )
                }
            }
        }

        // Section Title with count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INBOX (${emails.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                if (autoRefreshEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GmailGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = GmailGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Empty State or Email List
        if (emails.isEmpty()) {
            item {
                EmptyInboxCard(
                    address = address,
                    onSimulate = onSimulateMail,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Disposable Email", address)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied: $address", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            items(emails, key = { it.id }) { email ->
                EmailItemCard(
                    email = email,
                    onClick = { onEmailClick(email) },
                    onToggleStar = { onToggleStar(email) }
                )
            }
        }
    }
}

@Composable
private fun EmptyInboxCard(
    address: String,
    onSimulate: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = GmailBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Empty Mailbox",
                        tint = GmailBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Waiting for incoming emails...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use your disposable address on any website or app to receive verification codes and avoid spam.\n\n$address",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCopy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_inbox_copy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }

                OutlinedButton(
                    onClick = onSimulate,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate")
                }
            }
        }
    }
}
