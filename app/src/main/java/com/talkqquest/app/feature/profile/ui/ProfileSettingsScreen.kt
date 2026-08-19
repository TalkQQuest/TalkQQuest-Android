package com.talkqquest.app.feature.profile.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.talkqquest.app.R
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.figma
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.ModalDimOverlay
import com.talkqquest.app.core.designsystem.PretendardFamily
import com.talkqquest.app.core.designsystem.Primary50
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.White
import com.talkqquest.app.core.designsystem.modalCardEnter
import com.talkqquest.app.core.designsystem.modalCardExit
import com.talkqquest.app.core.designsystem.component.MenuRippleGroupState
import com.talkqquest.app.core.designsystem.component.MenuRippleLayer
import com.talkqquest.app.core.designsystem.component.TqAnchoredMenuRow
import com.talkqquest.app.core.designsystem.component.menuRowTextRippleAnchor
import com.talkqquest.app.core.designsystem.component.rememberHapticTick
import com.talkqquest.app.core.designsystem.component.rememberMenuRippleGroup
import com.talkqquest.app.core.designsystem.component.rememberMenuRowTextLayoutCallback
import com.talkqquest.app.core.designsystem.softShadow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween

private val SettingsTitleStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.01).em,
).figma()

private val SettingsBodyLargeStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
).figma()

private val SettingsBodyMediumStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
).figma()

private val SettingsBodySmallStyle = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
).figma()

@Composable
fun ProfileSettingsScreen(
    nickname: String = "다민",
    avatarUrl: String? = null,
    connectedAccount: String = "이메일 정보 없음",
    initialPushEnabled: Boolean = true,
    initialReminderEnabled: Boolean = false,
    initialReminderTime: String = "09:00",
    onPushEnabledChange: (Boolean) -> Unit = {},
    onReminderEnabledChange: (Boolean) -> Unit = {},
    onReminderTimeChange: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onAvatarClick: (Uri) -> Unit = {},
    onConnectedAccountClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
) = FitDesign(contentAlignment = Alignment.TopCenter) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var reminderTime by remember(initialReminderTime) { mutableStateOf(initialReminderTime.toReminderTime()) }

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
            nickname = nickname,
            avatarUrl = avatarUrl,
            onEditProfileClick = onEditProfileClick,
            onAvatarClick = { showPhotoPicker = true },
            modifier = Modifier
                .offset(x = 16.dp, y = 104.dp)
                .size(width = 158.dp, height = 62.dp),
        )

        val notificationGroup = rememberMenuRippleGroup()
        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 190.dp)
                .size(width = 362.dp, height = 214.dp)
                .onGloballyPositioned {
                    val top = it.positionInRoot().y
                    notificationGroup.setEdges(top, top + it.size.height, fillFirst = false, fillLast = true)
                },
        ) {
            MenuRippleLayer(notificationGroup, Modifier.fillMaxSize())
            SettingsSectionLabel(
                text = "알림",
                modifier = Modifier
                    .offset(x = 16.dp, y = 12.dp)
                    .size(width = 330.dp, height = 22.dp),
            )
            SettingsToggleRow(
                title = "푸시 알림 받기",
                checked = initialPushEnabled,
                onCheckedChange = onPushEnabledChange,
                modifier = Modifier
                    .offset(y = 40.dp)
                    .size(width = 362.dp, height = 56.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 6.dp)
                    .size(width = 329.dp, height = 44.dp),
                group = notificationGroup,
            )
            SettingsToggleRow(
                title = "미션 리마인드",
                description = "매일 미션 추천 시간에 알림을 보내드려요",
                checked = initialReminderEnabled,
                onCheckedChange = onReminderEnabledChange,
                modifier = Modifier
                    .offset(y = 96.dp)
                    .size(width = 362.dp, height = 56.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 6.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = notificationGroup,
            )
            SettingsTimeRow(
                time = reminderTime,
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .offset(y = 152.dp)
                    .size(width = 362.dp, height = 56.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp, y = 6.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = notificationGroup,
            )
        }

        val accountGroup = rememberMenuRippleGroup()
        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 416.dp)
                .size(width = 362.dp, height = 146.dp)
                .onGloballyPositioned {
                    val top = it.positionInRoot().y
                    accountGroup.setEdges(top, top + it.size.height, fillFirst = false, fillLast = true)
                },
        ) {
            MenuRippleLayer(accountGroup, Modifier.fillMaxSize())
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
                    .offset(y = 46.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = accountGroup,
            )
            SettingsArrowRow(
                title = "연결된 계정",
                trailing = connectedAccount,
                onClick = onConnectedAccountClick,
                modifier = Modifier
                    .offset(y = 90.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = accountGroup,
            )
        }

        val legalGroup = rememberMenuRippleGroup()
        SettingsCard(
            modifier = Modifier
                .offset(x = 16.dp, y = 574.dp)
                .size(width = 362.dp, height = 190.dp)
                .onGloballyPositioned {
                    val top = it.positionInRoot().y
                    legalGroup.setEdges(top, top + it.size.height, fillFirst = false, fillLast = true)
                },
        ) {
            MenuRippleLayer(legalGroup, Modifier.fillMaxSize())
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
                    .offset(y = 46.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = legalGroup,
            )
            SettingsArrowRow(
                title = "문의하기",
                onClick = onSupportClick,
                modifier = Modifier
                    .offset(y = 90.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = legalGroup,
            )
            SettingsArrowRow(
                title = "탈퇴하기",
                onClick = onWithdrawClick,
                modifier = Modifier
                    .offset(y = 134.dp)
                    .size(width = 362.dp, height = 44.dp),
                contentModifier = Modifier
                    .offset(x = 16.dp)
                    .size(width = 330.dp, height = 44.dp),
                group = legalGroup,
            )
        }

        BackHandler(enabled = showTimePicker) { showTimePicker = false }

        ModalDimOverlay(visible = showTimePicker, onDismiss = { showTimePicker = false })
        androidx.compose.animation.AnimatedVisibility(
            visible = showTimePicker,
            enter = modalCardEnter(),
            exit = modalCardExit(),
        ) {
            ReminderTimeDialog(
                selectedTime = reminderTime,
                onCancel = { showTimePicker = false },
                onSave = { nextTime ->
                    reminderTime = nextTime
                    onReminderTimeChange(nextTime)
                    showTimePicker = false
                },
                modifier = Modifier
                    .offset(x = 34.dp, y = 315.dp)
                    .size(width = 326.dp, height = 258.dp),
            )
        }

        ProfilePhotoPickerSheet(
            visible = showPhotoPicker,
            onDismiss = { showPhotoPicker = false },
            onPhotoConfirmed = onAvatarClick,
        )
    }
}

