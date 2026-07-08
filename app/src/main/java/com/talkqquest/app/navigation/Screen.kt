package com.talkqquest.app.navigation

/**
 * 화면 route 상수 정의.
 *
 * - 네이밍 규칙(소문자 스네이크케이스)은 CONVENTIONS.md 6번 참고.
 * - 각 상수는 NAVIGATION.md의 "스크린 ID"와 1:1로 대응됩니다.
 * - {id} 형태의 경로 인자는 예시입니다. 실제 인자 이름/유무는 담당자가 화면 만들 때 조정하세요.
 * - 팝업(이탈/게시완료/탈퇴 등)은 화면이 아니라 다이얼로그라 route가 없습니다.
 */
object Screen {

    // ── A담당 (지니/전준호): 진입 · 아카이브 · 프로필 ──
    const val SPLASH = "splash"                                   // SplashScreen
    const val LOGIN = "login"                                     // LoginScreen (계정 연동 팝업 상태 포함)
    const val SIGNUP_EMAIL = "signup_email"                       // SignupEmailScreen
    const val SIGNUP_VERIFY = "signup_verify"                     // SignupVerifyScreen
    const val SIGNUP_PASSWORD = "signup_password"                 // SignupPasswordScreen
    const val SIGNUP_NICKNAME = "signup_nickname"                 // SignupNicknameScreen
    const val ONBOARDING_WELCOME = "onboarding_welcome"           // OnboardingWelcomeScreen (가입 완료 애니메이션)
    const val ONBOARDING_PERSONALITY = "onboarding_personality"   // OnboardingPersonalityScreen
    const val ONBOARDING_DIFFICULTY = "onboarding_difficulty"     // OnboardingDifficultyScreen
    const val ONBOARDING_GOAL = "onboarding_goal"                 // OnboardingGoalScreen
    const val ONBOARDING_COMPLETE = "onboarding_complete"         // OnboardingCompleteScreen (완료 애니메이션)
    const val ARCHIVE_HOME = "archive_home"                       // ArchiveHomeScreen
    const val ARCHIVE_SEARCH = "archive_search"                   // ArchiveSearchScreen
    const val ARCHIVE_LIST = "archive_list"                       // ArchiveListScreen (미션/대화/문장/리포트 4탭)
    const val ARCHIVE_CONVERSATION_DETAIL = "archive_conversation_detail/{conversationId}" // ArchiveConversationDetailScreen
    const val ARCHIVE_SAVED_PHRASE = "archive_saved_phrase/{phraseId}"                     // ArchiveSavedPhraseScreen
    const val PROFILE = "profile"                                 // ProfileScreen (하단 탭 '프로필')

    // ── B담당 (이도/윤기수): 미션 · AI 대화 ──
    const val HOME = "home"                                       // HomeScreen
    const val MISSION_LIST = "mission_list"                       // MissionListScreen
    const val MISSION_DETAIL = "mission_detail/{missionId}"       // MissionDetailScreen
    const val CONVERSATION_PREP = "conversation_prep/{missionId}" // ConversationPrepScreen
    const val CONVERSATION = "conversation/{conversationId}"      // ConversationScreen
    const val CONVERSATION_COMPLETE = "conversation_complete/{conversationId}" // ConversationCompleteScreen (대화 요약)
    const val MISSION_COMPLETE = "mission_complete/{missionId}"   // MissionCompleteScreen (XP 획득)
    const val FEEDBACK = "feedback/{feedbackId}"                  // FeedbackScreen
    const val FEEDBACK_DETAIL = "feedback_detail/{feedbackId}"    // FeedbackDetailScreen

    // ── C담당 (훈/김재훈): 커뮤니티 · 성장 리포트 ──
    const val COMMUNITY_LIST = "community_list"                   // CommunityListScreen
    const val COMMUNITY_DETAIL = "community_detail/{communityId}" // CommunityDetailScreen
    const val COMMUNITY_CHAT_PREVIEW = "community_chat_preview/{communityId}" // CommunityChatPreviewScreen
    const val COMMUNITY_CREATE = "community_create"              // CommunityCreateScreen
    const val COMMUNITY_ADDRESS_SEARCH = "community_address_search" // CommunityAddressSearchScreen
    const val COMMUNITY_PREVIEW = "community_preview"            // CommunityPreviewScreen
    const val MY_GROUPS = "my_groups"                            // MyGroupsScreen
    const val REPORT = "report"                                  // ReportScreen (성장 리포트/주간 비교 탭 통합)
}
