# Mirror a desktop display

The mirror source picker lists the inner screen and currently available presentation displays (including scrcpy's desktop). Both capture and live Shizuku touch target the selected display ID. IDs are discovered each time; they are not persisted. The cover and OpenCover's own capture displays are excluded.

Choose Mirror, choose Desktop and the desired source, then select full-screen sharing in Android's screen-capture prompt. Shizuku must be running and already authorized. The desktop must already exist: this feature mirrors it, it does not create a standalone desktop or enable USB-C video output. Closing the source ends the session. A failure never falls back to the inner screen.

For an external source the service leaves the physical fold state, inner-screen resolution, developer settings and accessibility settings alone. Existing inner-screen mirroring retains its prior behavior. Capture is attached to the view only after the source switch succeeds. Android's protected content can remain black.

The source switch uses shell-level MANAGE_MEDIA_PROJECTION through a session-scoped Shizuku UserService, restricted to this package's active consented projection and its own OpenCover-mirror destination. AOSP ignores replacement sessions on the same virtual display, so the helper detaches and reattaches the recording session. It uses hidden Android APIs and must be verified on each supported One UI version.

Tests cover source filtering and a disappeared source; existing mirror geometry tests cover touch conversion and rotation. Device verification is required for capture, live touch, stopping, and regression of inner-screen mirroring.

## Local validation (2026-11-10)

A debug APK version 0.2.1 (versionCode 3) was signed with the user-provided matching key and installed as an in-place update; no app data was cleared. All 291 unit tests passed, with zero failures/errors/skips. Source selection was observed on the cover. Device verified while physically CLOSED: desktop display 364 was captured to mirror display 369; Calculator and taskbar appeared on the cover; a cover tap entered 7 on Calculator; cover taps maximized to [0,0,1920,1020] then restored [348,94,1428,914] exactly. Source-loss cleanup, rotations during desktop mirroring, and reboot are not yet verified.

The checkout's SDK 37 toolchain was unavailable locally. Validation used a separate copy with SDK 36, JDK 21, Gradle 8.14.3, AGP 8.13.0, Kotlin Android/Compose plugins 2.2.20, Java/Kotlin target 17, Compose BOM 2025.09.00, activity-compose 1.11.0 and test JSON 20250517; other dependencies and application sources were unchanged. Original repository toolchain settings are preserved.

The UserService receives the exact MediaProjection binder from the current consent result; it does not create a new projection or attempt to reuse consent through getProjection. It verifies the current projection belongs to this package and the destination virtual display is also owned by this package. The Binder is discarded and the helper unbound when the session stops.

For the current test, keep the computer window “Z Flip5 Desktop” open: it owns the virtual desktop source. Connecting a physical USB-C monitor and running without the computer still require separate hardware verification. The test does not establish standalone USB-C operation.
