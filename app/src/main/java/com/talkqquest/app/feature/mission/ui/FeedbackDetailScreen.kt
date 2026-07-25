package com.talkqquest.app.feature.mission.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary100
import com.talkqquest.app.core.designsystem.Primary500
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.component.TqButton
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.data.stubItemTexts
import com.talkqquest.app.feature.mission.data.model.FeedbackResult
import com.talkqquest.app.feature.mission.data.model.scoreItems
import com.talkqquest.app.feature.mission.data.model.textFor
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailUiState
import com.talkqquest.app.feature.mission.viewmodel.FeedbackDetailViewModel

// ── AI 피드백 상세 (CSS "AI 피드백 상세" 4프레임 전사 — 내용 공통, 배너 항목만 다름) ──
// 배너(항목명+점수) → 본문 카드(잘한 점 GREEN / 개선할 점 ORANGE / 베스트 문장+저장) → 다른 미션 버튼.
// 하단 네비 없음(사용자 결정 — 4프레임 중 질문 연결성에만 있어 실수로 판단, 디자이너 확인거리).
// CSS와 다른 점(합의됨):
//  - 다른 미션 버튼 색이 CSS상 프레임마다 다름(#6353F0/#536BF0/#8D53F0/#5356F0)
//    → 메인 컬러(Purple600) 통일이 최종 확정 (디자이너 결정 2026-07-11 — 한 번 4색 적용했다 재통일)
//  - 저장된 상태 북마크는 디자인이 없어 같은 모양 Purple600 채움(자작, 디자이너 확인거리)

// 칩 배경 2색은 디자인시스템 팔레트에 없는 CSS 전사 값 (글자색 GREEN=Success, ORANGE는 아래)
private val StrengthChipBg = Color(0xFFD9FAD4) // 잘한 점 칩 배경 (CSS)
private val ImproveChipBg = Color(0xFFFCF1E4) // 개선할 점 칩 배경 (CSS)
private val ImproveText = Color(0xFFEF8F22) // 개선할 점 글자 (CSS 주석 "ORANGE" — 팔레트엔 없음)

@Composable
fun FeedbackDetailScreen(
    onBack: () -> Unit = {},
    onOtherMissions: () -> Unit = {},
    viewModel: FeedbackDetailViewModel = hiltViewModel(),
    // ── C담당(아카이브) 연결 지점 — 문장 저장 시트 안에서 아카이브로 나가는 두 경로 ──
    onArchiveClick: () -> Unit = {}, // 시트 "보관함 >" → 아카이브 보관함(문장 탭)
    onPhraseClick: (String) -> Unit = {}, // 시트의 저장된 문장 카드 → 보관함 문장 상세
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedbackDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onOtherMissions = onOtherMissions,
        onTogglePhraseSave = viewModel::togglePhraseSave,
        onRetry = viewModel::loadFeedback,
        onToggleSavedPhrase = viewModel::toggleSavedPhrase,
        onDismissSaveSheet = viewModel::dismissSaveSheet,
        onArchiveClick = onArchiveClick,
        onPhraseClick = onPhraseClick,
    )
}

@Composable
private fun FeedbackDetailScreen(
    uiState: FeedbackDetailUiState,
    onBack: () -> Unit = {},
    onOtherMissions: () -> Unit = {},
    onTogglePhraseSave: () -> Unit = {},
    onRetry: () -> Unit = {},
    onToggleSavedPhrase: (String) -> Unit = {},
    onDismissSaveSheet: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onPhraseClick: (String) -> Unit = {},
) = FitDesign { // 작은 화면에선 디자인(393x852) 통째 축소 — 다른 화면들과 동일
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50), // 페이지 배경 Gray/50 BG (CSS)
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

            uiState.result != null ->
                // 베스트 문장을 저장하면 화면 위로 "저장됨" 시트가 올라옴 (UI 5차 "문장 저장시 바텀 시트").
                // 표준 시트라 배경 안 어두워지고 뒤 화면도 계속 스크롤 가능 — 미션·리포트 시트와 동일.
                FeedbackSaveSheetScaffold(
                    savedPhrase = uiState.saveSheetPhrase,
                    recentSavedPhrases = uiState.savedPhrases,
                    onDismiss = onDismissSaveSheet,
                    onToggleSave = onToggleSavedPhrase,
                    onArchiveClick = onArchiveClick,
                    onPhraseClick = onPhraseClick,
                ) {
                    FeedbackDetailContent(
                        result = uiState.result,
                        itemIndex = uiState.itemIndex,
                        isPhraseSaved = uiState.isPhraseSaved,
                        onBack = onBack,
                        onOtherMissions = onOtherMissions,
                        onTogglePhraseSave = onTogglePhraseSave,
                    )
                }
        }
    }
}

