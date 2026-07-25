package com.talkqquest.app.core.util

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val savedDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

// 서버 ISO8601 시각("2026-08-20T12:34:56.000Z")을 카드에 쓰는 "2026.08.20"으로.
// 기기 시간대 기준 날짜로 변환하고, 형식이 다르면 앞 10자리를 그대로 점 표기로 폴백한다.
fun String.toSavedDate(): String = runCatching {
    OffsetDateTime.parse(this).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate().format(savedDateFormatter)
}.getOrElse { take(10).replace('-', '.') }
