package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
        ProfileSimpleTopBar(title = "탈퇴하기", onBack = onBack)
        Column(
            modifier = Modifier
                .offset(x = 16.dp, y = 111.dp)
                .size(width = 346.dp, height = 280.dp),
        ) {
            Text(
                text = "회원 탈퇴 전 주의사항",
                style = TqType.TitleL.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                modifier = Modifier.size(width = 346.dp, height = 28.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "탈퇴 시 모든 서비스 이용내역이 삭제되며 복구가 불가능합니다.\n또한 같은 계정 정보로 재가입이 7일 동안 불가능 합니다.",
                style = TqType.BodyM,
                color = Gray700,
                modifier = Modifier.size(width = 346.dp, height = 44.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "삭제 사항",
                style = TqType.TitleL.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF1E293B),
                modifier = Modifier.size(width = 214.dp, height = 28.dp),
            )
            Spacer(Modifier.height(12.dp))
            WithdrawBullet("계정 정보 및 사용자 설정 정보")
            Spacer(Modifier.height(12.dp))
            WithdrawBullet("미션 기록 및 대화 기록")
            Spacer(Modifier.height(12.dp))
            WithdrawBullet("AI 피드백 및 리포트")
            Spacer(Modifier.height(12.dp))
            WithdrawBullet("그 외 모든 구매 기록 및 정보")
        }
        Row(
            modifier = Modifier
                .offset(x = 16.dp, y = 415.dp)
                .size(width = 361.dp, height = 26.dp)
                .clickable { agreed = !agreed },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
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
            Spacer(Modifier.size(width = 8.dp, height = 1.dp))
            Text(
                text = "위 주의 사항을 모두 확인했으며, 탈퇴에 동의합니다.",
                style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFF14444),
                modifier = Modifier.size(width = 322.dp, height = 24.dp),
            )
        }
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 728.dp)
                .size(width = 361.dp, height = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Primary600)
                .clickable(enabled = agreed) { showConfirm = true },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "탈퇴하기",
                style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold),
                color = Gray50,
            )
        }

        if (showConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF334155).copy(alpha = 0.23f))
                    .clickable { showConfirm = false },
            )
            WithdrawConfirmDialog(
                onCancel = { showConfirm = false },
                onConfirm = { showConfirm = false },
                modifier = Modifier.offset(x = 28.dp, y = 313.dp),
            )
        }
    }
}
@Composable
private fun WithdrawBullet(text: String) {
    Row(
        modifier = Modifier.size(width = 214.dp, height = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "·", style = TqType.BodyL, color = Gray700)
        Spacer(Modifier.size(width = 10.dp, height = 1.dp))
        Text(text = text, style = TqType.BodyL, color = Gray700)
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
            .size(width = 336.dp, height = 190.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 20.dp),
    ) {
        Column(
            modifier = Modifier.size(width = 288.dp, height = 82.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "정말 탈퇴하시겠어요?",
                style = TqType.HeadingM.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF1E293B),
                modifier = Modifier.size(width = 169.dp, height = 30.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "탈퇴 후에는 활동 기록과 저장된 정보를\n다시 복구할 수 없어요.",
                style = TqType.BodyL,
                color = Gray500,
                modifier = Modifier.size(width = 288.dp, height = 48.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.size(width = 288.dp, height = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WithdrawDialogButton(
                text = "아니요",
                background = Gray200,
                textColor = Gray500,
                onClick = onCancel,
                modifier = Modifier.size(width = 138.dp, height = 48.dp),
            )
            WithdrawDialogButton(
                text = "탈퇴할래요",
                background = Color(0xFFF14444),
                textColor = Gray50,
                onClick = onConfirm,
                modifier = Modifier.size(width = 138.dp, height = 48.dp),
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
            
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = TqType.TitleL.copy(fontWeight = FontWeight.SemiBold), color = textColor)
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileWithdrawScreenPreview() {
    TalkQQuestTheme {
        ProfileWithdrawScreen()
    }
}





