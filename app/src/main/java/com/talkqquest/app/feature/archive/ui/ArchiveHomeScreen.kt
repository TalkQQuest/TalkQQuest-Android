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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ── 아카이브 화면 로컬 텍스트 도구 ──
// 💡 외부 Import 에러를 방지하기 위해 로컬에 직접 선언합니다.
// (피그마 시안과 안드로이드 텍스트의 줄간격(Line Height) 렌더링 차이를 맞추기 위한 도구입니다)
private val FullLeading = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)
internal fun TextStyle.figma(): TextStyle = copy(lineHeightStyle = FullLeading)

/**
 * 1. 화면 진입점 (Stateful)
 * ViewModel과 연동하여 상태(State)를 구독하고, 네비게이션 이벤트를 처리합니다.
 */
@Composable
fun ArchiveHomeScreen(
    viewModel: ArchiveHomeViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToList: (tabIndex: Int) -> Unit = {},
    onNavigateToDetail: (activityId: String) -> Unit = {},
    onNavigateToArchiveBox: () -> Unit = {}
) {
    // ViewModel의 상태를 생명주기에 안전하게 수집
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 순수 UI 컴포넌트로 상태와 이벤트 람다를 전달 (State Hoisting 패턴)
    ArchiveHomeScreen(
        uiState = uiState,
        onSearchClick = {
            onNavigateToSearch()
        },
        onArchiveBoxClick = {
            Toast.makeText(context, "보관함 상세: 준비 중인 기능입니다.", Toast.LENGTH_SHORT).show()
            onNavigateToArchiveBox()
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

/**
 * 2. 순수 UI 컴포넌트 (Stateless)
 * 상태(uiState) 데이터만 받아서 화면을 그립니다. 비즈니스 로직이 없어 프리뷰 테스트에 용이합니다.
 */
@Composable
private fun ArchiveHomeScreen(
    uiState: ArchiveHomeUiState,
    onSearchClick: () -> Unit,
    onArchiveBoxClick: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onActivityClick: (String) -> Unit
) = FitDesign {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // CSS: background: #F8FAFC; (전체 배경색)
            .background(Gray50),
        contentAlignment = Alignment.TopStart // 💡 요소들을 자유롭게 배치하기 위해 TopStart로 기준 변경
    ) {
        // API 데이터를 불러오는 동안 로딩 스피너 노출
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary600)
            }
        } else {
            // 메인 스크롤 콘텐츠
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding() // 하단 시스템 바(소프트키) 영역 확보
                    .statusBarsPadding(),    // 상단 상태 바(시계/배터리) 영역 확보
                contentPadding = PaddingValues(
                    // 💡 CSS 기준 top: 69px (상태바 40px 제외 시 29dp)
                    top = 29.dp,
                    bottom = 32.dp // 스크롤 시 최하단 여유 공간
                )
            ) {
                // ==========================================
                // [헤더 영역] 보관함 타이틀
                // ==========================================
                // 💡 (요청 반영) 검색 아이콘은 더 이상 이 타이틀과 같은 프레임(Row)에 묶여있지 않습니다!
                item {
                    // 💡 CSS Frame 427321237
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // 💡 "보관함" 텍스트와 꺾쇠 (CSS Frame 427321236: 77x32 -> 꺾쇠 크기 변경 반영 너비 79x32)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onArchiveBoxClick() }
                                .size(width = 79.dp, height = 32.dp)
                        ) {
                            // CSS: font-size 18px, weight 600, color #334155
                            Text(
                                text = "보관함",
                                style = TqType.TitleL.figma(),
                                color = Gray700,
                                modifier = Modifier.width(47.dp)
                            )
                            // 💡 꺾쇠 박스를 너비 32dp, 높이 30dp로 수정
                            Box(
                                modifier = Modifier.size(width = 32.dp, height = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "보관함 상세 보기",
                                    tint = Gray700,
                                )
                            }
                        }

                        // CSS: font-size 13px, weight 400, color #475569
                        Text(
                            text = "톡깨와 함께한 기록을\n보관하고 다시 볼 수 있어요",
                            style = TqType.BodyS.figma(),
                            color = Gray600,
                            modifier = Modifier.size(width = 137.dp, height = 40.dp)
                        )
                    }
                }

                // 💡 CSS Frame 427321240 gap: 24px
                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ==========================================
                // [카테고리 영역] 미션 / 대화 / 문장 / 리포트
                // ==========================================
                item {
                    // 💡 CSS Frame 427321172: height 93px, padding 0 16px
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(93.dp),
                        // 💡 CSS gap: 24px를 정확히 적용하고 남은 공간(좌우 16.5dp)은 자동 중앙 정렬로 padding을 대신합니다.
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

                // 💡 CSS Frame 427321240 gap: 24px
                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ==========================================
                // [최근 활동 리스트 영역]
                // ==========================================
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

                // 💡 CSS Frame 427321239 gap: 16px
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // 서버에서 내려온 최근 활동 데이터를 반복하여 카드 UI 렌더링
                // LazyColumn의 items를 활용하여 스크롤 성능 최적화
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    // 💡 CSS Frame 427321218 gap: 16px
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ==========================================
            // [검색 아이콘] CSS Frame 427321189
            // ==========================================
            // 💡 (요청 반영) 타이틀과 묶여있지 않고, position: absolute 특성을 살려 완전히 독립적으로 존재합니다.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd) // 우측 상단 정렬
                    .statusBarsPadding()
                    // 💡 CSS 기준 left: 343px, width: 44px -> 화면(393)에서 남은 우측 여백은 6dp
                    // 💡 CSS 기준 top: 48px -> 상태바(40px) 제외 시 상단 여백은 8dp
                    .padding(top = 8.dp, end = 6.dp)
                    .size(44.dp)
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

/**
 * 3. 카테고 개별 아이템 컴포넌트
 * 이미지 아이콘 + 라벨 텍스트 + 카운트 숫자로 구성된 컴포넌트입니다.
 */
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
            .clickable { onClick() }
            .size(width = 64.dp, height = 93.dp) // 💡 CSS Frame 427321168: 64x93
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp), // 💡 CSS Frame 427321164 gap: 4px
            modifier = Modifier.size(width = 64.dp, height = 73.dp)
        ) {
            // 아이콘 랩퍼 49x49
            Box(modifier = Modifier.size(49.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(49.dp)
                )
            }
            // CSS: Gray700 (라벨 텍스트)
            Text(
                text = label,
                style = TqType.BodyS.figma(),
                color = Gray700,
                modifier = Modifier.height(20.dp)
            )
        }
        // CSS: Purple600 (카운트 텍스트)
        Text(
            text = count.toString(),
            style = TqType.LabelL.figma(),
            color = Primary600,
            modifier = Modifier.height(20.dp)
        )
    }
}

// ── Preview ──
// [프리뷰] 개발 환경에서 안드로이드 스튜디오 디자인 탭으로 UI를 즉시 확인하기 위한 목업(Mock) 세팅
@Preview(name = "보관함 메인 (393dp)", showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ArchiveHomeScreenPreview() {
    val mockActivities = listOf(
        RecentActivity("1", ActivityType.MISSION, "처음 보는 사람에게 짧게 인사하기", "미션 완료", "2026.08.20", "쉬움", "짧은 대화", 2, 20),
        RecentActivity("2", ActivityType.CONVERSATION, "처음 보는 사람에게 짧게 인사하기", "대화 완료", "2026.08.20"),
        RecentActivity("3", ActivityType.SENTENCE, "\"그렇군요! 저도 편해서 놀랐 ...\"", "문장 저장", "2026.08.20"),
        RecentActivity("4", ActivityType.REPORT, "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20")
    )
    TalkQQuestTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(3, 3, 2, 3, mockActivities),
            onSearchClick = {},
            onArchiveBoxClick = {},
            onCategoryClick = {},
            onActivityClick = {}
        )
    }
}
