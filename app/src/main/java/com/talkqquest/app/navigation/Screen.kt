package com.talkqquest.app.navigation

/**
 * ?붾㈃ route ?곸닔 ?뺤쓽.
 *
 * - ?ㅼ씠諛?洹쒖튃(?뚮Ц???ㅻ꽕?댄겕耳?댁뒪)? CONVENTIONS.md 6踰?李멸퀬.
 * - 媛??곸닔??NAVIGATION.md??"?ㅽ겕由?ID"? 1:1濡???묐맗?덈떎.
 * - {id} ?뺥깭??寃쎈줈 ?몄옄???덉떆?낅땲?? ?ㅼ젣 ?몄옄 ?대쫫/?좊Т???대떦?먭? ?붾㈃ 留뚮뱾 ??議곗젙?섏꽭??
 * - ?앹뾽(?댄깉/寃뚯떆?꾨즺/?덊눜 ??? ?붾㈃???꾨땲???ㅼ씠?쇰줈洹몃씪 route媛 ?놁뒿?덈떎.
 */
object Screen {

    // ?? A?대떦 (吏???꾩???: 吏꾩엯 쨌 ?꾨줈????
    // ???꾩뭅?대툕 route(ARCHIVE_*)????븷 ?ъ“??2026-07)?쇰줈 C?대떦?쇰줈 ?섏뼱媛붿뒿?덈떎. ?곸닔 ?꾩튂留??ш린 ?⑥쓬.
    const val SPLASH = "splash"                                   // SplashScreen
    const val LOGIN = "login"                                     // LoginScreen (怨꾩젙 ?곕룞 ?앹뾽 ?곹깭 ?ы븿)
    const val EMAIL_LOGIN = "email_login"                         // EmailLoginScreen
    const val SIGNUP_EMAIL = "signup_email"                       // SignupEmailScreen
    const val SIGNUP_VERIFY = "signup_verify"                     // SignupVerifyScreen
    const val SIGNUP_PASSWORD = "signup_password"                 // SignupPasswordScreen
    const val SIGNUP_NICKNAME = "signup_nickname"                 // SignupNicknameScreen
    const val ONBOARDING_WELCOME = "onboarding_welcome"           // OnboardingWelcomeScreen (媛???꾨즺 ?좊땲硫붿씠??
    const val ONBOARDING_PERSONALITY = "onboarding_personality"   // OnboardingPersonalityScreen
    const val ONBOARDING_DIFFICULTY = "onboarding_difficulty"     // OnboardingDifficultyScreen
    const val ONBOARDING_GOAL = "onboarding_goal"                 // OnboardingGoalScreen
    const val ONBOARDING_COMPLETE = "onboarding_complete"         // OnboardingCompleteScreen (?꾨즺 ?좊땲硫붿씠??
    const val ARCHIVE_HOME = "archive_home"                       // ArchiveHomeScreen
    const val ARCHIVE_SEARCH = "archive_search"                   // ArchiveSearchScreen
    const val ARCHIVE_LIST = "archive_list"                       // ArchiveListScreen (誘몄뀡/???臾몄옣/由ы룷??4??
    const val ARCHIVE_CONVERSATION_DETAIL = "archive_conversation_detail/{conversationId}" // ArchiveConversationDetailScreen
    const val ARCHIVE_SAVED_PHRASE = "archive_saved_phrase/{phraseId}"                     // ArchiveSavedPhraseScreen
    const val PROFILE = "profile"                                 // ProfileScreen (?섎떒 ??'?꾨줈??)
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
