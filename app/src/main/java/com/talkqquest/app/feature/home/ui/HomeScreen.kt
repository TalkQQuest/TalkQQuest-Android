package com.talkqquest.app.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.component.TqButtonSize
import com.talkqquest.app.core.designsystem.component.TqCard
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.TodayMission
import com.talkqquest.app.feature.home.viewmodel.HomeUiState
import com.talkqquest.app.feature.home.viewmodel.HomeViewModel

// ── 화면 = 2단으로 분리 (state hoisting) ──
// (1) 아래 HomeScreen(viewModel): ViewModel과 연결하는 바깥 껍데기. 실제 앱에서 이걸 씀.
// (2) 아래 HomeScreen(uiState, onRetry): 상태를 "값으로만" 받아 그리는 부분.
//     → ViewModel/서버 없이도 그릴 수 있어서 Preview로 확인 가능(파일 맨 아래 참고).
// 이렇게 나누는 이유: 서버 없이 화면이 제대로 만들어졌는지 미리보기로 검증하려고.

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // collectAsStateWithLifecycle(): 화면이 안 보일 땐 구독을 멈춰 자원을 아낌.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onRetry = viewModel::loadHome,
    )
}

// 상태를 값으로 받아 그리는 부분(내부용). Preview는 이걸 직접 부른다.
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary50),
        contentAlignment = Alignment.Center,
    ) {
        when {
            // ① 로딩 중
            uiState.isLoading -> {
                CircularProgressIndicator(color = Primary600)
            }

            // ② 에러 — 메시지 + 다시 시도
            uiState.errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage,
                        style = TqType.BodyM,
                        color = Error,
                    )
                    Spacer(Modifier.height(16.dp))
                    TqButton(
                        text = "다시 시도",
                        onClick = onRetry,
                        size = TqButtonSize.Medium,
                    )
                }
            }

            // ③ 성공 — 실제 내용
            uiState.summary != null -> {
                HomeContent(summary = uiState.summary)
            }
        }
    }
}

// 성공 시 그리는 실제 내용. (홈 UI는 피그마 확정 후 이 안을 채워가면 됩니다.)
@Composable
private fun HomeContent(summary: HomeSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "${summary.nickname}님, 안녕하세요",
            style = TqType.HeadingL,
            color = Gray900,
        )
        Text(
            text = "Lv.${summary.level} · ${summary.currentXp} / ${summary.nextLevelXp} XP",
            style = TqType.BodyM,
            color = Gray500,
        )

        summary.todayMission?.let { mission ->
            TqCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "오늘의 미션", style = TqType.LabelM, color = Primary600)
                Spacer(Modifier.height(8.dp))
                Text(text = mission.title, style = TqType.TitleL, color = Gray900)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${mission.difficulty} · 약 ${mission.estimatedMinutes}분 · +${mission.rewardXp} XP",
                    style = TqType.BodyS,
                    color = Gray500,
                )
            }
        }
    }
}

// ── Preview: 서버 없이 세 가지 상태를 안드로이드 스튜디오에서 바로 확인 ──
// 파일을 열고 오른쪽 'Split'/'Design' 탭을 누르면 아래 3개가 그려집니다.
private val previewSummary = HomeSummary(
    nickname = "이도",
    level = 3,
    currentXp = 120,
    nextLevelXp = 300,
    todayMission = TodayMission(
        id = 1,
        title = "카페에서 직원에게 오늘 날씨 한마디 건네기",
        difficulty = "쉬움",
        estimatedMinutes = 5,
        rewardXp = 30,
    ),
    archiveCount = 12,
    communityCount = 4,
    questionOfDay = "요즘 가장 설렜던 순간은?",
)

@Preview(name = "홈 - 성공", showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(summary = previewSummary), onRetry = {})
    }
}

@Preview(name = "홈 - 로딩", showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(isLoading = true), onRetry = {})
    }
}

@Preview(name = "홈 - 에러", showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    TalkQQuestTheme {
        HomeScreen(uiState = HomeUiState(errorMessage = "네트워크 연결을 확인해주세요."), onRetry = {})
    }
}
