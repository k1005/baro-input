# Play Store assets — 2026-09-09

Package: `im.umo.baro_input`. Default locale: `ko-KR`.

- `ko-KR/icon-512.png`: selected camera + I-beam icon, 512×512 RGB PNG, identical to `icon-concept-v6-512.png`. Built-in ImageGen; prompts in `icon-concept-v6.md`. User approved and Play listing updated with AI label. Also used by Android launcher in 0.1.3 (4).
- `ko-KR/icon-camera-legacy-512.png`: previous Material camera icon. Legacy reproduction script: `create-icon.ps1`; Material Icons attribution: `../design/material-icons-LICENSE.txt`. Do not run this script over the selected icon.
- `ko-KR/feature-graphic.png`: 1024×500 RGB PNG. Built-in ImageGen marketing illustration; generated source preserved in `feature-source.png`. Dimensions converted using System.Drawing.
- `ko-KR/01-photo.png`, `02-lines.png`, `03-confirm.png`, `04-edit.png`: unchanged 1080×2220 app screenshots from `docs/design-audit/after/04-image-preview.png` through `07-edit.png`. Captured on 2026-09-07 in the 0.1.0 debug emulator build. They show the embedded photo-practice flow, which remains implemented. They are not new release-build captures or physical-device evidence. The sample box shown inside the app is AI generated; the UI and recognition results are real captures.

Console accepted four screenshots and saved the listing as ready to send for review. AI labels were applied to the selected camera + I-beam icon, the feature graphic and the three screenshots containing the AI sample box. The edit-keypad screenshot is not an AI raster image.

Final prompt (built-in ImageGen, no API key):

Use case: ads-marketing. Create Google Play feature graphic for Korean Android utility app 바로입력. Wide banner 1024x500 aspect ratio, opaque RGB. Polished minimal teal (#087F72), warm off-white and mint palette. Exact Korean headline on left: "읽고, 확인하고, 바로 입력". Small supporting line: "카메라와 사진으로 옮기는 글자와 숫자". On right tasteful stylized paper product label with 0012345678 being scanned by teal corner brackets and a simple arrow towards an abstract input field containing the same 0012345678 and a check symbol. This is conceptual marketing art, not a fake app screenshot. Spacious modern Korean typography, excellent legibility, all text inset at least 65px from edges. No phone device frames, no app store badges, no rankings, no price claims, no other logos. Output a finished banner.
