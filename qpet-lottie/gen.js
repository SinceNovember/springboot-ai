/**
 * Q宠 Lottie 动画生成器 v2
 * 根据参考图片（Q版短发女孩形象）用矢量图形重绘角色，
 * 并生成多个动作的 Lottie JSON 文件。
 *
 * 布局按参考图 1:2 缩放到 512x512 画布：
 *   头顶 y≈30，刘海边缘 y≈150-168，眉毛 y≈168-175，眼睛 y≈176-232，
 *   嘴 y≈248-268，下巴 y≈296，上衣 296-400，短裤 400-437，脚底 y≈483
 *
 * 运行: node gen.js
 * 输出: qpet-idle.json / qpet-happy.json / qpet-sad.json / qpet-wave.json / preview.html
 */
const fs = require('fs');
const path = require('path');

const OUT = __dirname;
const W = 512, H = 512, FR = 30;

/* ---------- 调色板（取自参考图） ---------- */
const SKIN        = [0.984, 0.918, 0.886];
const SKIN_SHADOW = [0.925, 0.784, 0.737];
const HAIR        = [0.275, 0.204, 0.165];
const HAIR_TOP    = [0.376, 0.286, 0.224];   // 头顶渐变亮部
const HAIR_SHINE  = [0.478, 0.376, 0.302];   // 高光
const EYE         = [0.102, 0.082, 0.071];
const WHITE       = [1, 1, 1];
const BROW        = [0.557, 0.431, 0.345];
const BLUSH       = [0.961, 0.722, 0.706];
const LIP         = [0.949, 0.769, 0.753];
const LIP_TOP     = [0.914, 0.651, 0.635];
const LIP_DARK    = [0.788, 0.498, 0.482];
const TOP         = [0.976, 0.878, 0.882];
const TOP_SHADOW  = [0.918, 0.788, 0.796];
const SHORTS      = [0.953, 0.929, 0.871];
const SHORTS_SH   = [0.855, 0.812, 0.722];
const SHOE        = [0.957, 0.863, 0.855];
const MOUTH_IN    = [0.545, 0.271, 0.271];
const TONGUE      = [0.88, 0.50, 0.50];
const TEAR        = [0.62, 0.80, 0.95];

/* ---------- 关键锚点 ---------- */
const FEET = [256, 483];   // ROOT 锚点(脚底)
const NECK = [256, 295];   // HEAD 锚点(脖子)
const EYE_L = [219, 204];
const EYE_R = [309, 204];
const SH_L = [196, 318];   // 左肩(手臂旋转轴)
const SH_R = [316, 318];

/* ---------- Lottie 基础构件 ---------- */
const st = k => ({ a: 0, k });
const V = x => (Array.isArray(x) ? x : [x]);

function kf(frames) {
  return {
    a: 1,
    k: frames.map((f, i) => {
      const [t, s] = f;
      const key = { t, s: V(s) };
      if (i < frames.length - 1) {
        key.i = { x: [0.35], y: [1] };
        key.o = { x: [0.65], y: [0] };
      }
      return key;
    }),
  };
}

const fill = (c, o = 100) => ({ ty: 'fl', c: st([...c, 1]), o: st(o), r: 1 });
// 线性渐变填充: s/e 为起止点, c1/c2 为两端颜色
const gfill = (s, e, c1, c2, o = 100) =>
  ({ ty: 'gf', o: st(o), r: 1, s: st(s), e: st(e), t: 1, g: { p: 2, k: st([0, ...c1, 1, ...c2]) } });
const stroke = (c, w, o = 100) => ({ ty: 'st', c: st([...c, 1]), o: st(o), w: st(w), lc: 2, lj: 2, ml: 4 });
const el = (w, h, x = 0, y = 0) => ({ ty: 'el', p: st([x, y]), s: st([w, h]) });
const rc = (w, h, r, x = 0, y = 0) => ({ ty: 'rc', p: st([x, y]), s: st([w, h]), r: st(r) });
const sh = (v, i, o, c = true) => ({ ty: 'sh', ks: st({ i, o, v, c }) });

