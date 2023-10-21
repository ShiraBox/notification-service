package live.shirabox.notificationservice

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Firebase {
    private val logger = LoggerFactory.getLogger("Firebase")

    fun sendNotification(notification: AppNotification) {
        val message = Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.text)
                    .build()
            )
            .setTopic(notification.topic)
            .putAllData(
                notification.data
            )
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        logger.info("(${notification.topic}) Successfully sent message: $response")
    }

    fun authenticate(key: String) {
        try {
            val options = FirebaseOptions.builder().also {
                it.setCredentials(GoogleCredentials.fromStream(key.byteInputStream()))
            }.build()
            FirebaseApp.initializeApp(options)

            logger.info("Authenticated with environment key")
        } catch (ex: Exception) {
            logger.error("Failed to authenticate with environment key: \n${ex.localizedMessage}")
            exitProcess(1)
        }
    }
}