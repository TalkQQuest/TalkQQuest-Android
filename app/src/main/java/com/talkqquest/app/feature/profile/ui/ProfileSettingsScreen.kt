package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray100
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.softShadow

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            ProfileSimpleTopBar(
                title = "\uC124\uC815",
                onBack = onBack,
                modifier = Modifier,
            )
            Spacer(Modifier.height(17.dp))
            SettingsProfileHeader(onEditProfileClick = onEditProfileClick)
            Spacer(Modifier.height(26.dp))
            SettingsCard {
                SettingsSectionLabel("\uC54C\uB9BC")
                Spacer(Modifier.height(23.dp))
                SettingsToggleRow(
                    title = "\uD478\uC2DC \uC54C\uB9BC \uBC1B\uAE30",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it },
                )
                Spacer(Modifier.height(26.dp))
                SettingsToggleRow(
                    title = "\uBBF8\uC158 \uB9AC\uB9C8\uC778\uB4DC",
                    description = "\uB9E4\uC77C \uBBF8\uC158 \uCD94\uCC9C \uC2DC\uAC04\uC5D0 \uC54C\uB9BC\uC744 \uBCF4\uB0B4\uB4DC\uB824\uC694",
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                )
                Spacer(Modifier.height(26.dp))
                SettingsTimeRow(onClick = { showTimePicker = true })
            }
            Spacer(Modifier.height(12.dp))
            SettingsCard {
                SettingsSectionLabel("\uACC4\uC815")
                Spacer(Modifier.height(27.dp))
                SettingsArrowRow(title = "\uB0B4 \uC815\uBCF4")
                Spacer(Modifier.height(25.dp))
                SettingsArrowRow(title = "\uC5F0\uACB0\uB41C \uACC4\uC815", trailing = "talkqquest@naver.com")
            }
            Spacer(Modifier.height(12.dp))
            SettingsCard {
                SettingsSectionLabel("\uBC95\uC801 \uC815\uBCF4 \uBC0F \uAE30\uD0C0")
                Spacer(Modifier.height(27.dp))
                SettingsArrowRow(title = "\uC57D\uAD00 \uBC0F \uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC \uBC29\uCE68", onClick = onTermsClick)
                Spacer(Modifier.height(25.dp))
                SettingsArrowRow(title = "\uBB38\uC758\uD558\uAE30", onClick = onSupportClick)
                Spacer(Modifier.height(25.dp))
                SettingsArrowRow(title = "\uD0C8\uD1F4\uD558\uAE30", onClick = onWithdrawClick)
            }
            Spacer(Modifier.height(40.dp))
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
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun SettingsProfileHeader(onEditProfileClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsAvatar(modifier = Modifier.size(60.dp))
        Spacer(Modifier.width(18.dp))
        Column {
            Text(
                text = "\uB2E4\uBBFC \uB2D8",
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = Gray800,
            )
            Spacer(Modifier.height(9.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Gray300, RoundedCornerShape(50))
                    .clickable(onClick = onEditProfileClick)
                    .padding(horizontal = 13.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("\uB0B4 \uC815\uBCF4 \uC218\uC815", style = TqType.LabelM, color = Gray500)
            }
        }
    }
}

@Composable
private fun SettingsAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF806FF8), Primary600),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawCircle(color = Color(0xFFE9E6FF), radius = size.minDimension * 0.31f, center = Offset(size.width / 2f, size.height * 0.47f))
        drawOval(
            color = Color(0xFFE2DEFF),
            topLeft = Offset(size.width * 0.22f, size.height * 0.52f),
            size = Size(size.width * 0.56f, size.height * 0.44f),
        )
        drawCircle(color = Gray600, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.42f, size.height * 0.43f))
        drawCircle(color = Gray600, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.58f, size.height * 0.43f))
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
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
            .padding(horizontal = 16.dp, vertical = 17.dp),
        content = content,
    )
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text = text, style = TqType.BodyS, color = Gray500)
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TqType.BodyL.copy(fontWeight = FontWeight.Medium), color = Gray800)
            if (description != null) {
                Spacer(Modifier.height(5.dp))
                Text(text = description, style = TqType.BodyS, color = Gray500)
            }
        }
        SettingsSwitch(checked = checked, onClick = { onCheckedChange(!checked) })
    }
}

@Composable
private fun SettingsSwitch(checked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) Primary600 else Gray200)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) White else Gray50),
        )
    }
}

@Composable
private fun SettingsTimeRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\uBBF8\uC158 \uB9AC\uB9C8\uC778\uB4DC \uC2DC\uAC04",
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        Text(text = "11:28:55", style = TqType.BodyL, color = Gray600)
        Spacer(Modifier.width(5.dp))
        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Gray500, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsArrowRow(title: String, trailing: String? = null, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(text = trailing, style = TqType.BodyM, color = Gray500)
            Spacer(Modifier.width(12.dp))
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Gray700, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun ReminderTimeDialog(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 33.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(White)
            .padding(horizontal = 28.dp, vertical = 27.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimePickerRow(values = listOf("06", "27", "54", ""), selected = false)
        TimeDivider()
        TimePickerRow(values = listOf("06", "28", "55", "PM"), selected = true)
        TimeDivider()
        TimePickerRow(values = listOf("06", "27", "54", "AM"), selected = false)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            DialogButton(text = "\uCDE8\uC18C", primary = false, onClick = onCancel)
            DialogButton(text = "\uC800\uC7A5", primary = true, onClick = onSave)
        }
    }
}

@Composable
private fun TimePickerRow(values: List<String>, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        values.forEach { value ->
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = TqType.TitleL.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Primary600 else Gray300,
            )
        }
    }
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
private fun DialogButton(text: String, primary: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 124.dp, height = 48.dp)
            .clip(RoundedCornerShape(9.dp))
            .then(if (primary) Modifier.background(Primary600) else Modifier.border(1.dp, Gray300, RoundedCornerShape(9.dp)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Bold),
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