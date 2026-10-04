package com.example.data

import com.example.util.EmailGenerator
import com.example.util.OtpExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class MailRepository(
    private val mailboxDao: MailboxDao,
    private val emailDao: EmailDao
) {
    val activeMailbox: Flow<MailboxEntity?> = mailboxDao.getActiveMailbox().distinctUntilChanged()
    val allMailboxes: Flow<List<MailboxEntity>> = mailboxDao.getAllMailboxes()
    val starredEmails: Flow<List<EmailEntity>> = emailDao.getStarredEmails()
    val allHistoricalEmails: Flow<List<EmailEntity>> = emailDao.getAllHistoricalEmails()
    val totalReceivedCount: Flow<Int> = emailDao.getTotalReceivedCount()

    fun getEmailsForAddress(address: String): Flow<List<EmailEntity>> {
        return emailDao.getEmailsForMailbox(address)
    }

    fun getUnreadCount(address: String): Flow<Int> {
        return emailDao.getUnreadCount(address)
    }

    suspend fun initializeDefaultIfNeeded() = withContext(Dispatchers.IO) {
        val current = mailboxDao.getActiveMailboxDirect()
        if (current == null) {
            val newAddress = EmailGenerator.generateRandomAddress()
            val initialMailbox = MailboxEntity(
                address = newAddress,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 10 * 60 * 1000L,
                isActive = true
            )
            mailboxDao.insertMailbox(initialMailbox)

            // Seed welcome email
            val welcomeEmail = EmailGenerator.getWelcomeEmail(newAddress)
            emailDao.insertEmail(welcomeEmail)
        }
    }

    suspend fun createNewMailbox(customUsername: String? = null): MailboxEntity = withContext(Dispatchers.IO) {
        val address = if (!customUsername.isNullOrBlank()) {
            EmailGenerator.createAddress(customUsername)
        } else {
            EmailGenerator.generateRandomAddress()
        }

        mailboxDao.deactivateAllMailboxes()
        val newMailbox = MailboxEntity(
            address = address,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 10 * 60 * 1000L,
            isActive = true
        )
        mailboxDao.insertMailbox(newMailbox)

        // Seed welcome email for new inbox
        val welcome = EmailGenerator.getWelcomeEmail(address)
        emailDao.insertEmail(welcome)

        newMailbox
    }

    suspend fun switchToMailbox(address: String) = withContext(Dispatchers.IO) {
        mailboxDao.deactivateAllMailboxes()
        mailboxDao.setActiveMailbox(address)
    }

    suspend fun extendTimer(address: String, additionalMinutes: Int = 10) = withContext(Dispatchers.IO) {
        val mailbox = mailboxDao.getMailboxByAddress(address) ?: return@withContext
        val baseTime = maxOf(System.currentTimeMillis(), mailbox.expiresAt)
        val newExpiry = baseTime + additionalMinutes * 60 * 1000L
        mailboxDao.updateExpiresAt(address, newExpiry)
    }

    suspend fun resetTimerToTenMinutes(address: String) = withContext(Dispatchers.IO) {
        val newExpiry = System.currentTimeMillis() + 10 * 60 * 1000L
        mailboxDao.updateExpiresAt(address, newExpiry)
    }

    suspend fun burnMailbox(address: String) = withContext(Dispatchers.IO) {
        emailDao.deleteAllEmailsForMailbox(address)
        mailboxDao.deleteMailbox(address)

        // If active was deleted, pick another or make a new one
        val remaining = mailboxDao.getAllMailboxes().firstOrNull()
        if (!remaining.isNullOrEmpty()) {
            mailboxDao.setActiveMailbox(remaining.first().address)
        } else {
            createNewMailbox()
        }
    }

    suspend fun markEmailAsRead(id: Long) = withContext(Dispatchers.IO) {
        emailDao.markAsRead(id)
    }

    suspend fun toggleStarEmail(id: Long, isStarred: Boolean) = withContext(Dispatchers.IO) {
        emailDao.toggleStar(id, isStarred)
    }

    suspend fun deleteEmail(id: Long) = withContext(Dispatchers.IO) {
        emailDao.deleteEmail(id)
    }

    suspend fun deleteAllEmails(address: String) = withContext(Dispatchers.IO) {
        emailDao.deleteAllEmailsForMailbox(address)
    }

    suspend fun simulateIncomingEmail(
        address: String,
        template: EmailGenerator.Template? = null
    ): EmailEntity = withContext(Dispatchers.IO) {
        val tmpl = template ?: EmailGenerator.getRandomSimulationTemplate(address)
        val otp = tmpl.otp ?: OtpExtractor.extractOtp(tmpl.bodyText) ?: OtpExtractor.extractOtp(tmpl.subject)
        val link = tmpl.link ?: OtpExtractor.extractActionLink(tmpl.bodyHtml)

        val email = EmailEntity(
            mailboxAddress = address,
            senderName = tmpl.serviceName,
            senderEmail = tmpl.senderEmail,
            subject = tmpl.subject,
            previewText = tmpl.preview,
            bodyHtml = tmpl.bodyHtml,
            bodyText = tmpl.bodyText,
            receivedAt = System.currentTimeMillis(),
            isRead = false,
            isStarred = false,
            extractedOtp = otp,
            extractedLink = link,
            serviceCategory = tmpl.category
        )
        val id = emailDao.insertEmail(email)
        email.copy(id = id)
    }

    suspend fun sendCustomTestEmail(
        address: String,
        senderName: String,
        senderEmail: String,
        subject: String,
        body: String
    ): EmailEntity = withContext(Dispatchers.IO) {
        val otp = OtpExtractor.extractOtp(body) ?: OtpExtractor.extractOtp(subject)
        val link = OtpExtractor.extractActionLink(body)

        val email = EmailEntity(
            mailboxAddress = address,
            senderName = senderName.ifBlank { "Test Sender" },
            senderEmail = senderEmail.ifBlank { "test@example.com" },
            subject = subject.ifBlank { "Test message to $address" },
            previewText = body.take(90).replace("\n", " "),
            bodyHtml = "<div style=\"font-family:sans-serif;padding:12px;\"><p>${body.replace("\n", "<br/>")}</p></div>",
            bodyText = body,
            receivedAt = System.currentTimeMillis(),
            isRead = false,
            isStarred = false,
            extractedOtp = otp,
            extractedLink = link,
            serviceCategory = "Custom"
        )
        val id = emailDao.insertEmail(email)
        email.copy(id = id)
    }
}
