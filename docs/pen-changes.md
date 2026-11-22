# 点读笔适配改动（相对上游 cat3399/blbl 0.1.29）

上游基线：https://github.com/cat3399/blbl （0.1.29 源码 zip，commit 以仓库 main 为准）
改动原则：**不改变上游在大屏设备上的默认行为**，只在短边 < 720px 的小屏上生效。

## 一、代码改动（11 个文件）

| 文件 | 改动 |
| --- | --- |
| `app/src/main/java/blbl/cat3399/core/ui/UiScale.kt` | 新增常量 `SMALL_SCREEN_MAX_SHORT_SIDE_PX = 720f`、`SMALL_SCREEN_MIN_DENSITY = 1.0f` |
| `app/src/main/java/blbl/cat3399/core/ui/UiDensity.kt` | 密度计算加小屏兜底：`if (shortSidePx < 720) maxOf(targetDensity, 1.0f) else targetDensity`。原因：上游按 1920×1080@1.5 推算，258px 短边算出密度≈0.358，界面缩到 1/3 |
| `app/src/main/java/blbl/cat3399/feature/player/PlayerActivityVolume.kt`（新增） | `adjustMediaVolume(deltaSteps)`：调 `STREAM_MUSIC`、显示音量提示、重置自动隐藏计时；`installVolumeButton(button, delta)`：点击 ±1 级，长按 350ms 后每 120ms 连发 |
| `app/src/main/java/blbl/cat3399/feature/player/PlayerActivity.kt` | `AUTO_HIDE_MS` 4 秒 → 8 秒；`initControls()` 内绑定两个音量按钮 |
| `app/src/main/java/blbl/cat3399/feature/player/PlayerActivityTouch.kt` | ①`installVolumeTouchTargets()` 给真实按钮挂点击；②`volumeDeltaAt(event)`：在 `onTouch` 的 ACTION_DOWN 里用 `getLocationOnScreen` 换算"按钮相对手势层"的位置（容差 6px）判断落点；③命中时 `startVolumeHold(delta)` 并消费事件，长按每 120ms 连发；④远离按钮时行为与上游一致（单击=显示/隐藏控制栏） |
| `app/src/main/res/layout/activity_player.xml` | `controls_row` 的 `marginStart` 16dp → 0dp；新增 `btn_volume_down` / `btn_volume_up` 两个 ImageButton，放在 `btn_advanced`（设置齿轮）之前 |
| `app/src/main/res/layout/view_player_touch_overlay.xml` | 保持仅 `touch_gesture_layer` + `btn_touch_lock`（触摸转发由代码完成，不再用透明占位 View） |
| `app/src/main/res/drawable/ic_player_volume_down.xml`（新增） | 喇叭 + 减号矢量图标 |
| `app/src/main/res/drawable/ic_player_volume_up.xml`（新增） | 喇叭 + 加号矢量图标 |
| `app/src/main/res/values/strings.xml` | 新增 `player_btn_volume_down` / `player_btn_volume_up` / `player_touch_volume_fmt` |
| `app/src/main/java/blbl/cat3399/feature/settings/{SettingsConstants,SettingEntry,SettingsRenderer,SettingsInteractionHandler}.kt` | 新增 `PEN_PROJECT_URL`、`SettingId.PenProjectUrl`、"点读笔专用版地址"设置项与 `showUrlDialog`；原作者"项目地址"保留不动 |

## 二、构建配置改动（为在本机 SDK 上跑通）

| 文件 | 改动 |
| --- | --- |
| `app/build.gradle.kts` | `compileSdk` → 37，`buildToolsVersion = "36.0.0"` |
| `gradle.properties` | `android.suppressUnsupportedCompileSdk=36,37` |
| `gradle/wrapper/gradle-wrapper.properties` | 腾讯云镜像 `gradle-8.7-bin.zip`（原为 services.gradle.org） |
| `settings.gradle.kts` | 增加阿里云 maven 镜像（gradle-plugin / google / public） |

> 这些镜像改动只是国内网络加速，可按需回退成上游写法。

## 三、额外补充

- `tools/support/annotations.jar`：本机缺少的 `androidx.annotation 1.9.1` 编译依赖，补齐后才能编译。
- 首次换签名安装需卸载旧包；keystore 不入库。

## 四、已知限制

- 控制栏隐藏时，第一次点屏幕是"唤出控制栏"，第二次点音量键才调音量（这是播放器原有交互，未改动）。
- 音量键位置固定跟随控制栏（仅在控制栏可见时可点）。
- 上游"自动检查更新"逻辑仍请求上游仓库，点读笔版的更新请以本仓库 Release 为准。
