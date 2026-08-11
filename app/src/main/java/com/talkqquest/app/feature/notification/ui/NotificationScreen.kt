package com.talkqquest.app.feature.notification.ui

import android.app.ActivityOptions
import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
    onWeeklyReportClick: () -> Unit = {}, // 주간 비교 리포트 알림의 화살표
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationManager = remember(context) {
        context.getSystemService(NotificationManager::class.java)
    }
    var notificationsEnabled by remember {
        mutableStateOf(notificationManager.areNotificationsEnabled())
    }
    // 시스템 설정에서 알림을 켜고 돌아오면 안내 카드가 즉시 사라지도록 다시 확인한다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsEnabled = notificationManager.areNotificationsEnabled()
    }
    val closeNotifications: () -> Unit = {
        viewModel.readVisibleNotifications()
        onBack()
    }

    BackHandler(onBack = closeNotifications)
    NotificationScreen(
        uiState = uiState,
        onBack = closeNotifications,
        showNotificationSettingsBanner = !notificationsEnabled,
        onNotificationSettingsClick = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            val transition = ActivityOptions.makeCustomAnimation(
                context,
                R.anim.notification_slide_in_right,
                R.anim.notification_slide_out_left,
            )
            context.startActivity(intent, transition.toBundle())
        },
        onNotificationClick = { item ->
            viewModel.readNotification(item.id)
            if (item.hasLink) {
                onWeeklyReportClick()
            }
        },
    )
}

@Composable
private fun NotificationScreen(
    uiState: NotificationUiState,
    onBack: () -> Unit = {},
    showNotificationSettingsBanner: Boolean = true,
    onNotificationSettingsClick: () -> Unit = {},
    onNotificationClick: (NotificationUiItem) -> Unit = {},
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
                    tint = Gray500, // CSS Icon border 2px #64748B = Gray/500 (제목만 Gray/800)
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
                    .padding(start = 16.dp, end = 13.dp)
                    .navigationBarsPadding(), // 마지막 알림 항목이 시스템 네비게이션 바 위에서 끝나도록 확보
                verticalArrangement = Arrangement.spacedBy(16.dp), // CSS gap 16
                contentPadding = PaddingValues(bottom = 16.dp), // 마지막 알림 항목과 시스템 네비게이션 바 사이 여백
            ) {
                if (showNotificationSettingsBanner) {
                    item { NotificationSettingBanner(onClick = onNotificationSettingsClick) }
                }
                items(uiState.items, key = { it.id }) { item ->
                    NotificationCard(item, onClick = { onNotificationClick(item) })
                }
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
@Composable
private fun NotificationSettingBanner(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically, // CSS Frame 427321602 align-items: center
    ) {
        // CSS Frame 427321601: 아이콘·문구 묶음은 align-items: flex-start (상단 정렬)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
        ) {
            // CSS information-circle-contained 24x26 (padding 4px 3px) 안에 Icon 18x18.
            // 컨테이너를 생략하면 문구 시작점이 6dp 왼쪽으로 당겨진다.
            Box(
                modifier = Modifier.width(24.dp).height(26.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_info),
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(12.dp)) // CSS 아이콘-문구 gap 12
            // CSS 텍스트는 width 238 · height 44 · line-height 22 → 44/22 = 정확히 2줄.
            // 폭 238에 그냥 흘리면 "받아"까지 첫 줄에 들어가므로(실측 234.33dp) 시안과 달라진다.
            // = 시안에 수동 줄바꿈이 들어있다는 뜻 → 같은 위치에서 끊는다.
            Text(
                text = "알림 받기를 설정하고 유용한 알림들을\n받아보세요.",
                style = TqType.BodyM, // CSS 14/22 Gray600
                color = Gray600,
                modifier = Modifier.weight(1f),
            )
        }
        // CSS엔 gap 30으로 적혀 있으나 그 값만 나머지와 안 맞는다(합이 370 > 364).
        // 다른 값들(364·16·6·44·274=24+12+238)은 서로 맞물리므로 gap을 역산: 364-16-274-44-6 = 24.
        // 이 값이라야 문구 폭이 CSS의 238과 정확히 일치한다.
        Spacer(Modifier.width(24.dp))
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
//
// ★주간 비교 리포트 알림(CSS Frame 427321769)만 아래 줄 옆에 화살표가 붙는다.
//   화살표가 카드 오른쪽 끝이 아니라 문구 바로 옆에 있는 것 = 그 줄이 링크라는 뜻.
//   눌러서 바로 주간 비교 리포트로 가는 통로라서 붙였고, 나머지 알림은 읽고 마는 것이라 없다.
//   치수 주의: 그 프레임은 텍스트 열이 46인데 화살표 자리는 44로 적혀 있어 합이 안 맞는다
//   (46 = 20 + 2 + 24 여야 카드 78이 성립). 화살표는 줄 높이를 늘리지 않고 옆에 얹는다.
@Composable
private fun NotificationCard(item: NotificationUiItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // CSS는 78 고정이지만 그 목업 문구가 전부 한 줄짜리였다("이전 기기에서는 로그아웃 됩니다.").
            // 서버 실제 문구는 더 길어 두 줄이 되고, 78로 잠그면 아랫줄이 카드 밖으로 잘린다(실기기 확인).
            // → 최소 78을 지키되 내용에 맞춰 늘어나게 한다(사용자 결정 2026-08-10).
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(White)
            // 홈의 "다른 미션 보기"와 같은 기본 눌림 애니메이션을 사용한다.
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp), // CSS gap 2
        ) {
            Text(text = item.category, style = TqType.BodyS, color = Gray500)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.body,
                    // 어절 중간에서 끊기지 않게 — 서버 문구라 길이가 제각각이고,
                    // 실제로 "미션을 완료했/습니다."처럼 갈렸다(실기기 확인).
                    // 한 어절이 열 폭을 넘을 때만 그 안에서 자른다(긴 미션 제목 대비).
                    // 카드는 최대 2줄이라 줄 길이 균등 배분(HighQuality)은 필요 없어
                    // 보관함 대화 상세·대화 말풍선과 같은 Simple/Normal 조합을 쓴다.
                    style = TqType.BodyL.copy(
                        fontWeight = FontWeight.Medium, // CSS Body/L Medium
                        lineBreak = LineBreak(
                            strategy = LineBreak.Strategy.Simple,
                            strictness = LineBreak.Strictness.Normal,
                            wordBreak = LineBreak.WordBreak.Phrase,
                        ),
                    ),
                    color = Gray900,
                )
                if (item.hasLink) {
                    Icon(
                        painter = painterResource(R.drawable.ic_forward_chevron),
                        contentDescription = null,
                        tint = Gray600, // CSS Icon border 2px #475569
                    )
                }
            }
        }
        // CSS Frame 427321606/427321608: align-items: flex-start · gap 3 → 점도 상단 정렬
        Row(verticalAlignment = Alignment.Top) {
            Text(text = item.timeText, style = TqType.BodyS, color = Gray400)
            if (item.isUnread) {
                Spacer(Modifier.width(3.dp)) // CSS gap 3
                Box(
                    modifier = Modifier
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
