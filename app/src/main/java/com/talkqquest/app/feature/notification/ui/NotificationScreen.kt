package com.talkqquest.app.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.feature.notification.data.model.NotificationUiItem
import com.talkqquest.app.feature.notification.viewmodel.NotificationUiState
import com.talkqquest.app.feature.notification.viewmodel.NotificationViewModel

// ── 알림창 (최신 시안 "알림창" 프레임 전사, 2026-07-22) ──
// 홈 상단 벨 → 이 화면. 배너(알림 설정 유도) + 알림 카드 목록.
// 콘텐츠 블록: left 16 · 폭 364(오른쪽 여백 13, 비대칭 주의) · top 100 · 세로 gap 16 (CSS Frame 427321612)

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NotificationScreen(uiState = uiState, onBack = onBack)
}

@Composable
private fun NotificationScreen(
    uiState: NotificationUiState,
    onBack: () -> Unit = {},
) = FitDesign { // 다른 화면들과 동일: 작은 화면에선 디자인(393x852) 통째 축소
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS Frame 427321597)
        // 헤더: 뒤로가기 44 왼끝 + 제목 "알림" 화면 정중앙 (CSS: Body/L Regular Gray800)
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = backInteraction,
                        indication = ripple(bounded = true, color = Primary600),
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_chevron),
                    contentDescription = "뒤로가기",
                    tint = Gray800,
                )
            }
            Text(
                text = "알림",
                style = TqType.BodyL, // CSS Body/L weight 400 (이전 Medium에서 시안 값으로 정정)
                color = Gray800,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Spacer(Modifier.height(8.dp)) // 헤더 끝(92) → 콘텐츠(top 100)

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary600) }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 13.dp), // CSS left 16 · 폭 364 → 오른쪽 13 (비대칭)
                verticalArrangement = Arrangement.spacedBy(16.dp), // CSS gap 16
            ) {
                item { NotificationSettingBanner() }
                items(uiState.items, key = { it.id }) { item -> NotificationCard(item) }
                if (uiState.items.isEmpty()) {
                    // 빈 상태 — 시안에 빈 화면 정의가 없어 기존 문구 유지 (디자인 나오면 교체)
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "새로운 알림이 없어요", style = TqType.BodyM, color = Gray500)
                        }
                    }
                }
            }
        }
    }
}

// 알림 설정 유도 배너 (CSS Frame 427321602): 364x68 · Gray100 · r16 · padding 12/6/12/16 · 오른끝 화살표.
// TODO(연결): 탭 시 이동할 알림 설정 화면이 시안에 아직 없음 — 화면 나오면 onClick 연결.
@Composable
private fun NotificationSettingBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notification_info),
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp)) // CSS 아이콘-문구 gap 12
        Text(
            text = "알림 받기를 설정하고 유용한 알림들을 받아보세요.",
            style = TqType.BodyM, // CSS 14/22 Gray600
            color = Gray600,
            modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_forward_chevron),
                contentDescription = null,
                tint = Gray600,
            )
        }
    }
}

// 알림 카드 (CSS Frame 427321609/427321610): 364x78 · White · r24 · padding 16.
// 위 작은 회색 줄(카테고리) + 아래 굵은 줄(내용), 오른쪽 위 시간 + 안읽음 보라 점(7).
@Composable
private fun NotificationCard(item: NotificationUiItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(White)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp), // CSS gap 2
        ) {
            Text(text = item.category, style = TqType.BodyS, color = Gray500)
            Text(
                text = item.body,
                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium), // CSS Body/L Medium
                color = Gray900,
            )
        }
        Row(verticalAlignment = Alignment.Top) {
            Text(text = item.timeText, style = TqType.BodyS, color = Gray400)
            if (item.isUnread) {
                Spacer(Modifier.width(3.dp)) // CSS gap 3
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp) // 시간 줄(20) 가운데쯤에 점이 오도록
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Primary600),
                )
            }
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun NotificationScreenPreview() {
    TalkQQuestTheme {
        NotificationScreen(
            uiState = NotificationUiState(
                items = listOf(
                    NotificationUiItem("1", "새로운 리포트가 도착했어요!", "지금 바로 리포트를 보러갈 수 있어요.", "방금", true),
                    NotificationUiItem("2", "새로운 기기에서 로그인되었어요", "이전 기기에서는 로그아웃 됩니다.", "1일 전", true),
                ),
            ),
        )
    }
}
