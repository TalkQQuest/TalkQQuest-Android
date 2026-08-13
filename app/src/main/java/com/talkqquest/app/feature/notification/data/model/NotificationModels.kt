package com.talkqquest.app.feature.notification.data.model

import kotlinx.serialization.Serializable

// GET /api/v1/notifications ?묐떟 data ??{ notifications: [...] }.
// dev 諛깆뿏???ㅺ퀎??notification.dto.ts NotificationItem) ?議??뺤젙 ??2026-07-25.
@Serializable
data class NotificationsResponse(
    val notifications: List<NotificationItemDto> = emptyList(),
)

@Serializable
data class NotificationItemDto(
    val id: String = "",
    val type: String = "",         // mission_reminder | report_ready | ...
    val title: String = "",        // 移대뱶 ???묒? 以?(?? "?덈줈??由ы룷?멸? ?꾩갑?덉뼱??")
    val body: String? = null,      // 移대뱶 ?꾨옒 以?(nullable) ??dev ?ㅺ퀎???꾨뱶紐?(?댁쟾 異붿젙 message?먯꽌 ?뺤젙)
    val isRead: Boolean = false,
    val createdAt: String = "",    // ISO (?? 2026-07-22T09:51:57.440Z)
    // 이 알림이 가리키는 대상. 백엔드 추가(2026-08-13).
    // 주간 비교 리포트 알림이면 referenceType이 리포트 계열이고 referenceId가 그 리포트 id다.
    val referenceId: String? = null,
    val referenceType: String? = null,
)

// GET/PATCH /api/v1/notifications/settings ??dev NotificationSettingsResponseDto.
// ?????붾㈃(?뚮┝李??먮뒗 ?ㅼ젙 UI媛 ?녿떎. ?ㅼ젙 ?됱? ?꾨줈???ㅼ젙 ?붾㈃(A?대떦)???덇퀬,
//   ?ш린??API 怨꾩빟留?蹂닿??쒕떎.
@Serializable
data class NotificationSettings(
    val missionReminder: Boolean = false,
    val communityApproved: Boolean = false,
    val reportReady: Boolean = false,
    val marketing: Boolean = false,
    // 誘몄뀡 由щ쭏?몃뱶瑜?蹂대궪 ?쒓컖. "HH:mm" 24?쒓컙 ?쒓린, ?쒕쾭 湲곕낯媛?"09:00" (諛깆뿏??蹂닿퀬 2026-08-11).
    // ?깆? ??λ쭔 ?섎㈃ ?섍퀬 洹??쒓컖???ㅼ젣濡??뚮┝??蹂대궡??嫄??쒕쾭媛 ?쒕떎 ???곕줈 遺瑜?API媛 ?녿떎.
    // ?낇삎?앹씠 ?닿툔?섎㈃ 400(VALIDATION_ERROR "missionReminderTime? HH:mm ?뺤떇?댁뼱???⑸땲??)?대떎.
    //   ?ㅼ꽌踰??몄텧濡??뺤씤?? "9:00"쨌"09:00:00"쨌"9?? ?꾨? 嫄곗젅?섎땲 ???먮━濡?留욎떠 蹂대궪 寃?
    val missionReminderTime: String = "09:00",
)

// PATCH 蹂몃Ц ??遺遺??낅뜲?댄듃??諛붽? ??ぉ留??대뒗???섎㉧吏??null?대㈃ ??蹂대깂).
// ?묐떟 DTO(NotificationSettings)瑜?洹몃?濡?蹂몃Ц???곕㈃ ?먮?吏 ?딆? ??ぉ源뚯? 湲곕낯媛믪쑝濡???뼱?⑥꽌,
// ?쒓컖 ?섎굹 諛붽씀?ㅻ떎 ?뚮┝ ?ㅼ쐞移섍? ?꾨? 爰쇱쭊??
@Serializable
data class NotificationSettingsUpdateRequest(
    val missionReminder: Boolean? = null,
    val communityApproved: Boolean? = null,
    val reportReady: Boolean? = null,
    val marketing: Boolean? = null,
    val missionReminderTime: String? = null,
)


// ?붾㈃??紐⑤뜽 ???뚮┝ 移대뱶 1??(?붿옄?? ???묒? ?뚯깋 以?+ ?꾨옒 援듭? 以?+ ?쒓컙 + ?덉씫????.


data class NotificationUiItem(
    val id: String,
    val category: String,  // ???묒? 以?(Body/S Gray500)
    val body: String,      // ?꾨옒 援듭? 以?(Body/L Medium Gray900)
    val timeText: String,  // 諛⑷툑 / N遺???/ N?쒓컙 ??/ N????/ yyyy.MM.dd
    val isUnread: Boolean, // true = 蹂대씪 ???쒖떆
    // 二쇨컙 鍮꾧탳 由ы룷???뚮┝留??꾨옒 以??놁뿉 ?붿궡?쒓? 遺숇뒗?????뚮윭??諛붾줈 由ы룷?몃줈 ?대룞?섎뒗 ?듬줈?쇱꽌
    // (CSS "?뚮┝李? Frame 427321769). ?섎㉧吏 ?뚮┝? ?쎄퀬 留덈뒗 寃껋씠???붿궡?쒓? ?녿떎.
    val hasLink: Boolean = false,
    // 화살표가 데려갈 리포트 id (서버 referenceId). 없으면 가장 최근 주차로 들어간다.
    val linkReportId: String? = null,
)