@Composable
private fun FeedbackDetailContent(
    result: FeedbackResult,
    itemIndex: Int,
    isPhraseSaved: Boolean,
    onBack: () -> Unit,
    onOtherMissions: () -> Unit,
    onTogglePhraseSave: () -> Unit,
) {
    // 배너에 보여줄 항목 — 범위 밖 값(딥링크 등)은 첫 항목으로
    val (label, score) = result.scoreItems().getOrElse(itemIndex) { result.scoreItems().first() }
    // 잘한 점·개선할 점·베스트 문장은 항목별 값 (없으면 피드백 공통 값으로 폴백)
    val itemText = result.textFor(label)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48) (CSS)
        // 헤더: 뒤로가기 44 왼끝만 (요약 화면과 동일, CSS chevron Gray/500)
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_chevron),
                    contentDescription = "뒤로가기",
                    tint = Gray500,
                )
            }
        }

        Spacer(Modifier.height(12.dp)) // 헤더 끝(92) → 콘텐츠(top 104) (CSS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // 서버 문구가 길어 카드가 화면을 넘치면 스크롤
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 15.dp), // 콘텐츠 열 left 16 / w362 → 오른쪽 15 (CSS, 홈과 동일 비대칭)
        ) {
            // 배너 (CSS Frame 427321050): Gray100 r20, 패딩 8x20, [항목명] ↔ [점수 /100]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gray100)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = label, style = TqType.TitleL.figma(), color = Primary600)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$score", style = TqType.Display.figma(), color = Primary500)
                    Spacer(Modifier.width(4.dp)) // 점수 ↔ 점 (CSS gap 4)
                    Text(text = "점", style = TqType.BodyL.figma(), color = Primary500)
                    Spacer(Modifier.width(4.dp)) // 점 ↔ /100 (CSS gap 4)
                    Text(text = "/100", style = TqType.BodyL.figma(), color = Gray500)
                }
            }

            Spacer(Modifier.height(16.dp)) // 배너 → 카드 (CSS gap 16)

            // 본문 카드 (CSS Frame 427321056): 흰 r20 + 카드 그림자, 패딩 20x16, 섹션 간격 40
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                FeedbackSection(
                    chipLabel = "잘한 점",
                    chipBg = StrengthChipBg,
                    chipTextColor = Success,
                    bullets = itemText.strengths,
                )
                // 잘한점 → 개선할점 34 (CSS는 40이지만 잘한점 프레임이 내용(128)보다 6 작은
                // 122로 잠겨 있어, 실렌더 기준 다음 칩까지 거리 = 40-6. 카드 총높이 406 검산 일치)
                Spacer(Modifier.height(34.dp))
                FeedbackSection(
                    chipLabel = "개선할 점",
                    chipBg = ImproveChipBg,
                    chipTextColor = ImproveText,
                    bullets = itemText.improvements,
                )
                Spacer(Modifier.height(40.dp)) // 개선할점 → 베스트 문장 (CSS gap 40)
                BestPhraseSection(
                    phrase = itemText.savedPhrase,
                    isSaved = isPhraseSaved,
                    onToggleSave = onTogglePhraseSave,
                )
            }

            Spacer(Modifier.height(24.dp)) // 카드 → 버튼 (CSS Frame 427321061 gap 24)

            OtherMissionsButton(nickname = result.nickname, onClick = onOtherMissions)
            Spacer(Modifier.height(16.dp)) // 스크롤 끝 여백 (내용이 길 때 버튼이 바닥에 안 붙게)
        }
    }
}

// 잘한 점/개선할 점 섹션 (CSS Frame 427321052/427321053): 칩 + 불릿 목록 (칩↔불릿 12, 불릿 간 8)
@Composable
private fun FeedbackSection(
    chipLabel: String,
    chipBg: Color,
    chipTextColor: Color,
    bullets: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(chipBg)
                .padding(horizontal = 12.dp, vertical = 4.dp), // 칩 패딩 (CSS 4 12)
        ) {
            Text(text = chipLabel, style = TqType.LabelL.figma(), color = chipTextColor)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            bullets.forEach { line ->
                // 각 줄 앞 불릿 점(•) — CSS 텍스트 레이어엔 없지만 디자인 스샷에 있음(사용자 확인)
                Row {
                    Text(text = "•", style = TqType.BodyL.figma(), color = Gray800, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(8.dp))
                    // keepWordsIntact: 어절 중간 줄바꿈("좋/은") 방지 + glueShortWords: 한 글자 어절 고아 방지
                    // (홈·미션 카드와 동일). lineBreak Paragraph: 줄 길이를 고르게 배분해 오른쪽 끝 정돈.
                    // end 18 = 왼쪽 글자 들임(점 4+점 폭 6+간격 8)만큼 오른쪽도 최소 여백(사용자 결정).
                    Text(
                        text = line.keepWordsIntact().glueShortWords(),
                        style = TqType.BodyL.figma().copy(lineBreak = LineBreak.Paragraph),
                        color = Gray800,
                        modifier = Modifier.padding(end = 18.dp),
                    )
                }
            }
        }
    }
}

