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
    const val SIGNUP_TERMS = "signup_terms"                       // SignupTermsScreen
    const val SIGNUP_TERMS_SOCIAL = "signup_terms_social"         // SignupTermsScreen
    const val SIGNUP_EMAIL = "signup_email"                       // SignupEmailScreen
    const val SIGNUP_VERIFY = "signup_verify"                     // SignupVerifyScreen
    const val SIGNUP_PASSWORD = "signup_password"                 // SignupPasswordScreen
    const val SIGNUP_NICKNAME = "signup_nickname"                 // SignupNicknameScreen
    const val SIGNUP_NICKNAME_SOCIAL = "signup_nickname_social"   // SignupNicknameScreen
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
    const val PROFILE_INFO = "profile_info"                         // ProfileInfoScreen
    const val PROFILE_NICKNAME_EDIT = "profile_nickname_edit"     // ProfileNicknameEditScreen
    const val PROFILE_PASSWORD_CHANGE = "profile_password_change" // ProfilePasswordChangeScreen
    const val PROFILE_NEW_PASSWORD = "profile_new_password"       // ProfileNewPasswordScreen
    const val PROFILE_CONNECTED_ACCOUNT = "profile_connected_account" // ProfileConnectedAccountScreen
    const val PROFILE_CONCERN = "profile_concern"                 // ProfileConcernScreen
    const val PROFILE_TERMS = "profile_terms"                       // ProfileTermsScreen
    const val PROFILE_SERVICE_TERMS = "profile_service_terms"       // ProfileTermsDetailScreen
    const val PROFILE_PRIVACY_POLICY = "profile_privacy_policy"     // ProfileTermsDetailScreen
    const val PROFILE_SUPPORT = "profile_support"                 // ProfileSupportScreen
    const val PROFILE_WITHDRAW = "profile_withdraw"               // ProfileWithdrawScreen

    const val HOME = "home"                                       // HomeScreen
    const val NOTIFICATION = "notification"                       // NotificationScreen (알림 화면)
    const val MISSION_LIST = "mission_list"                       // MissionListScreen (하단 미션 탭)
    const val MISSION_LIST_HOME = "mission_list_home"             // MissionListScreen (홈 "다른 미션 보기" — 예전 헤더, 홈 탭 유지)
    const val MISSION_DETAIL = "mission_detail/{missionId}"       // MissionDetailScreen
    const val CONVERSATION_SETUP_1 = "conversation_setup_1/{missionId}" // 장소
    const val CONVERSATION_SETUP_2 = "conversation_setup_2/{missionId}" // 상대
    const val CONVERSATION_SETUP_3 = "conversation_setup_3/{missionId}" // 성별·나이
    const val CONVERSATION_SETUP_4 = "conversation_setup_4/{missionId}" // 친밀도·말투
    const val CONVERSATION = "conversation/{conversationId}"      // ConversationScreen
    const val CONVERSATION_COMPLETE = "conversation_complete/{conversationId}" // ConversationCompleteScreen
    const val MISSION_COMPLETE = "mission_complete/{missionId}"   // MissionCompleteScreen (XP 획득)
    const val FEEDBACK = "feedback/{feedbackId}"                  // FeedbackScreen
    const val FEEDBACK_DETAIL = "feedback_detail/{feedbackId}"    // FeedbackDetailScreen

    const val COMMUNITY_LIST = "community_list"                   // CommunityListScreen
    const val COMMUNITY_DETAIL = "community_detail/{communityId}" // CommunityDetailScreen
    const val COMMUNITY_CHAT_PREVIEW = "community_chat_preview/{communityId}" // CommunityChatPreviewScreen
    const val COMMUNITY_CREATE = "community_create"              // CommunityCreateScreen
    const val COMMUNITY_ADDRESS_SEARCH = "community_address_search" // CommunityAddressSearchScreen
    const val COMMUNITY_PREVIEW = "community_preview"            // CommunityPreviewScreen
    const val MY_GROUPS = "my_groups"                            // MyGroupsScreen
    // 서버 성장 리포트 응답엔 증가분이 없어, 방금 그 값을 받은 피드백 화면이 넘겨준다.
    const val REPORT = "report?missionTitle={missionTitle}&conversationId={conversationId}&gains={gains}"

    // WeeklyCompareScreen — 주간 비교 리포트(홈/알림창에서 진입).
    // ReportScreen(성장 리포트)과 다른 화면이다: 주차 이동·자주 연습한 주제·미션 진행률이 있고
    // 탭이 없다. 주가 끝나면 알림으로 안내되고 그 알림에서 들어온다.
    // reportId를 붙이면 그 리포트로 바로 들어간다(홈 도착 모달·알림). 없으면 가장 최근 주차.
    const val WEEKLY_COMPARE = "weekly_compare?reportId={reportId}"
}






