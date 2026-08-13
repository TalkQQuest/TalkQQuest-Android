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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
// 앱 최상위 레이어에 놓인다. 별도 Dialog 창을 쓰면 창 하단에서 퇴장 모션이 잘릴 수 있어,
// 대화 종료 팝업과 같이 같은 Compose 루트에서 딤과 카드를 함께 애니메이션한다.

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
private fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

@Composable
fun WeeklyReportModal(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // 대화 완료 확인 팝업과 동일한 360ms 딤 페이드.
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(360, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(360, easing = FastOutSlowInEasing)),
            ) {
                val scrimInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Gray700.copy(alpha = 0.23f))
                        .clickable(interactionSource = scrimInteraction, indication = null, onClick = onDismiss),
                )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(360, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.86f, animationSpec = tween(360, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(360, easing = FastOutSlowInEasing)) +
                scaleOut(targetScale = 0.86f, animationSpec = tween(360, easing = FastOutSlowInEasing)),
        ) {
            // AnimatedVisibility의 영역을 카드 크기가 아니라 화면 전체로 유지한다.
            // 카드 자체에 준 24dp offset이 애니메이션 경계 밖으로 나가 하단부터 잘리던 현상을 막는다.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
            // 카드: 302 폭, White, radius 16, padding 10/4/20, gap 16
            // 위치: 화면 중앙 기준 아래로 offset(눈 기준 조정값). CSS top 264는 중앙(248)보다 조금 아래.
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
}

@Preview(name = "주간 비교 리포트 모달", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun WeeklyReportModalPreview() {
    TalkQQuestTheme {
        WeeklyReportModal(visible = true, onConfirm = {}, onDismiss = {})
    }
}
