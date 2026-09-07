# 바로입력 — Android 카메라·이미지 입력기

작성일: 2026-09-07 · 단계: Android 네이티브 개발 시제품 v0.1.0 · 가칭, 상표 확인 전

바로입력은 카메라 촬영 또는 사진첩·이미지 파일에서 읽은 글자·숫자를 사용자가 확인한 뒤 다른 앱의 입력칸에 넣는 Android 보조 입력기입니다. 기존 삼성 키보드나 Gboard에 버튼을 추가하는 플러그인이 아니라, 필요할 때 전환해서 사용하는 별도 키보드 앱입니다.

**처음 사용한다면 [화면을 보며 따라 하는 사용자 도움말](HELP.md)을 먼저 확인하세요.** 실제 에뮬레이터 캡처와 숫자·영문·한글·박스 라벨 샘플로 안내합니다. **연습해보기 → 사진으로 인식 연습**에서 앱에 포함된 박스 샘플이나 기기에 저장된 사진을 읽을 수 있습니다.

| 바로가기 | 내용 |
|---|---|
| [테스트 APK](releases/baro-input-0.1.0-debug.apk) | Android 기기에 설치하는 개발용 파일 |
| [사용자 도움말](HELP.md) | 설치, 사용 준비, 스캔, 수정, 문제 해결 |
| [구현·검증 기록](docs/implementation-status.md) | 구현된 기능과 아직 검증하지 못한 범위 |

현재 버전은 개발용 시제품입니다. 빌드와 자동 테스트 23개는 통과했지만, 실제 기기의 사진 선택 복귀·카메라·인식 품질·화면·TalkBack 검증은 아직 수행하지 않았습니다. Google Play에는 등록하지 않았습니다.

화면 여백, 버튼 위계, 인식 결과 표시, 하단 고정 동작을 정리했습니다. [디자인 검토 및 변경 전후 화면](docs/design-audit/README.md)에서 에뮬레이터 검증 결과를 볼 수 있습니다.

## Android 프로젝트

패키지/네임스페이스/applicationId: **`im.umo.baro_input`**

사용자가 요청한 `im.umo.baro-input`은 Android 패키지명에 허용되지 않는 하이픈을 포함하므로 밑줄로 정규화했다. 앱 표시 이름은 **바로입력**. 선택된 디자인은 [1번 시안](design/01-quick-confirm.png)이다.

- Kotlin 네이티브 Views, InputMethodService, CameraX, 번들 ML Kit 한국어/라틴 OCR.
- 최소 Android 8.0(API 26), compile/target SDK 36.
- Gradle 9.5.0 + AGP 9.3.2 + Kotlin 2.2.10. Wrapper에 배포본 SHA-256 고정.
- 프로젝트 전용 Git 저장소를 초기화했다. 커밋·원격 업로드는 수행하지 않았다.

## 현재 기능

- 카메라 권한 및 입력기 활성화 안내, 연습용 입력 화면
- 카메라 미리보기·촬영·조명, 텍스트/숫자 모드
- 입력기의 **사진첩 · 이미지 파일 선택 → 이 사진 읽기 → 확인 → 입력**. 카메라 권한 없이 사용 가능. 선택 후 키보드가 닫혀 있으면 원래 입력칸을 다시 누릅니다.
- 사진 선택은 시스템 문서 선택기를 사용하며 별도 저장소 권한을 요청하지 않습니다. 다른 입력 대상이나 5분이 지난 요청에는 선택한 사진을 전달하지 않습니다.
- 한글·영문 OCR 연결, 여러 줄 중 입력할 줄 선택
- 원본과 인식 결과 비교, 공백 제거, 숫자 수정
- 사용자가 누른 경우에만 현재 입력칸에 삽입
- 비밀번호 입력 제한, 입력 세션 변경 시 이전 결과 차단

원본 확대·수동 크롭·연속 스캔·전체 한글 자판·클라우드 OCR은 아직 제공하지 않습니다.

## 폴더 구성

```text
baro-input/
├── README.md                   프로젝트 소개 및 개발 안내
├── HELP.md                     사용자 도움말
├── app/
│   ├── build.gradle.kts        앱 패키지·SDK·의존성 설정
│   └── src/
│       ├── main/              Android Manifest, Kotlin 코드, 리소스
│       └── test/              입력 규칙·세션·화면 동작 테스트
├── design/                    선택 시안과 디자인 기록
├── docs/                      기획·화면 명세·검증 기록
├── gradle/wrapper/            Gradle 실행 도구와 체크섬
└── releases/                  테스트 APK와 SHA256SUMS.txt
```

## 빌드

Android Studio에서 이 폴더를 열고 SDK 36 / Build Tools 35.0.0을 설치한다. JDK 17 이상을 JAVA_HOME에 지정하고, local.properties에 SDK 경로를 지정한다. Windows 경로는 `sdk.dir=C\:/Android/Sdk`처럼 드라이브 콜론을 이스케이프한다.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

