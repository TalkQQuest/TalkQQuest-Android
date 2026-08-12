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
        }
    }

    fun readNotification(notificationId: String) {
        val isUnread = _uiState.value.items.firstOrNull { it.id == notificationId }?.isUnread == true
        if (!isUnread) return

        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == notificationId) item.copy(isUnread = false) else item
                },
            )
        }
        notificationRepository.markRead(listOf(notificationId))
    }

    fun readVisibleNotifications() {
        val unreadIds = _uiState.value.items.filter { it.isUnread }.map { it.id }
        if (unreadIds.isEmpty()) return

        _uiState.update { state ->
            state.copy(items = state.items.map { it.copy(isUnread = false) })
        }
        notificationRepository.markRead(unreadIds)
    }

    // 알림창에서 롱프레스 후 삭제를 누르면 현재 목록에서 즉시 제거한다.
    // 서버에 삭제 계약이 없어 이번 동작은 현재 화면 목록에만 적용한다.
    fun removeNotification(notificationId: String) {
        _uiState.update { state ->
            state.copy(items = state.items.filterNot { it.id == notificationId })
        }
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun removeAllNotifications() {
        val ids = _uiState.value.items.map { it.id }
        _uiState.update { it.copy(items = emptyList()) }
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications(ids)
        }
    }
}
