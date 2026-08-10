package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity

private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
internal fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

@Composable
fun ArchiveHomeScreen(
    viewModel: ArchiveHomeViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToList: (tabIndex: Int) -> Unit = {},
    onNavigateToDetail: (activityId: String, type: ActivityType) -> Unit = { _: String, _: ActivityType -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 💡 화면이 보여질 때(ON_RESUME)마다 최신 데이터를 갱신합니다.[cite: 26]
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ArchiveHomeScreen(
        uiState = uiState,
        onSearchClick = {
            onNavigateToSearch()
        },
        onArchiveBoxClick = {
            onNavigateToList(0)
        },
        onCategoryClick = { tabIndex ->
            onNavigateToList(tabIndex)
        },
        onActivityClick = { activityId, type ->
            onNavigateToDetail(activityId, type)
        }
    )
}

@Composable
private fun ArchiveHomeScreen(
    uiState: ArchiveHomeUiState,
    onSearchClick: () -> Unit,
    onArchiveBoxClick: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onActivityClick: (String, ActivityType) -> Unit
) = FitDesign {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentAlignment = Alignment.TopStart
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary600)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    top = 29.dp,
                    // 💡 [수정됨] 하단 네비게이션 바(플로팅)에 카드가 가려지지 않도록 하단 여백을 대폭 늘림 (32.dp -> 120.dp)[cite: 26]
                    bottom = 120.dp
                )
            ) {
                // [헤더 영역] 보관함 타이틀
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp) // 💡 디자이너 피드백: "보관함과 아래 '톡깨와~' 문장 사이 간격 0->2"[cite: 26]
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .size(width = 79.dp, height = 32.dp)
                                .clip(CircleShape)
                                .clickable { onArchiveBoxClick() }
                        ) {
                            Text(
                                text = "보관함",
                                style = TqType.TitleL.figma(),
                                color = Gray700,
                                modifier = Modifier.width(47.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 32.dp, height = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_forward_chevron),
                                    contentDescription = "보관함 전체 보기",
                                    tint = Gray700,
                                )
                            }
                        }

                        Text(
                            text = "톡깨와 함께한 기록을\n보관하고 다시 볼 수 있어요",
                            style = TqType.BodyS.figma(),
                            color = Gray600,
                            modifier = Modifier.size(width = 137.dp, height = 40.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) } // 💡 헤더(타이틀)와 카테고리 사이는 기존대로 24.dp 유지[cite: 26]

                // [카테고리 영역]
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(93.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArchiveCategoryItem(
                            iconRes = R.drawable.img_archive_mission,
                            label = "미션",
                            count = uiState.completedMissionCount
                        ) { onCategoryClick(0) }

                        ArchiveCategoryItem(
                            iconRes = R.drawable.img_archive_conversation,
                            label = "대화",
                            count = uiState.conversationCount
                        ) { onCategoryClick(1) }

                        ArchiveCategoryItem(
                            iconRes = R.drawable.img_archive_sentence,
                            label = "문장",
                            count = uiState.savedSentenceCount
                        ) { onCategoryClick(2) }

                        ArchiveCategoryItem(
                            iconRes = R.drawable.img_archive_report,
                            label = "리포트",
                            count = uiState.reportCount
                        ) { onCategoryClick(3) }
                    }
                }

                item { Spacer(modifier = Modifier.height(36.dp)) } // 💡 디자이너 피드백: 카테고리 섹션과 최근 활동 섹션 사이 간격 36.dp로 변경[cite: 26]

                // [최근 활동 리스트 영역]
                item {
                    Text(
                        text = "최근 활동",
                        style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                        color = Gray700,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(24.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                items(uiState.recentActivities) { activity ->
                    if (activity.type == ActivityType.CONVERSATION) {
                        ArchiveConversationCard(
                            title = activity.title,
                            tags = activity.tags, // 💡 실제 데이터 매핑으로 수정
                            summary = activity.summary ?: "", // 💡 실제 데이터 매핑으로 수정
                            date = activity.date,
                            time = "14:35", // 💡 TODO: API의 createdAt에서 시간(HH:mm) 파싱 필요[cite: 26]
                            onClick = { onActivityClick(activity.id, activity.type) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        RecentActivityCard(
                            activity = activity,
                            onClick = { onActivityClick(activity.id, activity.type) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // [검색 아이콘]
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 6.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_archive_search),
                    contentDescription = "검색",
                    tint = Gray500
                )
            }
        }
    }
}

@Composable
private fun ArchiveCategoryItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(width = 64.dp, height = 93.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.size(width = 64.dp, height = 73.dp)
        ) {
            Box(modifier = Modifier.size(49.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(49.dp)
                )
            }
            Text(
                text = label,
                style = TqType.BodyS.figma(),
                color = Gray700,
                modifier = Modifier.height(20.dp)
            )
        }
        Text(
            text = count.toString(),
            style = TqType.LabelL.figma(),
            color = Primary600,
            modifier = Modifier.height(20.dp)
        )
    }
}

@Preview(name = "보관함 메인 (393dp)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveHomeScreenPreview() {
    val mockActivities = listOf(
        RecentActivity("1", ActivityType.MISSION, "처음 보는 사람에게 짧게 인사하기", "미션 완료", "2026.08.20", "쉬움", "짧은 대화", 2, 20),
        // 💡 프리뷰가 깨지지 않게 tags와 summary 데이터 추가
        RecentActivity("2", ActivityType.CONVERSATION, "처음 보는 사람에게 짧게 인사하기", "대화 완료", "2026.08.20", tags = listOf("자기 성장", "첫 만남"), summary = "간단한 인사와 자기소개를 나누며 첫 만남의 어색함을 줄이고 대화를 시작했어요."),
        RecentActivity("3", ActivityType.SENTENCE, "\"그렇군요! 저도 편해서 놀랐습니다.\"", "문장 저장", "2026.08.20"),
        RecentActivity("4", ActivityType.REPORT, "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20")
    )
    TalkQQuestTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(3, 3, 2, 3, mockActivities),
            onSearchClick = {},
            onArchiveBoxClick = {},
            onCategoryClick = {},
            onActivityClick = { _: String, _: ActivityType -> }
        )
    }
}