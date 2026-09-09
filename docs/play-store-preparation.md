# Google Play 등록 준비 — 2026-09-09

## 내부 테스트 업데이트 0.1.1 (2)

- IME 스캔 패널에 내비게이션 바 및 화면 컷아웃 여백 적용. 하단 입력·다시 스캔·수정 버튼의 시스템 버튼 겹침 수정.
- 제스처/3버튼 내비게이션 및 측면 내비게이션 영역, 화면 전환, Activity 내 미리보기의 중복 여백 방지 회귀 테스트 추가.
- `bundleRelease testDebugUnitTest lintRelease` 통과. 테스트 25개 통과. 실기기 확인은 연결된 기기가 없어 미수행.
- 서명된 AAB: `app/build/outputs/bundle/release/app-release.aab`.
- AAB SHA-256: `f55ea20420a1dbb1a6dd47d280cf797192de124ea227bedaae037881f2e5a0fb`.
- Play Console 내부 테스트 업로드 준비 완료.

## 내부 테스트 진행 상태 (9월 9일)

- 우모 개인 개발자 계정에 바로입력 앱 생성 완료. 패키지 `im.umo.baro_input`.
- 내부 테스트 `0.1.0 (1) 내부 테스트` 출시명과 한국어 출시 노트 초안 저장. 테스터 목록 2명 적용 완료.
- 업로드 키 생성 및 Gradle 출시 서명 설정 완료. `bundleRelease lintRelease` 통과.
- 키와 비밀번호 설정은 저장소 밖 `%USERPROFILE%/.android/baro-input/`에 보관하며 현재 Windows 사용자만 접근하도록 폴더 ACL을 설정함. 이 폴더는 안전한 별도 위치에 백업해야 함. 저장소에 키나 비밀번호를 추가하지 않음.
- AAB 업로드 및 내부 테스트 출시 완료. Console에서 트랙 활성, `내부 테스터에게 제공됨` 확인 (2026-09-09 17:29 KST).
- 참여 링크: https://play.google.com/apps/internaltest/4700439061568066999 (등록된 Google 계정으로 참여).
- 검토 전 임시 앱 이름은 `im.umo.baro_input (unreviewed)`. 네이티브 디버그 기호 미첨부 경고 1개가 있으며 출시를 차단하는 오류는 없었음.
- 업로드 AAB SHA-256: `e57f8f5e8bd03f0adb65312819b7dfab0628efb329c95a919c52d68e4ef9fcb4`.
- Console에서 일반 공개 전 비공개 테스트 12명 이상, 연속 14일 및 프로덕션 액세스 신청이 필요한 계정임을 확인함. 내부 테스트는 이 요건을 대신하지 않음.

## 이전 점검 기록 (9월 7일)

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