// 베스트 문장 섹션 (CSS Frame 427321055): [칩 + 저장 북마크] / "‟문장”" 인용 행
@Composable
private fun BestPhraseSection(
    phrase: String,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { // 헤더 ↔ 인용 (CSS gap 10)
        // 헤더행 높이 = 28 (CSS Frame 427321188): 40짜리 북마크 버튼이 행 밖으로 위아래 6씩
        // 삐져나오는 배치 — 행을 40으로 잡으면 카드가 12 길어져 아래 버튼까지 밀림 (실측 확인)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(28.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary100)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(text = "베스트 문장", style = TqType.LabelL.figma(), color = Primary600)
            }
            // 문장 저장 토글 (CSS 40x40 터치영역, favourite 20). 저장 = 보라 채움(자작).
            // 물결 없이 색 변화만으로 반응 표시 (사용자 결정).
            // TODO(서버 연동): 저장 시 아카이브 '문장'으로 — 지금은 화면 토글만.
            Box(
                modifier = Modifier
                    .offset(x = (-4).dp) // CSS 칩 margin -4: 버튼이 칩에 4 겹침
                    .requiredSize(40.dp) // 행(28)보다 큰 터치영역 — 위아래로 흘러넘침 (CSS 배치)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleSave,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (isSaved) R.drawable.ic_feedback_phrase_save_filled
                        else R.drawable.ic_feedback_phrase_save,
                    ),
                    contentDescription = if (isSaved) "문장 저장됨" else "문장 저장",
                    tint = if (isSaved) Color.Unspecified else Gray400,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        // 인용 (CSS Frame 427321054, 사용자 결정 최종):
        // - 따옴표↔문장 간격 18 고정 = 피그마 스크린샷 비율 실측(레이어 Alt 측정 9.5는 상자 기준이라
        //   잉크 기준 시각 간격과 다름 — 피그마/앱 캡처 비교로 확정).
        //   화면이 좁아지면 FitDesign 통째 축소로만 비례해서 줄어듦, 넓다고 안 벌어짐.
        // - 한 줄이면 문장+따옴표가 가운데(뒤 Spacer 28 = 여는 따옴표 10+18과 대칭이라 정중앙).
        // - 여러 줄이면 왼쪽 정렬 + 둘째 줄부터 들여쓰기가 따옴표 자리가 아닌 첫 줄 글자 시작 x에 맞음
        //   (따옴표가 문장 상자 밖에 있어 자동으로 그렇게 됨).
        // - 닫는 따옴표는 마지막 줄 글자 끝에서 9.5 (글자 배치 결과의 마지막 줄 끝 좌표에 그림).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp), // 인용 행 좌우 여백 (CSS)
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            Text(text = "‟", style = TqType.HeadingM.figma(), color = Gray400)
            Spacer(Modifier.width(18.dp))
            var closeMarkPos by remember(phrase) { mutableStateOf<Offset?>(null) }
            Box(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = phrase.keepWordsIntact().glueShortWords(), // 인용문도 어절 중간 줄바꿈 방지
                    style = TqType.BodyL.figma(),
                    color = Gray600,
                    onTextLayout = { result ->
                        val last = result.lineCount - 1
                        closeMarkPos = Offset(result.getLineRight(last), result.getLineTop(last))
                    },
                )
                closeMarkPos?.let { pos ->
                    Text(
                        text = "”",
                        style = TqType.HeadingM.figma(),
                        color = Gray400,
                        modifier = Modifier.offset {
                            IntOffset((pos.x + 18.dp.toPx()).roundToInt(), pos.y.roundToInt())
                        },
                    )
                }
            }
            // 닫는 따옴표(10)+간격(18) 몫 28에서 2 줄임 — 1.0 배율에서 목업 문장(274px)이
            // 가용 폭(273px)에 1px 모자라 감기던 것 보정 (한 줄 중앙이 1dp 왼쪽으로 — 시각 무의미)
            Spacer(Modifier.width(26.dp))
        }
    }
}

