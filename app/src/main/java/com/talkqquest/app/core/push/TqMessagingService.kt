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
import kotlin.random.Random

// FCM 수신 담당 서비스 (Manifest에 등록). 두 가지를 받음:
//  - onNewToken: 이 기기의 FCM 토큰(주소) 발급/갱신 → 백엔드 등록은 5번 작업(아직 미구현)
//  - onMessageReceived: 앱이 포그라운드일 때(또는 data 메시지) 푸시 수신 → 알림으로 표시
class TqMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // TODO(5번): 백엔드에 토큰 등록/갱신. 현재는 로그인 시 fcmToken 필드로만 전송 예정이라 여기선 로그만.
        Log.d(TAG, "FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // notification 페이로드 우선, 없으면 data 페이로드에서 title/body 추출
        val title = message.notification?.title ?: message.data["title"] ?: "TalkQQuest"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        // Android 13+ 는 권한 없으면 알림 못 띄움 — 조용히 무시 (권한 요청은 앱 실행 시 처리)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 알림 탭 시 앱 열기 (구체 화면 이동은 6번 작업)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
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

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }

    companion object {
        private const val TAG = "TqMessaging"
    }
}
