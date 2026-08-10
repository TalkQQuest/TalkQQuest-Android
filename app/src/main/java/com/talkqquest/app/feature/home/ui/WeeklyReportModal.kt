package com.talkqquest.app.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

// 주간 비교 리포트 도착 모달 (홈 알림 진입). CSS UI-14 "주간 비교 리포트(모달)" 1:1 전사.
// 서버 주간 비교 리포트가 목록 방식으로 바뀌어(백엔드 1번째 보고) 홈 알림으로 뜨는 그 팝업.
// nav 위까지 딤 덮으려 Popup(별도 윈도우)으로 띄움 — 하단 네비도 함께 어두워짐(op bg가 전체 393x852).

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

@Composable
fun WeeklyReportModal(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true), // 뒤로가기로 닫힘
    ) {
        // 딤 배경(op bg): 전체 Gray/700 #334155 @ 0.23. 배경 탭 시 닫힘(리플 없음).
        val scrimInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray700.copy(alpha = 0.23f))
                .clickable(interactionSource = scrimInteraction, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            // 카드: 302 폭, White, radius 16, padding 10/4/20, gap 16
            // 위치: 화면 중앙 기준 아래로 offset(눈 기준 조정값). CSS top 264는 중앙(248)보다 조금 아래.
            // Popup 안에선 WindowInsets가 0이라 절대좌표 앵커가 안 돼 이 방식이 유일하게 결과와 대응.
            val cardInteraction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .offset(y = 24.dp)
                    .width(302.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White)
                    // 카드 내부 탭이 배경(닫기)으로 새지 않게 소비(리플 없음)
                    .clickable(interactionSource = cardInteraction, indication = null, onClick = {})
                    .padding(top = 10.dp, start = 4.dp, end = 4.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 로봇 145x144 (Frame 427321772)
                Image(
                    painter = painterResource(R.drawable.img_weekly_report_robot),
                    contentDescription = null,
                    modifier = Modifier.size(width = 145.dp, height = 144.dp),
                )
                // 텍스트 + 버튼 묶음 (Frame 427321771, gap 12)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 제목/부제 + 버튼 (Frame 427321770, gap 16)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // 제목 + 부제 (Frame 427321555, gap 2)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            // "주간 비교 리포트"만 보라(Primary600), "가 도착했어요!"는 Gray800.
                            // (CSS는 부분 색을 단색으로 뭉쳐 추출 → 실제 렌더 기준으로 분리)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Primary600)) { append("주간 비교 리포트") }
                                    withStyle(SpanStyle(color = Gray800)) { append("가 도착했어요!") }
                                },
                                style = TqType.TitleL.figma().copy(letterSpacing = (-0.01).em), // 18/600
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "지난 주와 비교해 얼마나 성장했는지 지금 확인해보세요",
                                style = TqType.BodyM.figma(), // 14/400
                                color = Gray500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 200.dp), // CSS 196 → 2줄
                            )
                        }
                        // 버튼: 주간 리포트 확인하기 (버튼S_chevron) 264x44, Purple600, radius 16
                        Box(
                            modifier = Modifier
                                .width(264.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Primary600)
                                .clickable(onClick = onConfirm)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "주간 리포트 확인하기",
                                style = TqType.TitleL.figma().copy(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.01).em), // Button/Primary 16/600
                                color = Gray50,
                            )
                            // 우측 셔브론(흰색). CSS chevron-left 뒤집힘 = 우향
                            Icon(
                                painter = painterResource(R.drawable.ic_forward_chevron),
                                contentDescription = null,
                                tint = Gray50,
                                modifier = Modifier.align(Alignment.CenterEnd).size(28.dp),
                            )
                        }
                    }
                    // 다음에 볼게요 (Label/L 14/500 Gray500, 전체폭 중앙)
                    Text(
                        text = "다음에 볼게요",
                        style = TqType.LabelL.figma(),
                        color = Gray500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss,
                            ),
                    )
                }
            }
        }
    }
}

@Preview(name = "주간 비교 리포트 모달", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun WeeklyReportModalPreview() {
    TalkQQuestTheme {
        WeeklyReportModal(onConfirm = {}, onDismiss = {})
    }
}
