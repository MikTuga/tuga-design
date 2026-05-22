# Changelog

All notable changes to `tuga-design` are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.0] - 2026-05-22

### Added

Initial release as a standalone JitPack-publishable Android library, extracted from the Tuga monorepo.

**Design tokens**
- `Theme.TugaStore` base theme (extended by all 6 ecosystem apps)
- Dark palette: `bg_root` `#0B0F14`, `bg_surface` `#11161D`, `card_bg` `#1A2129`, `card_border` `#2A3340`
- Text: `text_primary` `#FFFFFF`, `text_secondary` `#B0BEC5`, `text_dim` `#78909C`
- Accents: `accent_blue` `#2196F3`, `accent_teal` `#26A69A`, `accent_purple` `#AB47BC`
- Status: `status_ok` `#4CAF50`, `status_warn` `#FFA726`, `status_error` `#EF5350`

**Drawables**
- `card_background.xml` — rounded card with subtle border
- `tile_background.xml` — large dashboard tile with elevation hint
- `icon_circle_blue/teal/purple.xml` — colored badge frames for activity icons
- `status_dot_green/orange/gray.xml` — indicator dots
- `button_filled_blue/teal/orange.xml` + `button_disabled.xml` — primary CTA backgrounds
- Common icons: `ic_storage`, `ic_android`, `ic_usb`, `ic_shield`, `ic_more_vert`

**Kotlin API**
- `com.miktuga.design.settings.TugaSetting<T>` — sealed class with typed keys (`UnitsSpeed`, `UnitsTemp`, `UnitsDistance`, `UsbMountPath`, `MusicFolder`, `ReportsFolder`, `AutoUpdateCheck`)
- `com.miktuga.design.settings.TugaSettingsClient` — ContentResolver wrapper, returns typed defaults if TugaSettings app not installed
- `com.miktuga.design.feedback.FeedbackPayload` + `FeedbackSubmitter` — HTTPS POST with offline `/sdcard` fallback queue
- `com.miktuga.design.feedback.FeedbackLauncher` — launches TugaStore's FeedbackActivity via explicit ComponentName with action intent fallback

### Compatibility

- minSdk 22, compileSdk 34, targetSdk 28
- Kotlin 1.9.22, AGP 8.2.2, Gradle 8.5
- Designed for **Geely Tugella head units** (Android 5.1, GMCustoms firmware). Do not bump SDK/Kotlin/Gradle.
