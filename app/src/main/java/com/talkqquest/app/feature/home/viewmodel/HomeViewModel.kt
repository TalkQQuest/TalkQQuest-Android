package com.talkqquest.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.home.data.HomeRepository
import com.talkqquest.app.feature.home.data.model.HomeSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 화면 상태(State). CONVENTIONS.md 6번 규칙: [화면이름]UiState.
// 화면이 그리는 데 필요한 값을 한 덩어리로 모아둔 것.
// - isLoading: 로딩 중이면 스피너 표시
// - summary: 성공 시 받은 데이터(없으면 null)
// - errorMessage: 실패 시 보여줄 메시지(없으면 null)
data class HomeUiState(
    val isLoading: Boolean = false,
    val summary: HomeSummary? = null,
    val errorMessage: String? = null,
)

// 홈 ViewModel (예시). 화면↔데이터 연결의 정답 패턴.
// 각 기능은 이 흐름 그대로 자기 ViewModel을 만들면 됩니다:
//   ① MutableStateFlow로 상태를 들고 있고
//   ② viewModelScope에서 Repository 호출
//   ③ ApiResult를 when으로 분기해 상태를 갱신
//   ④ 화면은 uiState만 구독하면 됨(아래 HomeScreen 참고)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    // _uiState: 내부에서만 값을 바꾸는 원본(Mutable). 밖에는 읽기 전용(uiState)만 공개.
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 화면이 처음 뜰 때 자동으로 1번 불러옴.
    init {
        loadHome()
    }

    // 에러 시 '다시 시도' 버튼에서도 호출 → public.
    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = homeRepository.getHomeSummary()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, summary = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message ?: "정보를 불러오지 못했어요.")
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "네트워크 연결을 확인해주세요.")
                }
            }
        }
    }
}