function grp(nm, items, tr = {}) {
  return {
    ty: 'gr', nm,
    it: [...items, {
      ty: 'tr',
      p: st(tr.p || [0, 0]), a: st(tr.a || [0, 0]),
      s: st(tr.s || [100, 100]), r: st(tr.r || 0),
      o: st(tr.o == null ? 100 : tr.o), sk: st(0), sa: st(0),
    }],
  };
}
// 纯色椭圆组（几何画在原点，组变换定位，旋转围绕自身中心）
const gEl = (nm, cx, cy, w, h, c, o = 100, rot = 0) =>
  grp(nm, [el(w, h, 0, 0), fill(c, o)], { p: [cx, cy], r: rot });

function baseKS(o = {}) {
  return {
    o: o.o || st(100),
    r: o.r || st(0),
    p: o.p || st([0, 0, 0]),
    a: o.a || st([0, 0, 0]),
    s: o.s || st([100, 100, 100]),
  };
}
function shapeLayer(ind, nm, shapes, o = {}) {
  const l = { ddd: 0, ind, ty: 4, nm, sr: 1, ks: baseKS(o), ao: 0, shapes, ip: 0, op: 9999, st: 0 };
  if (o.parent) l.parent = o.parent;
  return l;
}
function nullLayer(ind, nm, o = {}) {
  const l = { ddd: 0, ind, ty: 3, nm, sr: 1, ks: baseKS(o), ao: 0, ip: 0, op: 9999, st: 0 };
  if (o.parent) l.parent = o.parent;
  return l;
}

/* ---------- 角色搭建 ----------
 * ROOT(脚底) -> HEAD(脖子) -> 五官/头发
 *            -> 左右臂(肩部旋转轴) / 身体
 */
