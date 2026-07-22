package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

@Composable
fun ProfileTermsScreen(
    onBack: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
) = FitDesign {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .padding(horizontal = 16.dp),
    ) {
        ProfileSimpleTopBar(title = "약관 및 개인정보 처리 방침", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .softShadow(
                    color = Color.Black.copy(alpha = 0.025f),
                    offsetY = 8.dp,
                    blur = 24.dp,
                    cornerRadius = 14.dp,
                )
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text("상세 내용을 확인할 수 있어요", style = TqType.BodyM, color = Gray500)
            Spacer(Modifier.height(26.dp))
            TermsMenuRow(title = "이용약관", onClick = onTermsClick)
            Spacer(Modifier.height(25.dp))
            TermsMenuRow(title = "개인정보 처리방침", onClick = onPrivacyClick)
        }
    }
}

@Composable
private fun TermsMenuRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray700,
        )
    }
}

@Composable
fun ProfileTermsDetailScreen(
    title: String,
    sections: List<TermsSection>,
    onBack: () -> Unit = {},
) = FitDesign {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        ProfileSimpleTopBar(title = "약관 및 개인정보 처리 방침", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
        )
        Spacer(Modifier.height(25.dp))
        sections.forEachIndexed { index, section ->
            TermsSectionBlock(section)
            if (index != sections.lastIndex) Spacer(Modifier.height(30.dp))
        }
        Spacer(Modifier.height(36.dp))
    }
}

@Composable
private fun TermsSectionBlock(section: TermsSection) {
    Column {
        Text(
            text = section.title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Bold),
            color = Gray800,
        )
        Spacer(Modifier.height(11.dp))
        Text(
            text = section.body,
            style = TqType.BodyS,
            color = Gray600Text,
            lineHeight = TqType.BodyS.lineHeight,
        )
    }
}

data class TermsSection(val title: String, val body: String)

private val Gray600Text = Color(0xFF66778C)

val ServiceTermsSections = listOf(
    TermsSection(
        title = "서비스 이용 및 회원가입",
        body = "회원은 서비스 이용약관 및 개인정보 처리방침에 동의한 후 회원가입을 진행할 수 있습니다. 회원가입 시 정확한 정보를 제공해야 하며, 타인의 정보를 도용하여 가입할 수 없습니다. 만 14세 미만의 이용자는 법정대리인의 동의 없이 회원가입이 제한될 수 있습니다. 계정 및 비밀번호에 대한 관리 책임은 회원 본인에게 있으며, 계정 정보를 제3자가 이용하도록 해서는 안 됩니다. 계정 정보의 유출 또는 무단 사용을 인지한 경우 즉시 회사에 알려야 합니다.",
    ),
    TermsSection(
        title = "서비스 이용 규칙",
        body = "회원은 서비스의 정상적인 운영을 방해하거나 다른 이용자의 서비스 이용을 방해하는 행위를 해서는 안 됩니다. 해킹, 악성코드 유포, 비정상적인 접근 등 서비스 시스템에 영향을 주는 행위를 금지하며, 타인의 명예를 훼손하거나 제3자의 권리를 침해하는 콘텐츠를 게시할 수 없습니다. 또한 회사의 사전 동의 없이 서비스를 광고, 홍보, 판매 등 영리 목적으로 이용하는 행위를 제한할 수 있습니다.",
    ),
    TermsSection(
        title = "콘텐츠 및 저작권",
        body = "회원이 직접 작성하거나 등록한 콘텐츠의 권리는 해당 회원에게 귀속됩니다. 회사는 서비스 운영 및 개선을 위해 필요한 범위 내에서 회원이 등록한 콘텐츠를 활용할 수 있습니다. 서비스에서 제공하는 AI 답변, 디자인, 소프트웨어 등 회사가 제작하거나 제공하는 콘텐츠에 관한 권리는 회사 또는 해당 권리자에게 귀속됩니다. 타인의 저작권 또는 권리를 침해하는 콘텐츠가 확인될 경우 관련 법령 및 운영 정책에 따라 해당 콘텐츠를 삭제하거나 서비스 이용을 제한할 수 있습니다.",
    ),
    TermsSection(
        title = "콘텐츠 및 저작권",
        body = "회원이 직접 작성하거나 등록한 콘텐츠의 권리는 해당 회원에게 귀속됩니다. 회사는 서비스 운영 및 개선을 위해 필요한 범위 내에서 회원이 등록한 콘텐츠를 활용할 수 있습니다. 서비스에서 제공하는 AI 답변, 디자인, 소프트웨어 등 회사가 제작하거나 제공하는 콘텐츠에 관한 권리는 회사 또는 해당 권리자에게 귀속됩니다. 타인의 저작권 또는 권리를 침해하는 콘텐츠가 확인될 경우 관련 법령 및 운영 정책에 따라 해당 콘텐츠를 삭제하거나 서비스 이용을 제한할 수 있습니다.",
    ),
    TermsSection(
        title = "회원 탈퇴 및 이용 제한",
        body = "회원은 언제든지 서비스 내 회원 탈퇴 기능을 통해 탈퇴를 신청할 수 있습니다. 탈퇴 시 계정은 비활성화되며, 관련 법령에 따라 보관이 필요한 정보는 제외한 회원 정보는 파기됩니다. 부정 이용 및 반복적인 가입·탈퇴를 방지하기 위해 탈퇴 후 일정 기간 동일한 정보로 이용한 재가입이 제한될 수 있습니다. 소셜 로그인 계정은 서비스 탈퇴와 별도로 해당 플랫폼에서 연결 해제가 필요할 수 있습니다.",
    ),
    TermsSection(
        title = "분쟁 해결 및 기타",
        body = "서비스 이용과 관련하여 회사와 회원 간 분쟁이 발생한 경우, 회사와 회원은 원만한 해결을 위해 성실히 협의합니다. 협의로 해결되지 않는 경우 관련 법령 및 절차에 따라 처리합니다. 회사는 서비스 운영과 관련하여 개인정보보호법 등 대한민국의 관련 법령을 준수합니다.",
    ),
)

