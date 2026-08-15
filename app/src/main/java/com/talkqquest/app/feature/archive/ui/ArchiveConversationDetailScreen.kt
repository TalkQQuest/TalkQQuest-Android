package com.talkqquest.app.feature.archive.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.data.model.ReviewChatMessage
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel
import com.talkqquest.app.feature.archive.viewmodel.AiFeedbackItem
import com.talkqquest.app.feature.mission.ui.figma

// ── 공통 색상 상수 ──
private val ChatText = Color(0xFF1C1C1C)
private val TimeText = Color(0xFF999999)

// CSS 기준 상단 스크롤 마스크 브러시
private fun topScrollMaskBrush(): Brush {
    return Brush.verticalGradient(
        0f to Gray50.copy(alpha = 0.8f),
        0.3141f to Gray50.copy(alpha = 0.8f),
        0.8106f to Gray50.copy(alpha = 0.45f),
        1f to Gray50.copy(alpha = 0.28f)
    )
}

// CSS 기준 하단 스크롤 마스크 브러시
private fun bottomScrollMaskBrush(): Brush {
    return Brush.verticalGradient(
        0f to Gray50.copy(alpha = 0.28f),
        0.1894f to Gray50.copy(alpha = 0.45f),
        0.6859f to Gray50.copy(alpha = 0.8f),
        1f to Gray50.copy(alpha = 0.8f)
    )
}

@Composable
fun ArchiveConversationDetailScreen(
    onBackClick: () -> Unit = {},
    viewModel: ArchiveConversationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val handleBackClick = {
        if (uiState.isReviewMode) {
            viewModel.toggleReviewMode()
        } else {
            onBackClick()
        }
    }

    FitDesign {
        AnimatedContent(
            targetState = uiState.isReviewMode,
            transitionSpec = {
                if (targetState) {
                    (slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn(animationSpec = tween(300))) togetherWith fadeOut(animationSpec = tween(300))
                } else {
                    fadeIn(animationSpec = tween(300)) togetherWith (slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { fullHeight -> fullHeight }
                    ) + fadeOut(animationSpec = tween(300)))
                }
            },
            label = "ReviewScreenTransition"
        ) { isReviewMode ->
            if (isReviewMode) {
                ArchiveConversationReviewContent(uiState = uiState, onBackClick = handleBackClick)
            } else {
                ArchiveConversationDetailContent(
                    uiState = uiState,
                    onBackClick = handleBackClick,
                    onShowReviewClick = { viewModel.toggleReviewMode() }
                )
            }
        }
    }
}

// ==========================================
// ── 1. 상세(피드백 요약) 화면 컨텐츠 ──
// ==========================================
@Composable
private fun ArchiveConversationDetailContent(
    uiState: ArchiveConversationDetailUiState,
    onBackClick: () -> Unit,
    onShowReviewClick: () -> Unit
) {
    val tick = rememberHapticTick()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(44.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).align(Alignment.CenterStart).clip(CircleShape).clickable(onClick = { tick(); onBackClick() }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_chevron),
                        contentDescription = "뒤로가기",
                        tint = Gray500
                    )
                }
                Text(text = "대화 기록", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray800, modifier = Modifier.align(Alignment.Center))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(19.dp))

                ArchiveConversationCard(
                    title = uiState.title,
                    tags = uiState.summaryKeywords.take(2),
                    summary = uiState.description, // 💡 상단 카드: 짧은 요약 (description) 매핑 완료
                    date = uiState.date,
                    time = uiState.time,
                    onClick = { /* 상세 화면 최상단 썸네일이므로 클릭 동작 없음 */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ConversationSummarySection(uiState.summaryKeywords, uiState.summaryText)

                    // 💡 핵심 변경: 억지로 자르던 코드를 지우고 uiState.keyPoints 리스트를 그대로 전달
                    ConversationMainContentSection(uiState.keyPoints)

                    ConversationAiFeedbackSection(uiState.feedbacks)
                }

                Spacer(modifier = Modifier.height(180.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(colors = listOf(Color(0x00F8FAFC), Color(0x80F8FAFC), Color(0xFFF8FAFC))))
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 22.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary600)
                    .clickable(onClick = onShowReviewClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "대화 다시 보기", style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold).figma(), color = Primary50)
            }
        }
    }
}

