# 박스 시리얼 샘플 제작 기록

2026-09-07, 내장 ImageGen 도구로 생성했습니다. API/CLI는 사용하지 않았습니다.

앱 원본: `app/src/main/assets/sample-box.png` · 도움말 복사본: `docs/images/sample-box.png`

## 최종 프롬프트

Use case: product-mockup. Create a realistic photograph used as an Android OCR testing sample. Close-up of a brown corrugated cardboard electronics shipping box on a neutral desk, a large white matte product serial label on the front, nearly straight-on with slight natural perspective, crisp black printed sans-serif text, soft daylight, no glare or blur. Label occupies most of image so every character is legible. Exact label text in separate lines: "BARO DEVICE", "MODEL: BX-120", "SERIAL NUMBER", "0012 3456 7890", "LOT: 2026-09", "TEST SAMPLE". The serial digits must be especially large, on their own line, exact leading two zeroes. Add a small decorative barcode below the text. Fictional packaging, no real brand, no address, no personal data, no other text. Landscape image.

## 확인 결과

- 생성 이미지의 번호와 글자를 눈으로 확인했습니다.
- Android 14 에뮬레이터에서 번들 이미지 → 실제 ML Kit OCR → 번호 줄 선택 → 공백 제거 → 연습칸 입력 결과 `001234567890`을 확인했습니다.
- 같은 PNG를 `/sdcard/Download/baro-sample-box.png`에 넣고 Android 파일 선택기를 통해 불러온 경우에도 `0012 3456 7890` 줄이 인식됐습니다.
- 바코드는 장식이며 바코드 인식 테스트가 아닙니다. 카메라의 초점·노출·실제 촬영 품질을 검증한 결과가 아닙니다.
