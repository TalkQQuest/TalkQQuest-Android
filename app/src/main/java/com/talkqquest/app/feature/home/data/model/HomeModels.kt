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
    // 실전 티어(성장 티어 시스템 — 레벨 카드에 표시). 서버 growthTotals(누적 4필드)가 merge되면
    // 그 값으로 티어/별을 앱에서 계산해 채운다(300점 단위·마름모 3개=티어). 지금은 명세만 상태라 디자인 기본값.
    // TODO(백엔드 growthTotals merge 후): kindness/initiative/empathy/questionLink 누적값 → 티어·별 계산 연결.
    val tierName: String = "골드",  // 티어 이름 (브론즈/실버/골드/플래티넘/다이아/마스터)
    val tierStars: Int = 2,         // 채워진 별 수 0~3 (티어 내 단계)
    // 안 읽은 주간 비교 리포트가 있으면 홈 진입 시 도착 모달을 띄운다(백엔드 1번째 보고: 주간=목록·홈 알림 진입).
    // TODO(백엔드 연동): 서버가 '안 읽은 주간 비교 리포트 존재' 신호를 주면 매핑.
    val hasNewWeeklyReport: Boolean = false,
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
    val termsAgreedAt: String? = null,
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
data class ArchiveSummary(
    val totalCount: Int = 0,
    val missionRecordCount: Int = 0,
    val conversationCount: Int = 0,
    val phraseCount: Int = 0,
    val reportCount: Int = 0,
    val recentItems: List<ArchiveRecentItem> = emptyList(),
)

@Serializable
data class ArchiveRecentItem(
    val id: String = "",
    val referenceId: String = "",
    val type: String = "",
    val reportType: String? = null,
    val title: String = "",
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val duration: String? = null,
    val isBookmarked: Boolean = false,
    val missionId: String? = null,
    val conversationId: String? = null,
    val missionRecordId: String? = null,
    val missionStatus: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null,
    val createdAt: String = "",
)
@Serializable
data class ProfileImageUploadResponse(
    val avatarUrl: String = "",
)
@Serializable
data class UserSettings(
    val missionReminder: Boolean = false,
    val missionReminderTime: String = "09:00",
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
)
@Serializable
data class UserSettingsUpdateRequest(
    val missionReminder: Boolean? = null,
    val missionReminderTime: String? = null,
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
