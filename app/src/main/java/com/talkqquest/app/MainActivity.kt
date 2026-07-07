package com.talkqquest.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.navigation.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
