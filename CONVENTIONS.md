# 톡깨 (TalkQQuest) 안드로이드 컨벤션

이 문서는 팀원 모두가 같은 규칙으로 코드를 작성하기 위한 문서입니다. **PR 올리기 전에 이 문서 기준을 지켰는지 한 번 확인해주세요.**

---

## 1. 브랜치 네이밍

```
feature/기능이름
fix/버그이름
```

- 기능 단위로 브랜치를 만듭니다. (화면 단위가 아니라 기능 단위인 이유: 한 화면 안에 여러 기능이 섞여 있을 수 있어서, "무엇을 하는 작업인지"가 브랜치 이름만 봐도 드러나게 하기 위함)
- 예: `feature/notification-read-settings`, `feature/mission-setup-guideline`, `fix/notification-bottom-inset`
- 이름은 **영어 소문자 + 하이픈(`-`)만** 사용합니다. 슬래시로 더 쪼개지 마세요(`feature/archive/report-api` ❌ → `feature/archive-report-api` ✅).
- **`main`에서 브랜치를 따고, 남의 브랜치 위에 쌓지 마세요.** PR끼리 얽히면 리뷰와 머지가 같이 막힙니다.
- 브랜치를 따기 전에 항상 `git fetch origin` → 최신 `main` 기준으로 시작합니다.

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
| design | 디자인 시안 반영 (값·색·여백 조정, 동작 변경 없음) |
| docs | 문서 수정 (README, 주석 등) |
| refactor | 기능 변경 없는 코드 구조 개선 |
| chore | 빌드 설정, 패키지 매니저 등 기타 변경 |
| style | 코드 포맷팅 등 동작에 영향 없는 변경 |
| test | 테스트 코드 추가/수정 |

예시:
```
feat: 성장 리포트 티어 승급 모션 추가
fix: 알림 상태 동기화 및 보관함 이동 개선
design: 대화 설정 선택지 선택 상태 색상 적용
```

> `design`은 시안 값을 코드에 옮기는 작업이 잦아 팀에서 실제로 쓰이는 타입입니다. `style`과 헷갈리지 마세요 —
> **`design` = 화면에 보이는 값이 바뀜**, **`style` = 화면은 그대로고 코드 모양만 바뀜.**

## 3. PR(Pull Request) 규칙

- **`main` 브랜치 직접 push 금지.** 반드시 브랜치를 파서 PR을 통해 머지합니다.
- **머지 조건**: 리뷰어 1명 승인(Approve) 시 머지 가능.
- **머지 방식: "Squash and merge"로 통일.** PR 안의 여러 커밋을 하나로 뭉쳐 main에 커밋 1개로 올립니다. (레포 Settings에서 Squash만 허용해둠. 명령어 머지·리베이스 사용 X)
- **리뷰어 지정 방식: 선착순(먼저 보는 사람이 리뷰)**
  - 특정 인원을 지정하지 않습니다. PR을 올리면 팀 단체 채팅방에 링크를 공유하고, **가장 먼저 확인 가능한 사람이 리뷰**합니다.
  - 본인이 작성한 PR은 본인이 리뷰/승인할 수 없습니다.
  - PR 설명에는 "무엇을 했는지 + 확인해줬으면 하는 부분"을 간단히 적어주세요.
  - 리뷰 요청 후 하루 이상 아무도 확인하지 않으면 채팅방에 리마인드합니다.
- **PR에 뜨는 충돌은 리더가 처리합니다.** 로컬에서 임의로 rebase/merge해서 남의 커밋을 덮어쓰지 마세요.

### 브랜치·커밋·push 단위

- **단위는 "화면"이 아니라 "하나의 완결된 작업(기능·수정)"입니다.** 브랜치 이름이 곧 그 작업 하나를 가리키게 하세요.
- 한 PR에 **여러 화면이 들어가도 됩니다** — 한 흐름·한 목적이면 묶으세요. (예: 리포트 화면 + 그 화면으로 가는 네비게이션 배선을 한 PR로)
- 반대로 **한 화면 작업이 성격이 다른 변경을 품으면 나눕니다** — 특히 공통 시스템(디자인 토큰, 네비 구조, 공통 컴포넌트)을 건드리는 부분은 별도 브랜치·PR로 떼어, "그 화면이 없어도 의미 있는 변경"이 리뷰에서 따로 드러나게 하세요.
- **커밋은 의미 단위로 여러 개**로 쪼개도 됩니다(리뷰 읽기 편하게). 어차피 머지는 Squash라 main에는 커밋 1개로 남습니다.
- 나머지 흐름은 그대로입니다: **main pull은 자주(안전)** · **main 직접 push 금지** · **PR은 Squash 머지**.

