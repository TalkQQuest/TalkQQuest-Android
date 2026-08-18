package com.talkqquest.app.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult

// 글자를 한 자씩 왼쪽→오른쪽(여러 줄이면 위→아래)으로 드러내는 모션.
// 홈 "오늘의 미션" 카드가 새로고침될 때 쓰는 것과 같은 방식이다.
//
// progress 0 = 아무것도 안 보임, 1 = 전부 보임. 글자마다 시작 시점을 조금씩 늦춰
// 훑고 지나가는 느낌을 만든다.
//
// ★주의: 글자마다 clipRect로 칸을 잘라 그리는데 그 칸은 텍스트 레이아웃 좌표(축소 전)다.
// 이 modifier보다 안쪽에 크기 변형(graphicsLayer의 scale 등)을 두면 칸은 그대로인데
// 내용만 작아져 글자 사이에 안 칠해진 세로 틈이 생긴다. 변형은 반드시 바깥에 둘 것.
fun Modifier.glyphWipe(
    progress: Float,
    layout: TextLayoutResult?,
    // 글자 하나가 흐릿한 상태에서 또렷해지기까지 걸리는 구간(글자 수 기준).
    // 클수록 여러 글자가 동시에 번지듯 나타난다.
    fadeWindow: Float = 4f,
): Modifier = graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        val textLayout = layout ?: run {
            drawContent()
            return@drawWithContent
        }
        val text = textLayout.layoutInput.text.text
        // 공백은 그릴 게 없어 순서에서 뺀다 — 넣으면 중간에 멈칫하는 느낌이 생긴다.
        val offsets = text.indices.filter { !text[it].isWhitespace() }
        if (offsets.isEmpty()) {
            drawContent()
            return@drawWithContent
        }
        val total = offsets.size + fadeWindow
        offsets.forEachIndexed { order, offset ->
            val onset = order / total
            val completion = (order + fadeWindow) / total
            val alpha = ((progress - onset) / (completion - onset)).coerceIn(0f, 1f)
            if (alpha <= 0f) return@forEachIndexed
            val bounds = textLayout.getBoundingBox(offset)
            // 글자 칸을 먼저 자르고 그 안에서만 원본을 다시 그린다. 이렇게 해야
            // 반투명 레이어가 겹치며 앞 글자를 지우는 일이 생기지 않는다.
            clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom) {
                drawContext.canvas.saveLayer(bounds, Paint().apply { this.alpha = alpha })
                this@drawWithContent.drawContent()
                drawContext.canvas.restore()
            }
        }
    }
