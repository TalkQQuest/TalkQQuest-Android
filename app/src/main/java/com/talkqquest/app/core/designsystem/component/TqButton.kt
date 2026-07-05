package com.talkqquest.app.core.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

// 공통 버튼 (CONVENTIONS.md 8번). Large=52/radius16, Medium=44/radius12. 폭은 호출부 modifier로.
enum class TqButtonSize { Large, Medium }

@Composable
fun TqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: TqButtonSize = TqButtonSize.Large,
    enabled: Boolean = true,
) {
    val height = if (size == TqButtonSize.Large) 52.dp else 44.dp
    val radius = if (size == TqButtonSize.Large) 16.dp else 12.dp

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary600,
            contentColor = White,
            disabledContainerColor = Gray200,
            disabledContentColor = Gray400,
        ),
    ) {
        Text(text = text, style = TqType.BodyL.copy(fontWeight = FontWeight.SemiBold))
    }
}
