# Bagan Museum Native Android V1.3
Branch: `bagan-museum-native-v1-3` — isolated from main and V1.2.

- Bagan-themed offline Kotlin/Compose UI, gallery search/category, favorites, timeline and museum guided tour.
- Offline procedural GLB building; exterior, interior cutaway, five hotspots and concept walk navigation.
- Native English/Burmese Text-to-Speech with eight narration topics (language availability depends on phone).
- Optional native ARCore camera placement: detect a floor and place an anchored procedural miniature temple marker. This does not AR-render the full GLB museum.
- Camera permission requested on AR entry; ARCore service availability/device compatibility required.
- Offline 3D remains available when AR is unavailable.

Debug build: GitHub Actions > Build Bagan Museum Native APK V1.3. Artifact bundles APK, GLB, offline HTML viewer.
Prototype only. No real surveyed building, official museum collection, real indoor navigation, GPS maps or signed production app. AR real-device performance is unverified until tested on compatible phone.