package com.talkqquest.app.feature.mission.data.model

// 대화 메시지 1개. 서버 미배포 — 명세 확정 시 필드 조정(TODO).
data class ChatMessage(
    val id: String, // 서버 메시지 id = UUID 문자열 (백엔드 ConversationMessage.id)

    val text: String,
    val isFromUser: Boolean, // true = 내 말풍선(보라, 오른쪽) / false = AI(흰색, 왼쪽)
    val time: String,        // 예: "9:20" — 같은 발신자 묶음의 마지막 말풍선 옆에만 표시
)
