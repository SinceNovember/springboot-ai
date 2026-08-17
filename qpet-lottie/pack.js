/**
 * 打包 dotLottie (.lottie) 文件：manifest.json + animations/*.json 的 zip 包
 * 运行: node pack.js  (依赖 PowerShell Compress-Archive，见下方调用方式)
 * 这里只负责生成 staging 目录，压缩由命令行完成。
 */
const fs = require('fs');
const path = require('path');

const OUT = __dirname;
const staging = path.join(OUT, '.dotlottie-staging');
const animDir = path.join(staging, 'animations');
fs.rmSync(staging, { recursive: true, force: true });
fs.mkdirSync(animDir, { recursive: true });

const ids = ['idle', 'happy', 'sad', 'wave'];
for (const id of ids) {
  fs.copyFileSync(path.join(OUT, `qpet-${id}.json`), path.join(animDir, `${id}.json`));
}
const manifest = {
  version: '1.0',
  generator: 'qpet-lottie-gen',
  author: '',
  animations: ids.map(id => ({ id, loop: true, speed: 1 })),
};
fs.writeFileSync(path.join(staging, 'manifest.json'), JSON.stringify(manifest));
console.log('staging ready:', staging);
