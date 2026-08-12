package com.talkqquest.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.navigation.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Android 13+ 알림 권한 요청 런처 (결과는 지금 따로 처리 안 함 — 거부해도 앱은 정상 동작)
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TalkQQuest)
        super.onCreate(savedInstanceState)

        // Android 13+ 에서 알림 권한이 없으면 1회 요청 (FCM 알림 표시용)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // 시스템 바(상단/하단)를 완전 투명으로 → 앱 배경(Gray50)이 그대로 비침(피그마처럼 한 톤).
        // 기본 scrim(반투명 막)을 투명으로 꺼서 배경색과 정확히 동일하게. light=밝은 배경용(어두운 버튼).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            TalkQQuestTheme {
                MainScreen()
            }
        }
    }
}
