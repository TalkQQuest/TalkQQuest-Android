package com.talkqquest.app.feature.mission.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── 미션 준비 정보(대화 설정 1~4단계) ──
// POST /api/v1/missions/{missionId}/setups — 2026-08-13 실서버 실호출로 계약 확인(추정 아님).
//
// 화면 4단계에서 고른 6축을 한 번에 보낸다. 전 필드 필수다.
// 지금까지는 고른 값이 화면 안에서만 살다 사라졌고 서버로 가지 않았다.

// 대화 장소 — 설정 1단계 카드 5개와 1:1로 맞는다.
@Serializable
enum class MissionSetupEnvironment {
    @SerialName("school") SCHOOL,           // 학교/대학교
    @SerialName("workplace") WORKPLACE,     // 직장
    @SerialName("daily_place") DAILY_PLACE, // 일상공간 (카페, 헬스장, 편의점 등)
    @SerialName("community") COMMUNITY,     // 모임 (동아리, 스터디, 소모임 등)
    @SerialName("online") ONLINE,           // 온라인 (SNS, 채팅)
}

// 대화 상대 — 설정 2단계 카드 5개와 1:1로 맞는다.
@Serializable
enum class MissionSetupPartnerRole {
    @SerialName("friend") FRIEND, // 친구
    @SerialName("peer") PEER,     // 동기/ 동료
    @SerialName("senior") SENIOR, // 선배
    @SerialName("junior") JUNIOR, // 후배
    @SerialName("other") OTHER,   // 직접 설정
}

// 상대 성별 — 설정 3단계.
@Serializable
enum class MissionSetupPartnerGender {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE,
}

// 상대 나이대 — 설정 3단계 칩 6개와 1:1.
@Serializable
enum class MissionSetupPartnerAgeGroup {
    @SerialName("teens") TEENS,               // 10대
    @SerialName("twenties") TWENTIES,         // 20대
    @SerialName("thirties") THIRTIES,         // 30대
    @SerialName("forties") FORTIES,           // 40대
    @SerialName("fifties") FIFTIES,           // 50대
    @SerialName("sixties_plus") SIXTIES_PLUS, // 60대 이상
}

// 요청 body.
// ★intimacyLevel·formalityLevel은 1~5다. 스웨거엔 범위가 없어 실서버로 확인했다 —
//   0이면 "Number must be greater than or equal to 1", 6이면 "less than or equal to 5".
//   화면 눈금은 5칸(0~4)이라 +1 해서 보낸다.
@Serializable
data class MissionSetupRequest(
    val environment: MissionSetupEnvironment,
    val partnerRole: MissionSetupPartnerRole,
    val partnerGender: MissionSetupPartnerGender,
    val partnerAgeGroup: MissionSetupPartnerAgeGroup,
    val intimacyLevel: Int,
    val formalityLevel: Int,
)

@Serializable
data class MissionSetupResponse(
    val missionSetupId: String = "",
    val createdAt: String = "",
)

// GET /missions/{missionId}의 setupGuideline. 화면에는 기본값과 비활성 항목만 반영하고,
// note·recommendedTopics·tags는 추후 디자인이 정해질 때 쓸 수 있도록 계약만 보존한다.
@Serializable
data class MissionSetupGuideline(
    val defaults: MissionSetupDefaults = MissionSetupDefaults(),
    val disabled: MissionSetupDisabled = MissionSetupDisabled(),
    val note: String? = null,
    val recommendedTopics: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

@Serializable
data class MissionSetupDefaults(
    val environment: MissionSetupEnvironment = MissionSetupEnvironment.DAILY_PLACE,
    val partnerRole: MissionSetupPartnerRole = MissionSetupPartnerRole.FRIEND,
    val intimacyLevel: Int = 3,
    val formalityLevel: Int = 3,
    val partnerGender: MissionSetupPartnerGender = MissionSetupPartnerGender.MALE,
    val partnerAgeGroup: MissionSetupPartnerAgeGroup = MissionSetupPartnerAgeGroup.TWENTIES,
)

@Serializable
data class MissionSetupDisabled(
    val environment: List<MissionSetupEnvironment> = emptyList(),
    val partnerRole: List<MissionSetupPartnerRole> = emptyList(),
    val intimacyLevel: List<Int> = emptyList(),
    val formalityLevel: List<Int> = emptyList(),
    val partnerGender: List<MissionSetupPartnerGender> = emptyList(),
    val partnerAgeGroup: List<MissionSetupPartnerAgeGroup> = emptyList(),
)

// ── 화면이 들고 다니는 선택값 ──
// 설정 1~4단계가 각자 자기 화면에서만 값을 들고 있어 다음 화면으로 넘길 방법이 없었다.
// 네 화면이 공유하는 ConversationSetupViewModel이 이 모델 하나를 채워 나간다.
//
// 아직 안 고른 축은 null. 4단계의 두 눈금은 기본값이 있어(가운데) null이 될 일이 없다.
data class ConversationSetupSelection(
    val environment: MissionSetupEnvironment? = null,
    val partnerRole: MissionSetupPartnerRole? = null,
    val partnerGender: MissionSetupPartnerGender? = null,
    val partnerAgeGroup: MissionSetupPartnerAgeGroup? = null,
    val intimacyIndex: Int = 2, // 눈금 0~4, 가운데("보통")
    val formalityIndex: Int = 2,
) {
    // 여섯 축이 다 정해졌을 때만 서버에 보낼 수 있다. 하나라도 비면 null.
    fun toRequest(): MissionSetupRequest? {
        val env = environment ?: return null
        val role = partnerRole ?: return null
        val gender = partnerGender ?: return null
        val age = partnerAgeGroup ?: return null
        return MissionSetupRequest(
            environment = env,
            partnerRole = role,
            partnerGender = gender,
            partnerAgeGroup = age,
            intimacyLevel = intimacyIndex + 1, // 눈금 0~4 → 서버 1~5
            formalityLevel = formalityIndex + 1,
        )
    }
}

// 화면 카드 순서 → 서버 enum. 카드 순서가 바뀌면 여기도 같이 바꿔야 한다.
val SetupEnvironmentByIndex = listOf(
    MissionSetupEnvironment.SCHOOL,
    MissionSetupEnvironment.WORKPLACE,
    MissionSetupEnvironment.DAILY_PLACE,
    MissionSetupEnvironment.COMMUNITY,
    MissionSetupEnvironment.ONLINE,
)

// partnerOptions 순서 그대로: 친구 · 동기/동료 · 선배 · 후배 · 직접 설정.
val SetupPartnerRoleByIndex = listOf(
    MissionSetupPartnerRole.FRIEND,
    MissionSetupPartnerRole.PEER,
    MissionSetupPartnerRole.SENIOR,
    MissionSetupPartnerRole.JUNIOR,
    MissionSetupPartnerRole.OTHER,
)

val SetupGenderByIndex = listOf(
    MissionSetupPartnerGender.MALE,
    MissionSetupPartnerGender.FEMALE,
)

val SetupAgeGroupByIndex = listOf(
    MissionSetupPartnerAgeGroup.TEENS,
    MissionSetupPartnerAgeGroup.TWENTIES,
    MissionSetupPartnerAgeGroup.THIRTIES,
    MissionSetupPartnerAgeGroup.FORTIES,
    MissionSetupPartnerAgeGroup.FIFTIES,
    MissionSetupPartnerAgeGroup.SIXTIES_PLUS,
)
