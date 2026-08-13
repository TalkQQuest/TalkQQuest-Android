package com.talkqquest.app.feature.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.talkqquest.app.core.designsystem.Primary600

@Composable
internal fun Modifier.profileItemClick(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(bounded = true, color = Primary600),
    enabled = enabled,
    onClick = onClick,
)

@Composable
internal fun Modifier.profileCircleClick(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = clip(CircleShape).clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = ripple(bounded = true, color = Primary600),
    enabled = enabled,
    onClick = onClick,
)