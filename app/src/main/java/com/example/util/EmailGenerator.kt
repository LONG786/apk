package com.example.util

import com.example.data.EmailEntity
import kotlin.random.Random

object EmailGenerator {

    const val DOMAIN = "gmail10p.com"

    private val ADJECTIVES = listOf(
        "swift", "cyber", "stealth", "nova", "silent", "pixel",
        "echo", "spark", "frost", "shadow", "clean", "hyper",
        "alpha", "zen", "vivid", "rapid", "turbo", "quantum",
        "shield", "cosmic", "solar", "mystic", "prime"
    )

    private val NOUNS = listOf(
        "fox", "coder", "pilot", "ninja", "wolf", "hawk",
        "spark", "guard", "wave", "ghost", "pulse", "vault",
        "runner", "falcon", "badger", "beacon", "scout", "ranger"
    )

    fun generateRandomAddress(): String {
        val adj = ADJECTIVES.random()
        val noun = NOUNS.random()
        val num = Random.nextInt(10, 999)
        return "$adj.$noun.$num@$DOMAIN"
    }

    fun isValidUsername(username: String): Boolean {
        val clean = username.trim().lowercase()
        return clean.matches(Regex("^[a-z0-9._-]{3,30}$"))
    }

    fun createAddress(username: String): String {
        val clean = username.trim().lowercase().removeSuffix("@$DOMAIN")
        return "$clean@$DOMAIN"
    }

    data class Template(
        val serviceName: String,
        val senderEmail: String,
        val subject: String,
        val preview: String,
        val bodyHtml: String,
        val bodyText: String,
        val otp: String?,
        val link: String?,
        val category: String
    )

    fun getRandomSimulationTemplate(mailboxAddress: String): Template {
        val templates = getSimulationTemplates(mailboxAddress)
        return templates.random()
    }

