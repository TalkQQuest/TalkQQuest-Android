package com.talkqquest.app.feature.archive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
            .wrapContentHeight()
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 💡 [수정] 태그와 요약 내용의 존재 여부를 파악합니다.
                    val hasTags = tags.isNotEmpty()
                    val hasSummary = summary.isNotBlank()

                    // 둘 중 하나라도 있을 때만 영역을 그려 불필요한 여백을 제거합니다.
                    if (hasTags || hasSummary) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            if (hasTags) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    tags.forEachIndexed { index, tag ->
                                        Text(
                                            text = tag,
                                            style = TqType.Caption.figma(),
                                            color = Gray500
                                        )
                                        if (index < tags.lastIndex) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 10.dp)
                                                    .width(1.dp)
                                                    .height(9.dp)
                                                    .background(Gray300)
                                            )
                                        }
                                    }
                                }
                            }

                            if (hasSummary) {
                                Text(
                                    text = summary,
                                    style = TqType.BodyS.copy(lineHeight = 20.sp).figma(),
                                    color = Gray600,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
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
            // 💡 1. 정상적으로 태그와 1줄 요약이 있는 케이스
            ArchiveConversationCard(
                title = "처음 보는 사람에게 짧게 인사하기",
                tags = listOf("자기 성장", "첫 만남"),
                summary = "간단한 인사와 자기소개로 대화를 시작했어요.",
                date = "2026.08.20",
                time = "14:35",
                onClick = {}
            )
            // 💡 2. 긴 타이틀과 2줄 요약이 있는 케이스
            ArchiveConversationCard(
                title = "대학교 전공과 진로에 대해 깊게 이야기 했어요",
                tags = listOf("학교", "진로"),
                summary = "서로의 전공과 관심 분야를 공유하고, 진로 고민에 대해 조언을 주고받았어요. 아주 길게 적어보았습니다.",
                date = "2026.08.20",
                time = "14:35",
                onClick = {}
            )
            // 💡 3. 내용이 아무것도 없는 케이스 (여백 제거 테스트)
            ArchiveConversationCard(
                title = "대화를 진행하지 않고 바로 종료했어요",
                tags = emptyList(),
                summary = "",
                date = "2026.08.20",
                time = "14:35",
                onClick = {}
            )
        }
    }
}