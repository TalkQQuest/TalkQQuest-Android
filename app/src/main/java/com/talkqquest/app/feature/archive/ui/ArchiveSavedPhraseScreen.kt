package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
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
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveSavedPhraseViewModel
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity

@Composable
fun ArchiveSavedPhraseScreen(
    onBackClick: () -> Unit = {},
    onConversationClick: (String) -> Unit = {},
    viewModel: ArchiveSavedPhraseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArchiveSavedPhraseContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleBookmark = viewModel::toggleBookmark,
        onConversationClick = onConversationClick
    )
}

@Composable
private fun ArchiveSavedPhraseContent(
    uiState: ArchiveSavedPhraseUiState,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onConversationClick: (String) -> Unit
) {
    FitDesign {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Gray50)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // [헤더 영역]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_chevron),
                            contentDescription = "뒤로가기",
                            tint = Gray500
                        )
                    }
                    Text(
                        text = "베스트 문장",
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray800,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // [본문 스크롤 영역]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(19.dp))

                    PhraseHighlightCard(
                        phraseText = uiState.phraseText,
                        isBookmarked = uiState.isBookmarked,
                        onToggleBookmark = onToggleBookmark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        MemoSection(uiState.memoKeywords, uiState.memoText)

                        uiState.relatedConversation?.let { conversation ->
                            RelatedConversationSection(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(160.dp))
                }
            }

            // [하단 스크롤 마스크 브러시]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x00F8FAFC),
                                Color(0x73F8FAFC),
                                Color(0xFFF8FAFC)
                            )
                        )
                    )
            )
        }
    }
}

// ==========================================
// ── UI 컴포넌트 ──
// ==========================================

@Composable
private fun PhraseHighlightCard(
    phraseText: String,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.img_archive_sentence),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        // 💡 [수정됨] 텍스트가 길어질 경우 한 줄(maxLines = 1)로 제한하고 말줄임표(Ellipsis) 처리
        Text(
            text = phraseText,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = Gray900,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onToggleBookmark() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (isBookmarked) R.drawable.ic_mission_bookmark_filled else R.drawable.ic_mission_bookmark),
                contentDescription = "북마크",
                tint = Color.Unspecified, // 원본 아이콘 색상 그대로 사용
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MemoSection(keywords: List<String>, memoText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "메모",
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = Gray700,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            keywords.forEach { keyword ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gray100)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = keyword,
                        style = TqType.LabelM.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray500
                    )
                }
            }
        }

        Text(
            text = memoText,
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
private fun RelatedConversationSection(
    conversation: RecentActivity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "해당 대화 기록",
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
            color = Gray700,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .clickable { onClick() }
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.img_archive_conversation),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = conversation.title,
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = conversation.status, style = TqType.Caption.figma(), color = Gray500)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.width(1.dp).height(9.dp).background(Gray300))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = conversation.date, style = TqType.Caption.figma(), color = Gray500)
                    }
                }
            }

            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward_chevron),
                    contentDescription = "상세 보기",
                    tint = Gray600
                )
            }
        }
    }
}

// ==========================================
// ── Previews ──
// ==========================================
@Preview(name = "보관함 베스트 문장 화면", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveSavedPhraseScreenPreview() {
    val mockConversation = RecentActivity(
        id = "conv_1",
        title = "처음 보는 사람에게 짧게 인사하기",
        type = ActivityType.CONVERSATION,
        status = "대화 완료",
        date = "2026.08.20"
    )

    TalkQQuestTheme {
        ArchiveSavedPhraseContent(
            uiState = ArchiveSavedPhraseUiState(
                phraseText = "“그렇군요! 저도 생각보다 편해서 많이 놀랐어요. 정말 길이가 아주 아주 긴 문장 테스트입니다.”",
                isBookmarked = true,
                memoKeywords = listOf("자기 성장", "첫 만남", "스몰 토크"),
                memoText = "상대방의 감정을 자연 스럽게 열어줄 수 있는 좋은 문장이에요.",
                relatedConversation = mockConversation
            ),
            onBackClick = {},
            onToggleBookmark = {},
            onConversationClick = {}
        )
    }
}