function build() {
  const L = {};

  L.root = nullLayer(1, 'ROOT', { a: st([...FEET, 0]), p: st([...FEET, 0]) });
  L.head = nullLayer(2, 'HEAD', { parent: 1, a: st([...NECK, 0]), p: st([...NECK, 0]) });

  // 刘海：顶部大圆弧 + 底部柔和波浪边（碗盖头），带上浅下深渐变和高光
  const bangsV = [
    [140, 148], [252, 30], [368, 150],                 // 左角 -> 头顶 -> 右角
    [324, 164], [288, 158], [252, 168],                // 波浪边(右->左)
    [216, 158], [180, 164],
  ];
  const bangsI = [
    [0, 0], [-78, 0], [0, -88],
    [14, 2], [-12, -3], [12, -2],
    [12, 3], [-12, -2],
  ];
  const bangsO = [
    [0, -88], [78, 0], [0, 0],
    [-14, -3], [12, 2], [-12, -3],
    [-12, 2], [-14, -4],
  ];
  L.bangs = shapeLayer(3, 'bangs', [
    grp('shine', [el(170, 46, 0, 0), fill(HAIR_SHINE, 40)], { p: [252, 82], r: -6 }),
    grp('bangs', [sh(bangsV, bangsI, bangsO), gfill([252, 34], [252, 200], HAIR_TOP, HAIR)]),
  ], { parent: 2 });

  // 眉毛（微微下垂的担忧弧线，露在刘海边缘下）
  L.browL = shapeLayer(4, 'browL', [
    grp('b', [sh([[197, 175], [229, 168]], [[0, 0], [-10, -3]], [[10, -5], [0, 0]], false), stroke(BROW, 5)]),
  ], { parent: 2 });
  L.browR = shapeLayer(5, 'browR', [
    grp('b', [sh([[279, 168], [311, 175]], [[0, 0], [-10, -5]], [[10, -3], [0, 0]], false), stroke(BROW, 5)]),
  ], { parent: 2 });

  // 大眼睛（黑亮 + 双高光 + 外侧睫毛），锚点在眼球中心以便眨眼
  L.eyeL = shapeLayer(6, 'eyeL', [
    grp('lash', [sh([[200, 180], [190, 171]], [[0, 0], [0, 0]], [[0, 0], [0, 0]], false), stroke(EYE, 3.5)]),
    gEl('hi2', 228, 219, 8, 9, WHITE, 85),
    gEl('hi1', 209, 190, 15, 19, WHITE),
    gEl('eye', ...EYE_L, 47, 56, EYE),
  ], { parent: 2, a: st([...EYE_L, 0]), p: st([...EYE_L, 0]) });
  L.eyeR = shapeLayer(7, 'eyeR', [
    grp('lash', [sh([[328, 180], [338, 171]], [[0, 0], [0, 0]], [[0, 0], [0, 0]], false), stroke(EYE, 3.5)]),
    gEl('hi2', 318, 219, 8, 9, WHITE, 85),
    gEl('hi1', 299, 190, 15, 19, WHITE),
    gEl('eye', ...EYE_R, 47, 56, EYE),
  ], { parent: 2, a: st([...EYE_R, 0]), p: st([...EYE_R, 0]) });

  // 泪滴（默认隐藏，sad 中出现）
  L.tear = shapeLayer(8, 'tear', [
    gEl('drop', 322, 246, 14, 19, TEAR),
  ], { parent: 2, a: st([322, 246, 0]), p: st([322, 246, 0]), o: st(0) });

  // 张嘴笑（默认隐藏，happy 中出现）
  L.mouthSmile = shapeLayer(9, 'mouthSmile', [
    gEl('tongue', 262, 268, 22, 10, TONGUE),
    grp('open', [sh(
      [[240, 248], [284, 248], [262, 274]],
      [[0, 0], [0, 0], [12, 2]],
      [[0, 0], [0, 14], [-12, 2]],
    ), fill(MOUTH_IN)]),
  ], { parent: 2, o: st(0) });

  // 嘟嘴（默认表情，双层嘴唇）
  L.mouthPout = shapeLayer(10, 'mouthPout', [
    gEl('topL', 253, 249, 21, 11, LIP_TOP),
    gEl('topR', 271, 249, 21, 11, LIP_TOP),
    gEl('seam', 262, 253, 33, 5, LIP_DARK),
    gEl('bottom', 262, 260, 35, 16, LIP),
  ], { parent: 2 });

  L.nose = shapeLayer(11, 'nose', [gEl('n', 258, 238, 11, 6, SKIN_SHADOW, 60)], { parent: 2 });

  L.blush = shapeLayer(12, 'blush', [
    gEl('l', 178, 234, 48, 21, BLUSH, 50),
    gEl('r', 332, 234, 48, 21, BLUSH, 50),
  ], { parent: 2 });

  // 侧发：贴着脸颊两侧、向下收尖的发绺（在脸前面，止于下巴上方）
  const lockL = grp('lockL', [sh(
    [[136, 148], [130, 220], [154, 286], [174, 224], [164, 156]],
    [[6, -22], [2, -26], [-12, -18], [-2, 24], [3, 24]],
    [[-8, 22], [-2, 24], [9, -16], [2, -23], [-4, -18]],
  ), fill(HAIR)]);
  const lockR = grp('lockR', [sh(
    [[372, 148], [378, 220], [354, 286], [334, 224], [344, 156]],
    [[-6, -22], [-2, -26], [12, -18], [2, 24], [-3, 24]],
    [[8, 22], [2, 24], [-9, -16], [-2, -23], [4, -18]],
  ), fill(HAIR)]);
  L.hairSide = shapeLayer(13, 'hairSide', [lockL, lockR], { parent: 2 });

  // 圆胖脸
  L.face = shapeLayer(14, 'face', [gEl('face', 255, 192, 224, 208, SKIN)], { parent: 2 });

  // 后发（比脸大一圈的深色底）
  L.hairBack = shapeLayer(15, 'hairBack', [
    grp('back', [el(258, 264, 0, 0), gfill([252, 30], [252, 290], HAIR_TOP, HAIR)], { p: [252, 160] }),
  ], { parent: 2 });

  // 胖乎乎的手臂（锚点在肩，方便挥手）
  L.armL = shapeLayer(16, 'armL', [
    gEl('hand', 176, 392, 24, 24, SKIN),
    gEl('arm', 184, 356, 27, 76, SKIN, 100, 6),
  ], { parent: 1, a: st([...SH_L, 0]), p: st([...SH_L, 0]) });
  L.armR = shapeLayer(17, 'armR', [
    gEl('hand', 336, 392, 24, 24, SKIN),
    gEl('arm', 328, 356, 27, 76, SKIN, 100, -6),
  ], { parent: 1, a: st([...SH_R, 0]), p: st([...SH_R, 0]) });

  // 身体：荷叶边罩衫 + 灯笼短裤 + 胖腿 + 圆头鞋
  const blouseV = [
    [200, 296], [312, 296],                       // 领口
    [344, 390],                                   // 右下角
    [300, 402], [256, 394], [212, 402],           // 波浪下摆(右->左)
    [168, 390],                                   // 左下角
  ];
  const blouseI = [
    [-12, 28], [-20, -8],
    [-4, -28],
    [16, 2], [14, 4], [14, -2],
    [8, 10],
  ];
  const blouseO = [
    [20, -8], [12, 28],
    [-8, 10],
    [-14, -2], [-14, 4], [-16, 2],
    [4, -28],
  ];
  L.body = shapeLayer(18, 'body', [
    gEl('sleeveL', 192, 314, 52, 42, TOP, 100, 18),
    gEl('sleeveR', 320, 314, 52, 42, TOP, 100, -18),
    gEl('collar', 256, 300, 92, 14, TOP_SHADOW),
    grp('blouse', [sh(blouseV, blouseI, blouseO), fill(TOP)]),
    grp('split', [rc(8, 24, 4, 256, 430), fill(SHORTS_SH, 70)]),
    grp('shorts', [rc(130, 52, 22, 256, 418), fill(SHORTS)]),
    gEl('shoeL', 224, 473, 46, 20, SHOE),
    gEl('shoeR', 288, 473, 46, 20, SHOE),
    gEl('legL', 226, 450, 40, 42, SKIN),
    gEl('legR', 286, 450, 40, 42, SKIN),
  ], { parent: 1 });

  // 手臂放在最上层，举手/挥手时不被头发遮住
  const layers = [
    L.root, L.head, L.armL, L.armR,
    L.bangs, L.browL, L.browR, L.eyeL, L.eyeR, L.tear,
    L.mouthSmile, L.mouthPout, L.nose, L.blush, L.hairSide, L.face, L.hairBack,
    L.body,
  ];
  return { L, layers };
}

