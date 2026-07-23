package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

private val SettingsTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.01).em,
)

private val SettingsBodyLargeStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
)

private val SettingsBodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
)

private val SettingsBodySmallStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
) = FitDesign {
    var pushEnabled by remember { mutableStateOf(true) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(
            title = "설정",
            onBack = onBack,
        )

        SettingsProfileHeader(
            onEditProfileClick = onEditProfileClick,
            modifier = Modifier
                .offset(x = 16.dp, y = 104.dp)
                .size(width = 158.dp, height = 62.dp),
        )

        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 190.dp)
                .size(width = 362.dp, height = 214.dp),
        ) {
            SettingsSectionLabel(
                text = "알림",
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 330.dp, height = 22.dp),
            )
            SettingsToggleRow(
                title = "푸시 알림 받기",
                checked = pushEnabled,
                onCheckedChange = { pushEnabled = it },
                modifier = Modifier
                    .offset(x = 16.dp, y = 46.dp)
                    .size(width = 329.dp, height = 44.dp),
            )
            SettingsToggleRow(
                title = "미션 리마인드",
                description = "매일 미션 추천 시간에 알림을 보내드려요",
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it },
                modifier = Modifier
                    .offset(x = 16.dp, y = 102.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
            SettingsTimeRow(
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .offset(x = 16.dp, y = 158.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
        }

        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 416.dp)
                .size(width = 362.dp, height = 146.dp),
        ) {
            SettingsSectionLabel(
                text = "계정",
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 330.dp, height = 22.dp),
            )
            SettingsArrowRow(
                title = "내 정보",
                onClick = onEditProfileClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 46.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
            SettingsArrowRow(
                title = "연결된 계정",
                trailing = "talkqquest@naver.com",
                modifier = Modifier
                    .offset(x = 16.dp, y = 90.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
        }

        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 574.dp)
                .size(width = 362.dp, height = 190.dp),
        ) {
            SettingsSectionLabel(
                text = "법적 정보 및 기타",
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 330.dp, height = 22.dp),
            )
            SettingsArrowRow(
                title = "약관 및 개인정보 처리 방침",
                onClick = onTermsClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 46.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
            SettingsArrowRow(
                title = "문의하기",
                onClick = onSupportClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 90.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
            SettingsArrowRow(
                title = "탈퇴하기",
                onClick = onWithdrawClick,
                modifier = Modifier
                    .offset(x = 16.dp, y = 134.dp)
                    .size(width = 330.dp, height = 44.dp),
            )
        }

        if (showTimePicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF273449).copy(alpha = 0.24f))
                    .clickable { showTimePicker = false },
            )
            ReminderTimeDialog(
                onCancel = { showTimePicker = false },
                onSave = { showTimePicker = false },
                modifier = Modifier
                    .offset(x = 34.dp, y = 315.dp)
                    .size(width = 326.dp, height = 258.dp),
            )
        }
    }
}

@Composable
private fun SettingsProfileHeader(
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.img_profile_avatar),
            contentDescription = null,
            modifier = Modifier
                .offset(y = 1.dp)
                .size(60.dp),
        )
        Text(
            text = "다민 님",
            style = SettingsTitleStyle,
            color = Color.Black,
            modifier = Modifier
                .offset(x = 78.dp)
                .size(width = 80.dp, height = 28.dp),
        )
        Box(
            modifier = Modifier
                .offset(x = 72.dp, y = 32.dp)
                .size(width = 86.dp, height = 30.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Gray300, RoundedCornerShape(24.dp))
                .clickable(onClick = onEditProfileClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "내 정보 수정",
                style = SettingsBodySmallStyle,
                color = Gray500,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .softShadow(
                color = Color(0xFF0F172A).copy(alpha = 0.01f),
                offsetY = 8.dp,
                blur = 24.dp,
                cornerRadius = 16.dp,
            )
            .clip(RoundedCornerShape(16.dp))
            .background(White),
    ) {
        content()
    }
}

@Composable
private fun SettingsSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = SettingsBodyMediumStyle,
        color = Gray500,
        modifier = modifier,
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Box(modifier = modifier) {
        Text(
            text = title,
            style = SettingsBodyLargeStyle,
            color = Gray800,
            modifier = Modifier
                .offset(y = if (description == null) 10.dp else 0.dp)
                .size(width = if (description == null) 180.dp else 208.dp, height = 24.dp),
        )
        if (description != null) {
            Text(
                text = description,
                style = SettingsBodySmallStyle,
                color = Gray500,
                modifier = Modifier
                    .offset(y = 24.dp)
                    .size(width = 208.dp, height = 20.dp),
            )
        }
        SettingsSwitch(
            checked = checked,
            onClick = { onCheckedChange(!checked) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 52.dp, height = 32.dp),
        )
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Primary600 else Gray200)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (checked) 24.dp else 6.dp, y = if (checked) 4.dp else 7.dp)
                .size(if (checked) 24.dp else 18.dp)
                .clip(CircleShape)
                .background(if (checked) Primary50 else Gray50),
        )
    }
}

