# tuga-design

Shared Android library for the **Tuga ecosystem** — utilities for Geely Tugella head units on GMCustoms firmware (Android 5.1, API 22).

Provides:
- **Design tokens** — dark theme palette (`colors.xml`), `Theme.TugaStore` base style
- **Drawable kit** — card backgrounds, status dots, filled buttons, icon-circle frames, common icons (USB, storage, Android, shield)
- **`TugaSetting` schema** — sealed class with typed settings keys (units, paths, auto-update flags)
- **`TugaSettingsClient`** — ContentResolver wrapper for reading/writing settings via `content://com.miktuga.settings.provider`
- **`FeedbackSubmitter`** + **`FeedbackLauncher`** — common feedback flow (HTTPS POST with offline JSON queue)

## Install via JitPack

In your app `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

In your app `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.MikTuga:tuga-design:0.1.0")
}
```

## Usage

### Reading shared settings (units, paths)

```kotlin
import com.miktuga.design.settings.TugaSettingsClient
import com.miktuga.design.settings.TugaSetting

val speedUnit = TugaSettingsClient.get(context, TugaSetting.UnitsSpeed)
// returns UnitsSpeedValue.KMH or UnitsSpeedValue.MPH
```

Requires consumer manifest to declare:

```xml
<uses-permission android:name="com.miktuga.permission.READ_SETTINGS" />
<uses-permission android:name="com.miktuga.permission.WRITE_SETTINGS" />
```

TugaSettings app must be installed for cross-app settings to work; otherwise `get()` returns the default.

### Sending feedback

```kotlin
import com.miktuga.design.feedback.FeedbackSubmitter

FeedbackSubmitter.submit(context, FeedbackPayload(...))
// Tries HTTPS POST to miktuga.ru/api/feedback, falls back to /sdcard queue
```

### Applying the base theme

```xml
<application android:theme="@style/Theme.TugaStore">
```

Each app can extend `Theme.TugaStore.Base` with overrides.

## Compatibility

- **min SDK**: 22 (Android 5.1 Lollipop)
- **compile SDK**: 34
- **target SDK**: 28
- **Kotlin**: 1.9.22
- **AGP**: 8.2.2

Do not bump these — Tuga ecosystem targets Geely Tugella head units which are locked to Android 5.1.

## Consumer apps in the Tuga ecosystem

- [tugastore](https://github.com/MikTuga/tugastore) — app store + diagnostics + feedback host
- [tugasettings](https://github.com/MikTuga/tugasettings) — settings storage + ContentProvider
- [tugaobd](https://github.com/MikTuga/tugaobd) — OBD-II dashboard
- [tugagps](https://github.com/MikTuga/tugagps) — GPS + compass
- [tugamedia](https://github.com/MikTuga/tugamedia) — USB media scanner
- [tugasync](https://github.com/MikTuga/tugasync) — USB ↔ device file sync

## License

MIT — see [LICENSE](LICENSE).