// ==========================================
// ── 2. 대화 다시보기(채팅) 화면 컨텐츠 ──
// ==========================================
@Composable
private fun ArchiveConversationReviewContent(
    uiState: ArchiveConversationDetailUiState,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "닫기", tint = Gray500)
                }
                Text(
                    text = uiState.title,
                    style = TqType.BodyM.figma(),
                    color = Gray800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center).widthIn(max = 260.dp)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 88.dp, bottom = 220.dp)
                ) {
                    itemsIndexed(uiState.messages) { index, message ->
                        val prev = uiState.messages.getOrNull(index - 1)
                        val next = uiState.messages.getOrNull(index + 1)
                        val topGap = when {
                            prev == null -> 0.dp
                            prev.isFromUser == message.isFromUser -> 8.dp
                            else -> 16.dp
                        }
                        val showTime = next == null || next.isFromUser != message.isFromUser

                        ChatBubbleRow(message = message, topGap = topGap, showTime = showTime)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(topScrollMaskBrush())
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(203.dp)
                        .background(bottomScrollMaskBrush())
                )

                Box(
                    modifier = Modifier.offset(x = 16.dp, y = 12.dp).size(70.dp)
                        .softShadow(color = Color(0xFF9A73FF).copy(alpha = 0.08f), offsetY = 6.dp, blur = 12.dp, cornerRadius = 35.dp)
                        .clip(CircleShape).background(Gray100),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(R.drawable.img_conversation_bot), contentDescription = null, modifier = Modifier.requiredSize(70.dp, 81.dp))
                }
            }
        }
    }
}

// ==========================================
// ── 공통 UI 컴포넌트 ──
// ==========================================

@Composable
private fun ChatBubbleRow(message: ReviewChatMessage, topGap: androidx.compose.ui.unit.Dp, showTime: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = topGap),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (message.isFromUser && showTime) { TimeLabel(message.time); Spacer(modifier = Modifier.width(8.dp)) }

        Box(
            modifier = Modifier.widthIn(max = if (message.isFromUser) 260.dp else 230.dp)
                .let { if (message.isFromUser) it else it.softShadow(color = Color.Black.copy(alpha = 0.08f), offsetY = 2.dp, blur = 6.dp, cornerRadius = 24.dp) }
                .clip(if (message.isFromUser) RoundedCornerShape(24.dp, 24.dp, 2.dp, 24.dp) else RoundedCornerShape(24.dp, 24.dp, 24.dp, 2.dp))
                .background(if (message.isFromUser) Primary600 else White)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(text = message.text, style = TqType.BodyM.copy(lineHeight = 22.sp).figma(), color = if (message.isFromUser) Gray50 else ChatText)
        }

        if (!message.isFromUser && showTime) { Spacer(modifier = Modifier.width(8.dp)); TimeLabel(message.time) }
    }
}

@Composable
private fun TimeLabel(time: String) {
    Text(text = time, style = TqType.Caption.figma().copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium), color = TimeText)
}

