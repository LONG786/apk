package com.example

import com.example.util.EmailGenerator
import com.example.util.OtpExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testEmailDomainMatchesGmail10p() {
        val address = EmailGenerator.generateRandomAddress()
        assertTrue(address.endsWith("@gmail10p.com"))
    }

    @Test
    fun testCustomAddressCreation() {
        val address = EmailGenerator.createAddress("privacy.hero")
        assertEquals("privacy.hero@gmail10p.com", address)
    }

    @Test
    fun testOtpExtractorNumeric() {
        val emailBody = "Your verification code is 849201. Please enter it within 10 minutes."
        val otp = OtpExtractor.extractOtp(emailBody)
        assertEquals("849201", otp)
    }

    @Test
    fun testOtpExtractorGoogleFormat() {
        val emailBody = "G-948123 is your Google verification code."
        val otp = OtpExtractor.extractOtp(emailBody)
        assertEquals("G-948123", otp)
    }

    @Test
    fun testLinkExtractor() {
        val emailBody = "Please confirm by visiting https://service.com/verify?token=abc123xyz"
        val link = OtpExtractor.extractActionLink(emailBody)
        assertNotNull(link)
        assertTrue(link!!.contains("verify"))
    }
}
