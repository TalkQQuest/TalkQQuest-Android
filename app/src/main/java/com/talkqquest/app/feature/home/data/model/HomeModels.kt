package com.talkqquest.app.feature.home.data.model

import com.talkqquest.app.core.util.GrowthTotalsDto
import kotlinx.serialization.Serializable

@Serializable
data class HomeSummary(
    val nickname: String = "", // ?쒕쾭媛 null 媛???됰꽕??誘몄꽕??怨꾩젙 ?ㅼ륫) ??coerce濡?""媛 ?? ?쒖떆??Repository媛 ?대갚.
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val todayMission: TodayMission? = null,
    val archiveCount: Int,
    val communityCount: Int,
    val questionOfDay: String? = null,
    val hasNewNotification: Boolean = false,
    // ?λ젰移??꾩쟻 ?먭컪 4媛????ㅼ쟾 ?곗뼱???먯쿇. ?쒕쾭媛 ???붿빟?먮룄 ?④퍡 ?대젮以???ㅼ썾嫄?HomeSummaryResponseDto).
    val growthTotals: GrowthTotalsDto = GrowthTotalsDto(),
    // ?ㅼ쟾 ?곗뼱(?덈꺼 移대뱶 ?쒖떆) ???쒕쾭媛 二쇰뒗 媛믪씠 ?꾨땲????growthTotals濡?Repository媛 怨꾩궛??梨꾩슫??
    // ?깆옣 由ы룷?몄? 媛숈? 怨꾩궛(core/util/TierProgress)???⑥빞 ???붾㈃???닿툔?섏? ?딅뒗??
    val tierName: String = "怨⑤뱶",  // ?곗뼱 ?대쫫 (釉뚮줎利??ㅻ쾭/怨⑤뱶/?뚮옒?곕꽆/?ㅼ씠??留덉뒪??
    val tierStars: Int = 2,         // 梨꾩썙吏?蹂???0~3 (?곗뼱 ???④퀎)
    // ??二쇨컙 鍮꾧탳 由ы룷???꾩갑 ?좏샇. 諛깆뿏??異붽?(2026-08-13) ????吏꾩엯 ???꾩갑 紐⑤떖???꾩슦怨?
    // "蹂대윭媛湲???reportId濡?洹?由ы룷?몃? 諛붾줈 ?곕떎(紐⑸줉 理쒖떊??異붿륫?섏? ?딅뒗??.
    val newWeeklyCompareReport: NewWeeklyCompareReport? = null,
)

@Serializable
data class NewWeeklyCompareReport(
    val available: Boolean = false,
    val reportId: String? = null,
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
    val personalityType: String? = null,
    val difficultSituations: List<String> = emptyList(),
    val purpose: List<String> = emptyList(),
    val preferredStyle: String? = null,
    val interests: List<String> = emptyList(),
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
    val refreshCount: Int = 0,
    val refreshLimit: Int? = null,
    val remainingRefreshes: Int? = null,
)
