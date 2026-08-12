package com.talkqquest.app.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.talkqquest.app.core.datastore.TokenDataStore
import com.talkqquest.app.feature.notification.data.NotificationApi
import com.talkqquest.app.feature.notification.data.model.FcmTokenRegisterRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FcmTokenRegistrar @Inject constructor(
    private val notificationApi: NotificationApi,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun registerCurrentTokenIfAuthenticated() {
        val token = getFcmTokenOrNull() ?: return
        registerTokenIfAuthenticated(token)
    }

    suspend fun registerTokenIfAuthenticated(token: String) {
        if (token.isBlank() || tokenDataStore.accessToken.first().isNullOrBlank()) return

        try {
            notificationApi.registerFcmToken(
                FcmTokenRegisterRequest(
                    fcmToken = token,
                    platform = "android",
                ),
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register FCM token", e)
        }
    }

    private suspend fun getFcmTokenOrNull(): String? =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (continuation.isActive) continuation.resume(token.takeIf { it.isNotBlank() })
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

    companion object {
        private const val TAG = "FcmTokenRegistrar"
    }
}