val PrivacyPolicySections = listOf(
    TermsSection(
        title = "수집하는 개인정보",
        body = "회사는 회원가입 및 서비스 제공을 위해 필요한 개인정보를 수집할 수 있습니다. 필수 정보로 이메일, 비밀번호, 이름, 생년월일 등을 수집할 수 있으며, 서비스 이용 과정에서 IP 주소, 쿠키, 서비스 이용 기록 등이 자동으로 생성되어 수집될 수 있습니다. 프로필 이미지 및 마케팅 정보 수신 여부 등의 선택 정보는 이용자의 동의를 받아 수집함",
    ),
    TermsSection(
        title = "개인정보 이용 목적",
        body = "수집한 개인정보는 회원가입 및 본인 확인, 서비스 제공, 연락 확인, 서비스 개인화 및 특정 이용 방지 등의 목적으로 이용합니다. 서비스 이용 기록 등은 이용 환경 개선 및 서비스 품질 향상을 위한 분석에 활용될 수 있습니다.",
    ),
    TermsSection(
        title = "AI 대화 데이터 활용",
        body = "서비스 이용 과정에서 생성된 AI 대화 데이터는 AI 답변의 품질 향상 및 서비스 개선을 위해 활용될 수 있습니다. AI 학습 및 분석에 데이터를 활용하는 경우 개인을 식별할 수 있는 정보는 비식별화하여 처리하고, 필요한 보호 조치를 적용합니다.",
    ),
    TermsSection(
        title = "개인정보 제공 및 처리 위탁",
        body = "회사는 원칙적으로 이용자의 사전 동의 없이 개인정보를 외부에 제공하지 않습니다. 다만 법령에 특별한 규정이 있거나 적법한 절차에 따른 요청이 있는 경우 예외적으로 제공할 수 있습니다. 서비스 운영을 위해 서버 호스팅 및 데이터 보관, 결제 처리, 본인 인증 등의 업무를 외부 전문 업체에 위탁할 수 있으며, 개인정보 보호가 안전하게 관리될 수 있도록 필요한 조치를 시행합니다. 서비스 제공 과정에서 국외 클라우드 서버에 데이터가 보관될 수 있으며, 이 경우 관련 법령에 따른 보호 조치를 적용합니다.",
    ),
    TermsSection(
        title = "개인정보 보유 및 파기",
        body = "개인정보는 원칙적으로 수집 및 이용 목적이 달성되거나 회원이 탈퇴한 경우 지체 없이 파기합니다. 다만 계약 및 결제 기록, 소비자 분쟁 처리 기록, 서비스 접속 기록 등 관련 법령에서 일정 기간 보관하도록 정한 정보는 해당 기간 동안 별도로 안전하게 보관한 후 파기합니다.",
    ),
    TermsSection(
        title = "개인정보 보호 및 이용자 권리",
        body = "회사는 이용자의 개인정보를 안전하게 보호하기 위해 데이터 암호화, 접근 권한 관리 등 필요한 기술적·관리적 보호 조치를 시행합니다. 이용자는 자신의 개인정보에 대한 열람, 수정 및 삭제 등을 요청할 수 있으며, 개인정보 처리와 관련한 동의 사항을 변경할 수 있습니다.",
    ),
)

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileTermsScreenPreview() {
    TalkQQuestTheme {
        ProfileTermsScreen()
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileTermsDetailScreenPreview() {
    TalkQQuestTheme {
        ProfileTermsDetailScreen(title = "이용약관", sections = ServiceTermsSections)
    }
}