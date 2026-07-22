package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.talkqquest.app.core.designsystem.FitDesign
import com.talkqquest.app.core.designsystem.Gray50
import com.talkqquest.app.core.designsystem.Gray500
import com.talkqquest.app.core.designsystem.Gray800
import com.talkqquest.app.core.designsystem.TalkQQuestTheme
import com.talkqquest.app.core.designsystem.TqType

@Composable
fun ProfileRecentMissionScreen(
    onBack: () -> Unit = {},
) = FitDesign {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
    ) {
        ProfileSimpleTopBar(
            title = "\uCD5C\uADFC \uBBF8\uC158 \uC694\uC57D",
            onBack = onBack,
            backIconStartPadding = 16.dp,
        )
    }
}

@Composable
internal fun ProfileSimpleTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backIconStartPadding: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(top = 39.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = backIconStartPadding)
                .size(28.dp)
                .clickable(onClick = onBack),
        )
        Text(
            text = title,
            style = TqType.BodyL.copy(fontWeight = FontWeight.Medium),
            color = Gray800,
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=393dp,height=852dp")
@Composable
private fun ProfileRecentMissionScreenPreview() {
    TalkQQuestTheme {
        ProfileRecentMissionScreen()
    }
}