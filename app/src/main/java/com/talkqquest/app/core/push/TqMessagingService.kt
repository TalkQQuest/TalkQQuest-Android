package com.talkqquest.app.core.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.talkqquest.app.MainActivity
import com.talkqquest.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

// FCM 수신 서비스. 토큰 갱신과 포그라운드 메시지 알림 표시를 담당한다.
@AndroidEntryPoint
class TqMessagingService : FirebaseMessagingService() {
    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token refreshed")
        pushTokenRegistrar.register(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "TalkQQuest"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data[EXTRA_NOTIFICATION_TYPE].orEmpty()
        val reportId = message.data[EXTRA_REFERENCE_ID]

        // 휴대폰 실제 알림은 주간 비교 리포트 도착 안내만 표시한다.
        // 미션 완료 등 나머지 알림은 서버 목록에 남아 앱 내부 알림창에서만 확인한다.
        if (type != WEEKLY_COMPARE_NOTIFICATION_TYPE &&
            !title.contains(WEEKLY_COMPARE_NOTIFICATION_TEXT) &&
            !body.contains(WEEKLY_COMPARE_NOTIFICATION_TEXT)
        ) {
            Log.d(TAG, "앱 내부 전용 알림 — 시스템 알림 표시 생략")
            return
        }

        showNotification(title, body, reportId)
    }

    private fun showNotification(title: String, body: String, reportId: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationId = Random.nextInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, WEEKLY_COMPARE_NOTIFICATION_TYPE)
            reportId?.takeIf { it.isNotBlank() }?.let { putExtra(EXTRA_REFERENCE_ID, it) }
        }
        val pending = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, PushNotifications.DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "TqMessaging"
        private const val WEEKLY_COMPARE_NOTIFICATION_TEXT = "주간 비교 리포트"
        const val WEEKLY_COMPARE_NOTIFICATION_TYPE = "weekly_compare_ready"
        const val EXTRA_NOTIFICATION_TYPE = "type"
        const val EXTRA_REFERENCE_ID = "referenceId"
    }
}
