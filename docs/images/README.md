# 도움말 화면 캡처

## 3번 항목의 박스 촬영 예시

`03-scan-box-example.png`는 원본 `03-scan.png`와 `sample-box.png`를 내장 ImageGen으로 합성한 도움말용 예시입니다. 실제 카메라 캡처나 인식 검증 자료가 아닙니다. 원본은 보존했습니다. 생성 후 한글 버튼·안내 문구와 시리얼 번호의 가독성을 눈으로 확인했습니다. 편집 프롬프트는 [기록](03-scan-box-prompt.md)에 있습니다.

## 촬영 연습용 샘플

`sample-numbers.png`, `sample-product-code.png`, `sample-text.png`는 2026-09-07에 `create-samples.ps1`로 만든 가상 촬영 자료입니다. 흰 바탕에 맑은 고딕으로 정확한 숫자·영문·한글을 그렸습니다. 앱 캡처 또는 실제 OCR 결과가 아닙니다. 생성 후 세 이미지의 글자와 잘림 여부를 직접 확인했습니다. 이번 추가 시점에는 연결된 에뮬레이터나 기기가 없어 이 샘플의 실제 OCR 결과는 검증하지 않았습니다.

## 실제 앱 캡처

추가 캡처: `06-image-test-chooser.png`는 사진 선택 시작 화면, `07-box-preview.png`는 저장 이미지 미리보기, `08-box-result.png`는 **실제 ML Kit 인식 후 번호 줄을 선택한 확인 화면**입니다. 박스 샘플의 생성 프롬프트와 실측 결과는 [제작 기록](sample-box-prompt.md)에 있습니다. 기존 01~05 캡처의 한계와 구분합니다.

2026-09-07, Pixel 3a / Android 14(API 34) 에뮬레이터에서 바로입력 0.1.0 디버그 앱을 실행하고 ADB `screencap -p`로 저장한 원본 PNG입니다. 디자인 시안이나 합성 이미지가 아닙니다.

| 파일 | 촬영 상태 |
|---|---|
| 01-setup.png | 카메라 권한 허용 및 입력기 활성화가 완료된 시작 화면 |
| 02-practice.png | 비어 있는 입력 연습 화면 |
| 03-scan.png | 제품번호 칸 선택 후 숫자 모드 카메라 미리보기 |
| 04-retry.png | 에뮬레이터 테스트 영상 촬영 후 글자 없음 안내 |
| 05-keyboard-picker.png | 키보드 전환 버튼으로 연 Android 입력기 선택 창 |

시스템 언어는 영어입니다. 카메라 속 이미지는 에뮬레이터의 테스트 영상입니다. OCR 성공·결과 수정·실제 휴대폰에서의 입력 성공을 입증하는 캡처는 아닙니다. 도움말의 해당 기능 설명은 앱 구현을 기준으로 합니다.

캡처를 준비하며 발견한 `BaroInputService.onCreate()`의 테마 지정 순서를 수정했습니다. `setTheme()`을 `super.onCreate()`보다 먼저 호출한 빌드에서 앱 실행, 연습 화면 진입, 입력기 표시, 카메라 미리보기, 글자 없음 안내, 키보드 선택 창을 확인했습니다. `assembleDebug testDebugUnitTest lintDebug`가 통과했습니다. 같은 빌드로 `releases/baro-input-0.1.0-debug.apk`와 체크섬을 갱신했습니다.
