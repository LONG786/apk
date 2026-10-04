package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mailboxes")
data class MailboxEntity(
    @PrimaryKey val address: String, // e.g., "fast.coder.99@gmail10p.com"
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 10 * 60 * 1000L, // 10 minutes from creation
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val aliasLabel: String = ""
)