// "소다123님을 위한 다른 미션 보러가기" 버튼 (CSS Frame 427321060): 362x56 r16.
// 배경 = 메인 컬러 통일(디자이너 확정). 다트 이미지(66x70, 1.56도 기울임)가
// 버튼 위로 삐져나오는 디자인이라 배경만 clip하고 내용은 안 자름.
@Composable
private fun OtherMissionsButton(nickname: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp)) // r20 → r16 (디자인 변경 2026-07)
                .background(Primary600)
                .clickable(onClick = onClick),
        )
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 8.dp), // 왼쪽 여백 (CSS)
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 다트 이미지: 피그마 fill 클릭 export(180x189 = 66x70의 2.7배, 자르기 반영본).
            // 같은 그림의 원본(394x412, CSS 파일명이 가리키는 쪽)은 여백이 넓어 다트가 뜨게 앉음 — 주의.
            // CSS 배치(left -6, top -20)를 중심 이동으로 환산해 위로 14 올림(버튼 위로 삐져나옴).
            Box(modifier = Modifier.size(width = 55.dp, height = 56.dp)) {
                Image(
                    painter = painterResource(R.drawable.img_feedback_dart),
                    contentDescription = null,
                    modifier = Modifier
                        .requiredSize(width = 66.dp, height = 70.dp)
                        .offset(y = (-14).dp)
                        .rotate(1.56f), // CSS transform rotate(1.56deg)
                )
            }
            Spacer(Modifier.width(12.dp)) // 이미지 ↔ 글자 (CSS gap 12)
            Text(
                text = "${nickname}님을 위한 다른 미션 보러가기",
                style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold), // CSS 16/24 600
                color = Gray100,
            )
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_forward_chevron), // CSS chevron 좌우반전(수치 동일)
                contentDescription = null,
                tint = Gray100,
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

// ── Preview ──
private val previewResult = FeedbackResult(
    missionTitle = "처음 보는 사람에게 짧게 인사하기",
    nickname = "소다123",
    kindnessScore = 92,
    initiativeScore = 88,
    empathyScore = 85,
    questionLinkScore = 78,
    strengths = listOf(
        "상대를 존중하는 표현을 사용했어요",
        "대화를 따뜻하게 시작했어요",
        "긍정적인 말투를 유지했어요",
    ),
    improvements = listOf(
        "조금 더 구체적인 칭찬을 해보세요",
        "상대의 감정을 확인하는 표현을 사용해보세요",
    ),
    savedPhrase = "그렇군요! 저도 생각보다 편해서 놀랐어요",
    itemTexts = stubItemTexts, // 항목별 문구 (에뮬과 같은 stub)
)

@Preview(name = "피드백 상세 393dp", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "피드백 상세 360dp", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FeedbackDetailScreenPreview() {
    TalkQQuestTheme {
        FeedbackDetailScreen(
            uiState = FeedbackDetailUiState(isLoading = false, result = previewResult, itemIndex = 0),
        )
    }
}

// ── 4개 항목 문구 비교용 (줄바꿈·자간·불릿 정렬 확인) ──
@Composable
private fun FeedbackDetailItemPreview(index: Int) {
    TalkQQuestTheme {
        FeedbackDetailScreen(
            uiState = FeedbackDetailUiState(isLoading = false, result = previewResult, itemIndex = index),
        )
    }
}

@Preview(name = "1행 친절한 태도 (92)", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun FeedbackDetailItem0Preview() = FeedbackDetailItemPreview(0)

@Preview(name = "2행 대화 주도 (88·긴 문장)", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun FeedbackDetailItem1Preview() = FeedbackDetailItemPreview(1)

@Preview(name = "3행 공감 능력 (85·짧은 문장)", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun FeedbackDetailItem2Preview() = FeedbackDetailItemPreview(2)

@Preview(name = "4행 질문 연결성 (78·기호 포함)", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun FeedbackDetailItem3Preview() = FeedbackDetailItemPreview(3)

// 저장된 상태 + 다른 항목 배너 확인용
@Preview(name = "피드백 상세 - 질문 연결성/저장됨", widthDp = 393, heightDp = 852, showBackground = true)
@Composable
private fun FeedbackDetailSavedPreview() {
    TalkQQuestTheme {
        FeedbackDetailScreen(
            uiState = FeedbackDetailUiState(
                isLoading = false,
                result = previewResult,
                itemIndex = 3,
                isPhraseSaved = true,
            ),
        )
    }
}
