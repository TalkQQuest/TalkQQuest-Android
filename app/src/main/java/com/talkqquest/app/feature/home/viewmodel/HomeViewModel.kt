package com.talkqquest.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.datastore.TierStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.home.data.HomeRepository
import com.talkqquest.app.feature.home.data.HomeSummaryCache
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
    val isRefreshingMission: Boolean = false,
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
    private val homeSummaryCache: HomeSummaryCache, // 스플래시 프리페치 결과 — 있으면 스피너 없이 즉시 표시
    private val tierStore: TierStore, // 성장 리포트가 갱신해 둔 티어를 구독 — 재조회 왕복 없이 즉시 반영
) : ViewModel() {

    // _uiState: 내부에서만 값을 바꾸는 원본(Mutable). 밖에는 읽기 전용(uiState)만 공개.
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 홈 화면 재진입(ON_RESUME) 판정용. 이 ViewModel은 홈 백스택 엔트리에 스코프돼 있어
    // 다른 화면으로 이동했다가 돌아와도(컴포저블이 폐기·재생성돼도) 살아남는다.
    // 그래서 remember가 아니라 여기 필드로 둬야 "최초 진입"과 "재진입"을 구분할 수 있다.
    private var hasResumedOnce = false

    // 화면이 처음 뜰 때 자동으로 1번 불러옴.
    // 스플래시에서 방금 받아둔 요약이 있으면 그걸로 바로 그리고(스피너 생략), 뒤에서 조용히 최신화한다.
    init {
        val prefetched = homeSummaryCache.takeFresh()
        if (prefetched != null) {
            _uiState.update { it.copy(isLoading = false, summary = prefetched) }
            loadHome(showLoading = false)
        } else {
            loadHome()
        }

        // 티어 공유 저장소 구독. 홈이 화면 밖에 있는 동안 성장 리포트가 승급을 계산해 저장소를
        // 갱신해 두면, 홈 재조회(loadHome)가 서버를 왕복하기 전에도 여기서 먼저 값을 받아
        // 복귀 첫 프레임부터 새 티어·별을 그릴 수 있다.
        viewModelScope.launch {
            tierStore.tier.collect { snapshot ->
                if (snapshot == null) return@collect
                _uiState.update { state ->
                    val summary = state.summary ?: return@update state
                    state.copy(
                        summary = summary.copy(
                            tierName = snapshot.tierName,
                            tierStars = snapshot.tierStars,
                        ),
                    )
                }
            }
        }
    }

    // 에러 시 '다시 시도' 버튼에서도 호출 → public.
    // showLoading=false: 화면 복귀 시 조용한 재조회(스피너 없이 XP 등 최신 상태만 반영)
    fun loadHome(showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }

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

    // 홈 백스택 엔트리가 ON_RESUME될 때마다 호출됨(NavGraph에서 연결).
    // 최초 1회는 init에서 이미 로드했으므로 건너뛰고, 그 이후 재진입부터만
    // 조용히(스피너 없이) 다시 조회해 미션 완료 등으로 바뀐 티어·별·XP를 반영한다.
    fun onScreenResumed() {
        if (!hasResumedOnce) {
            hasResumedOnce = true
            return
        }
        loadHome(showLoading = false)
    }

    fun refreshTodayMission() {
        val current = _uiState.value.summary?.todayMission ?: return
        // null은 횟수 정보가 없는 상태이지 소진 상태가 아니다.
        if (_uiState.value.isRefreshingMission || current.remainingRefreshes == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingMission = true) }
            when (val result = homeRepository.refreshTodayMission()) {
                is ApiResult.Success -> _uiState.update { state ->
                    state.copy(
                        isRefreshingMission = false,
                        summary = state.summary?.copy(todayMission = result.data),
                    )
                }
                else -> _uiState.update { it.copy(isRefreshingMission = false) }
            }
        }
    }
}