### 남의 파트를 건드릴 때

- 화면·기능은 A/B/C로 나뉘어 있습니다([`FOLDER_API_ROLE_ALLOCATION.md`](FOLDER_API_ROLE_ALLOCATION.md)). **착수 전에 그 화면이 내 담당인지 확인하세요.**
- 남의 폴더 파일을 고쳐야 하면 착수 전에 채팅방에 알립니다. 특히 **한 파일을 두 파트가 나눠 쓰는 곳**(`HomeApi.kt`는 홈(B)과 프로필(A)이, `NavGraph.kt`는 전원이 씁니다)은 충돌이 잦습니다.
- 같은 엔드포인트가 두 Api 파일에 각각 선언된 곳이 있습니다(미션 저장·문장 저장·리포트 저장 등). **한쪽을 고치면 다른 쪽도 확인하세요.**

## 4. 코드 네이밍 규칙

Kotlin 공식 컨벤션(https://kotlinlang.org/docs/coding-conventions.html)을 따릅니다.

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스, 객체, 인터페이스 | PascalCase | `MissionRepository`, `HomeViewModel` |
| 함수, 변수 | camelCase | `getMissionList()`, `userNickname` |
| 상수 (companion object, top-level) | UPPER_SNAKE_CASE | `MAX_MISSION_COUNT` |
| Composable 함수 | PascalCase (명사형) | `MissionCard()`, `LoginButton()` |
| 패키지명 | 소문자, 구두점 없이 | `com.talkqquest.app.feature.mission` |
| 공통 컴포넌트 | `Tq` 접두사 | `TqButton`, `TqChip`, `TqCard`, `TqBottomBar` |

**주석은 한글로 쓰되, 파일 인코딩은 반드시 UTF-8(BOM 없이)로 저장하세요.**
윈도우에서 편집기 인코딩이 다르면 한글 주석이 깨진 채로 커밋됩니다. 이미 깨진 파일이 몇 개 있으니 그 파일을 만질 땐 주변 주석도 같이 살펴봐 주세요.

## 5. 패키지 구조 규칙 (상세)

**원칙: 기능(feature) 기준으로 나누고, 여러 화면이 공유하는 것만 `core`로 뺀다.**

```
com.talkqquest.app
├── core/
│   ├── network/          # ApiResponse/ApiResult, AuthInterceptor, TokenRefreshClient, safeApiCall
│   ├── datastore/        # 토큰(TokenDataStore), 알림 상태, XP 로컬 저장
│   ├── designsystem/     # 컬러·타이포·테마·그림자·화면 대응
│   │   └── component/    # 재사용 컴포넌트 (TqButton, TqChip, TqCard, TqSaveSheetScaffold …)
│   ├── di/               # 앱 전역 Hilt 모듈 (NetworkModule)
│   ├── push/             # FCM 수신·토큰 등록
│   └── util/             # 날짜 포맷(DateFormat), 티어 계산(TierProgress)
├── feature/
│   ├── auth/
│   │   ├── ui/           # SplashScreen, SignupStartScreen … Composable
│   │   ├── viewmodel/    # AuthViewModel (+ UiState)
│   │   ├── data/
│   │   │   ├── model/    # 서버와 주고받는 DTO
│   │   │   ├── AuthApi.kt         # Retrofit 인터페이스 (이 기능 전용 API)
│   │   │   └── AuthRepository.kt  # API 호출을 화면(ViewModel)에 연결
│   │   └── di/           # 이 기능 전용 Hilt 모듈 (AuthModule) — Api 제공
│   ├── onboarding/       # (A)
│   ├── profile/          # (A)
│   ├── home/             # (B)
│   ├── notification/     # (B)
│   ├── mission/          # (B) — 대화 진행·대화 설정 화면도 여기 있습니다
│   ├── report/           # (B)
│   ├── archive/          # (C)
│   ├── community/        # (C) 미구현 — 빈 폴더
│   └── conversation/     # 빈 폴더 (대화 화면은 mission/ 안에 있음)
└── navigation/
    ├── MainScreen.kt     # 앱 최상위 셸 (NavGraph + 하단 바 + 전역 모달)
    ├── NavGraph.kt       # 전체 네비게이션 그래프
    ├── MainTabsPager.kt  # 하단 4탭 페이저
    ├── BottomNavItem.kt  # 하단 탭 정의
    ├── TqBottomBar.kt    # 하단 네비게이션 바
    └── Screen.kt         # 화면 route(경로) 상수 정의
```

> 위에서 `auth`만 내부를 펼쳐 보여줬습니다. 나머지 feature도 **똑같은 내부 구조**(`ui/`, `viewmodel/`, `data/`, `di/`)를 따릅니다.

**판단 기준**
- 한 화면에서만 쓰는 컴포넌트/로직 → 그 `feature` 폴더 안에 둔다.
- 두 개 이상의 feature에서 재사용되는 것(버튼, 카드, 날짜 포맷 함수 등) → `core`로 옮긴다.
- **서버와 주고받는 데이터 모델(DTO)** → 그 기능의 `data/model/` 안에 둔다.
- **Hilt DI 모듈** → 앱 전역에서 쓰는 것(네트워크)은 `core/di/`, 기능별 Api 제공은 그 `feature/.../di/`에 둔다.
- **Repository는 `@Inject constructor`로 만듭니다.** DI 모듈에 따로 `@Provides`를 쓰지 않습니다.
- 새로운 화면을 추가할 때 어느 feature에도 속하지 않는다면, 먼저 팀 채팅방에 공유하고 새 feature 패키지를 만들지 상의합니다.

**지금 구조가 원칙과 어긋난 곳** — 알고 쓰라고 적어둡니다. 새 코드는 원칙 쪽으로 쓰세요.
- `feature/profile/`에 Api 파일이 없고 `ProfileRepository`가 `HomeApi`·`NotificationApi`를 씁니다. 프로필 전용 엔드포인트를 새로 만든다면 `ProfileApi.kt`를 새로 파는 편이 낫습니다.
- `feature/onboarding/`에 Api·ViewModel이 없고 `AuthApi`·`AuthViewModel`을 씁니다.
- `feature/conversation/`, `feature/community/`는 `.gitkeep`만 있는 빈 폴더입니다.

## 6. 화면(Screen) 네이밍 규칙

**모든 화면 단위 Composable 함수 이름은 `[화면이름]Screen` 형태로 끝나야 합니다.**

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 화면 Composable | PascalCase + Screen | `MissionDetailScreen()`, `WeeklyCompareScreen()` |
| 화면 전용 ViewModel | [화면이름]ViewModel | `MissionDetailViewModel`, `ReportViewModel` |
| 화면 상태(State) 데이터 클래스 | [화면이름]UiState | `MissionDetailUiState`, `ReportUiState` |
| 파일명 | Composable 이름과 동일 | `MissionDetailScreen.kt` |
| 바텀시트 | [이름]Sheet | `MissionSaveSheet`, `ArchiveSortSheet` |

**예시 구조 (하나의 화면 기준)**
```kotlin
// feature/mission/ui/MissionDetailScreen.kt
@Composable
fun MissionDetailScreen(
    viewModel: MissionDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
) { ... }

// feature/mission/viewmodel/MissionDetailViewModel.kt
data class MissionDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
) : ViewModel() { ... }
```

> **화면은 `Screen(viewModel)`(연결)과 `Screen(uiState, onEvent)`(그리기)로 나눠주세요.** 서버 없이 `@Preview`로 검증할 수 있습니다.
> `feature/home`이 이 흐름의 동작하는 예시입니다 — Api → Repository → di → ViewModel(UiState) → Screen(로딩/에러/성공 + Preview)까지 다 있습니다.

**네비게이션 route(경로) 이름**은 화면 이름을 소문자 스네이크 케이스로 씁니다.

```kotlin
object Screen {
    const val LOGIN = "login"
    const val MISSION_DETAIL = "mission_detail/{missionId}"
    const val WEEKLY_COMPARE = "weekly_compare?reportId={reportId}"
}
```

- **route는 `Screen.kt`에 상수로 먼저 정의하고** `NavGraph.kt`에 등록합니다. NavGraph에 문자열을 직접 박지 마세요.
- 필수 인자는 `{id}`, 선택 인자는 `?key={key}` 형태로 붙입니다(선택 인자는 `navArgument`에 `defaultValue` 필수).
- 팝업·바텀시트처럼 화면 단위가 아닌 UI는 **route를 만들지 않습니다.** 해당 화면 안의 상태로 처리합니다.
- 화면 목록·route·진입 경로 전체는 [`NAVIGATION.md`](NAVIGATION.md)에 있습니다. **화면을 추가·삭제하면 그 문서도 같이 고쳐주세요.**

**와이어프레임 프레임 ≠ Screen 개수 (중요)**

피그마에는 화면 크기 프레임이 아주 많지만, 그대로 화면 수만큼 만들지 않습니다.

- **같은 화면의 상태 변형**은 하나의 `Screen` + 하나의 `UiState`로 구현합니다. (로딩/빈 상태/선택 상태/팝업 열린 상태 등)
- **탭으로 묶이는 프레임**도 하나의 Screen입니다. 예: 보관함의 미션/대화/문장/리포트 4탭 → `ArchiveListScreen` 1개(탭 index로 구분).
- **팝업**은 별도 Screen이 아니라 해당 화면 위에 띄우는 다이얼로그(Composable)로 구현합니다.
- 반대로 **route가 2개인데 화면이 1개**인 경우도 있습니다. 예: 약관 동의(이메일용·소셜용), 미션 목록(하단 탭용·홈에서 들어온 것).

## 7. 라이브러리 버전 (고정)

**아래 버전은 임의로 올리지 마세요.** 실제 빌드 테스트를 거쳐 확정된 조합입니다. 최신 버전이 나왔다고 그냥 올리면 빌드가 깨질 수 있습니다.

| 구분 | 라이브러리 | 버전 | 비고 |
| --- | --- | --- | --- |
| 빌드 | AGP | 9.1.1 | |
| 빌드 | Gradle | 9.3.1 | wrapper 고정 |
| 언어 | Kotlin | 2.2.10 | |
| DI | Hilt | 2.60 | |
| DI | hilt-navigation-compose | 1.3.0 | 1.4.0은 compileSdk 37 요구 → 다운그레이드 |
| DI | KSP | 2.3.9 | Kotlin과 분리된 독립 버전 (구버전 페어링 방식은 AGP built-in Kotlin과 충돌) |
| Navigation | navigation-compose | 2.9.8 | 2.10.0은 alpha라 제외 |
| Network | Retrofit | 3.0.0 | kotlinx-serialization 공식 컨버터 사용 |
| Network | OkHttp | 5.4.0 | |
| Network | kotlinx-serialization-json | 1.9.0 | |
| 비동기 | kotlinx-coroutines | 1.10.2 | |
| 이미지 | Coil | 3.2.0 | 3.5.0은 kotlin-stdlib 2.4.0 요구(우리 컴파일러 2.2.10과 충돌) → 다운그레이드 |
| 블러 | haze | 1.6.10 | 하단 네비게이션 유리 효과 |
| 푸시 | Firebase BOM | 34.17.0 | firebase-messaging (FCM) |
| 푸시 | google-services 플러그인 | 4.5.0 | |
| 로컬저장 | DataStore Preferences | 1.2.1 | |
| 소셜로그인 | Kakao SDK (v2-user) | 2.24.0 | 전용 Maven 저장소 필요(`devrepo.kakao.com`, `settings.gradle.kts`에 등록됨) |
| 소셜로그인 | Naver 로그인 SDK | 5.11.2 | |
| Compose | Compose BOM | 2026.06.01 | |
| AndroidX | core-ktx | 1.18.0 | 1.19.0은 compileSdk 37 요구 → 다운그레이드 |
| AndroidX | lifecycle | 2.10.0 | 2.11.0은 compileSdk 37 요구 → 다운그레이드 |
| AndroidX | activity-compose | 1.13.0 | |

**SDK 설정**: `minSdk 26` / `targetSdk 36` / `compileSdk 36.1`(`release(36) { minorApiLevel = 1 }`) / JDK 17.
**compileSdk 37 전환은 보류**로 확정돼 있으니, 37을 요구하는 라이브러리 버전은 정식 출시 전까지 올리지 않습니다.

**새 라이브러리를 추가하거나 버전을 올리고 싶으면**, 먼저 로컬에서 `./gradlew :app:assembleDebug`가 성공하는지 확인한 뒤 PR을 올려주세요.

> 실제 값의 출처(source of truth)는 `gradle/libs.versions.toml` 파일입니다. 이 표는 참고용이며, 최신 상태는 항상 그 파일을 확인하세요.

## 8. 디자인 토큰 & 공통 컴포넌트

> **`core/designsystem`에 코드로 구현돼 있습니다.** 색은 `Primary600`·`Gray200` 등 이름으로, 타이포는 `TqType.HeadingL`, 컴포넌트는 `TqButton`/`TqChip`/`TqCard`로 **갖다 쓰면 됩니다**(새로 만들지 마세요).
> **값의 최종 출처는 디자이너가 넘긴 최신 UI CSS 추출본입니다.** 아래 표와 다르면 항상 최신 추출본이 우선이고, 화면을 만들 땐 그 화면의 프레임 CSS를 직접 열어 **값을 그대로 옮겨 적으세요(눈대중 재구성 금지).**
> 레포의 `design/design-system.css` / `design/components.css`는 초기 기준이라 최신과 어긋난 값이 있습니다.

### 폰트

| 폰트 | 파일 | 쓰는 곳 |
| --- | --- | --- |
| **Pretendard** | `res/font/pretendard_{regular,medium,semibold,bold}.ttf` | 앱 전체 |
| **A2Z** | `res/font/a2z_bold.ttf` | 로고 워드마크("톡깨")만 — 스플래시·로그인 |

### 색상 (Color) — `core/designsystem/Color.kt`

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

**시맨틱**: 성공/체크 `Success` `#36DA21` · 실패/에러 `Error` `#F14444` · `White` `#FFFFFF`

### 타이포그래피 — `core/designsystem/Type.kt` (`TqType`)

| 스타일 | 굵기 | 크기 / 행간 | 자간 |
| --- | --- | --- | --- |
| `Display` | Bold 700 | 32 / 44 | -2% |
| `HeadingXL` | Bold 700 | 28 / 40 | -2% |
| `HeadingL` | Bold 700 | 24 / 34 | -1% |
| `HeadingM` | SemiBold 600 | 20 / 30 | -1% |
| `TitleL` | SemiBold 600 | 18 / 28 | -1% |
| `BodyL` | Regular 400 | 16 / 24 | 0% |
| `BodyM` | Regular 400 | 14 / 22 | 0% |
| `BodyS` | Regular 400 | 13 / 20 | 0% |
| `LabelL` | Medium 500 | 14 / 20 | 0% |
| `LabelM` | Medium 500 | 12 / 18 | 0% |
| `Caption` | Regular 400 | 12 / 18 | 0% |

### 공통 컴포넌트 — `core/designsystem/component/`

| 컴포넌트 | 핵심 스펙 |
| --- | --- |
| `TqButton` | `Large` = 높이 52 / radius 16, `Medium` = 높이 44 / radius 12. 배경 Primary600 · 글자 White · BodyL SemiBold. 비활성 = Gray200 배경 / Gray400 글자 |
| `TqChip` | 높이 40 / radius 20. 선택 = Primary600 배경 + White 글자, 미선택 = White 배경 + Gray200 테두리 + Gray700 글자. LabelL |
| `TqCard` | White 배경, radius 20, 그림자 elevation 4, 기본 여백 16 |
| `TqSaveSheetScaffold` | 저장 바텀시트 공용 틀 — 미션·문장·리포트 저장 시트가 전부 이걸 씁니다 |
| `TierPromotionSheet` | 티어 승급 안내 시트 (홈) |
| `TqLoadingScreen` / `TqLoadingSpinner` | 전체 화면 로딩 · 인라인 스피너 |
| `LevelUpBurst` | 레벨업 연출 |

**같이 있는 유틸**

| 이름 | 용도 |
| --- | --- |
| `Modifier.softShadow(color, offsetY, blur, cornerRadius)` | CSS `box-shadow` 값을 그대로 옮길 때. 기본 `Modifier.shadow()`는 방향·흐림·색을 못 담습니다. **clip/background보다 먼저** 붙이세요 |
| `TalkQQuestTheme` | 앱 테마 (colorScheme + Typography) |
| `FitDesign` | 아래 10번 참고 |

**하단 네비게이션은 `navigation/TqBottomBar.kt`에 있습니다**(designsystem 아님).
4탭: **홈 / 미션 / 아카이브 / 프로필**. 활성 = Primary600, 비활성 = Gray300.
바 = 높이 64 / radius 36 / 흰색 0.8 + 블러 / 선택 칩 = 최대 92×44 / radius 22.

> **아직 공통 컴포넌트가 아닌 것**: 난이도 라벨(쉬움/보통/어려움)은 `feature/mission`의 미션 카드 안에 있습니다. 다른 파트에서도 필요해지면 같은 규칙으로 `core/designsystem/component/`로 옮겨 PR 올려주세요.

## 9. 리소스(이미지·아이콘) 네이밍 규칙

이미지·아이콘은 **위치가 `res/drawable/` 하나로 고정**입니다(사진·큰 일러스트는 `res/drawable-nodpi/`). 안드로이드는 이 폴더를 앱 전체가 공유하며 코드처럼 기능별로 나눌 수 없어서, **이름 규칙으로 충돌을 방지**합니다.

**형식: `타입_영역_이름`**

| 요소 | 값 |
| --- | --- |
| 타입 접두사 | `ic_`(아이콘·벡터) / `img_`(이미지·일러스트) / `bg_`(배경) |
| 영역 | 화면 이름(`home`, `mission`, `report`, `archive`, `auth`, `profile`, `notification`, `conversation`, `feedback`, `onboarding`) **또는** 기능 영역(`nav`, `tier`, `setup`, `weekly`, `metric`) |
| 이름 | 무엇인지 (예: `target`, `bronze`, `place_school`) |

예시: `img_home_target` · `ic_nav_archive` · `img_tier_gold` · `ic_setup_place_school` · `img_report_kindness` · `ic_weekly_prev`

**왜 영역을 넣나:** `ic_`/`img_` 접두사만으론 서로 겹칩니다. 영역 이름을 넣으면 파일명이 자연히 유니크해져 충돌하지 않습니다. 혹시 겹쳐도 빌드가 `duplicate resources` 에러로, 또는 PR에서 충돌로 잡아줍니다.

**추가 규칙**
- **소문자 + 언더스코어(`_`)만** 사용 (대문자·하이픈·공백·한글 불가 — 리소스 이름 제약).
- 아이콘은 가능하면 **벡터 드로어블(XML)**, 사진·일러스트는 **PNG**.
- **새 리소스를 추가하기 전에 같은 이름이 이미 있는지 확인하세요.** 추가한 뒤 `git status`에 `M`(수정)으로 뜨면 남의 파일을 덮어쓴 것입니다. `A`(추가)여야 정상입니다.

## 10. 화면 크기 대응 (FitDesign)

**디자인 기준 화면은 393×852(dp)입니다.** 그보다 작은 기기에서 레이아웃이 깨지지 않도록, `FitDesign`이 **밀도를 낮춰 글자·카드·이미지·여백을 전부 같은 비율로 줄입니다.**

```kotlin
@Composable
fun MyScreen() = FitDesign {
    // 393x852 기준 값을 그대로 dp로 적으면 됩니다
}
```

- 축소율 = `min(가로/393, (세로-140)/712, 1)` — 393×852 기기에서는 정확히 1.0이라 **피그마와 픽셀 일치**합니다.
- 화면 Composable 바깥을 이걸로 감싸면 안쪽은 시안 값을 그대로 쓰면 됩니다. 기기별로 값을 따로 계산하지 마세요.
- **판정 기준: CSS 목데이터로 렌더했을 때 393dp 화면에서 피그마와 일치해야 합니다.** "유동이라 달라졌다"는 이유가 되지 않습니다. 동시에 화면 폭과 글자 수가 달라져도 깨지지 않아야 합니다.
