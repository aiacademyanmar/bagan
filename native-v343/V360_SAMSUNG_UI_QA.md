# BAGAN Heritage Museum V3.6.0 — Samsung Native QA Checklist

Reference designs: Google Drive folder 18hy4kto05d9Ns_CjojSz5zOz7Mic10c0, UI/UX 20-screen concept pack (1080 x 1920 images). These images include phone frame and baked-in lettering and are NOT directly suitable as full-screen native backgrounds.

## Current verified scope
- Kotlin source updated: minimum 48dp button targets, Android back-to-home, shared-preference language selection, 20-second 3D loading timeout.
- NativeARActivity updated: ARCore unsupported/transient availability states, local GLB path existence check and tracking-anchor exception handling.
- Version: 3.6.0 / versionCode 360.
- GitHub Actions auto-build triggered; a successful V3.6.0 APK and device QA are not claimed until confirmed.

## Required manual Samsung regression checks
1. First launch: Splash, onboarding, language selection and Home; reopen to verify onboarding persistence.
2. System status/navigation bars, gesture nav, cutout, small/large font, One UI light/dark modes.
3. Search Myanmar Unicode names, open temple details, add/remove favorites.
4. All 20 reference designs: compare screenshots by manually aligned visible viewport; do not count route number as screenshot number.
5. 3D normal GLB, missing GLB, offline demo, timeout, rotate/zoom, background/restore.
6. AR on supported and unsupported Samsung phones, permission allow/deny, install service missing, move to background, return to app.
7. Maps with and without Google Maps/browser available; offline error messaging.
8. Scroll and touch targets in bottom navigation, profile, help, settings, and exit confirmation.

## Outstanding
- Pixel-exact native screen layout matching the original 20 visual references is NOT finished.
- The 20 screenshot labels do not map 1-to-1 onto internal app route IDs; a screen-index remapping and visual regression captures remain to be implemented.
- Myanmar text baked into the generated concept PNGs should be replaced with verified editable native Unicode UI copy.
- ARCore device hardware validation and frame-by-frame 3D rendering tests have NOT been run.
