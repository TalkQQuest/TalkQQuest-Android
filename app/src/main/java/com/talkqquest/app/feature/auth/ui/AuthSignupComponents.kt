package com.talkqquest.app.feature.auth.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Error
import com.talkqquest.app.core.designsystem.Gray300
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray600
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.Success
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White
@Composable
internal fun AuthScreenFrame(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "뒤로가기",
                    tint = Gray500,
                )
            }
            Text(
                text = title,
                style = TqType.LabelL,
                color = Gray600,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        content()
    }
}

@Composable
internal fun AuthHeadline(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = TqType.HeadingL,
        color = Gray800,
        modifier = modifier,
    )
}

@Composable
internal fun AuthInputCard(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    actionCornerRadius: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(start = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = TqType.Caption, color = Gray500)
            Spacer(Modifier.height(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TqType.TitleL.copy(color = Gray900),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = TqType.TitleL,
                                color = Gray300,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        if (actionText != null && onActionClick != null) {
            SmallAuthButton(text = actionText, onClick = onActionClick, cornerRadius = actionCornerRadius)
        }
        trailing?.invoke()
    }
}

@Composable
internal fun SmallAuthButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, cornerRadius: Dp = 24.dp) {
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Primary600)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = TqType.LabelL, color = Gray50)
    }
}

@Composable
internal fun RequirementItem(text: String, satisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        RequirementIcon(satisfied = satisfied)
        Text(text = text, style = TqType.LabelL, color = Gray500)
    }
}

@Composable
internal fun EyeOffIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val eye = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.52f)
            cubicTo(size.width * 0.28f, size.height * 0.22f, size.width * 0.72f, size.height * 0.22f, size.width * 0.9f, size.height * 0.52f)
            cubicTo(size.width * 0.72f, size.height * 0.82f, size.width * 0.28f, size.height * 0.82f, size.width * 0.1f, size.height * 0.52f)
        }
        drawPath(eye, color = Gray400, style = stroke)
        drawCircle(color = Gray400, radius = 3.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.52f), style = stroke)
        drawLine(
            color = Gray400,
            start = Offset(size.width * 0.16f, size.height * 0.12f),
            end = Offset(size.width * 0.86f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun RequirementIcon(satisfied: Boolean) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val color = if (satisfied) Success else Error
        drawCircle(color = color, style = Stroke(width = 1.5.dp.toPx()))
        if (satisfied) {
            drawLine(
                color = color,
                start = Offset(size.width * 0.28f, size.height * 0.52f),
                end = Offset(size.width * 0.44f, size.height * 0.68f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.44f, size.height * 0.68f),
                end = Offset(size.width * 0.74f, size.height * 0.32f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        } else {
            drawLine(
                color = color,
                start = Offset(size.width * 0.34f, size.height * 0.34f),
                end = Offset(size.width * 0.66f, size.height * 0.66f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.66f, size.height * 0.34f),
                end = Offset(size.width * 0.34f, size.height * 0.66f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}