@Composable
private fun ConversationSummarySection(keywords: List<String>, summaryText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "대화 요약", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray700, modifier = Modifier.padding(horizontal = 2.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            keywords.forEach { Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Gray100).padding(horizontal = 12.dp, vertical = 4.dp), contentAlignment = Alignment.Center) { Text(text = it, style = TqType.LabelM.copy(fontWeight = FontWeight.Medium).figma(), color = Gray500) } }
        }
        Text(
            text = summaryText,
            style = TqType.BodyM.copy(
                lineHeight = 22.sp,
                lineBreak = LineBreak(
                    strategy = LineBreak.Strategy.Simple,
                    strictness = LineBreak.Strictness.Normal,
                    wordBreak = LineBreak.WordBreak.Phrase
                )
            ).figma(),
            color = Gray600,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun ConversationMainContentSection(keyPoints: List<String>) {
    // 💡 피드백 로딩 중이거나 배열이 비어있으면 아예 영역을 그리지 않음
    if (keyPoints.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "주요 내용", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray700, modifier = Modifier.padding(horizontal = 2.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            // 💡 억지로 문장을 자르지 않고 백엔드 배열 항목을 순회하여 그립니다.
            keyPoints.forEach { point ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        text = "•",
                        style = TqType.BodyM.copy(lineHeight = 22.sp).figma(),
                        color = Gray600,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = point,
                        style = TqType.BodyM.copy(
                            lineHeight = 22.sp,
                            lineBreak = LineBreak(
                                strategy = LineBreak.Strategy.Simple,
                                strictness = LineBreak.Strictness.Normal,
                                wordBreak = LineBreak.WordBreak.Phrase
                            )
                        ).figma(),
                        color = Gray600
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationAiFeedbackSection(feedbacks: List<AiFeedbackItem>) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "AI 피드백", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray700, modifier = Modifier.padding(horizontal = 4.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
                .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp)).background(White).padding(vertical = 14.dp)
        ) {
            feedbacks.forEachIndexed { index, feedback ->
                val titleColor = if (feedback.score < 80) Gray800 else Gray600
                Row(
                    modifier = Modifier.fillMaxWidth().height(44.dp).padding(start = 16.dp, end = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = feedback.title, style = TqType.BodyM.copy(lineHeight = 22.sp).figma(), color = titleColor)
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)).background(Primary100)) {
                            Box(modifier = Modifier.fillMaxWidth(feedback.score / 100f).height(8.dp).clip(RoundedCornerShape(8.dp)).background(Primary600))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = feedback.score.toString(), style = TqType.HeadingM.figma(), color = Primary600)
                            Text(text = "점", style = TqType.BodyS.figma(), color = Primary600, modifier = Modifier.padding(bottom = 3.dp))
                        }
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_forward_chevron),
                                contentDescription = "상세 보기",
                                tint = Gray400
                            )
                        }
                    }
                }
                if (index < feedbacks.lastIndex) Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth().height(1.dp).background(Gray200))
            }
        }
    }
}

// ==========================================
// ── Previews ──
// ==========================================
@Preview(name = "상세(피드백) 모드 화면", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveConversationDetailScreenPreview() {
    TalkQQuestTheme {
        ArchiveConversationDetailContent(
            uiState = ArchiveConversationDetailUiState(
                title = "팀플 조원에게 먼저 연락하기",
                date = "2026.08.13",
                time = "01:22",
                duration = "1분 22초",
                description = "먼저 인사를 건넨 짧은 대화",
                summaryKeywords = listOf("팀플 조율", "역할 분배", "일정 확정"),
                summaryText = "팀 프로젝트 조원에게 먼저 연락해 역할 분배 방식을 제안하고, 회의 일정을 조율하는 대화를 진행했습니다. 상대방의 모호한 응답에 대해 명확히 재확인하며 대화를 이끌었습니다.",
                keyPoints = listOf(
                    "역할 분배 방식을 제안하며 대화를 시작함",
                    "상대의 모호한 발언에 대해 명확히 재확인함",
                    "구체적 일정을 제시하며 회의 일정을 조율함"
                ),
                feedbacks = listOf(
                    AiFeedbackItem("친절한 태도", 75),
                    AiFeedbackItem("대화 주도", 85),
                    AiFeedbackItem("공감 능력", 70),
                    AiFeedbackItem("질문 연결성", 65)
                )
            ),
            onBackClick = {},
            onShowReviewClick = {}
        )
    }
}

@Preview(name = "대화 다시보기(채팅) 모드 화면", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveConversationReviewScreenPreview() {
    TalkQQuestTheme {
        ArchiveConversationReviewContent(
            uiState = ArchiveConversationDetailUiState(
                title = "팀플 조원에게 먼저 연락하기",
                messages = listOf(
                    ReviewChatMessage("1", "안녕하세요! 처음 뵙네요 \uD83D\uDE42", false, "9:20"),
                    ReviewChatMessage("2", "오늘 여기 처음 오셨어요?", false, "9:20"),
                    ReviewChatMessage("3", "분위기가 좋아보여서요!", true, "9:21"),
                    ReviewChatMessage("4", "오, 그러셨구나. 저는 여기 몇 번 와봤는데 생각보다 괜찮더라고요", false, "9:21"),
                    ReviewChatMessage("5", "오 그렇군요!", true, "9:21")
                ),
                isReviewMode = true
            ),
            onBackClick = {}
        )
    }
}