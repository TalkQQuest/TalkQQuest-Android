package com.talkqquest.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 앱 전역 테마. dynamicColor 미사용(브랜드 색 유지), 디자인이 라이트 전용이라 라이트로 통일.

private val LightColorScheme = lightColorScheme(
    primary = Primary600,
    onPrimary = White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary900,
    secondary = Primary400,
    onSecondary = White,
    secondaryContainer = Primary200,
    onSecondaryContainer = Primary900,
    tertiary = Primary500,
    onTertiary = White,
    background = Primary50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray200,
    outlineVariant = Gray200,
    error = Error,
    onError = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Gray1000,
    background = Gray1000,
    onBackground = Gray50,
    surface = Gray900,
    onSurface = Gray50,
    error = Error,
    onError = White,
)

@Composable
fun TalkQQuestTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
