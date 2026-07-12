package com.talkqquest.app.feature.archive.ui

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.feature.archive.viewmodel.ActivityType
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeUiState
import com.talkqquest.app.feature.archive.viewmodel.ArchiveHomeViewModel
import com.talkqquest.app.feature.archive.viewmodel.RecentActivity

@Composable
fun ArchiveHomeScreen(
    viewModel: ArchiveHomeViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToList: (tabIndex: Int) -> Unit = {},
    onNavigateToDetail: (activityId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ArchiveHomeScreen(
        uiState = uiState,
        onSearchClick = {
            Toast.makeText(context, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
            onNavigateToSearch()
        },
        onCategoryClick = { tabIndex ->
            Toast.makeText(context, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
            onNavigateToList(tabIndex)
        },
        onActivityClick = { activityId ->
            Toast.makeText(context, "준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
            onNavigateToDetail(activityId)
        }
    )
}

@Composable
private fun ArchiveHomeScreen(
    uiState: ArchiveHomeUiState,
    onSearchClick: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onActivityClick: (String) -> Unit
) = FitDesign {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // CSS: background: #F8FAFC;
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = Primary600)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    top = 16.dp, // 상단 시작 여백 (status bar 이후)
                    bottom = 32.dp // 하단 네비게이션 바가 제거되었으므로 기본 여백으로 축소
                )
            ) {
                // 1. Top Bar (제목 + 검색)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 15.dp, bottom = 16.dp), // 제목 영역 위치 조정
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            // CSS: font-size 18px, weight 600, color #334155
                            Text(
                                text = "아카이브",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            // CSS: font-size 13px, weight 400, color #475569
                            Text(
                                text = "톡깨와 함께한 기록을\n보관하고 다시 볼 수 있어요",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF475569),
                                lineHeight = 20.sp
                            )
                        }

                        // 터치 영역(44x44) 확보 및 아이콘(24x24) 배치
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { onSearchClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_archive_search),
                                contentDescription = "검색",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // 2. Categories
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp) // CSS: gap 24px
                    ) {
                        ArchiveCategoryItem(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.img_archive_mission,
                            label = "완료한 미션",
                            count = uiState.completedMissionCount
                        ) { onCategoryClick(0) }

                        ArchiveCategoryItem(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.img_archive_conversation,
                            label = "대화 기록",
                            count = uiState.conversationCount
                        ) { onCategoryClick(1) }

                        ArchiveCategoryItem(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.img_archive_sentence,
                            label = "저장한 문장",
                            count = uiState.savedSentenceCount
                        ) { onCategoryClick(2) }

                        ArchiveCategoryItem(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.img_archive_report,
                            label = "리포트",
                            count = uiState.reportCount
                        ) { onCategoryClick(3) }
                    }
                }

                // 3. 최근 활동 타이틀
                item {
                    Text(
                        text = "최근 활동",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155), // Gray700
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    )
                }

                // 최근 활동 리스트
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp) // 아이템 간 상하 여백
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveCategoryItem(
    modifier: Modifier = Modifier, // 💡 Modifier 추가로 외부에서 weight 적용 가능
    iconRes: Int,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() } // 💡 고정폭(width 64dp)을 지우고 modifier 적용
    ) {
        // 아이콘 랩퍼 49x49
        Box(modifier = Modifier.size(49.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(49.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF334155), // CSS: Gray700
            lineHeight = 20.sp
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6353F0), // CSS: Purple600
            lineHeight = 20.sp
        )
    }
}

// ── Preview ──
@Preview(name = "아카이브 메인 (393dp)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveHomeScreenPreview() {
    val mockActivities = listOf(
        RecentActivity("1", ActivityType.MISSION, "처음 보는 사람에게 짧게 인사하기", "미션 완료", "2026.08.20"),
        RecentActivity("2", ActivityType.CONVERSATION, "처음 보는 사람에게 짧게 인사하기", "대화 완료", "2026.08.20"),
        RecentActivity("3", ActivityType.SENTENCE, "\"그렇군요! 저도 편해서 놀랐 ...\"", "문장 저장", "2026.08.20"),
        RecentActivity("4", ActivityType.REPORT, "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20")
    )
    TalkQQuestTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(3, 3, 2, 3, mockActivities),
            onSearchClick = {},
            onCategoryClick = {},
            onActivityClick = {}
        )
    }