package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

@Composable
fun ProfileWithdrawScreen(
    onBack: () -> Unit = {},
) = FitDesign {
    var agreed by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            ProfileSimpleTopBar(title = "탈퇴하기", onBack = onBack)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "회원 탈퇴 전 주의사항",
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
            )
            Spacer(Modifier.height(21.dp))
            Text(
                text = "탈퇴 시 모든 서비스 이용내역이 삭제되며 복구가 불가능합니다.\n또한 같은 계정 정보로 재가입이 7일 동안 불가능 합니다.",
                style = TqType.BodyM,
                color = Gray700,
            )
            Spacer(Modifier.height(30.dp))
            Text(
                text = "삭제 사항",
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = Gray800,
            )
            Spacer(Modifier.height(22.dp))
            WithdrawBullet("계정 정보 및 사용자 설정 정보")
            Spacer(Modifier.height(22.dp))
            WithdrawBullet("미션 기록 및 대화 기록")
            Spacer(Modifier.height(22.dp))
            WithdrawBullet("AI 피드백 및 리포트")
            Spacer(Modifier.height(22.dp))
            WithdrawBullet("그 외 모든 구매 기록 및 정보")
            Spacer(Modifier.height(27.dp))
            Row(
                modifier = Modifier.clickable { agreed = !agreed },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (agreed) Color(0xFFFF4A4A) else Gray200),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (agreed) White else Gray500,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.size(11.dp))
                Text(
                    text = "위 주의 사항을 모두 확인했으며, 탈퇴에 동의합니다.",
                    style = TqType.BodyM.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFFFF4A4A),
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Primary600)
                    .clickable(enabled = agreed) { showConfirm = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "탈퇴하기",
                    style = TqType.BodyL.copy(fontWeight = FontWeight.Bold),
                    color = White,
                )
            }
            Spacer(Modifier.height(54.dp))
        }

        if (showConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF273449).copy(alpha = 0.24f))
                    .clickable { showConfirm = false },
            )
            WithdrawConfirmDialog(
                onCancel = { showConfirm = false },
                onConfirm = { showConfirm = false },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun WithdrawBullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = "·", style = TqType.BodyL, color = Gray800)
        Spacer(Modifier.size(10.dp))
        Text(text = text, style = TqType.BodyL, color = Gray800)
    }
}

@Composable
private fun WithdrawConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(White)
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = "정말 탈퇴하시겠어요?",
            style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "탈퇴 후에는 활동 기록과 저장된 정보를\n다시 복구할 수 없어요.",
            style = TqType.BodyM,
            color = Gray700,
        )
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WithdrawDialogButton(
                text = "아니요",
                background = Gray100,
                textColor = Gray500,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            WithdrawDialogButton(
                text = "탈퇴할래요",
                background = Color(0xFFFF4040),
                textColor = White,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WithdrawDialogButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .then(if (background == White) Modifier.border(1.dp, Gray200, RoundedCornerShape(8.dp)) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = TqType.BodyL.copy(fontWeight = FontWeight.Bold), color = textColor)
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileWithdrawScreenPreview() {
    TalkQQuestTheme {
        ProfileWithdrawScreen()
    }
}