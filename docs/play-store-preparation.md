# Google Play 등록 준비 — 2026-09-07

아직 업로드하거나 심사에 제출하지 않았습니다.

## 이번에 확인한 것

- `bundleRelease lintRelease` 성공. R8 최적화를 적용한 AAB 생성.
- 출력: `app/build/outputs/bundle/release/app-release.aab`.
- 출시 서명 설정이 없어 현재 AAB는 업로드용으로 확정하지 않았습니다. 업로드 키와 Play App Signing 설정이 필요합니다.
- arm64-v8a와 x86_64의 네이티브 라이브러리 3개씩 모두 ELF LOAD 정렬 16KB 이상 확인. 16KB 기기 실행 검증 및 Play Console 검증은 미수행.
- 기존 에뮬레이터 사진 인식 테스트는 디버그 빌드 기준입니다. 최적화된 출시 빌드의 실기기 테스트가 남아 있습니다.
- Play Console 로그인은 Google 추가 본인 확인 단계에서 대기. 사용자는 기존 계정의 장기 미사용 폐쇄 안내를 확인했다고 알려주었습니다.

## 등록 문구 초안

앱 이름: 바로입력

짧은 설명: 카메라로 읽은 글자와 숫자를 확인하고 원하는 입력칸에 넣는 보조 키보드

상세 설명:

종이나 제품 라벨에 적힌 긴 번호를 하나씩 옮겨 적는 대신, 바로입력으로 읽고 확인해보세요.

바로입력은 필요할 때 전환해서 사용하는 Android 보조 키보드입니다. 입력할 칸을 선택하고 바로입력으로 전환한 뒤, 카메라로 글자를 읽으세요. 인식한 줄을 선택하고 원본과 비교한 다음 직접 입력 버튼을 누르면 내용이 들어갑니다.

- 한글·영문 텍스트와 숫자 인식
- 여러 줄 중 필요한 줄 선택
- 공백 제거와 숫자 수정
- 저장된 사진과 내장 박스 샘플로 인식 연습
- 앱에 포함된 모델을 이용한 기기 내 문자 인식

기존 삼성 키보드나 Gboard에 버튼을 추가하는 앱은 아닙니다. 비밀번호 입력칸에서는 스캔을 사용할 수 없습니다. 인식 정확도는 사진 상태에 따라 달라지므로 입력 전에 내용을 확인해주세요. 메시지 전송이나 검색 실행은 원래 앱에서 직접 진행합니다.

## 계정 정보가 필요한 항목

- 활성 개발자 계정, 공개 개발자 이름과 고객지원 이메일
- 개인정보처리방침 공개 URL 및 앱 내부 접근 경로
- 업로드 서명 키 보관 방식, 기존 패키지 등록 여부
- 배포 국가, 가격, 대상 연령, 콘텐츠 등급과 데이터 보안 설문
- 새 개인 계정이면 비공개 테스트 참여자 12명 및 연속 14일 요건

등록용 512×512 아이콘과 1024×500 그래픽도 필요합니다. 도움말의 합성 촬영 이미지는 실제 앱 캡처와 구분하고, 스토어에는 최종 빌드의 실제 화면을 준비합니다. 개인정보·SDK 처리 방식의 최종 확인 없이 데이터 보안 답변을 확정하지 않습니다.

## 공식 근거

- [휴면 계정과 재가입](https://support.google.com/googleplay/android-developer/answer/9899234?hl=en)
- [등록 수수료와 본인 확인](https://support.google.com/googleplay/android-developer/answer/6112435?hl=ko)
- [새 개인 계정 테스트 요건](https://support.google.com/googleplay/android-developer/answer/14151465)
- [사용자 데이터와 개인정보처리방침](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en)
- [스토어 이미지 규격](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en)
- [16KB 페이지 지원](https://developer.android.com/guide/practices/page-sizes)
