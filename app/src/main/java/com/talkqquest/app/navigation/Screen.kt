package com.talkqquest.app.navigation

/**
 * 화면 route 상수 정의.
 *
 * - route 이름은 CONVENTIONS.md의 화면 네이밍 규칙을 따른다.
 * - 화면 추가 시 Screen.kt에 route를 먼저 정의하고 NavGraph.kt에 composable을 등록한다.
 * - {id} 형태는 Navigation argument가 필요한 상세 화면 route에 사용한다.
 * - 팝업/바텀시트처럼 화면 단위가 아닌 UI는 별도 route를 만들지 않는다.
 */
object Screen {

    // A 담당: 진입/auth/onboarding/profile route
    // archive route 상수는 기존 위치를 유지하지만, 실제 화면 구현은 C 담당 영역입니다.
    const val SPLASH = "splash"                                   // SplashScreen
    const val LOGIN = "login"                                     // LoginScreen
    const val EMAIL_LOGIN = "email_login"                         // EmailLoginScreen
    const val SIGNUP_EMAIL = "signup_email"                       // SignupEmailScreen
    const val SIGNUP_VERIFY = "signup_verify"                     // SignupVerifyScreen
    const val SIGNUP_PASSWORD = "signup_password"                 // SignupPasswordScreen
    const val SIGNUP_NICKNAME = "signup_nickname"                 // SignupNicknameScreen
    const val ONBOARDING_WELCOME = "onboarding_welcome"           // OnboardingWelcomeScreen
    const val ONBOARDING_PERSONALITY = "onboarding_personality"   // OnboardingPersonalityScreen
    const val ONBOARDING_DIFFICULTY = "onboarding_difficulty"     // OnboardingDifficultyScreen
    const val ONBOARDING_GOAL = "onboarding_goal"                 // OnboardingGoalScreen
    const val ONBOARDING_COMPLETE = "onboarding_complete"         // OnboardingCompleteScreen
    const val ARCHIVE_HOME = "archive_home"                       // ArchiveHomeScreen
    const val ARCHIVE_SEARCH = "archive_search"                   // ArchiveSearchScreen
    const val ARCHIVE_LIST = "archive_list"                       // ArchiveListScreen
    const val ARCHIVE_CONVERSATION_DETAIL = "archive_conversation_detail/{conversationId}" // ArchiveConversationDetailScreen
    const val ARCHIVE_SAVED_PHRASE = "archive_saved_phrase/{phraseId}"                     // ArchiveSavedPhraseScreen
    const val PROFILE = "profile"                                 // ProfileScreen
    const val PROFILE_BADGES = "profile_badges"                   // ProfileBadgesScreen
    const val PROFILE_RECENT_MISSION = "profile_recent_mission"     // ProfileRecentMissionScreen
    const val PROFILE_SETTINGS = "profile_settings"                 // ProfileSettingsScreen
    const val PROFILE_TERMS = "profile_terms"                       // ProfileTermsScreen
    const val PROFILE_SERVICE_TERMS = "profile_service_terms"       // ProfileTermsDetailScreen
    const val PROFILE_PRIVACY_POLICY = "profile_privacy_policy"     // ProfileTermsDetailScreen
    const val PROFILE_SUPPORT = "profile_support"                 // ProfileSupportScreen
    const val PROFILE_WITHDRAW = "profile_withdraw"               // ProfileWithdrawScreen

    // ?? B?대떦 (?대룄/?ㅺ린??: 誘몄뀡 쨌 AI ???쨌 ?깆옣 由ы룷????
    const val HOME = "home"                                       // HomeScreen
    const val NOTIFICATION = "notification"                       // NotificationScreen (??踰????뚮┝李? ?붿옄??誘몄셿??placeholder)
    const val MISSION_LIST = "mission_list"                       // MissionListScreen
    const val MISSION_DETAIL = "mission_detail/{missionId}"       // MissionDetailScreen
    const val CONVERSATION_PREP = "conversation_prep/{missionId}" // ConversationPrepScreen
    const val CONVERSATION = "conversation/{conversationId}"      // ConversationScreen
    const val CONVERSATION_COMPLETE = "conversation_complete/{conversationId}" // ConversationCompleteScreen (????붿빟)
    const val MISSION_COMPLETE = "mission_complete/{missionId}"   // MissionCompleteScreen (XP ?띾뱷)
    const val FEEDBACK = "feedback/{feedbackId}"                  // FeedbackScreen
    const val FEEDBACK_DETAIL = "feedback_detail/{feedbackId}"    // FeedbackDetailScreen

    // ?? C?대떦 (??源?ы썕): ?꾩뭅?대툕 쨌 而ㅻ??덊떚 ?? (?꾩뭅?대툕 route????ARCHIVE_* 李멸퀬)
    const val COMMUNITY_LIST = "community_list"                   // CommunityListScreen
    const val COMMUNITY_DETAIL = "community_detail/{communityId}" // CommunityDetailScreen
    const val COMMUNITY_CHAT_PREVIEW = "community_chat_preview/{communityId}" // CommunityChatPreviewScreen
    const val COMMUNITY_CREATE = "community_create"              // CommunityCreateScreen
    const val COMMUNITY_ADDRESS_SEARCH = "community_address_search" // CommunityAddressSearchScreen
    const val COMMUNITY_PREVIEW = "community_preview"            // CommunityPreviewScreen
    const val MY_GROUPS = "my_groups"                            // MyGroupsScreen
    // ReportScreen (?깆옣 由ы룷??二쇨컙 鍮꾧탳 ???듯빀).
    // missionTitle = ??由ы룷?멸? ?섏삩 誘몄뀡 ?쒕ぉ (????쒗듃 移대뱶 ?쒕ぉ???곗엫). ?놁쑝硫?鍮?媛?
    const val REPORT = "report?missionTitle={missionTitle}"
}


