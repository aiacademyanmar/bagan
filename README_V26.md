# Bagan Museum Native V2.6

Android Kotlin / Jetpack Compose app building on V2.5, branch `bagan-museum-native-v2-6`.

## V2.6 changes
- Crash recovery gate on the **next launch** after a locally recorded unhandled exception, offering optional Safe Mode and normal launch. Safe Mode suppresses the AR/3D screens. This is not automatic in-process crash repair. No telemetry or background uploads.
- Offline Learning Packages: choose All / Temple / Pagoda / Gate / Inscription and export actual packaged GLB educational concept models + manifest to a ZIP using Android's document picker. Includes simple header validation. Does not install model packages back into the app, because 10 models already come bundled.
- 3D viewer performance samples: approximate on-device WebGL FPS, triangle count and model-load duration recorded in app-private preferences; refresh, view and clear. No third-party analytics. Lower page resolution / battery saver / device heat may influence samples.
- Install/Update Center displays installed package version and signing status (debug vs release), checks the latest published GitHub release and links to Releases / Actions. **No silent self-update**; GitHub Actions artifacts are development builds, not published releases.
- V2.5 Offline Photo Cache, audio completion, learning achievements, maps, GLB viewer and museum education content retained.

## Known limitations
The bundled ten GLBs are *illustrative generated concepts* and not the original source Blender scenes.
No claim of real Samsung device runtime test: GitHub Actions only validates compilation and asset integrity.
ARCore camera operation and speech voices remain device-specific. Installability depends on compatible package signature; release builds require proper signing.
