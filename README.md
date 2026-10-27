# Bagan Museum Native UI V1.8

## UI/UX Refinement
The 21-screen Bagan Museum Native Android Kotlin / Jetpack Compose project is based on the prior full-HD mockup design directions (cream, dark brown, gold, rich panels).

V1.8 UI updates:
- Home: illustrated heritage hero, six quick action tiles, three featured monument cards and learning module.
- Gallery: accessible search, category chips, illustrated image cards and live favorite toggles.
- Monument detail: large illustrated hero, topic metadata, offline 3D/AR/audio/Drive source routes.
- Consistent five-tab bottom navigation with active-state highlighting and a polished dark brown header.
- Offline 3D: actual previously generated ten GLB concept models plus AR/Info shortcuts.
- Audio: native Android TextToSpeech guide with English/Myanmar switch, 5 chapters, previous/next, play/stop, optional slower speech.
- Certificate: two-tier decorative border, live visitor name and saved best quiz score, share text action.
- Previously compiled V1.7 screens retained for Map, Indoor Navigation, Collections, History, Quiz, Favorites, Search, Settings, Profile, Menu and Offline Learning.
- Fix source menu 'Drive Library' routing to native Google Drive catalog.
- Preserve optional ARCore camera and illustrative full-museum GLB placement, subject to phone-specific compatibility.

## Known limitations
This is real native code (not a screenshot WebView), but not a pixel-perfect redraw of each mockup. The primary architectural images are illustrative placeholders, not certified photos.
Offline ten monument GLBs are concept geometry, **not exports** from the Drive Blender source files. AR real-phone tracking not tested. A display certificate is not an official credential. Audio works only if supported voices are installed.
The app still needs usability/visual QA on real Android devices and professional release signing.

Build on branch `bagan-museum-native-v1-8-refinement`.