@Composable
private fun SettingsProfileHeader(
    nickname: String,
    avatarUrl: String?,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        SettingsAvatarImage(
            avatarUrl = avatarUrl,
            modifier = Modifier
                .offset(y = 1.dp)
                .size(60.dp)
                .profileCircleClick(onClick = onAvatarClick),
        )
        Text(
            text = "$nickname 님",
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
                .profileItemClick(onClick = onEditProfileClick),
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

// ProfileScreen.kt의 AvatarImage와 동일한 fallback/placeholder/error 처리.
@Composable
private fun SettingsAvatarImage(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (avatarUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(R.drawable.img_profile_avatar),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = modifier,
            error = painterResource(R.drawable.img_profile_avatar),
            placeholder = painterResource(R.drawable.img_profile_avatar),
        )
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
    contentModifier: Modifier = Modifier,
    description: String? = null,
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(modifier = modifier, group = group, onClick = { onCheckedChange(!checked) }) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", title, SettingsBodyLargeStyle)
        val descriptionLayout = description?.let {
            rememberMenuRowTextLayoutCallback(anchor, "description", it, SettingsBodySmallStyle)
        }
        Box(modifier = contentModifier) {
        Text(
            text = title,
            style = SettingsBodyLargeStyle,
            color = Gray800,
            onTextLayout = titleLayout,
            modifier = Modifier
                .menuRowTextRippleAnchor(anchor, "title")
                .offset(y = if (description == null) 10.dp else 0.dp)
                .size(width = if (description == null) 180.dp else 208.dp, height = 24.dp),
        )
        if (description != null) {
            Text(
                text = description,
                style = SettingsBodySmallStyle,
                color = Gray500,
                onTextLayout = descriptionLayout!!,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "description")
                    .offset(y = 24.dp)
                    .size(width = 208.dp, height = 20.dp),
            )
        }
        SettingsSwitch(
            checked = checked,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 52.dp, height = 32.dp),
        )
        }
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Primary600 else Gray200),
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
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(modifier = modifier, group = group, onClick = onClick) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", "미션 리마인드 시간", SettingsBodyLargeStyle)
        val timeLayout = rememberMenuRowTextLayoutCallback(anchor, "time", time, SettingsBodyLargeStyle)
        Box(modifier = contentModifier) {
        Text(
            text = "미션 리마인드 시간",
            style = SettingsBodyLargeStyle,
            color = Gray800,
            onTextLayout = titleLayout,
            modifier = Modifier
                .menuRowTextRippleAnchor(anchor, "title")
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
                text = time,
                style = SettingsBodyLargeStyle,
                color = Gray500,
                onTextLayout = timeLayout,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "time")
                    .size(width = 61.dp, height = 24.dp),
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
}