    fun getSimulationTemplates(mailboxAddress: String): List<Template> {
        val code6 = (100000 + Random.nextInt(900000)).toString()
        val code4 = (1000 + Random.nextInt(9000)).toString()
        val steamCode = (10000..99999).random().toString(36).uppercase()

        return listOf(
            Template(
                serviceName = "GitHub",
                senderEmail = "noreply@github.com",
                subject = "[$code6] Your GitHub verification code",
                preview = "Here is your verification code to complete sign-in: $code6",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#24292e;">GitHub Authentication</h2>
                        <p>We received a sign-in or verification request for your account associated with <b>$mailboxAddress</b>.</p>
                        <div style="background:#f6f8fa;padding:16px;border-radius:8px;text-align:center;margin:20px 0;">
                            <span style="font-size:32px;font-weight:bold;letter-spacing:4px;color:#0969da;">$code6</span>
                        </div>
                        <p>This code expires in 10 minutes. If you did not make this request, you can safely ignore this email.</p>
                    </div>
                """.trimIndent(),
                bodyText = "GitHub Verification Code\n\nYour one-time code is: $code6\n\nThis code will expire in 10 minutes. Please enter it to complete your authorization.",
                otp = code6,
                link = "https://github.com/login/verify?otp=$code6",
                category = "Developer"
            ),
            Template(
                serviceName = "Netflix",
                senderEmail = "info@mailer.netflix.com",
                subject = "Your Netflix temporary sign-in code: $code6",
                preview = "Use code $code6 to finish setting up your profile.",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#E50914;">Netflix Sign-In Code</h2>
                        <p>Hi there,</p>
                        <p>Please enter the following 6-digit code to continue your registration on Netflix:</p>
                        <div style="background:#141414;color:#ffffff;padding:16px;border-radius:8px;text-align:center;margin:20px 0;">
                            <span style="font-size:32px;font-weight:bold;letter-spacing:6px;color:#E50914;">$code6</span>
                        </div>
                        <p>Need help? Visit the Netflix Help Center.</p>
                    </div>
                """.trimIndent(),
                bodyText = "Netflix Security Code\n\nUse this code to sign in: $code6\n\nCode expires in 15 minutes.",
                otp = code6,
                link = "https://netflix.com/activate",
                category = "Entertainment"
            ),
            Template(
                serviceName = "Discord",
                senderEmail = "noreply@discord.com",
                subject = "Verify Email Address for Discord",
                preview = "Welcome to Discord! Please verify your email: $mailboxAddress",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#5865F2;">Hey Discord Gamer!</h2>
                        <p>Thanks for registering for an account on Discord! Before we get started, please confirm your email address.</p>
                        <div style="text-align:center;margin:24px 0;">
                            <a href="https://discord.com/verify?token=$code6" style="background:#5865F2;color:#ffffff;padding:12px 24px;border-radius:4px;text-decoration:none;font-weight:bold;">Verify Email</a>
                        </div>
                        <p>Your one-time backup PIN is: <b>$code6</b></p>
                    </div>
                """.trimIndent(),
                bodyText = "Discord Account Verification\n\nPlease verify your email by clicking: https://discord.com/verify?token=$code6\n\nOr enter verification code: $code6",
                otp = code6,
                link = "https://discord.com/verify?token=$code6",
                category = "Social"
            ),
            Template(
                serviceName = "Google Accounts",
                senderEmail = "no-reply@accounts.google.com",
                subject = "G-$code6 is your Google verification code",
                preview = "Google received a request to verify your email address.",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#1a73e8;">Google Verification Code</h2>
                        <p>Google has received a request to use this email address with a Google Account.</p>
                        <div style="background:#f8f9fa;padding:16px;border-radius:8px;text-align:center;margin:20px 0;">
                            <span style="font-size:28px;font-weight:bold;color:#202124;">G-$code6</span>
                        </div>
                        <p>If you didn't ask for this code, someone may be trying to access your account.</p>
                    </div>
                """.trimIndent(),
                bodyText = "Google Verification Code\n\nG-$code6 is your verification code.\n\nDon't share this code with anyone.",
                otp = "G-$code6",
                link = null,
                category = "Security"
            ),
            Template(
                serviceName = "Steam Guard",
                senderEmail = "noreply@steampowered.com",
                subject = "Your Steam account: Access from new computer",
                preview = "Here is the Steam Guard code you need to login: $steamCode",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#171a21;">Steam Support</h2>
                        <p>Dear Valve user,</p>
                        <p>Here is the Steam Guard code you need to login to account associated with $mailboxAddress:</p>
                        <div style="background:#101822;color:#66c0f4;padding:16px;border-radius:6px;text-align:center;margin:20px 0;">
                            <span style="font-size:32px;font-weight:bold;letter-spacing:5px;">$steamCode</span>
                        </div>
                    </div>
                """.trimIndent(),
                bodyText = "Steam Guard Code\n\nCode: $steamCode\n\nEnter this code into Steam to authenticate your device.",
                otp = steamCode,
                link = "https://help.steampowered.com",
                category = "Gaming"
            ),
            Template(
                serviceName = "Amazon",
                senderEmail = "account-update@amazon.com",
                subject = "$code6 is your Amazon OTP",
                preview = "Do not share this OTP with anyone. It is valid for 10 minutes.",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#FF9900;">Amazon OTP</h2>
                        <p>For your security, don't share this code with anyone.</p>
                        <div style="background:#f7f7f7;padding:16px;border-radius:4px;text-align:center;margin:20px 0;">
                            <span style="font-size:30px;font-weight:bold;color:#111;">$code6</span>
                        </div>
                        <p>Amazon will never call or message asking for your OTP.</p>
                    </div>
                """.trimIndent(),
                bodyText = "Amazon OTP\n\n$code6 is your Amazon verification code.\n\nDo not share it with anyone.",
                otp = code6,
                link = null,
                category = "Shopping"
            ),
            Template(
                serviceName = "Spotify",
                senderEmail = "no-reply@spotify.com",
                subject = "Confirm your Spotify account",
                preview = "Confirm your address and start listening to millions of songs.",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#1DB954;">Welcome to Spotify</h2>
                        <p>Thanks for signing up! Click the link below to verify your email address:</p>
                        <div style="text-align:center;margin:24px 0;">
                            <a href="https://spotify.com/confirm?token=$code6" style="background:#1DB954;color:#ffffff;padding:12px 28px;border-radius:50px;text-decoration:none;font-weight:bold;">Confirm Account</a>
                        </div>
                        <p>Verification PIN: <b>$code6</b></p>
                    </div>
                """.trimIndent(),
                bodyText = "Spotify Email Confirmation\n\nClick link to confirm: https://spotify.com/confirm?token=$code6\n\nOr enter code: $code6",
                otp = code6,
                link = "https://spotify.com/confirm?token=$code6",
                category = "Entertainment"
            ),
            Template(
                serviceName = "OpenAI",
                senderEmail = "noreply@tm.openai.com",
                subject = "OpenAI - Your verification code is $code6",
                preview = "Your temporary verification code for ChatGPT is $code6",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2 style="color:#10a37f;">OpenAI Verification</h2>
                        <p>Your one-time code to sign in is:</p>
                        <div style="background:#f4f4f4;padding:16px;border-radius:8px;text-align:center;margin:20px 0;">
                            <span style="font-size:32px;font-weight:bold;color:#10a37f;">$code6</span>
                        </div>
                        <p>This code will remain active for 10 minutes.</p>
                    </div>
                """.trimIndent(),
                bodyText = "OpenAI Verification Code\n\nYour code: $code6\n\nExpires in 10 minutes.",
                otp = code6,
                link = null,
                category = "Developer"
            ),
            Template(
                serviceName = "Uber",
                senderEmail = "uber@uber.com",
                subject = "Your Uber code is $code4",
                preview = "Use code $code4 to log into your account.",
                bodyHtml = """
                    <div style="font-family:sans-serif;padding:16px;">
                        <h2>Your Uber verification code</h2>
                        <p>Enter this 4-digit code into your Uber app:</p>
                        <div style="background:#000000;color:#ffffff;padding:16px;border-radius:4px;text-align:center;margin:20px 0;">
                            <span style="font-size:36px;font-weight:bold;letter-spacing:8px;">$code4</span>
                        </div>
                    </div>
                """.trimIndent(),
                bodyText = "Your Uber code is $code4. Never share this code with anyone.",
                otp = code4,
                link = null,
                category = "Travel"
            )
        )
    }

