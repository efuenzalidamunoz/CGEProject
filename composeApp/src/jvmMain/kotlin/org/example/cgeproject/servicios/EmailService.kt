package org.example.cgeproject.servicios

import org.example.cgeproject.BuildConfig
import java.util.*
import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

class EmailService {

    private val properties: Properties = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }

    private val emailFrom = BuildConfig.EMAIL_FROM
    private val password = BuildConfig.EMAIL_PASSWORD

    private val authenticator: Authenticator = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(emailFrom, password)
        }
    }

    private val session: Session = Session.getInstance(properties, authenticator)

    fun enviarBoletaPorCorreo(destinatario: String, asunto: String, cuerpo: String, pdfBytes: ByteArray, nombreArchivo: String) {
        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(emailFrom))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
            message.subject = asunto

            // Cuerpo del mensaje
            val mimeBodyPart = MimeBodyPart()
            mimeBodyPart.setText(cuerpo)

            // Archivo adjunto
            val attachmentBodyPart = MimeBodyPart()
            val dataSource = ByteArrayDataSource(pdfBytes, "application/pdf")
            attachmentBodyPart.dataHandler = DataHandler(dataSource)
            attachmentBodyPart.fileName = nombreArchivo

            // Multipart
            val multipart = MimeMultipart()
            multipart.addBodyPart(mimeBodyPart)
            multipart.addBodyPart(attachmentBodyPart)

            message.setContent(multipart)

            Transport.send(message)
        } catch (e: MessagingException) {
            // Imprime el error para facilitar la depuración
            e.printStackTrace()
            throw RuntimeException("Error al enviar el correo: ${e.message}", e)
        }
    }
}
