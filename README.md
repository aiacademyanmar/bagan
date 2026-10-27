# Bagan Museum V1.5 — Drive Heritage Library
Version 1.5.0, branch `bagan-museum-native-v1-5`. V1.4 and repository main remain unchanged.

## Source
Authenticated Google Drive folder: https://drive.google.com/drive/folders/1PuQyz1T8T5s2bihHqKdpsLhbyARGGa6_

Snapshot catalog of 10 Drive subfolders:
Thatbyinnyu, Myazedi, Bagan Gateway, Tharabar Gate, Gawdawpalin, Ananda, Htilominlo, Alodawpyae, Shwezigon, Bupaya.

The app bundles **only metadata** and uses Android implicit intents to open the original private Drive resources with the user's existing Google authentication. Access permissions apply. It does **not** copy private Drive pictures, Blender models or ZIPs into public GitHub or APK.

## Features
- Native Kotlin/Compose monument and artifact catalog, category search, Burmese/English descriptions, link to Blender source and Drive preview.
- Locally generated QR code for each folder using ZXing, plus native Android link sharing.
- Offline six-question museum and file-format learning quiz with stored best score.
- All previous 3D concept model, native ARCore, guided tour and TTS features retained.

## Limits
- Online access is needed to open Drive previews or source models; Drive files can be restricted.
- The source `.blend` files **are not yet automatically converted** to GLB or rendered as separate mobile 3D exhibits.
- The offline 3D museum is still the original stylized V1.4 concept model.
- The quiz is illustrative training content, not an official examination.
- Must test Google Drive account access, ARCore runtime, QR scanning and device UI on an actual Android phone.

## Build
GitHub Actions workflow: Build Bagan Museum Native APK V1.5. Artifact contains debug APK, offline museum GLB and manifest JSON.