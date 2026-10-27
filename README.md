# Bagan Museum Native Android V1.4

**Branch:** `bagan-museum-native-v1-4`. Retains previous versions on their branches and does not overwrite the existing main application.

## Shipped functionality
- Native Kotlin / Jetpack Compose Bagan Museum mobile app, sample galleries, history, favorites and guided tour.
- Offline 3D museum viewer (GLB), conceptual walkthrough, interior cutaway and five heritage information hotspots.
- **Full GLB placement in optional ARCore native camera**. AR renderer now loads `bagan_museum_concept.glb` from assets and draws all GLB mesh primitives as one miniature museum at a tracked plane anchor (solid colors, no advanced PBR). If asset fails to load a simplified fallback monument renders instead.
- **Four WebGL artifact models** (clay pot, inscription tablet, temple finial, heritage column), rotation/zoom and bilingual explanatory popups.
- **Five-room concept navigation** with interactive room selection, previous/next buttons, and saved route progress.
- Text-to-Speech guides inherited from V1.3. Requires a compatible installed TTS voice.
- Artifacts, architecture and room mapping are creative educational concepts, **not a photographed or scientifically surveyed museum**, not actual archived artifacts and **not live indoor positioning**.

## APK
GitHub Actions > Build Bagan Museum Native APK V1.4 (branch).
Produced artifact includes debug APK, GLB and offline HTML files. Android 8.0+; AR requires supported hardware, Google Play Services for AR and camera permissions. AR Runtime must be verified on a supported physical Samsung/Android device.

Production signing, Play Store release, official museum content, accessibility wayfinding, real GPS and complete PBR AR are future milestones.