@Composable
private fun SettingsArrowRow(
    title: String,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    trailing: String? = null,
    onClick: () -> Unit = {},
    group: MenuRippleGroupState? = null,
) {
    TqAnchoredMenuRow(modifier = modifier, group = group, onClick = onClick) { anchor ->
        val titleLayout = rememberMenuRowTextLayoutCallback(anchor, "title", title, SettingsBodyLargeStyle)
        val trailingLayout = trailing?.let {
            rememberMenuRowTextLayoutCallback(anchor, "trailing", it, SettingsBodyLargeStyle)
        }
        Box(modifier = contentModifier) {
        Text(
            text = title,
            style = SettingsBodyLargeStyle,
            color = Gray800,
            onTextLayout = titleLayout,
            modifier = Modifier
                .menuRowTextRippleAnchor(anchor, "title")
                .align(Alignment.CenterStart)
                .size(width = 210.dp, height = 24.dp),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = SettingsBodyLargeStyle,
                color = Gray500,
                textAlign = TextAlign.End,
                onTextLayout = trailingLayout!!,
                modifier = Modifier
                    .menuRowTextRippleAnchor(anchor, "trailing")
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
}

@Composable
private fun ReminderTimeDialog(
    selectedTime: String,
    onCancel: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hour by remember(selectedTime) { mutableIntStateOf(selectedTime.toHour()) }
    var minute by remember(selectedTime) { mutableIntStateOf(selectedTime.toMinute()) }
    val isPm = hour >= 12
    val touchZoneWidth = 326.dp / 3f
    val dividerInnerWidth = 262.dp
    val visualColumnWidth = dividerInnerWidth / 3f
    val firstVisualColumnOffset = 32.dp - (touchZoneWidth - visualColumnWidth) / 2f

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
                .size(width = 262.dp, height = 0.5.dp)
                .background(Color(0xFF8B8B8B)),
        )
        Box(
            modifier = Modifier
                .offset(x = 32.dp, y = 128.dp)
                .size(width = 262.dp, height = 0.5.dp)
                .background(Color(0xFF8B8B8B)),
        )

        Box(
            modifier = Modifier
                .offset(y = 16.dp)
                .size(width = 326.dp, height = 168.dp),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                TimePickerWheelColumn(
                    itemCount = 24,
                    selectedValue = hour,
                    label = { value -> (if (value % 12 == 0) 12 else value % 12).twoDigits() },
                    onValueChange = { hour = it },
                    visualContentOffsetX = firstVisualColumnOffset,
                    visualRippleWidth = visualColumnWidth,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                TimePickerWheelColumn(
                    itemCount = 60,
                    selectedValue = minute,
                    label = Int::twoDigits,
                    onValueChange = { minute = it },
                    visualRippleWidth = visualColumnWidth,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                TimePickerWheelColumn(
                    itemCount = 2,
                    selectedValue = if (isPm) 0 else 1,
                    label = { if (it == 0) "PM" else "AM" },
                    onValueChange = { amPm -> hour = (hour % 12) + if (amPm == 0) 12 else 0 },
                    finite = true,
                    visualContentOffsetX = -firstVisualColumnOffset,
                    visualRippleWidth = visualColumnWidth,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }

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
            onClick = { onSave("${hour.twoDigits()}:${minute.twoDigits()}") },
            modifier = Modifier
                .offset(x = 175.dp, y = 194.dp)
                .size(width = 124.dp, height = 48.dp),
        )
    }
}

@Composable
private fun TimePickerWheelColumn(
    itemCount: Int,
    selectedValue: Int,
    label: (Int) -> String,
    onValueChange: (Int) -> Unit,
    finite: Boolean = false,
    visualContentOffsetX: Dp = 0.dp,
    visualRippleWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val tick = rememberHapticTick()
    val middleIndex = Int.MAX_VALUE / 2
    val initialCenterIndex = remember(itemCount, selectedValue, finite) {
        if (finite) selectedValue else middleIndex - Math.floorMod(middleIndex - selectedValue, itemCount)
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (finite) initialCenterIndex else initialCenterIndex - 1,
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(listState, itemCount, finite) {
        var previousCenterIndex: Int? = null
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index
        }.distinctUntilChanged().collect { centerIndex ->
            if (centerIndex != null) {
                val previous = previousCenterIndex
                previousCenterIndex = centerIndex
                if (previous != null && previous != centerIndex && listState.isScrollInProgress) {
                    tick()
                    onValueChange(if (finite) centerIndex else centerIndex % itemCount)
                }
            }
        }
    }

    LaunchedEffect(selectedValue, itemCount) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val currentCenter = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index
            val currentValue = currentCenter?.let { if (finite) it else it % itemCount }
            if (currentValue == null || currentValue != selectedValue) {
                val anchor = currentCenter ?: initialCenterIndex
                val target = if (finite) {
                    selectedValue
                } else {
                    anchor - Math.floorMod(anchor - selectedValue, itemCount)
                }
                listState.scrollToItem(if (finite) target else target - 1)
            }
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = if (finite) androidx.compose.foundation.layout.PaddingValues(vertical = 56.dp) else androidx.compose.foundation.layout.PaddingValues(),
        ) {
            items(count = if (finite) itemCount else Int.MAX_VALUE, key = { index -> index }) { index ->
                val value = if (finite) index else index % itemCount
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { selected = value == selectedValue }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            if (value != selectedValue) {
                                val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                                if (item != null) {
                                    val viewportCenter = (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
                                    val distance = item.offset + item.size / 2 - viewportCenter
                                    scope.launch {
                                        listState.scroll {
                                            var previous = 0f
                                            animate(
                                                initialValue = 0f,
                                                targetValue = distance.toFloat(),
                                                animationSpec = tween(140, easing = FastOutSlowInEasing),
                                            ) { current, _ ->
                                                scrollBy(current - previous)
                                                previous = current
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = visualContentOffsetX)
                            .size(width = visualRippleWidth, height = 56.dp)
                            .indication(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TimePickerValue(
                            text = label(value),
                            selected = value == selectedValue,
                            modifier = Modifier.size(width = 60.dp, height = 24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerValue(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (selected) Primary600 else Gray300,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 160),
        label = "timePickerValueColor",
    )
    Text(
        text = text,
        style = TextStyle(
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 24.sp,
        ).figma(),
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier,
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
            .profileItemClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SettingsTitleStyle,
            color = if (primary) White else Gray400,
        )
    }
}

private fun String.toReminderTime(): String =
    takeIf { it.matches(Regex("^\\d{2}:\\d{2}$")) } ?: "09:00"

private fun String.toHour(): Int =
    substringBefore(":").toIntOrNull()?.coerceIn(0, 23) ?: 9

private fun String.toMinute(): Int =
    substringAfter(":", "00").toIntOrNull()?.coerceIn(0, 59) ?: 0

private fun Int.twoDigits(): String = toString().padStart(2, '0')

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileSettingsScreenPreview() {
    TalkQQuestTheme {
        ProfileSettingsScreen()
    }
}
