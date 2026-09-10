const fs = require('fs');
const path = require('path');
const sharp = require(process.env.SHARP_MODULE || 'sharp');
const root = __dirname;
const locales = Object.keys(JSON.parse(fs.readFileSync(path.join(root, 'listings.json'), 'utf8')));
(async () => {
  await sharp(path.join(root, 'shared/feature-source.png')).resize(1024, 500).removeAlpha().png()
    .toFile(path.join(root, 'shared/feature-graphic.png'));
  for (const locale of locales) {
    const dir = path.join(root, 'localized', locale);
    for (const file of fs.readdirSync(dir).filter(f => f.endsWith('.svg'))) {
      await sharp(path.join(dir, file)).removeAlpha().png().toFile(path.join(dir, file.replace('.svg', '.png')));
    }
    fs.copyFileSync(path.join(root, 'shared/feature-graphic.png'), path.join(dir, 'feature-graphic.png'));
    fs.copyFileSync(path.join(root, 'ko-KR/icon-512.png'), path.join(dir, 'icon-512.png'));
    const upload = path.join(root, 'upload');
    fs.mkdirSync(upload, {recursive:true});
    for (const file of fs.readdirSync(dir).filter(f => /^0.*\.png$/.test(f))) {
      fs.copyFileSync(path.join(dir,file), path.join(upload,locale+'-'+file));
    }
    console.log(locale, 'exported');
  }
  const tiles = [];
  for (const [row,locale] of locales.entries()) {
    for (const [col,name] of ['01-photo','02-lines','03-confirm','04-done','05-languages'].entries()) {
      tiles.push({input: await sharp(path.join(root,'localized',locale,name+'.png')).resize(216,384).toBuffer(),left:col*224,top:row*392});
    }
  }
  await sharp({create:{width:1112,height:1952,channels:3,background:'#ffffff'}}).composite(tiles).png().toFile(path.join(root,'localized/overview.png'));
})();
