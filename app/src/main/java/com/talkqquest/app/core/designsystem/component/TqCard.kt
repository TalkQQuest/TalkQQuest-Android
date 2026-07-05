package com.talkqquest.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.Gray900
import com.talkqquest.app.core.designsystem.White

// 공통 카드 (CONVENTIONS.md 8번). 흰 배경, radius 20, 그림자. 여백은 contentPadding.
@Composable
fun TqCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = White,
        contentColor = Gray900,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}
