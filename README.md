# KidMathLocker 🧠📱

**KidMathLocker** is a lightweight parental control and screen-time management Android application. It gates access to selected games and recreational apps behind dynamic Grade-3 mental math challenges. 

Whenever a locked app is launched or its allowed session expires, the app draws an interactive math challenge directly over the screen. Answering correctly grants seamless gameplay for a parent-configured duration (default: 15 minutes).

---

## 🌟 Key Features

* **Smart App Interception:** Detects foreground launches of target games using Android's native `UsageStatsManager` and overlays a challenge screen before the app can be played.
* **Per-App Granular Locking:** Dedicated management dashboard with clean toggle switches to select exactly which apps/games should be locked.
* **Configurable Session Timer:** Parents can adjust the granted gameplay duration (e.g., 5 min, 15 min, 30 min) directly from the dashboard stepper.
* **Rotation & Landscape Optimized:** Seamlessly supports landscape games without forcing unwanted portrait orientation or regenerating/resetting the math equation mid-session.
* **Mid-Game Re-locking:** Continuously tracks session expiration in the background; when time runs out while a child is actively playing, the lock automatically reappears.
* **Exit Detection:** Leaving a locked game to the home screen immediately invalidates remaining session time, requiring a fresh question upon return.
* **Zero External Dependencies / 100% Offline:** No cloud trackers, accounts, or third-party SDKs. All operations run locally on the device with zero network footprint.

---

## 📱 Compatibility & Tech Stack

* **Platform:** Android
* **Target & Tested Version:** Android 7.1.1 Nougat (API Level 25)
* **Minimum SDK:** API Level 21 (Android 5.0 Lollipop)
* **Language:** Kotlin
* **UI Architecture:** Android Views with XML (Palette C: Clean Minimalist Slate theme)
* **Core APIs:** `UsageStatsManager`, `SYSTEM_ALERT_WINDOW`, `Foreground Service`, `SharedPreferences`

---

## 🔐 Required Device Permissions

To function as a device app locker, the app requires two privileged system permissions that must be manually granted upon first launch:

1. **Display Over Other Apps (`android.permission.SYSTEM_ALERT_WINDOW`):** Allows rendering the math challenge dialog above the target game.
2. **Usage Data Access (`android.permission.PACKAGE_USAGE_STATS`):** Allows the background monitor to identify when a locked package enters the foreground.

*Note: The main dashboard provides direct shortcut buttons to toggle and manage these permissions within the device's system settings.*

---

## 🛠️ Project Structure

```text
com.example.kidmathlocker/
├── MainActivity.kt        # Parent dashboard (service status, permissions, timer config)
├── AppSelectActivity.kt   # App picker list with toggle switches
├── MathLockActivity.kt    # Non-scrolling math challenge overlay (portrait & landscape)
├── AppMonitorService.kt   # Background foreground-polling service
├── AppAdapter.kt          # RecyclerView adapter for installed applications
└── AppItem.kt             # Data model for app items
