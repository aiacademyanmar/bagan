# Bagan Heritage Android

Native Android project for the Bagan Heritage Guide with 20 UI/UX screens, 30 pagodas, Google Maps directions, and offline Bupaya 3D.

## Build status

The repository is initialized, but **the full Android source and 3D/UI assets have not yet been uploaded**. Do not treat this as a compiled APK or release.

## Upload and build

1. Download the prepared `Bagan_Heritage_GitHub_Actions_Ready_V3_2.zip` from the ChatGPT conversation.
2. Extract the archive and upload its project contents to this repository (the contents, not the ZIP itself). Use Git for its binary PNG and GLB assets.
3. Ensure `.github/workflows/android-build.yml`, `settings.gradle`, `build.gradle`, `app/` and `verify_source.py` are at the repository root.
4. Push to `main`. The GitHub Actions debug APK workflow starts on push, or run it manually from the Actions tab.
5. Download the APK from the workflow run's Artifacts section only after the build succeeds.

**Note:** True ARCore spatial tracking is not implemented in this build; AR is a camera overlay preview.
