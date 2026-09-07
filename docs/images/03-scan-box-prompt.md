# 박스 촬영 도움말 예시 편집

도구: 내장 ImageGen (CLI/API 사용 안 함)

입력: `03-scan.png` (편집 대상), `sample-box.png` (삽입할 박스 사진)

출력: `docs/images/03-scan-box-example.png`

## 최종 프롬프트

Use case: compositing. Edit image 1 (Android app screenshot) using image 2 (box photograph) as the supporting insert. Return the complete tall screenshot with exactly the same aspect ratio and UI layout as image 1. Change ONLY the rectangular camera preview area in the lower middle (approximately x44..1036, y1010..1478 in the original 1080x2220 screenshot): replace the strange pixelated green emulator scene and its background with the realistic cardboard box and serial label from image 2. Fit the label clearly inside the preview, with serial number "0012 3456 7890" clearly readable, realistic camera framing. Preserve ALL other pixels/sections as closely as possible: Korean text, input field, buttons, teal selected numeric mode, status and navigation bars. Do not redesign, crop, add UI, or change any Korean text. No cartoon or pixel art. This is an illustrative help screenshot showing a box being aimed at by the camera.
