package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray1000
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow
import com.talkqquest.app.feature.mission.ui.figma // 💡 figma() 확장을 위해 추가

@Composable
fun ArchiveConversationCard(
    title: String,
    tags: List<String>,
    summary: String,
    date: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // 💡 [수정1] 1줄일 땐 148dp, 2줄일 땐 172dp로 자연스럽게 늘어나도록 가변 높이 적용
            .heightIn(min = 148.dp)
            .softShadow(color = Gray1000.copy(alpha = 0.01f), offsetY = 8.dp, blur = 24.dp, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 좌측 카테고리 아이콘
            Image(
                painter = painterResource(id = R.drawable.img_archive_conversation),
                contentDescription = null,
                modifier = Modifier
                    .size(49.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // 2. 우측 텍스트 및 정보 영역
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 타이틀
                Text(
                    text = title,
                    style = TqType.BodyL.copy(fontWeight = FontWeight.Medium).figma(),
                    color = Gray900,
                    // 💡 [수정1] 제목이 2줄까지 렌더링되도록 수정하고 고정 높이(24.dp) 제거
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 태그 및 요약 텍스트
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if (tags.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                // 💡 [수정2] 전체 컨테이너의 4dp 일괄 간격 대신 내부에서 10.dp씩 패딩을 주도록 제거
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                modifier = Modifier.height(22.dp)
                            ) {
                                tags.forEachIndexed { index, tag ->
                                    // 💡 CaptionL -> Caption으로 수정 (디자인 시스템 호환)
                                    Text(
                                        text = tag,
                                        style = TqType.Caption.figma(),
                                        color = Gray500
                                    )
                                    if (index < tags.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                // 💡 [수정2] 피그마 CSS 기준: 양옆에 각각 10px 간격을 완벽 반영
                                                .padding(horizontal = 10.dp)
                                                .width(1.dp)
                                                .height(9.dp)
                                                .background(Gray300)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(22.dp))
                        }

                        // 💡 BodyS의 경우 lineHeight 재정의 시 figma() 폰트 패밀리 유지 적용
                        Text(
                            text = summary,
                            style = TqType.BodyS.copy(lineHeight = 20.sp).figma(),
                            color = Gray600,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 하단 날짜 및 시간 영역
                    Row(
                        modifier = Modifier
                            .background(color = Gray100, shape = RoundedCornerShape(4.dp))
                            .padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 날짜
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 💡 기존 프로젝트에 존재하는 아이콘으로 대체 (달력/시계 아이콘이 없다면 mission_time 아이콘 재활용 또는 제거)
                            Image(
                                painter = painterResource(id = R.drawable.ic_archive_calendar),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = date,
                                style = TqType.LabelM.copy(fontWeight = FontWeight.Medium).figma(),
                                color = Gray400
                            )
                        }

                        // 구분선
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(11.dp)
                                .background(Gray300)
                        )

                        // 시간
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 💡 기존 프로젝트에 존재하는 아이콘으로 대체
                            Image(
                                painter = painterResource(id = R.drawable.ic_archive_time),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = time,
                                style = TqType.LabelM.copy(fontWeight = FontWeight.Medium).figma(),
                                color = Gray400
                            )
                        }
                    }
                }
            }
        }

        // 3. 우측 화살표 꺾쇠
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_forward_chevron),
                contentDescription = "상세 보기",
                tint = Gray500
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun ArchiveConversationCardPreview() {
    TalkQQuestTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ArchiveConversationCard(
                title = "처음 보는 사람에게 짧게 인사하기",
                tags = listOf("자기 성장", "첫 만남"),
                summary = "간단한 인사와 자기소개를 나누며 첫 만남의 어색함을 줄이고 대화를 시작했어요.",
                date = "2026.08.20",
                time = "14:35",
                onClick = {}
            )
            // 💡 2줄짜리 긴 제목이 들어갔을 때 자연스럽게 늘어나는지 테스트하는 프리뷰 추가
            ArchiveConversationCard(
                title = "대학교 전공과 진로에 대해 깊게 이야기 했어요",
                tags = listOf("학교", "진로"),
                summary = "서로의 전공과 관심 분야를 공유하고, 진로 고민에 대해 조언을 주고받았어요.",
                date = "2026.08.20",
                time = "14:35",
                onClick = {}
            )
        }
    }
}