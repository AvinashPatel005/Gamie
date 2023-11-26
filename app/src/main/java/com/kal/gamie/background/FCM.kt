package com.kal.gamie.background
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCM : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
    override fun onNewToken(token: String) {
        Firebase.database.getReference("Token").setValue(token)
        super.onNewToken(token)
    }
}