    fun getWelcomeEmail(mailboxAddress: String): EmailEntity {
        return EmailEntity(
            mailboxAddress = mailboxAddress,
            senderName = "Gmail10p Team",
            senderEmail = "welcome@gmail10p.com",
            subject = "🛡️ Welcome to your @gmail10p.com Disposable Inbox",
            previewText = "Your 10-minute temporary mailbox is ready! Protect your privacy and stop spam.",
            bodyHtml = """
                <div style="font-family:sans-serif;padding:16px;line-height:1.6;">
                    <h2 style="color:#EA4335;">Welcome to Gmail10p! 🎉</h2>
                    <p>Your disposable email address is active:</p>
                    <div style="background:#EFF6FF;border-left:4px solid #1A73E8;padding:12px;margin:16px 0;font-weight:bold;color:#1E3A8A;">
                        $mailboxAddress
                    </div>
                    <h3>Why use Gmail10p Disposable Mail?</h3>
                    <ul>
                        <li><b>100% Anonymous:</b> No registration or personal data required.</li>
                        <li><b>Zero Spam:</b> Keeps your primary personal mailbox clean from marketing lists.</li>
                        <li><b>Auto OTP Extractor:</b> Instant 1-tap copy for verification codes.</li>
                        <li><b>Tracker Shield:</b> Invisible tracking pixels and beacons are stripped.</li>
                        <li><b>10-Minute Auto Expire:</b> Self-destructs unless you choose to extend or burn.</li>
                    </ul>
                    <p style="color:#64748B;font-size:12px;margin-top:24px;">Powered by Gmail10p Disposable Mail Engine. Enjoy private browsing!</p>
                </div>
            """.trimIndent(),
            bodyText = "Welcome to your Gmail10p Disposable Inbox!\n\nYour address is: $mailboxAddress\n\nProtect your privacy, register on any website without giving your real email, and copy verification codes with a single tap.",
            receivedAt = System.currentTimeMillis(),
            isRead = false,
            isStarred = true,
            extractedOtp = null,
            extractedLink = null,
            serviceCategory = "Welcome"
        )
    }
}
