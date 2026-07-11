package com.talkqquest.app.feature.mission.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.LocalDesignScale
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.model.ConversationPrep
import com.talkqquest.app.feature.mission.viewmodel.ConversationPrepUiState
import com.talkqquest.app.feature.mission.viewmodel.ConversationPrepViewModel

// ── 대화 준비 / 미션 진입 (UI 1차 v2.css "미션 진입" 전사) ──
// 미션 상세 "다음"에서 들어옴. 상단 흰 영역(일러스트+주제) + 회색 영역(바로 쓰는 첫 마디) + 시작 버튼.
// 2단 분리(state hoisting): (1) viewModel 연결 / (2) 값만 받아 그림(Preview). 홈·미션 패턴 동일.

@Composable
fun ConversationPrepScreen(
    viewModel: ConversationPrepViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onStartClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConversationPrepScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadPrep,
        onRefreshOpeners = viewModel::refreshOpeners,
        onStartClick = onStartClick,
    )
}

@Composable
private fun ConversationPrepScreen(
    uiState: ConversationPrepUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefreshOpeners: () -> Unit = {},
    onStartClick: () -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 스크롤 없이 한 화면에
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 (CSS)
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(color = Primary600)

            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage, style = TqType.BodyM.figma(), color = Error)
                    Spacer(Modifier.height(16.dp))
                    TqButton(text = "다시 시도", onClick = onRetry)
                }
            }

            uiState.prep != null -> ConversationPrepContent(
                prep = uiState.prep,
                onBack = onBack,
                onRefreshOpeners = onRefreshOpeners,
                onStartClick = onStartClick,
            )

            else -> Unit // 첫 프레임(로드 전)
        }
    }
}

@Composable
private fun ConversationPrepContent(
    prep: ConversationPrep,
    onBack: () -> Unit,
    onRefreshOpeners: () -> Unit,
    onStartClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 긴 화면: 남는 세로 공간을 전부 헤더↔말풍선 사이에 넣어, 말풍선부터 아래
        // (제목·칩·카드·버튼) 배열은 피그마 위치 그대로 유지 (사용자 결정).
        // 일러스트·여백은 FitDesign이 이미 화면 비율만큼 균일 축소하므로 추가 축소 안 함(shrink=1).
        //  (designScale을 또 곱하면 이중 축소 — 그 실수 되돌림). 극소형만 스크롤 폴백.
        // 판정은 maxHeight(FitDesign 안에서 뒤집힘)가 아니라 FitDesign 축소율로. (2026-07-11)
        val shrink = 1f
        val compact = LocalDesignScale.current <= 0.5f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .navigationBarsPadding(), // 끝이 시스템 네비에 안 가리게
        ) {
            // ── 상단 흰 영역 (CSS Frame 427321003): 헤더 + 일러스트 + 주제 ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (compact) Modifier else Modifier.weight(1f)) // 남는 공간은 흰 영역이 흡수
                    .background(White)
                    .statusBarsPadding()
                    .padding(bottom = 29.dp * shrink), // 흰 영역 끝(437) → 첫 마디 섹션(454) 사이 여백
            ) {
                Spacer(Modifier.height(8.dp)) // 상태바 → 헤더 (CSS Frame 361 top 48)
                // 헤더: 뒤로가기만 (제목은 디자인 개정으로 삭제됨)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape) // 눌림 효과 원형 (아이콘 버튼 관례)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        tint = Gray500,
                    )
                }

                Spacer(Modifier.height(45.dp * shrink)) // 헤더 끝(92) → 일러스트 틀(top 137) = 45 (CSS)
                if (!compact) Spacer(Modifier.weight(1f)) // 화면이 길어진 만큼은 전부 이 사이로

                // 말풍선 일러스트: PNG(423x423)가 피그마 141x141 틀의 3배수 export라
                // 안쪽 여백까지 그대로 담겨 있음 → 141x141로 그리면 피그마 배치와 동일.
                Image(
                    painter = painterResource(R.drawable.img_conversation_bubble),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(141.dp * shrink),
                )

                // 일러스트 틀 끝(278) → 제목(top 272): CSS상 6 겹침 — 겹치게는 못 그려서 0 (6px 차이 고지)

                Text(
                    text = "가볍게 시작하기 좋은 주제예요",
                    style = TqType.HeadingL.figma(),
                    color = Gray700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )

                Spacer(Modifier.height(24.dp * shrink)) // 제목 → 주제 칩 (CSS Frame 427321008 gap 24)

                // 추천 주제 칩 (서버 개수 가변, 표시 전용): 폭 맞춰 가운데 정렬 줄바꿈
                TopicChips(
                    topics = prep.topics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
            }

            // ── 바로 쓰는 첫 마디 (CSS Frame 427321161, 좌우 17) ──
            Column(
                modifier = Modifier.padding(start = 17.dp, end = 17.dp, top = 17.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 헤더 줄: 제목+설명 / 새로고침 (CSS Frame 413, space-between)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.padding(start = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "바로 쓰는 첫 마디",
                            style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold).figma(),
                            color = Gray900,
                        )
                        Text(
                            text = "대화의 시작을 도와줄 첫 마디예요.",
                            style = TqType.Caption.figma(),
                            color = Gray500,
                        )
                    }
                    // 새로고침: 리플 없이 (아이콘만). 새 첫 마디 조회.
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRefreshOpeners),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_conversation_refresh),
                            contentDescription = "첫 마디 새로고침",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // 첫 마디 카드들 (서버 개수 가변, 각 복사 가능) — CSS Frame 398 gap 8
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    prep.openers.forEach { opener ->
                        OpenerCard(text = opener)
                    }
                }
            }

            // 카드 끝이 하단 고정 버튼(52)+간격(12)+알약 영역(92)에 안 가리게.
            // 알약 몫 92는 축소 대상이 아니라 비율로 나눠 실제 크기 유지.
            Spacer(Modifier.height(64.dp + 92.dp / LocalDesignScale.current))
        }

        // 미션 시작하기 버튼 (CSS Frame 272: left 16, 폭 361 = 좌우 16): 알약 위 16에 고정.
        // 피그마(852) 기준에선 내용 바로 뒤 = 바닥 고정이 같은 위치 — 바닥 고정이 의도로 판단(사용자 결정).
        // 하단 92(알약 아래 12 + 알약 64 + 간격 16, CSS)는 알약이 축소 대상이 아니라 비율로 나눠 실제 크기 유지.
        TqButton(
            text = "미션 시작하기",
            onClick = onStartClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 92.dp / LocalDesignScale.current)
                .fillMaxWidth(),
        )
    }
}