function makeAnim(name, op, mutate) {
  const { L, layers } = build();
  mutate(L);
  layers.forEach(l => (l.op = op));
  return { v: '5.7.4', fr: FR, ip: 0, op, w: W, h: H, nm: name, ddd: 0, assets: [], layers };
}

function blink(L, t) {
  const s = [
    [0, [100, 100, 100]], [t, [100, 100, 100]],
    [t + 4, [100, 6, 100]], [t + 9, [100, 100, 100]],
  ];
  L.eyeL.ks.s = kf(s);
  L.eyeR.ks.s = kf(JSON.parse(JSON.stringify(s)));
}

/* ---------- 动作 1: 待机（呼吸 + 摇摆 + 眨眼） ---------- */
const idle = makeAnim('qpet-idle', 120, L => {
  L.root.ks.p = kf([[0, [256, 483, 0]], [60, [256, 487, 0]], [120, [256, 483, 0]]]);
  L.root.ks.s = kf([[0, [100, 100, 100]], [60, [101, 99, 100]], [120, [100, 100, 100]]]);
  L.head.ks.r = kf([[0, 0], [30, -1.8], [75, 1.6], [120, 0]]);
  L.armL.ks.r = kf([[0, 0], [60, 3], [120, 0]]);
  L.armR.ks.r = kf([[0, 0], [60, -3], [120, 0]]);
  blink(L, 68);
});

/* ---------- 动作 2: 开心（跳跃 + 举手 + 笑） ---------- */
const happy = makeAnim('qpet-happy', 78, L => {
  L.root.ks.p = kf([
    [0, [256, 483, 0]], [10, [256, 486, 0]], [24, [256, 415, 0]],
    [34, [256, 411, 0]], [48, [256, 483, 0]], [78, [256, 483, 0]],
  ]);
  L.root.ks.s = kf([
    [0, [100, 100, 100]], [10, [108, 90, 100]], [20, [95, 107, 100]],
    [40, [97, 104, 100]], [48, [100, 100, 100]], [52, [106, 92, 100]], [62, [100, 100, 100]],
  ]);
  L.armL.ks.r = kf([[0, 0], [8, -15], [20, 145], [46, 135], [60, 0]]);
  L.armR.ks.r = kf([[0, 0], [8, 15], [20, -145], [46, -135], [60, 0]]);
  L.head.ks.r = kf([[0, 0], [24, -5], [48, 0]]);
  L.mouthPout.ks.o = kf([[0, 100], [6, 0], [66, 0], [74, 100]]);
  L.mouthSmile.ks.o = kf([[0, 0], [6, 100], [66, 100], [74, 0]]);
  const squint = [[0, [100, 100, 100]], [18, [100, 78, 100]], [48, [100, 78, 100]], [58, [100, 100, 100]]];
  L.eyeL.ks.s = kf(squint);
  L.eyeR.ks.s = kf(JSON.parse(JSON.stringify(squint)));
});

