package com.talkqquest.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.talkqquest.app.core.designsystem.Gray400
import com.talkqquest.app.core.designsystem.TqType

// 임시 자리표시자. TODO(각 담당): NavGraph.kt에서 실제 화면으로 교체.
@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "$name 화면 (준비 중)", style = TqType.HeadingM, color = Gray400)
    }
}