// 추천 주제 칩 (CSS Frame 400~405): 흰 배경, radius 20, 카드그림자, pad 6x16, Body/M Gray800. 표시 전용.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicChips(topics: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        topics.forEach { topic ->
            Box(
                modifier = Modifier
                    .softShadow(color = Gray1000.copy(alpha = 0.04f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(text = topic, style = TqType.BodyM.figma(), color = Gray800)
            }
        }
    }
}

// 첫 마디 카드 (CSS Frame 395~397): 흰 배경 radius 8, 높이 44, 왼쪽 pad 12, 오른쪽 복사 버튼.
@Composable
private fun OpenerCard(text: String) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(White),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = TqType.BodyM.figma(),
            color = Gray800,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        // 복사: 누르면 원형 리플(가운데서 퍼지는 반투명 효과) + 클립보드 복사 + 토스트.
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape) // 리플이 동그랗게 퍼지도록
                .clickable {
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "복사했어요", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_conversation_copy),
                contentDescription = "복사",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── Preview ──
private val previewPrep = ConversationPrep(
    topics = listOf("오늘 날씨", "주말 계획", "좋아하는 음식", "최근 본 영화", "학교 생활", "취미 활동"),
    openers = listOf("안녕하세요! 처음 뵙겠습니다.", "오늘 하루 잘 보내고 계신가요?", "여기 분위기 좋네요!"),
)

@Preview(name = "대화 준비 (393dp 실기기)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ConversationPrepScreenPreview() {
    TalkQQuestTheme {
        ConversationPrepScreen(uiState = ConversationPrepUiState(prep = previewPrep), onBack = {}, onRetry = {})
    }
}

@Preview(name = "대화 준비 (320dp 좁은 화면)", showSystemUi = true, device = "spec:width=320dp,height=640dp")
@Composable
private fun ConversationPrepScreenNarrowPreview() {
    TalkQQuestTheme {
        ConversationPrepScreen(uiState = ConversationPrepUiState(prep = previewPrep), onBack = {}, onRetry = {})
    }
}
