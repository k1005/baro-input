# Play Store assets — 2026-09-09

Package: `im.umo.baro_input`. Current default locale: `en-US`.

## Multilingual refresh (current)

Saved in Play Console as **ready to send for review** on 2026-09-09. English (US) is the default; Korean, Japanese, Simplified Chinese and Traditional Chinese have their own title, short/full description and five screenshot cards. The new language-neutral feature graphic is shared. The matching localized AAB 0.1.4 (5) was subsequently uploaded and saved in Alpha with release notes in all five locales. Review submission remains blocked by missing account information; this is not a published release. See `../docs/play-store-preparation.md` for current status.

- `listings.json`: canonical copy, card headings, subtitles and descriptive alt copy in five locales. Console did not expose an alt-text input for these uploads.
- `localized/overview.png`: contact sheet of all 25 cards.
- `localized/<locale>/`: final 1080×1920 RGB PNG cards, editable SVG sources, listing text, approved icon and feature graphic.
- `upload/`: the same 25 cards with locale-prefixed names for unambiguous Console asset selection. Order: photo → recognized lines → confirmation → inserted result → recognition languages.
- `captures/<locale>/`: unchanged 1080×2220 emulator screenshots from the localized 0.1.3 debug source on API 34. These are real sample-photo OCR and UI results, not release-build or physical-device evidence. Chinese glyph accuracy limitations remain documented in `../docs/localization.md`.
- `shared/feature-source.png`: original built-in ImageGen output. `shared/feature-graphic.png`: 1024×500 RGB export, conceptual promotional illustration.
- AI declarations are applied to the feature graphic, approved icon and all 25 cards. Every card incorporates the AI-created app icon; several also show the AI-created sample box within the actual app capture. Typography, layout and UI captures are authored/rendered directly, not generated UI.

### Reproduction

`StoreCaptureTest` runs only with instrumentation argument `-e storeCapture true`; set the app locale with `adb shell cmd locale set-app-locales im.umo.baro_input --locales <tag>`, then run `im.umo.baro_input.StoreCaptureTest`. Capture files are written to the app's external files directory. Restore the locale override after capturing. The numeric sample recognition and UI actions must succeed before captures are accepted.

Run `python store-assets/build-assets.py` with fontTools available, then `node store-assets/render-assets.cjs` with `SHARP_MODULE` set to the installed sharp package path. The authoring script uses Windows Segoe UI, Malgun Gothic, Yu Gothic, Microsoft YaHei and Microsoft JhengHei fonts and outlines glyphs in SVG. It embeds each untouched screenshot. Exports were checked for dimensions, RGB format, file-size limits, text limits and visual consistency across all five locales.

New feature graphic prompt (built-in ImageGen):

> Use case: ads-marketing. Asset type: Google Play feature graphic for Baro Input, an Android camera/photo text input utility. Create one finished wide 1024 by 500 banner, opaque RGB. Minimal premium editorial 3D paper illustration, sophisticated deep teal #087F72, pale mint #E8F5F1, warm cream #FAFBFA, subtle coral accent only. Convey a printed label becoming editable text: a crisp gently angled cream paper label with exactly '0012 3456', surrounded by four teal scanning corner brackets on the left; a clear flowing arrow leads to a clean floating rounded input field showing exactly '00123456' and a teal insertion caret on the right, with a small confirmation check nearby. Large beautifully rendered objects, tactile paper texture, soft ambient shadows, restrained modern composition, no clutter. Place entire action in center 80% leaving generous margin. Absolutely no words, no marketing headline, no phones, no app icons, no additional UI, no store badges, no awards. Digits exact. Conceptual promo, not screenshot.

## Previous Korean assets (historical)

- `ko-KR/icon-512.png`: selected camera + I-beam icon, 512×512 RGB PNG, identical to `icon-concept-v6-512.png`. Built-in ImageGen; prompts in `icon-concept-v6.md`. User approved and Play listing updated with AI label. Also used by Android launcher in 0.1.3 (4).
- `ko-KR/icon-camera-legacy-512.png`: previous Material camera icon. Legacy reproduction script: `create-icon.ps1`; Material Icons attribution: `../design/material-icons-LICENSE.txt`. Do not run this script over the selected icon.
- `ko-KR/feature-graphic.png`: 1024×500 RGB PNG. Built-in ImageGen marketing illustration; generated source preserved in `feature-source.png`. Dimensions converted using System.Drawing.
- `ko-KR/01-photo.png`, `02-lines.png`, `03-confirm.png`, `04-edit.png`: unchanged 1080×2220 app screenshots from `docs/design-audit/after/04-image-preview.png` through `07-edit.png`. Captured on 2026-09-07 in the 0.1.0 debug emulator build. They show the embedded photo-practice flow, which remains implemented. They are not new release-build captures or physical-device evidence. The sample box shown inside the app is AI generated; the UI and recognition results are real captures.

Console accepted four screenshots and saved the listing as ready to send for review. AI labels were applied to the selected camera + I-beam icon, the feature graphic and the three screenshots containing the AI sample box. The edit-keypad screenshot is not an AI raster image.

Final prompt (built-in ImageGen, no API key):

Use case: ads-marketing. Create Google Play feature graphic for Korean Android utility app 바로입력. Wide banner 1024x500 aspect ratio, opaque RGB. Polished minimal teal (#087F72), warm off-white and mint palette. Exact Korean headline on left: "읽고, 확인하고, 바로 입력". Small supporting line: "카메라와 사진으로 옮기는 글자와 숫자". On right tasteful stylized paper product label with 0012345678 being scanned by teal corner brackets and a simple arrow towards an abstract input field containing the same 0012345678 and a check symbol. This is conceptual marketing art, not a fake app screenshot. Spacious modern Korean typography, excellent legibility, all text inset at least 65px from edges. No phone device frames, no app store badges, no rankings, no price claims, no other logos. Output a finished banner.
