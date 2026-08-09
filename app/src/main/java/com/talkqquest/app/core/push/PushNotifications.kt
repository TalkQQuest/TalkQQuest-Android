package com.talkqquest.app.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

// FCM 푸시 알림 공통 (채널·상수). minSdk 26이라 알림 채널은 항상 필요.
object PushNotifications {
    // ★strings.xml의 default_notification_channel_id 값과 동일해야 함 (백그라운드 메시지가 이 채널을 씀).
    const val DEFAULT_CHANNEL_ID = "talkqquest_default"

    // 앱 시작 시 1회 호출 (이미 있으면 덮어써도 무해). 채널 없으면 알림이 아예 안 뜸.
    fun createDefaultChannel(context: Context) {
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "일반 알림",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "미션·리포트 등 앱 알림"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
