package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mailboxAddress: String,
    val senderName: String,
    val senderEmail: String,
    val subject: String,
    val previewText: String,
    val bodyHtml: String,
    val bodyText: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val extractedOtp: String? = null,
    val extractedLink: String? = null,
    val serviceCategory: String = "Verification"
)
