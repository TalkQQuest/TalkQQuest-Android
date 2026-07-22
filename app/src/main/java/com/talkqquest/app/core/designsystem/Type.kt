package com.talkqquest.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R

// 타이포그래피. 출처: CONVENTIONS.md 8번. 폰트: Pretendard.

val PretendardFamily: FontFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

// 로고("톡깨") 전용 폰트. 로그인·스플래시의 로고 워드마크에서만 사용 (디자인 스펙: font-family 'A2Z', weight 700).
val A2ZFamily: FontFamily = FontFamily(
    Font(R.font.a2z_bold, FontWeight.Bold),
)

// 이름으로 직접 쓰는 스케일 (또는 MaterialTheme.typography.* 로도 사용 가능)
object TqType {
    val Display = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 44.sp, letterSpacing = (-0.02).em)
    val HeadingXL = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em)
    val HeadingL = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 34.sp, letterSpacing = (-0.01).em)
    val HeadingM = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 30.sp, letterSpacing = (-0.01).em)
    val TitleL = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em)
    val BodyL = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val BodyM = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp)
    val BodyS = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp)
    val LabelL = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
    val LabelM = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp)
    val Caption = TextStyle(fontFamily = PretendardFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp)
}

val Typography = Typography(
    displayLarge = TqType.Display,
    headlineLarge = TqType.HeadingXL,
    headlineMedium = TqType.HeadingL,
    headlineSmall = TqType.HeadingM,
    titleLarge = TqType.TitleL,
    bodyLarge = TqType.BodyL,
    bodyMedium = TqType.BodyM,
    bodySmall = TqType.BodyS,
    labelLarge = TqType.LabelL,
    labelMedium = TqType.LabelM,
    labelSmall = TqType.Caption,
)