@Composable
private fun SettingsTimeRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = "미션 리마인드 시간",
            style = SettingsBodyLargeStyle,
            color = Gray800,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 150.dp, height = 24.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 101.dp, height = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "11:28:55",
                style = SettingsBodyLargeStyle,
                color = Gray500,
                modifier = Modifier.size(width = 61.dp, height = 24.dp),
            )
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsArrowRow(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    onClick: () -> Unit = {},
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = title,
            style = SettingsBodyLargeStyle,
            color = Gray800,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 210.dp, height = 24.dp),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = SettingsBodyLargeStyle,
                color = Gray500,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 44.dp)
                    .size(width = 195.dp, height = 24.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Gray600,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .padding(horizontal = 7.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ReminderTimeDialog(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .softShadow(
                color = Color(0xFF0F172A).copy(alpha = 0.06f),
                offsetY = 0.dp,
                blur = 24.dp,
                cornerRadius = 12.dp,
            )
            .clip(RoundedCornerShape(12.dp))
            .background(White),
    ) {
        Box(
            modifier = Modifier
                .offset(x = 32.dp, y = 72.dp)
                .size(width = 262.dp, height = 1.dp)
                .background(Color(0xFF8B8B8B)),
        )
        Box(
            modifier = Modifier
                .offset(x = 32.dp, y = 128.dp)
                .size(width = 262.dp, height = 1.dp)
                .background(Color(0xFF8B8B8B)),
        )

        TimePickerNumberRow(
            values = listOf("06", "27", "54"),
            selected = false,
            modifier = Modifier
                .offset(x = 32.dp, y = 16.dp)
                .size(width = 202.dp, height = 56.dp),
        )
        TimePickerNumberRow(
            values = listOf("06", "28", "55"),
            selected = true,
            modifier = Modifier
                .offset(x = 32.dp, y = 72.dp)
                .size(width = 202.dp, height = 56.dp),
        )
        TimePickerNumberRow(
            values = listOf("06", "27", "54"),
            selected = false,
            modifier = Modifier
                .offset(x = 32.dp, y = 128.dp)
                .size(width = 202.dp, height = 56.dp),
        )

        TimePickerPeriod(
            text = "PM",
            selected = true,
            modifier = Modifier
                .offset(x = 234.dp, y = 72.dp)
                .size(width = 60.dp, height = 56.dp),
        )
        TimePickerPeriod(
            text = "AM",
            selected = false,
            modifier = Modifier
                .offset(x = 234.dp, y = 128.dp)
                .size(width = 60.dp, height = 56.dp),
        )

        DialogButton(
            text = "취소",
            primary = false,
            onClick = onCancel,
            modifier = Modifier
                .offset(x = 27.dp, y = 194.dp)
                .size(width = 124.dp, height = 48.dp),
        )
        DialogButton(
            text = "저장",
            primary = true,
            onClick = onSave,
            modifier = Modifier
                .offset(x = 175.dp, y = 194.dp)
                .size(width = 124.dp, height = 48.dp),
        )
    }
}

@Composable
private fun TimePickerNumberRow(
    values: List<String>,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        values.forEach { value ->
            TimePickerValue(
                text = value,
                selected = selected,
                modifier = Modifier.size(width = 38.dp, height = 24.dp),
            )
        }
    }
}

@Composable
private fun TimePickerPeriod(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        TimePickerValue(
            text = text,
            selected = selected,
            modifier = Modifier.size(width = 60.dp, height = 24.dp),
        )
    }
}

@Composable
private fun TimePickerValue(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = TextStyle(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 24.sp,
        ),
        color = if (selected) Primary600 else Gray300,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier,
    )
}

@Composable
private fun TimeDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Gray300),
    )
}
@Composable
private fun DialogButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .then(if (primary) Modifier.background(Primary600) else Modifier.border(1.dp, Gray300, RoundedCornerShape(9.dp)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SettingsTitleStyle,
            color = if (primary) White else Gray400,
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileSettingsScreenPreview() {
    TalkQQuestTheme {
        ProfileSettingsScreen()
    }
}



