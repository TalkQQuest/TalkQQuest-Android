package com.talkqquest.app.feature.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeSummary(
    val nickname: String = "", // 서버가 null 가능(닉네임 미설정 계정 실측) → coerce로 ""가 됨. 표시는 Repository가 폴백.
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val todayMission: TodayMission? = null,
    val archiveCount: Int,
    val communityCount: Int,
    val questionOfDay: String? = null,
    val hasNewNotification: Boolean = false,
)

@Serializable
data class UserMe(
    val id: String = "",
    val name: String = "",
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val dailyConversationGoal: Int = 1,
    val onboardingCompleted: Boolean = false,
)

@Serializable
data class CurrentPasswordVerifyRequest(
    val currentPassword: String,
)

@Serializable
data class PasswordChangeRequest(
    val newPassword: String,
)

@Serializable
data class UserUpdateRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val dailyConversationGoal: Int? = null,
    val preferredStyle: String? = null,
    val interests: List<String>? = null,
)

@Serializable
data class MyBadgesResponse(
    val badges: List<MyBadge> = emptyList(),
)

@Serializable
data class MyBadge(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val iconUrl: String? = null,
    val isEarned: Boolean = false,
    val earnedAt: String? = null,
    val progress: BadgeProgress? = null,
)

@Serializable
data class BadgeProgress(
    val current: Int = 0,
    val target: Int = 0,
)




@Serializable
data class MyPageDashboard(
    val nickname: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val badges: List<DashboardBadge> = emptyList(),
    val weeklyMissionStatus: WeeklyMissionStatus = WeeklyMissionStatus(),
    val recentMissionsSummary: List<RecentMissionSummary> = emptyList(),
)

@Serializable
data class DashboardBadge(
    val id: String = "",
    val name: String = "",
    val iconUrl: String? = null,
)

@Serializable
data class WeeklyMissionStatus(
    val completed: Int = 0,
    val total: Int = 7,
)

@Serializable
data class RecentMissionSummary(
    val id: String = "",
    val title: String = "",
    val result: String = "",
    val completedAt: String = "",
)


@Serializable
data class ProfileImageUploadResponse(
    val avatarUrl: String = "",
)
@Serializable
data class UserSettings(
    val missionReminder: Boolean = false,
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
)
@Serializable
data class UserSettingsUpdateRequest(
    val missionReminder: Boolean? = null,
    val communityApproved: Boolean? = null,
    val reportReady: Boolean? = null,
    val marketing: Boolean? = null,
)
@Serializable
data class LegalDocument(
    val type: String = "",
    val version: String = "",
    val content: String = "",
    val createdAt: String = "",
)
@Serializable
data class TodayMission(
    val id: String,
    val title: String,
    val description: String? = null,
    val difficulty: String,
    val estimatedMinutes: Int,
    val rewardXp: Int,
)
