package com.example.musicplayer

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


object EmailSender {
    fun sendOtpEmail(recipientEmail: String, otp: String) {
        Thread {
            // Email configuration
            val senderEmail = "shaiknasheer18@gmail.com"
            val senderPassword = "chdw jebm zxtk irox"

            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"

            val session = Session.getInstance(props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(senderEmail))
                message.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                message.subject = "Your OTP"
                message.setText("Your OTP is: $otp")
                Transport.send(message)
                println("Sent message successfully....")
            } catch (mex: MessagingException) {
                mex.printStackTrace()
            }
        }.start()
    }
}