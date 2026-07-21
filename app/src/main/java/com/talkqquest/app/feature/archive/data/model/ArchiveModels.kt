package com.talkqquest.app.feature.archive.data.model

import kotlinx.serialization.Serializable

// 아카이브 홈 요약(F101) DTO. 기능명세서 기준.
// GET /api/v1/archives/summary 의 응답(data) 형태.
@Serializable
data class ArchiveSummary(
    val completedMissionCount: Int,
    val conversationCount: Int,
    val savedSentenceCount: Int,
    val reportCount: Int,
    val recentActivities: List<ArchiveRecentActivity>
)

// 최근 활동 요약. 서버에서는 타입을 String으로 내려줌 (예: "MISSION", "CONVERSATION")
@Serializable
data class ArchiveRecentActivity(
    val id: String,
    val type: String,   // 서버 연동 시 Enum 매핑을 위해 String 수신
    val title: String,
    val status: String,
    val date: String
)

// 💡 ArchiveRepository.kt 에서 여기로 옮겨온 데이터 클래스들

data class ReviewChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val time: String
)

// 나중에 서버 응답에 맞춰 수정될 수 있도록 Mock 대신 일반적인 이름 추천
data class ConversationDetailMock(
    val id: String,
    val title: String,
    val date: String,
    val duration: String,
    val summaryKeywords: List<String>,
    val summaryText: String,
    val mainContentText: String,
    val feedbacks: List<Pair<String, Int>>,
    val messages: List<ReviewChatMessage>
)