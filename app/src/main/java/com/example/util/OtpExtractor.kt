package com.example.util

import java.util.regex.Pattern

object OtpExtractor {

    private val OTP_PATTERNS = listOf(
        // "code is 123456", "OTP: 123456", "verification code: 123456"
        Pattern.compile("(?i)(?:verification|security|confirm|activation|login|one-time|otp)?\\s*(?:code|otp|pin|passcode|token)[\\s:=#-]+([A-Za-z0-9]{4,8})\\b"),
        // "G-123456"
        Pattern.compile("\\b([Gg]-\\d{6})\\b"),
        // "123-456" or "1234-5678"
        Pattern.compile("\\b(\\d{3}-\\d{3}|[A-Za-z0-9]{4}-[A-Za-z0-9]{4})\\b"),
        // Generic 6 digits surrounded by spaces or punctuation
        Pattern.compile("(?<!\\d)(\\d{6})(?!\\d)"),
        // Generic 4-5 digits
        Pattern.compile("(?<!\\d)(\\d{4,5})(?!\\d)")
    )

    private val LINK_PATTERN = Pattern.compile("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+")

    fun extractOtp(text: String?): String? {
        if (text.isNullOrBlank()) return null
        for (pattern in OTP_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val groupCount = matcher.groupCount()
                val candidate = if (groupCount >= 1) matcher.group(1) else matcher.group(0)
                if (!candidate.isNullOrBlank() && candidate.length in 4..10) {
                    return candidate.trim()
                }
            }
        }
        return null
    }

    fun extractActionLink(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val matcher = LINK_PATTERN.matcher(text)
        var firstValidUrl: String? = null
        while (matcher.find()) {
            val url = matcher.group(0) ?: continue
            val lower = url.lowercase()
            if (lower.contains("verify") || lower.contains("confirm") || 
                lower.contains("activate") || lower.contains("validate") || 
                lower.contains("token") || lower.contains("auth")) {
                return url
            }
            if (firstValidUrl == null && !lower.contains("unsubscribe") && !lower.contains("privacy")) {
                firstValidUrl = url
            }
        }
        return firstValidUrl
    }
}