/* ---------- 动作 3: 难过（低头 + 流泪） ---------- */
const sad = makeAnim('qpet-sad', 105, L => {
  L.head.ks.p = kf([[0, [256, 295, 0]], [25, [258, 304, 0]], [80, [258, 304, 0]], [105, [256, 295, 0]]]);
  L.head.ks.r = kf([[0, 0], [25, 5], [80, 5], [105, 0]]);
  L.root.ks.s = kf([[0, [100, 100, 100]], [25, [100, 98.5, 100]], [80, [100, 98.5, 100]], [105, [100, 100, 100]]]);
  L.armL.ks.r = kf([[0, 0], [25, -6], [80, -6], [105, 0]]);
  L.armR.ks.r = kf([[0, 0], [25, 6], [80, 6], [105, 0]]);
  L.tear.ks.o = kf([[0, 0], [30, 0], [36, 95], [62, 95], [70, 0]]);
  L.tear.ks.p = kf([[30, [322, 246, 0]], [68, [327, 300, 0]]]);
  L.tear.ks.s = kf([[30, [55, 55, 100]], [45, [100, 100, 100]]]);
  blink(L, 40);
});

/* ---------- 动作 4: 打招呼（右手挥动 + 歪头） ---------- */
const wave = makeAnim('qpet-wave', 66, L => {
  L.armR.ks.r = kf([
    [0, 0], [10, -150], [18, -120], [26, -150], [34, -120], [42, -150], [56, 0],
  ]);
  L.head.ks.r = kf([[0, 0], [12, -5], [46, -5], [58, 0]]);
  L.root.ks.p = kf([[0, [256, 483, 0]], [30, [256, 485, 0]], [66, [256, 483, 0]]]);
  blink(L, 28);
});

/* ---------- 输出 ---------- */
const anims = { idle, happy, sad, wave };
for (const [name, data] of Object.entries(anims)) {
  fs.writeFileSync(path.join(OUT, `qpet-${name}.json`), JSON.stringify(data));
  console.log(`written qpet-${name}.json (${JSON.stringify(data).length} bytes)`);
}

/* ---------- 预览页面（JSON 已内联，双击打开即可） ---------- */
const preview = `<!DOCTYPE html>
<html lang="zh">
<head>
<meta charset="utf-8">
<title>Q宠 Lottie 预览</title>
<style>
  body{font-family:system-ui,sans-serif;background:#faf6f2;margin:0;padding:24px;}
  h1{text-align:center;color:#6b5348;font-size:20px;}
  .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:16px;max-width:1100px;margin:0 auto;}
  .card{background:#fff;border-radius:16px;box-shadow:0 2px 10px rgba(0,0,0,.06);padding:12px;text-align:center;}
  .card .anim{width:100%;aspect-ratio:1;}
  .card p{margin:4px 0 0;color:#8a7263;font-weight:600;}
</style>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bodymovin/5.12.2/lottie.min.js"><\/script>
</head>
<body>
<h1>Q宠 Lottie 动作预览</h1>
<div class="grid" id="grid"></div>
<script>
const ANIMS = {
  "待机 idle": ${JSON.stringify(idle)},
  "开心 happy": ${JSON.stringify(happy)},
  "难过 sad": ${JSON.stringify(sad)},
  "打招呼 wave": ${JSON.stringify(wave)}
};
for (const [name, data] of Object.entries(ANIMS)) {
  const card = document.createElement('div'); card.className = 'card';
  const box = document.createElement('div'); box.className = 'anim';
  const label = document.createElement('p'); label.textContent = name;
  card.appendChild(box); card.appendChild(label);
  document.getElementById('grid').appendChild(card);
  lottie.loadAnimation({ container: box, renderer: 'svg', loop: true, autoplay: true, animationData: data });
}
<\/script>
</body>
</html>`;
fs.writeFileSync(path.join(OUT, 'preview.html'), preview);
console.log('written preview.html');
