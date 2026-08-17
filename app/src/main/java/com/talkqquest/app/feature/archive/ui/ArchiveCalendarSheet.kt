package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqChoiceBottomSheet
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 💡 캘린더 전용 신규 컬러 (Base/Gray 40)
private val Gray40 = Color(0xFFB5BEC6)

@Composable
fun TqCalendarBottomSheet(
    isVisible: Boolean,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    TqChoiceBottomSheet(
        visible = isVisible,
        onDismiss = onDismissRequest,
        cornerRadius = 48.dp,
        // 공용 핸들(top20/bottom12)은 CSS 값(top12·핸들뒤gap22)과 ~2dp 어긋나 직접 그림.
        dragHandle = false,
    ) {
        // CSS 명세: padding 12px 67px 24px(67은 259dp폭 요소를 393폭에서 가운데 정렬한 값과 동일),
        // gap 22px가 핸들→월행→요일행→날짜그리드 사이에 균일하게 반복된다.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // [드래그 핸들] Frame 453 (36x4, Gray/600)
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Gray600)
            )

            // [캘린더 헤더 (월 변경)]
            Row(
                modifier = Modifier.width(259.dp).height(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달", tint = Gray40,
                    modifier = Modifier.size(16.dp).clip(CircleShape).clickable { currentMonth = currentMonth.minusMonths(1) }
                )
                Text(
                    text = "${currentMonth.monthValue}월 ${currentMonth.year}",
                    style = TqType.LabelL.copy(fontSize = 14.sp, lineHeight = 14.sp).figma(),
                    color = Gray600,
                    textAlign = TextAlign.Center
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달", tint = Gray40,
                    modifier = Modifier.size(16.dp).clip(CircleShape).clickable { currentMonth = currentMonth.plusMonths(1) }
                )
            }

            // [요일 텍스트 영역]
            val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
            Row(
                modifier = Modifier.width(259.dp).height(20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDays.forEach { day ->
                    Box(modifier = Modifier.size(width = 30.dp, height = 20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            style = TqType.BodyS.copy(
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp,
                            ).figma(),
                            color = Gray40
                        )
                    }
                }
            }

            // [날짜 그리드 영역]
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = currentMonth.lengthOfMonth()

            // 내부 선택 상태: initialDate가 현재 표시 중인 달에 속하면 그 day로 초기화, 아니면 미선택
            var selectedDay by remember(currentMonth) {
                mutableStateOf(
                    if (YearMonth.from(initialDate) == currentMonth) initialDate.dayOfMonth else null
                )
            }

            // 셀 피치(30 셀 + 8 간격) px 환산 — 좌표→(row, col)→day 계산에 사용
            val pitchPx = with(density) { 38.dp.toPx() }

            Column(
                modifier = Modifier
                    .width(259.dp)
                    .pointerInput(currentMonth) {
                        awaitEachGesture {
                            fun updateFromPosition(position: Offset) {
                                val col = (position.x / pitchPx).toInt().coerceIn(0, 6)
                                val row = (position.y / pitchPx).toInt().coerceIn(0, 5)
                                val day = row * 7 + col - firstDayOfWeek + 1
                                if (day in 1..daysInMonth && day != selectedDay) {
                                    selectedDay = day
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }

                            val down = awaitFirstDown(requireUnconsumed = false)
                            updateFromPosition(down.position)

                            while (true) {
                                val event = awaitPointerEvent()
                                val stillPressed = event.changes.any { it.pressed }
                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        updateFromPosition(change.position)
                                        change.consume()
                                    }
                                }
                                if (!stillPressed) {
                                    val finalDay = selectedDay
                                    if (finalDay != null) {
                                        val date = currentMonth.atDay(finalDay)
                                        coroutineScope.launch {
                                            delay(150)
                                            onDateSelected(date)
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..6) {
                            val dayIndex = row * 7 + col
                            val day = dayIndex - firstDayOfWeek + 1

                            if (day in 1..daysInMonth) {
                                val isSelected = (day == selectedDay)

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Primary600 else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        // CSS lineHeight 18px(112%) — 폰트는 'Avenir Next LT Pro'(미보유, 앱 전체 유일 인스턴스)라
                                        // 전사 불가해 앱 표준 Pretendard SemiBold 유지, 줄높이만 맞춤
                                        style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold, lineHeight = 18.sp).figma(),
                                        color = if (isSelected) White else Gray600
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 💡 [프리뷰 영역 추가]
// ==========================================
@Preview(name = "캘린더 바텀 시트", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun TqCalendarBottomSheetPreview() {
    TalkQQuestTheme {
        // 프리뷰에서는 isVisible을 true로 고정해두어야 화면에 표시됩니다.
        TqCalendarBottomSheet(
            isVisible = true,
            initialDate = LocalDate.of(2026, 7, 14), // 테스트용 날짜
            onDateSelected = {},
            onDismissRequest = {}
        )
    }
}
