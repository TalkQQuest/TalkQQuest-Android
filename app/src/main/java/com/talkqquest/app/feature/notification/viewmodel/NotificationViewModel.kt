package com.talkqquest.app.feature.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.notification.data.NotificationRepository
import com.talkqquest.app.feature.notification.data.model.NotificationUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 알림창 화면 상태 (CONVENTIONS 6번: [화면이름]UiState)
data class NotificationUiState(
    val isLoading: Boolean = false,
    val items: List<NotificationUiItem> = emptyList(),
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = notificationRepository.getNotifications()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, items = result.data)
                }
                else -> _uiState.update { it.copy(isLoading = false) } // 폴백이 있어 실패해도 조용히
            }
            // 알림창을 봤으니 전체 읽음 처리(PATCH /notifications/all/read) — 홈 벨 빨간 점이 꺼지는 근거.
            // 목록을 받은 "뒤"에 호출해 이번 화면에선 안읽음 강조가 그대로 보이고, 홈 복귀 시부터 반영됨.
            notificationRepository.markAllRead()
        }
    }
}
