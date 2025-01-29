package com.example.musicroom

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    private const val EMAIL = "alenmahmutovic2@gmail.com" // Zameni svojim emailom
    private const val PASSWORD = "zsvy weot zaek ayzw" // Koristi App Password (ne regularnu lozinku)

    fun sendEmail(recipientEmail: String, subject: String, message: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(EMAIL, PASSWORD)
            }
        })

        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(EMAIL))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                setSubject(subject)
                setText(message)
            }

            Transport.send(mimeMessage)
            println("Email poslat na: $recipientEmail")
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
