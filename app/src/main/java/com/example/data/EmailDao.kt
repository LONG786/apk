package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails WHERE mailboxAddress = :mailboxAddress ORDER BY receivedAt DESC")
    fun getEmailsForMailbox(mailboxAddress: String): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails ORDER BY receivedAt DESC")
    fun getAllHistoricalEmails(): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE isStarred = 1 ORDER BY receivedAt DESC")
    fun getStarredEmails(): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE id = :id LIMIT 1")
    suspend fun getEmailById(id: Long): EmailEntity?

    @Query("SELECT COUNT(*) FROM emails WHERE mailboxAddress = :mailboxAddress AND isRead = 0")
    fun getUnreadCount(mailboxAddress: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM emails")
    fun getTotalReceivedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity): Long

    @Update
    suspend fun updateEmail(email: EmailEntity)

    @Query("UPDATE emails SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE emails SET isStarred = :isStarred WHERE id = :id")
    suspend fun toggleStar(id: Long, isStarred: Boolean)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmail(id: Long)

    @Query("DELETE FROM emails WHERE mailboxAddress = :mailboxAddress")
    suspend fun deleteAllEmailsForMailbox(mailboxAddress: String)

    @Query("SELECT COUNT(*) FROM emails WHERE mailboxAddress = :mailboxAddress")
    suspend fun getEmailCountForMailbox(mailboxAddress: String): Int
}
