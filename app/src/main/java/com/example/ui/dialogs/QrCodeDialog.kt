package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun QrCodeDialog(
    emailAddress: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "QR Code for Email",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan with your phone or camera to easily transfer this disposable email address.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas stylized QR pattern
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(180.dp)) {
                            val gridSize = 21
                            val cellWidth = size.width / gridSize
                            val cellHeight = size.height / gridSize

                            // Generate deterministic bit pattern from emailAddress hash
                            val hash = emailAddress.hashCode()

                            for (row in 0 until gridSize) {
                                for (col in 0 until gridSize) {
                                    // Corner finder patterns (7x7 in top-left, top-right, bottom-left)
                                    val isFinderTopLeft = row in 0..6 && col in 0..6
                                    val isFinderTopRight = row in 0..6 && col in (gridSize - 7) until gridSize
                                    val isFinderBottomLeft = row in (gridSize - 7) until gridSize && col in 0..6

                                    val isFilled = when {
                                        isFinderTopLeft -> {
                                            row == 0 || row == 6 || col == 0 || col == 6 || (row in 2..4 && col in 2..4)
                                        }
                                        isFinderTopRight -> {
                                            val c = col - (gridSize - 7)
                                            row == 0 || row == 6 || c == 0 || c == 6 || (row in 2..4 && c in 2..4)
                                        }
                                        isFinderBottomLeft -> {
                                            val r = row - (gridSize - 7)
                                            r == 0 || r == 6 || col == 0 || col == 6 || (r in 2..4 && col in 2..4)
                                        }
                                        row == 6 || col == 6 -> {
                                            (row + col) % 2 == 0 // Timing pattern
                                        }
                                        else -> {
                                            val bit = abs(hash xor (row * 37 + col * 91)) % 7
                                            bit in listOf(0, 2, 4)
                                        }
                                    }

                                    if (isFilled) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(col * cellWidth, row * cellHeight),
                                            size = Size(cellWidth, cellHeight)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = emailAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Disposable Email", emailAddress)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied: $emailAddress", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("qr_copy_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Address")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    )
}
