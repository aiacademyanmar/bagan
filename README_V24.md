# Bagan Museum Native V2.4
- Offline Photo Gallery uses cached Wikimedia photo thumbnails where available, otherwise labeled concept drawings. Original image licenses link back to Commons.
- Museum virtual tour progress is manually marked for the ten monument learning stops, persisted in local Android SharedPreferences. It does **not** verify physical visits.
- Samsung / Android runtime test checklist can be completed by the tester on their own device; it is not a claim that device QA has been run.
- 3D viewer now reuses a WebView across selected monuments, releases its resources on disposal, and stops drawing when its page is hidden.
- 10 generated GLB concept models continue in the offline APK. Original user Blender files were not converted. AR functionality remains device-dependent and untested physically.
