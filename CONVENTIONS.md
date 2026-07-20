# 톡깨 (TalkQQuest) 안드로이드 컨벤션

이 문서는 팀원 모두가 같은 규칙으로 코드를 작성하기 위한 문서입니다. **PR 올리기 전에 이 문서 기준을 지켰는지 한 번 확인해주세요.**

---

## 1. 브랜치 네이밍

```
feature/기능이름
fix/버그이름
```

- 기능 단위로 브랜치를 만듭니다. (화면 단위가 아니라 기능 단위인 이유: 한 화면 안에 여러 기능이 섞여 있을 수 있어서, "무엇을 하는 작업인지"가 브랜치 이름만 봐도 드러나게 하기 위함)
- 예: `feature/login`, `feature/mission-list`, `fix/crash-on-home`
- 이름은 영어 소문자 + 하이픈(-)만 사용합니다.

## 2. 커밋 메시지 규칙

```
타입: 내용
```

- Conventional Commits 형식을 따르되, **내용은 한글로** 작성합니다.
- 타입 종류:

| 타입 | 의미 |
| --- | --- |
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 (README, 주석 등) |
| style | 코드 포맷팅, 세미콜론 등 동작에 영향 없는 변경 |
| refactor | 기능 변경 없는 코드 구조 개선 |
| test | 테스트 코드 추가/수정 |
| chore | 빌드 설정, 패키지 매니저 등 기타 변경 |

예시:
```
feat: 로그인 화면 UI 구현
fix: 온보딩 진행바 초기값 오류 수정
refactor: MissionViewModel 상태 관리 구조 개선
```

## 3. PR(Pull Request) 규칙

- **메인 브랜치 직접 push 금지.** 반드시 브랜치를 파서 PR을 통해 머지합니다.
- **머지 조건**: 리뷰어 1명 승인(Approve) 시 머지 가능.
- **머지 방식: "Squash and merge"로 통일.** PR 안의 여러 커밋을 하나로 뭉쳐 main에 커밋 1개로 올립니다. (히스토리를 깔끔하게 유지 — 레포 Settings에서 Squash만 허용해둠. 명령어 머지·리베이스 사용 X)
- **리뷰어 지정 방식: 선착순(먼저 보는 사람이 리뷰)**
  - 특정 인원을 지정하지 않습니다. PR을 올리면 팀 단체 채팅방에 링크를 공유하고, **가장 먼저 확인 가능한 사람이 리뷰**합니다.
  - 본인이 작성한 PR은 본인이 리뷰/승인할 수 없습니다.
  - PR 설명에는 "무엇을 했는지 + 확인해줬으면 하는 부분"을 간단히 적어주세요. (리뷰하는 사람이 맥락을 빨리 파악할 수 있도록)
  - 리뷰 요청 후 하루 이상 아무도 확인하지 않으면 채팅방에 리마인드합니다.

### 브랜치·커밋·push 단위 (2026-07 갱신)

> 초기 팀 문서(`pull push 규칙.md`)에는 **"한 화면 = 한 브랜치 = 한 PR"**로 적혀 있었지만, 화면과 작업이 1:1로 딱 떨어지지 않아 아래 기준으로 바꿉니다. (브랜치 이름을 "기능 단위"로 짓는다는 1번 규칙과도 맞춥니다.)

- **단위는 "화면"이 아니라 "하나의 완결된 작업(기능·수정)"입니다.** 브랜치 이름이 곧 그 작업 하나를 가리키게 하세요.
- 한 PR에 **여러 화면이 들어가도 됩니다** — 한 흐름·한 목적이면 묶으세요. (예: 리포트 화면 + 그 화면으로 가는 네비게이션 배선을 한 PR로)
- 반대로 **한 화면 작업이 성격이 다른 변경을 품으면 나눕니다** — 특히 공통 시스템(디자인 토큰, 네비 구조, 공통 컴포넌트)을 건드리는 부분은 별도 브랜치·PR로 떼어, "그 화면이 없어도 의미 있는 변경"이 리뷰에서 따로 드러나게 하세요.
- **커밋은 의미 단위로 여러 개**로 쪼개도 됩니다(리뷰 읽기 편하게). 어차피 머지는 Squash라 main에는 커밋 1개로 남습니다.
- 나머지 흐름은 그대로입니다: **main pull은 자주(안전)** · **main 직접 push 금지** · **PR은 Squash 머지** · **PR에 뜨는 충돌은 리더가 처리**.