macOS/Linux에서는 `sh gradlew assembleDebug testDebugUnitTest lintDebug` 사용. 의존성 다운로드에는 인터넷이 필요하지만 앱의 OCR에는 서버/API 키가 필요하지 않다.

빌드 도구가 준비된 환경에서는 검사별로 실행할 수 있습니다.

```powershell
# 디버그 APK 생성
.\gradlew.bat assembleDebug

# 단위 테스트 및 Android UI 로직 테스트
.\gradlew.bat testDebugUnitTest

# Android 정적 검사
.\gradlew.bat lintDebug
```

테스트 보고서는 `app/build/reports/tests/testDebugUnitTest/index.html`, Lint 보고서는 `app/build/reports/lint-results-debug.html`에 생성됩니다. 현재 검증 기록은 테스트 16개 통과, Lint 오류 0개·경고 12개입니다. 자동 테스트가 실제 카메라나 인식 정확도를 검증하는 것은 아닙니다.

출력: `app/build/outputs/apk/debug/app-debug.apk`. 복사된 사용자용 테스트 APK는 `releases/`에 둔다. 디버그 서명으로 생성된 개발용 APK이며 Play 출시용 서명은 설정하지 않았다.

## 기기에서 시작

1. 테스트 APK 설치 → 바로입력 앱 실행.
2. 카메라 허용 → 입력기 설정에서 바로입력 활성화.
3. 연습해보기 → 입력칸 선택 → 키보드 전환에서 바로입력 선택.
4. 텍스트/숫자 모드 선택 → 촬영하고 읽기 → 여러 줄이면 선택 → 결과 확인.
5. 필요하면 수정·공백 제거 → 입력. 기존 키보드 복귀는 상단 키보드 전환 사용.

첫 버전의 텍스트 수정은 삭제·구분자 삽입을 제공한다. 한글/영문 자판 전체는 포함하지 않아 새 글자를 추가하려면 삽입 후 기존 키보드에서 편집한다. 원본 영역 확대·수동 크롭·안정화 음성 안내·연속 스캔은 아직 구현하지 않았다.

## 제품 목표

종이·라벨·화면의 한글, 영문, 숫자를 다른 Android 앱의 입력칸에 빠르고 정확하게 옮긴다. 삼성 키보드나 Gboard의 플러그인이 아니라 사용자가 선택하는 별도 IME다.

## 프로젝트 문서

- [제품 기획](docs/product-brief.md): 대상, 범위, 경쟁 가설, 성공 기준
- [사용자 도움말](HELP.md): 설치·스캔·수정·문제 해결
- [사용 흐름과 화면 명세](docs/ux-spec.md): 설정, 스캔, 확인, 수정, 오류 상태
- [기술 검증과 출시 계획](docs/validation-plan.md): 실기기 검증 및 단계별 구현
- [디자인 방향](design/README.md): 세 가지 시각 탐색 방향

이 폴더는 기획·디자인·Android 소스를 함께 관리하는 독립 프로젝트다. 실제 카메라/OCR 호출 코드와 테스트 APK가 포함되며 스토어에는 등록하지 않았다. 호스트 앱은 직접 제어하지 않으므로 시안 상단의 예시 폼은 우리 IME UI에 포함하지 않는다. 시제품은 [검증 기록](docs/implementation-status.md)에 적힌 범위까지만 확인했다.

## 현재 결정

- Android 우선. iOS는 이번 범위에서 제외.
- 모델을 앱에 포함하는 ML Kit 기반 OCR을 우선 검증.
- 기본 동작은 사용자 확인 후 삽입. 자동 제출·자동 전송 없음.
- 숫자를 정수로 변환하지 않고 문자열로 보관하여 앞자리 0을 보존.
- 원본 사진과 인식 결과의 영구 저장은 기본 비활성화.
- 클라우드 OCR, 광고, 계정, 결제, 일괄 입력은 첫 검증 버전에서 제외.

## 조사 근거와 한계

- [Android 입력기](https://developer.android.com/develop/ui/views/touch-and-input/creating-input-method)
- [ML Kit OCR](https://developers.google.com/ml-kit/vision/text-recognition/v2/android)
- [Scanner Keyboard 스토어](https://play.google.com/store/apps/details?id=com.tecit.android.barcodekbd.demo)

경쟁 앱은 공개 설명만 확인했다. 편의성 우위, 한글 정확도, 카메라를 IME에 임베드했을 때의 호환성은 아직 실측하지 않았다. 아래 문서의 수치는 출시된 성능이 아니라 검증 목표다.

## 라이선스와 배포

프로젝트 자체의 공개 라이선스는 아직 정하지 않았습니다. 임시 카메라 아이콘은 Google Material Icons를 사용하며 [라이선스 원문](design/material-icons-LICENSE.txt)을 포함합니다. 각 SDK는 해당 배포자의 라이선스를 따릅니다. 출시용 서명 키·개발자 계정·Play 등록은 이 프로젝트에 포함하지 않습니다.
