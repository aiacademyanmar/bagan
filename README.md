# Bagan Museum — Native Android V1.2
Branch: `bagan-museum-native-v1-2` (keeps `main` and V1.1 unchanged).

## Features
- Offline-friendly native Kotlin / Jetpack Compose UI; illustrated museum building hero and icon.
- Galleries, favorites, history, floor plan, five-stop guided tour inherited from V1.1.
- GLB 2.0 concept model generated at build time (tools/generate_glb.py), embedded into the offline WebGL renderer.
- Exterior orbit (drag), zoom / pinch, and interior cutaway camera mode.
- Native Android TextToSpeech guide in English and Burmese (Burmese voice availability depends on device).

## Limitations
Museum map and exhibits are illustrative, not a verified physical museum floor plan or collections.
No actual ARCore world-tracking in this release, nor real-world navigation.
This is a concept GLB model, not a reconstruction of an existing temple or a surveyed museum.

## Build
Open GitHub Actions > Build Bagan Museum Native APK V1.2; use the build on this branch.
Successful artifact includes `app-debug.apk` and `bagan_museum_concept.glb`.
Works with Android API 26+ when suitable WebGL and TTS support are present.
