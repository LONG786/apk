package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GmailBlue
import com.example.util.EmailGenerator

@Composable
fun CustomAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom @gmail10p.com Address",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pick a custom username for your 10-minute temporary mailbox. Letters, numbers, dots, and hyphens allowed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.lowercase().replace(" ", "")
                        errorText = null
                    },
                    label = { Text("Username") },
                    trailingIcon = {
                        Text(
                            text = "@gmail10p.com",
                            style = MaterialTheme.typography.labelMedium,
                            color = GmailBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AlternateEmail,
                            contentDescription = "Email icon"
                        )
                    },
                    isError = errorText != null,
                    supportingText = {
                        if (errorText != null) {
                            Text(text = errorText ?: "")
                        } else {
                            Text("Full: ${username.ifEmpty { "username" }}@gmail10p.com")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_username_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = username.trim().lowercase().removeSuffix("@gmail10p.com")
                    if (clean.length < 3) {
                        errorText = "Must be at least 3 characters"
                    } else if (!clean.matches(Regex("^[a-z0-9._-]+$"))) {
                        errorText = "Only letters, numbers, dot, underscore, dash"
                    } else {
                        onConfirm(clean)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GmailBlue),
                modifier = Modifier.testTag("confirm_custom_address_button")
            ) {
                Text("Create Mailbox")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
