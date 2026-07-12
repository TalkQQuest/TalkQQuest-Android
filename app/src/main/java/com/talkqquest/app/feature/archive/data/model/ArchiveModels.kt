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