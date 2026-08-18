package com.talkqquest.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.feature.notification.data.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

// 주간 비교 리포트 도착 모달 전용 ViewModel. 이 모달은 MainScreen(앱 최상위)에 붙어 세션 내내 살아
// 있으므로, 알림창의 NotificationViewModel처럼 init에서 목록을 조회하면 앱 시작마다 불필요한 호출이
// 하나 늘어난다. 이 ViewModel은 모달을 본 순간 알림을 읽음 처리하는 동작 하나만 담당한다.
@HiltViewModel
class WeeklyReportModalViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    // 확인/닫기/바깥 탭 어느 경로로 모달을 벗어나든 호출된다.
    // 실패해도 화면 흐름에는 영향이 없어야 하므로 결과를 화면에 반영하지 않는다.
    fun markSeen(reportId: String?) {
        viewModelScope.launch {
            runCatching { notificationRepository.markWeeklyCompareRead(reportId) }
        }
    }
}
