package com.talkqquest.app.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import com.talkqquest.app.R
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

// 알림창(placeholder). 홈 상단 벨 → 이 화면.
// ⚠️ 디자이너 미완성: V7(UI 7차.css 116282) "알림창" 프레임이 상태바+제목만 있는 빈 껍데기라
//    실제 알림 리스트/빈 상태 디자인이 아직 없음. 지금은 헤더 + 빈 상태 문구만 두고,
//    디자인 나오면 본문(알림 카드 목록)만 채우면 됨. 헤더는 저장목록/미션상세와 같은 패턴.
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(8.dp)) // 상태바(40) → 헤더(top 48), 저장목록 헤더와 동일
        // 헤더: 뒤로가기 44 왼끝, 제목 화면 가로 정중앙 (미션 상세/저장목록 헤더와 같은 패턴)
        Box(modifier = Modifier.fillMaxWidth().height(44.dp)) {
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape) // 리플이 원형으로 퍼지도록 먼저 원형 클립
                    .clickable(
                        interactionSource = backInteraction,
                        indication = ripple(bounded = true, color = Primary600), // 원 안을 채우는 원형 물결(브랜드색으로 진하게)
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
                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
                color = Gray800,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // 본문: 아직 디자인 없음 → 빈 상태 문구만 (디자인 나오면 여기 알림 목록으로 교체)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "새로운 알림이 없어요",
                style = TqType.BodyM,
                color = Gray500,
            )
        }
    }
}

@Preview
@Composable
private fun NotificationScreenPreview() {
    TalkQQuestTheme {
        NotificationScreen()
    }
}
