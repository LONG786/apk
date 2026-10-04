package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MailboxDao {
    @Query("SELECT * FROM mailboxes ORDER BY isActive DESC, createdAt DESC")
    fun getAllMailboxes(): Flow<List<MailboxEntity>>

    @Query("SELECT * FROM mailboxes WHERE isActive = 1 LIMIT 1")
    fun getActiveMailbox(): Flow<MailboxEntity?>

    @Query("SELECT * FROM mailboxes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveMailboxDirect(): MailboxEntity?

    @Query("SELECT * FROM mailboxes WHERE address = :address LIMIT 1")
    suspend fun getMailboxByAddress(address: String): MailboxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMailbox(mailbox: MailboxEntity)

    @Update
    suspend fun updateMailbox(mailbox: MailboxEntity)

    @Query("UPDATE mailboxes SET isActive = 0")
    suspend fun deactivateAllMailboxes()

    @Query("UPDATE mailboxes SET isActive = 1 WHERE address = :address")
    suspend fun setActiveMailbox(address: String)

    @Query("UPDATE mailboxes SET expiresAt = :newExpiresAt WHERE address = :address")
    suspend fun updateExpiresAt(address: String, newExpiresAt: Long)

    @Query("DELETE FROM mailboxes WHERE address = :address")
    suspend fun deleteMailbox(address: String)

    @Query("SELECT COUNT(*) FROM mailboxes")
    suspend fun getMailboxCount(): Int
}
