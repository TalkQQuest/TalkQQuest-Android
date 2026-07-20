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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.data.ReviewChatMessage
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveConversationDetailViewModel
import com.talkqquest.app.feature.archive.viewmodel.AiFeedbackItem

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
        // 💡 화면 전환 애니메이션 적용 (AnimatedContent)
        AnimatedContent(
            targetState = uiState.isReviewMode,
            transitionSpec = {
                if (targetState) {
                    // 상세 -> 리뷰 (들어가기): 아래에서 위로 슬라이드 업 + 페이드 인
                    (slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn(animationSpec = tween(300))) togetherWith fadeOut(animationSpec = tween(300))
                } else {
                    // 리뷰 -> 상세 (닫기): 위에서 아래로 슬라이드 다운 + 페이드 아웃
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(44.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).align(Alignment.CenterStart).clip(CircleShape).clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    // 💡 [수정됨] 커스텀 뒤로가기 아이콘으로 변경
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

                ConversationProfileCard(uiState.title, uiState.date, uiState.duration)

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ConversationSummarySection(uiState.summaryKeywords, uiState.summaryText)
                    ConversationMainContentSection(uiState.mainContentText)
                    ConversationAiFeedbackSection(uiState.feedbacks)
                }

                Spacer(modifier = Modifier.height(120.dp))
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 88.dp, bottom = 48.dp)
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
private fun ConversationProfileCard(title: String, date: String, duration: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp)).background(White).padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.img_archive_conversation), contentDescription = null, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(text = title, style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = date, style = TqType.Caption.figma(), color = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(1.dp).height(9.dp).background(Gray300))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = duration, style = TqType.Caption.figma(), color = Gray500)
            }
        }
    }
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
private fun ConversationMainContentSection(mainContentText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "주요 내용", style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(), color = Gray700, modifier = Modifier.padding(horizontal = 2.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            val points = mainContentText.split(". ").filter { it.isNotBlank() }
            points.forEach { point ->
                val formattedText = if (point.endsWith(".")) point else "$point."
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        text = "•",
                        style = TqType.BodyM.copy(lineHeight = 22.sp).figma(),
                        color = Gray600,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = formattedText,
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "상세 보기", tint = Gray400)
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
                title = "처음 보는 사람에게 짧게 인사하기",
                date = "2026.07.16",
                duration = "5분 30초",
                summaryKeywords = listOf("자기 성장", "첫 만남", "스몰 토크"),
                summaryText = "카페에서 처음 만난 사람에게 자연스럽게 인사를 건네고, 간단한 질문을 이어가며 어색하지 않게 대화를 시작하는 연습을 진행했습니다.",
                mainContentText = "먼저 인사를 건네며 대화를 시작했어요. \"자주는 오시나요?\"와 같은 질문으로 대화를 이어갔어요. 상대의 답변에 반응하고 공감하며 대화를 마무리했어요.",
                feedbacks = listOf(
                    AiFeedbackItem("친절한 태도", 92),
                    AiFeedbackItem("대화 주도", 88),
                    AiFeedbackItem("공감 능력", 85),
                    AiFeedbackItem("질문 연결성", 78)
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
                title = "처음 보는 사람에게 짧게 인사하기",
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