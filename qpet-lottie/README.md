# Q宠 Lottie 动画

根据参考图片（Q版短发女孩）用矢量图形重绘的宠物形象，附带 4 个动作动画。
所有文件均为标准 Lottie JSON，可直接用于 Web / Android / iOS / Flutter / 小程序。

## 文件说明

| 文件 | 动作 | 时长 | 建议用法 |
|---|---|---|---|
| `qpet-idle.json` | 待机：呼吸起伏 + 轻微摇头摆臂 + 眨眼 | 4s 循环 | 默认状态，`loop: true` |
| `qpet-happy.json` | 开心：下蹲蓄力 → 跳跃 + 双手举起 + 张嘴笑 | 2.6s | 喂食/互动成功时播放一次 |
| `qpet-sad.json` | 难过：低头缩肩 + 泪滴滑落 | 3.5s | 长时间未互动/失败时 |
| `qpet-wave.json` | 打招呼：右手挥动 + 歪头 + 眨眼 | 2.2s | 进入页面/点击宠物时 |
| `qpet.lottie` | 以上 4 个动作的 dotLottie 打包（zip 格式，含 manifest） | - | 配合 dotlottie 播放器按 id 切换动作 |
| `preview.html` | 预览页（JSON 已内联，双击即可在浏览器打开） | - | - |
| `gen.js` | 生成脚本，`node gen.js` 重新生成全部文件 | - | 调整造型/动作后重新生成 |
| `pack.js` | dotLottie 打包的 staging 脚本 | - | - |

## 接入示例

### Web (lottie-web)
```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/bodymovin/5.12.2/lottie.min.js"></script>
<div id="pet" style="width:200px;height:200px"></div>
<script>
const pet = lottie.loadAnimation({
  container: document.getElementById('pet'),
  renderer: 'svg', loop: true, autoplay: true,
  path: 'qpet-idle.json',
});
// 切换动作：销毁后重新加载，或用 dotLottie 播放器按 id 切换
</script>
```

### Android (lottie-android)
```kotlin
// build.gradle: implementation "com.airbnb.android:lottie:6.4.0"
lottieView.setAnimation("qpet-idle.json")   // 放到 assets 目录
lottieView.repeatCount = LottieDrawable.INFINITE
lottieView.playAnimation()
```

### iOS (lottie-ios)
```swift
let pet = LottieAnimationView(name: "qpet-idle")
pet.loopMode = .loop
pet.play()
```

### 微信小程序 (lottie-miniprogram)
```js
const lottie = require('lottie-miniprogram')
lottie.loadAnimation({ loop: true, autoplay: true, animationData: require('./qpet-idle.json'), rendererSettings: { context } })
```

## 状态机建议

```
进入页面 → wave 播 1 次 → idle 循环
用户互动(喂食等) → happy 播 1 次 → idle 循环
冷落超时 → sad 播 1 次 → idle 循环
```

## 关于 Rive

`.riv` 是 Rive 编辑器的二进制专有格式，无法脱离编辑器直接生成。
如需 Rive 版本（骨骼绑定 + 状态机混合更平滑），可以把参考图导入
[rive.app](https://rive.app) 编辑器手动绑定；本目录的 Lottie 文件已覆盖
相同的多平台运行时需求（Web/Android/iOS/Flutter 均有官方 Lottie 库）。

## 修改造型 / 动作

所有造型（颜色、五官位置）和动作关键帧都在 `gen.js` 中以常量/函数定义，
修改后执行：

```bash
node gen.js && node pack.js
```
