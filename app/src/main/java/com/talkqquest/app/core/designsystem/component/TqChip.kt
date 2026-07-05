package com.talkqquest.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray200
import com.talkqquest.app.core.designsystem.Gray700
import com.talkqquest.app.core.designsystem.Primary600
import com.talkqquest.app.core.designsystem.TqType
import com.talkqquest.app.core.designsystem.White

// 선택형 칩 (CONVENTIONS.md 8번). 선택 시 색 반전.
@Composable
fun TqChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Primary600 else White,
        contentColor = if (selected) White else Gray700,
        border = if (selected) null else BorderStroke(1.dp, Gray200),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(text = text, style = TqType.LabelL)
        }
    }
}
