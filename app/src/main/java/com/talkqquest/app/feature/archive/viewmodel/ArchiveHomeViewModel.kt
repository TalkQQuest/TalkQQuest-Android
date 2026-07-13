package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. UI용 데이터 모델 및 Enum 정의
enum class ActivityType {
    MISSION, CONVERSATION, SENTENCE, REPORT
}

// 💡 미션용 부가 데이터 필드 추가
data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val status: String,
    val date: String,
    // 💡 미션 카드용 추가 필드 (기본값 null로 설정하여 기존 로직과 호환 유지)
    val difficulty: String? = null,
    val category: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null
)

// 2. UI 상태 클래스 정의
data class ArchiveHomeUiState(
    val completedMissionCount: Int = 0,
    val conversationCount: Int = 0,
    val savedSentenceCount: Int = 0,
    val reportCount: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// 3. ViewModel 구현
@HiltViewModel
class ArchiveHomeViewModel @Inject constructor(
    private val archiveRepository: ArchiveRepository // 💡 새로 만든 Repository 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveHomeUiState())
    val uiState: StateFlow<ArchiveHomeUiState> = _uiState.asStateFlow()

    init {
        loadArchiveHomeData()
    }

    private fun loadArchiveHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 💡 Repository를 통해 (임시) API 호출 결과를 받아옴
            when (val result = archiveRepository.getArchiveSummary()) {
                is ApiResult.Success -> {
                    val summary = result.data

                    // 서버 DTO(ArchiveRecentActivity)를 UI 모델(RecentActivity)로 매핑
                    val uiActivities = summary.recentActivities.map { dto ->
                        RecentActivity(
                            id = dto.id,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = dto.status,
                            date = dto.date,
                            // TODO: 서버에서 내려주는 미션 부가 데이터 매핑 필요
                            difficulty = "쉬움",
                            category = "짧은 대화",
                            estimatedMinutes = 2,
                            rewardXp = 20
                        )
                    }

                    _uiState.update {
                        it.copy(
                            completedMissionCount = summary.completedMissionCount,
                            conversationCount = summary.conversationCount,
                            savedSentenceCount = summary.savedSentenceCount,
                            reportCount = summary.reportCount,
                            recentActivities = uiActivities,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "데이터를 불러오는데 실패했습니다.")
                    }
                }
                is ApiResult.Exception -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.")
                    }
                }
            }
        }
    }

    // 서버의 String 타입을 UI의 Enum 타입으로 안전하게 변환하는 헬퍼 함수
    private fun mapToActivityType(typeString: String): ActivityType {
        return when (typeString.uppercase()) {
            "MISSION" -> ActivityType.MISSION
            "CONVERSATION" -> ActivityType.CONVERSATION
            "SENTENCE" -> ActivityType.SENTENCE
            "REPORT" -> ActivityType.REPORT
            else -> ActivityType.MISSION // 알 수 없는 값일 경우 기본값 폴백
        }
    }
}