## 4. 코드 네이밍 규칙

Kotlin 공식 컨벤션(https://kotlinlang.org/docs/coding-conventions.html)을 따릅니다.

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스, 객체, 인터페이스 | PascalCase | `MissionRepository`, `HomeViewModel` |
| 함수, 변수 | camelCase | `getMissionList()`, `userNickname` |
| 상수 (companion object, top-level) | UPPER_SNAKE_CASE | `MAX_MISSION_COUNT` |
| Composable 함수 | PascalCase (명사형) | `MissionCard()`, `LoginButton()` |
| 패키지명 | 소문자, 구두점 없이 | `com.talkqquest.feature.mission` |

## 5. 패키지 구조 규칙 (상세)

**원칙: 기능(feature) 기준으로 나누고, 여러 화면이 공유하는 것만 `core`로 뺀다.**

```
com.talkqquest.app
├── core/
│   ├── network/          # Retrofit 인스턴스, Interceptor, 공통 API 응답 래퍼(ApiResult)
│   ├── datastore/        # 로그인 토큰, 유저 설정 등 로컬 저장
│   ├── designsystem/     # 여러 화면에서 재사용하는 공통 컴포넌트 (버튼, 카드, 바텀 네비게이션, 컬러, 타이포그래피)
│   ├── di/               # 앱 전역 Hilt 모듈 (NetworkModule, DataStoreModule 등)
│   └── util/             # 날짜 포맷, 확장 함수 등 공통 유틸
├── feature/
│   ├── auth/
│   │   ├── ui/           # SplashScreen, LoginScreen, SignupScreen 등 Composable
│   │   ├── viewmodel/    # LoginViewModel, SignupViewModel
│   │   ├── data/
│   │   │   ├── model/    # 서버와 주고받는 데이터 모델 (LoginRequest, LoginResponse 등 DTO)
│   │   │   ├── AuthApi.kt         # Retrofit 인터페이스 (이 기능 전용 API)
│   │   │   └── AuthRepository.kt  # API 호출을 화면(ViewModel)에 연결
│   │   └── di/           # 이 기능 전용 Hilt 모듈 (AuthModule 등) — 필요할 때만
│   ├── onboarding/
│   ├── home/
│   ├── notification/
│   ├── mission/
│   ├── conversation/
│   ├── archive/
│   ├── community/
│   ├── report/
│   └── profile/
└── navigation/
    ├── NavGraph.kt       # 전체 네비게이션 그래프
    └── Screen.kt         # 화면 route(경로) 상수 정의
```

> 위에서 `auth`만 내부를 펼쳐 보여줬습니다. 나머지 feature(onboarding, home, mission...)도 **똑같은 내부 구조**(`ui/`, `viewmodel/`, `data/`, 필요 시 `di/`)를 따릅니다.

**판단 기준**
- 한 화면에서만 쓰는 컴포넌트/로직 → 그 `feature` 폴더 안에 둔다.
- 두 개 이상의 feature에서 재사용되는 것(버튼, 카드, 날짜 포맷 함수 등) → `core`로 옮긴다.
- **서버와 주고받는 데이터 모델(DTO)** → 그 기능의 `data/model/` 안에 둔다. (예: 로그인 요청/응답은 `feature/auth/data/model/`)
- **Hilt DI 모듈** → 앱 전역에서 쓰는 것(네트워크, DataStore 등)은 `core/di/`, 특정 기능에서만 쓰는 것은 그 `feature/.../di/`에 둔다. 헷갈리면 일단 `core/di/`에 두고 나중에 나눠도 된다.
- 새로운 화면을 추가할 때 어느 feature에도 속하지 않는다면, 먼저 팀 채팅방에 공유하고 새 feature 패키지를 만들지 상의합니다.

## 6. 화면(Screen) 네이밍 규칙

**모든 화면 단위 Composable 함수 이름은 `[화면이름]Screen` 형태로 끝나야 합니다.**

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 화면 Composable | PascalCase + Screen | `LoginScreen()`, `MissionDetailScreen()` |
| 화면 전용 ViewModel | [화면이름]ViewModel | `LoginViewModel`, `MissionDetailViewModel` |
| 화면 상태(State) 데이터 클래스 | [화면이름]UiState | `LoginUiState`, `MissionDetailUiState` |
| 파일명 | Composable 이름과 동일 | `LoginScreen.kt` |

**예시 구조 (하나의 화면 기준)**
```kotlin
// feature/auth/ui/LoginScreen.kt
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) { ... }

// feature/auth/viewmodel/LoginViewModel.kt
data class LoginUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() { ... }
```

> **참고: `feature/home`이 이 흐름의 동작하는 예시입니다.** Api → Repository → di → ViewModel(UiState) → Screen(로딩/에러/성공 + Preview)까지 실제로 구현돼 있으니, 화면을 서버에 붙일 때 그 구조를 그대로 복사해서 시작하세요. 화면은 `Screen(viewModel)`(연결)과 `Screen(uiState, onEvent)`(그리기)로 나눠 서버 없이 Preview로 검증할 수 있게 돼 있습니다.

**네비게이션 route(경로) 이름**은 화면 이름을 소문자 스네이크 케이스로 씁니다.
```kotlin
object Screen {
    const val LOGIN = "login"
    const val MISSION_DETAIL = "mission_detail/{missionId}"
}
```

이 규칙은 전체 화면 목록 표(`NAVIGATION.md`)의 "스크린 ID" 컬럼과 그대로 대응됩니다.

**와이어프레임 프레임 ≠ Screen 개수 (중요)**

피그마 와이어프레임에는 화면 크기 프레임이 아주 많지만(UI 5차 54개 → **UI 7차 87개**), 이걸 그대로 화면 수만큼 만들지 않습니다.

- **같은 화면의 상태 변형**은 하나의 `Screen` + 하나의 `UiState`로 구현합니다.
  - 예: 와이어프레임의 `홈 화면(미션 상세)` 2개, `대화 진행(기본)` 2개, `모임(상세)` 2개, `내 모임(참여중)` 2개 → 각각 **1개 Screen**. 상태 차이는 `UiState`의 값으로 표현합니다.
- **탭으로 묶이는 프레임**도 하나의 Screen입니다.
  - 예: `아카이브(미션/대화/문장/리포트 선택시)` 4개 → `ArchiveListScreen` 1개(탭 상태).
- **팝업**은 별도 Screen이 아니라 해당 화면 위에 띄우는 다이얼로그(Composable)로 구현합니다.

이 정리를 거쳐 UI 5차 기준 **논리 화면 35개 + 팝업 4개**로 집계했습니다(UI 7차 기준 재집계는 아직). 화면별 "와이어프레임 대응" 관계는 [`NAVIGATION.md`](NAVIGATION.md)에 정리돼 있으니, 내 담당 화면이 어떤 프레임에서 왔는지 거기서 확인하세요.

## 7. 라이브러리 버전 (고정)

**아래 버전은 임의로 올리지 마세요.** `compileSdk 36` 고정 제약 때문에 실제 빌드 테스트를 거쳐 확정된 조합입니다. 최신 버전이 나왔다고 그냥 올리면 빌드가 깨질 수 있습니다. (실제 최신 버전 다수가 `compileSdk 37`을 요구하도록 바뀐 상태지만, Android 17(API 37)이 아직 베타라 **compileSdk 36 유지로 확정**했습니다.)

| 구분 | 라이브러리 | 버전 | 비고 |
| --- | --- | --- | --- |
| DI | Hilt | 2.60 | |
| DI | hilt-navigation-compose | 1.3.0 | 최신 1.4.0은 compileSdk 37 요구 → 다운그레이드 |
| Navigation | navigation-compose | 2.9.8 | 2.10.0은 alpha라 제외 |
| Network | Retrofit | 3.0.0 | kotlinx-serialization 공식 컨버터 사용 |
| Network | OkHttp | 5.4.0 | |
| Network | kotlinx-serialization-json | 1.9.0 | |
| 비동기 | kotlinx-coroutines | 1.10.2 | |
| 이미지 | Coil | 3.2.0 | 최신 3.5.0은 kotlin-stdlib 2.4.0 요구(우리 컴파일러 2.2.10과 충돌) → 다운그레이드 |
| 로컬저장 | DataStore Preferences | 1.2.1 | |
| 소셜로그인 | Kakao SDK (v2-user) | 2.24.0 | 카카오 전용 Maven 저장소 필요(`devrepo.kakao.com`, `settings.gradle.kts`에 등록됨) |
| 소셜로그인 | Naver 로그인 SDK | 5.11.2 | |
| KSP | KSP | 2.3.9 | Kotlin 버전과 분리된 최신 독립 버전 사용(구버전 페어링 방식은 AGP built-in Kotlin과 충돌) |
| Compose | Compose BOM | 2026.06.01 | |
| AndroidX | core-ktx | 1.18.0 | 최신 1.19.0은 compileSdk 37 요구 → 다운그레이드 |
| AndroidX | lifecycle-runtime-ktx | 2.10.0 | 최신 2.11.0은 compileSdk 37 요구 → 다운그레이드 |

**새 라이브러리를 추가하거나 버전을 올리고 싶으면**, 먼저 로컬에서 `./gradlew :app:assembleDebug`가 성공하는지 확인한 뒤 PR을 올려주세요. **compileSdk 37 전환은 보류**로 확정되었으니(API 37 베타 상태), 37을 요구하는 라이브러리 버전은 정식 출시 전까지 올리지 않습니다.

> 실제 값의 출처(source of truth)는 `gradle/libs.versions.toml` 파일입니다. 이 표는 참고용이며, 최신 상태는 항상 그 파일을 확인하세요.

## 8. 디자인 토큰 & 공통 컴포넌트 (참고)

> **이제 `core/designsystem`에 코드로 구현·커밋 완료됐습니다.** 색은 `Primary600`·`Gray200` 등 이름으로, 타이포는 `TqType.HeadingL`, 컴포넌트는 `TqButton`/`TqChip`/`TqCard`로 **갖다 쓰면 됩니다**(새로 만들지 마세요). 아래 표는 그 스펙 요약입니다.
> **값의 최종 출처(source of truth)는 디자이너가 넘긴 최신 UI CSS 추출본(현재 UI 7차, 2026-07-18)입니다.** 아래 표와 다르면 항상 최신 추출본이 우선이고, 화면을 만들 땐 그 화면의 프레임 CSS를 직접 열어 값을 그대로 옮겨 적으세요(눈대중 재구성 금지). 초기에 받은 `design/design-system.css` / `design/components.css`는 1차 기준이라 최신과 어긋난 값이 있습니다. (자세한 사용법은 팀 배포용 `공통 세팅 안내.md` 참고)

### 색상 (Color)

폰트는 **Pretendard** (시스템 폰트 아님 → 폰트 파일 `res/font`에 추가 필요, 무료 오픈소스).

**Primary (브랜드 보라)**

| 단계 | HEX | 주요 용도 |
| --- | --- | --- |
| 50 | `#F8F7FF` | 앱 전체 배경, 온보딩 배경, Empty State |
| 100 | `#F1EEFF` | 선택된 카드, 말풍선 배경 |
| 200 | `#E4DFFF` | 선택 상태, Hover |
| 300 | `#CEC4FF` | 비활성 태그, 프로필 배경 |
| 400 | `#AA9CFF` | 보조 강조, 배지, Progress Track |
| **500** | `#7264F8` | **브랜드 메인, 로고, 활성 탭** |
| **600** | `#6353F0` | **메인 CTA 버튼, 주요 액션 (MAIN)** |
| 700 | `#5443DB` | Pressed, 선택된 버튼 |
| 800 | `#4436B6` | 강조 배경, 차트 |
| 900 | `#342A8F` | 최강 강조, 다크 카드 |

**Gray**: 50 `#F8FAFC` · 100 `#F1F5F9` · 200 `#E2E8F0` · 300 `#CBD5E1` · 400 `#94A3B8` · 500 `#64748B` · 600 `#475569` · 700 `#334155` · 800 `#273449` · 900 `#1E293B` · 1000 `#0F172A` (배경→보더→텍스트 위계 순)

**시맨틱**: 성공/체크 `#36DA21`(GREEN) · 실패/에러 `#F14444`(RED)

### 타이포그래피 (Pretendard 기준)

| 스타일 | 굵기 | 크기 / 행간 | 자간 |
| --- | --- | --- | --- |
| Display | Bold 700 | 32 / 44 | -2% |
| Heading/XL | Bold 700 | 28 / 40 | -2% |
| Heading/L | Bold 700 | 24 / 34 | -1% |
| Heading/M | SemiBold 600 | 20 / 30 | -1% |
| Title/L | SemiBold 600 | 18 / 28 | -1% |
| Body/L | Regular 400 | 16 / 24 | 0% |
| Body/M | Regular 400 | 14 / 22 | 0% |
| Body/S | Regular 400 | 13 / 20 | 0% |
| Label/L | Medium 500 | 14 / 20 | 0% |
| Label/M | Medium 500 | 12 / 18 | 0% |
| Caption | Regular 400 | 12 / 18 | 0% |

### 공통 컴포넌트 (여러 화면에서 재사용 → `core/designsystem`)

| 컴포넌트 | 핵심 스펙 |
| --- | --- |
| 하단 네비게이션 | 4탭: 아카이브 / 홈 / 모임 / 프로필. 활성 = Primary 600, 비활성 = Gray 300 |
| 버튼 L (예: "다음") | 높이 52, 배경 Primary 600, radius **16**, 텍스트 Gray 50, SemiBold 16 |
| 버튼 M (예: "미션 시작하기") | 높이 44, 배경 Primary 600, radius **12** |
| 칩 (select) | 높이 **34**(미션 목록·저장 목록 기준, 화면에 따라 40도 있음), radius **20**, 선택 = Primary 600 배경 + Primary 50 글자 / 미선택 = 흰 배경 + 카드 그림자 + Gray 900 글자 |
| 난이도 라벨 | 쉬움 = GREEN / 보통 / 어려움 (색으로 구분) |
| 카드 | 흰 배경, radius **20**, 그림자 `0 8px 24px rgba(15,23,42,0.01)`, stroke/fill 2종 |

> **위 컴포넌트는 대부분 구현 완료됐습니다.** 색/타이포 + 하단네비 + 버튼(`TqButton`)/칩(`TqChip`)/카드(`TqCard`) + 저장 바텀시트 틀(`TqSaveSheetScaffold` — 미션·문장·리포트 저장 시트 공용)은 `core/designsystem`에 커밋돼 있으니 갖다 쓰면 됩니다. 다만 **난이도 라벨은 아직 공통 컴포넌트로 없고**(현재 `feature/mission`의 미션 카드 안에 있음), 다른 파트에서도 필요해지면 같은 규칙(`core/designsystem/component/`)으로 옮겨 PR 올려주세요.

## 9. 리소스(이미지·아이콘) 네이밍 규칙

이미지·아이콘 같은 그래픽 리소스는 **위치가 `res/drawable/` 하나로 고정**입니다. 안드로이드는 이 폴더를 앱 전체가 공유하며, 코드처럼 기능별 폴더로 나눌 수 없습니다. 그래서 **이름 규칙으로 구분·충돌을 방지**합니다.

**형식: `타입_담당(화면)_이름`**

| 요소 | 값 |
| --- | --- |
| 타입 접두사 | `ic_`(아이콘·벡터) / `img_`(이미지·일러스트) / `bg_`(배경) |
| 담당(화면) | `home`, `mission`, `community`, `report`, `archive`, `auth` 등 — **본인 담당 화면 이름을 꼭 넣기** |
| 이름 | 무엇인지 (예: `target`, `waving_hand`, `goal`) |

예시: `img_home_target`, `img_home_waving_hand`, `ic_home_goal`, `img_report_chart`, `ic_community_pin`

**왜 담당 이름을 넣나:** `ic_`/`img_` 접두사만으론 서로 겹칠 수 있습니다(다 같이 씀). 담당 영역이 갈려 있으니 **화면 이름을 넣으면 파일명이 자연히 유니크**해져 충돌하지 않습니다. 혹시 같은 이름이 나와도 빌드가 `duplicate resources` 에러로, 또는 PR에서 충돌로 즉시 잡아줍니다.

**추가 규칙**
- **소문자 + 언더스코어(`_`)만** 사용 (대문자·하이픈·공백·한글 불가 — 리소스 이름 제약).
- 아이콘은 가능하면 **벡터 드로어블(XML)**, 사진·일러스트는 **PNG**.
