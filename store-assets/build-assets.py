"""Author native SVG store cards from unmodified app captures and exact localized copy.

Typography is outlined from installed fonts so exports do not depend on renderer fonts.
Render SVGs with render-assets.cjs. Source screenshots are always retained unchanged.
"""
import base64
import json
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen

ROOT = Path(__file__).resolve().parent
COPY = json.loads((ROOT / 'listings.json').read_text(encoding='utf-8'))
FONT = {
    'en-US': 'segoeuib.ttf', 'ko-KR': 'malgunbd.ttf', 'ja-JP': 'YuGothB.ttc',
    'zh-CN': 'msyhbd.ttc', 'zh-TW': 'msjhbd.ttc',
}
NAMES = ['01-photo', '02-lines', '03-confirm', '04-done', '05-languages']

def image(path):
    return 'data:image/png;base64,' + base64.b64encode(path.read_bytes()).decode()

for locale, copy in COPY.items():
    font = TTFont(Path('C:/Windows/Fonts') / FONT[locale], fontNumber=0)
    glyphs = font.getGlyphSet()
    cmap = font.getBestCmap()
    metrics = font['hmtx'].metrics
    units = font['head'].unitsPerEm
    def width(text, size):
        return sum(metrics[cmap[ord(char)]][0] for char in text) * size / units
    def text(value, x, y, size, fill, maxwidth=936):
        size = min(size, size * maxwidth / max(1, width(value, size)))
        result = []
        for char in value:
            glyph = cmap[ord(char)]
            pen = SVGPathPen(glyphs)
            glyphs[glyph].draw(pen)
            result.append(f'<path fill="{fill}" transform="translate({x:.3f} {y}) scale({size/units:.6f} {-size/units:.6f})" d="{pen.getCommands()}"/>')
            x += metrics[glyph][0] * size / units
        return ''.join(result)
    dest = ROOT / 'localized' / locale
    dest.mkdir(parents=True, exist_ok=True)
    (dest / 'title.txt').write_text(copy['title'], encoding='utf-8')
    (dest / 'short-description.txt').write_text(copy['short'], encoding='utf-8')
    (dest / 'full-description.txt').write_text(copy['full'], encoding='utf-8')
    for i, name in enumerate(NAMES):
        dark = i in (0, 4)
        bg = '#08695F' if dark else '#F2F7F3' if i == 2 else '#F7F5EF'
        fg = '#FFFFFF' if dark else '#142C29'
        subtle = '#C8E8DE' if dark else '#47615B'
        wash = '#0C7B6E' if dark else '#DDEDE4'
        capture = ROOT / 'captures' / locale / (name + '.png')
        svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
<defs><filter id="shadow" x="-30%" y="-10%" width="160%" height="125%"><feDropShadow dx="0" dy="20" stdDeviation="20" flood-color="#062D27" flood-opacity=".16"/></filter>
<clipPath id="screen"><rect x="204" y="430" width="672" height="1381.33" rx="20"/></clipPath></defs>
<rect width="1080" height="1920" fill="{bg}"/>
<circle cx="850" cy="1260" r="730" fill="{wash}"/>
<image x="72" y="53" width="46" height="46" href="{image(ROOT / 'ko-KR/icon-512.png')}"/>
{text(copy['title'], 134, 88, 34, fg, 650)}
{text(f'{i+1:02d} / 05', 874, 84, 28, subtle, 150)}
{text(copy['headlines'][i].split(chr(10))[0], 72, 204, 82, fg)}
{text(copy['headlines'][i].split(chr(10))[1], 72, 301, 82, fg)}
{text(copy['subtitles'][i], 72, 367, 31, subtle)}
<rect x="196" y="422" width="688" height="1397.33" rx="28" fill="#FFFFFF" filter="url(#shadow)"/>
<image x="204" y="430" width="672" height="1381.33" preserveAspectRatio="xMidYMid meet" href="{image(capture)}" clip-path="url(#screen)"/>
<rect x="204" y="430" width="672" height="1381.33" rx="20" fill="none" stroke="#D2DDD7"/>
{text(copy['footer'], 72, 1875, 27, subtle)}
</svg>'''
        (dest / (name + '.svg')).write_text(svg, encoding='utf-8')
    print(locale, 'native SVG cards authored')
