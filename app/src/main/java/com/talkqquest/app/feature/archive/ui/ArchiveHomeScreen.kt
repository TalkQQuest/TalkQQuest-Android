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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // 💡 화면으로 돌아올 때마다(예: 상세 페이지에서 해제하고 백 버튼) 최신 카운트를 갱신합니다!
    LaunchedEffect(Unit) {
        viewModel.refreshData()
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
                    bottom = 32.dp
                )
            ) {
                // [헤더 영역] 보관함 타이틀
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
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

                item { Spacer(modifier = Modifier.height(24.dp)) }

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

                item { Spacer(modifier = Modifier.height(24.dp)) }

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
                    RecentActivityCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.id, activity.type) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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
        RecentActivity("2", ActivityType.CONVERSATION, "처음 보는 사람에게 짧게 인사하기", "대화 완료", "2026.08.20"),
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