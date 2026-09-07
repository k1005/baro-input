# 구현 및 검증 기록 — 2026-09-07

## 구현됨

- 패키지 `im.umo.baro_input`, 앱 이름 바로입력.
- 시스템에 등록되는 독립 IME와 최초 설정/연습 Activity.
- 카메라 권한 확인과 설정 이동, 입력기 활성화 상태 표시.
- CameraX IME 미리보기·촬영·조명. 화면 숨김/입력 종료 시 카메라 해제.
- 번들 ML Kit 한국어/라틴 OCR 호출, 여러 줄 선택, 원본 이미지와 결과 확인.
- 공백 제거 선택, 숫자 수정 키패드, 커서 이동/삭제/구분자 입력.
- 결과 확정 후 commitText만 실행. 엔터/검색/전송은 자동 실행하지 않음.
- 다른 입력 세션·화면 숨김·비밀번호 필드에서 결과 전달 차단.
- 1번 시안의 흰색/청록, 원본+큰 결과, 수정/재스캔/입력 구조 적용.

## 검증 범위

빌드·JVM/Robolectric 테스트·Android Lint 수행. 테스트는 OCR 출력 이후 UI와 입력 규칙을 검증하며 카메라 하드웨어나 ML Kit의 실제 인식 정확도를 모사해 증명하지 않는다.

최종 `assembleDebug testDebugUnitTest lintDebug`: 성공. 테스트 **14개 통과**, 실패/오류 0. Lint **오류 0, 경고 12**. 경고는 의존성 업데이트 권고, 백업 정책 최신 형식 권고, 사용하지 않는 문자열과 코드 스타일/레이아웃 에디터 관련 항목이다. 경고를 숨기는 baseline은 만들지 않았다.

최종 병합 Manifest의 패키지가 `im.umo.baro_input`이며 INTERNET/ACCESS_NETWORK_STATE 권한이 없음을 확인했다. 모델 파일은 APK에 포함된다. SDK 내부 동작까지 포함한 실기기 네트워크·디스크 관찰은 아직 수행하지 않았다.

APK: `releases/baro-input-0.1.0-debug.apk`, 약 49 MB. 디버그 서명, 버전 0.1.0(1), minSdk 26, targetSdk 36.

런처의 임시 카메라 아이콘은 Google Material Icons의 photo_camera를 사용하며 청록색 tint만 변경했다. 원본: https://github.com/google/material-design-icons/tree/master/android/image/photo_camera/materialicons/black/res/drawable . 라이선스는 `design/material-icons-LICENSE.txt`에 보관했다. 최종 앱 브랜드 아이콘은 추후 제작 대상이다. APK 패키지 메타데이터를 aapt로 확인했고 체크섬은 `releases/SHA256SUMS.txt`에 저장했다.

ADB 조회 결과 연결된 디바이스 없음. 실행 중인 에뮬레이터도 없으며 호스트의 HypervisorPresent는 false였다. OS 가상화 설정은 변경하지 않았다.

따라서 실제 기기에서 IME 카메라 권한 전달, 초점·회전, 키보드 높이, 한글 표시, TalkBack, 전체 작업 완료는 아직 확인하지 않았다. 정적 시안과 실제 화면의 시각 비교도 미완료다. 현재 APK는 초기 기술 시제품이며 사용자 테스트/상용 출시 준비 완료를 뜻하지 않는다.

## 다음 단계

1. 삼성 기기에서 기본 스캔 → 결과 확인 → 숫자 수정 → 메모/브라우저 입력 실험.
2. 비삼성 기기 및 최신 OS에서 카메라/IME 수명주기 검증.
3. 실기기 캡처와 1번 시안 비교, 글꼴 확대·TalkBack·가로 방향 보정.
4. 원본 영역 확대 및 한글 수정 접근성, 사용자 피드백 반영.
5. 출시 전 target SDK 정책·16KB 페이지 지원·서명·개인정보 고지·테스트 트랙 검토.

## 개발 환경

이 세션에서 도구는 별도 `work/android-tools`에 준비했다. 시스템 JAVA_HOME이나 Android 설정을 영구 변경하지 않았다. SDK·Gradle 캐시·로컬 SDK 경로는 프로젝트 Git 추적 대상에서 제외한다. 초기 의존성은 호환되는 버전으로 고정했으며 Lint의 업데이트 권고는 출시 전 별도 검토한다.
