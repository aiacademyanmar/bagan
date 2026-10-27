# Bagan Museum V3.0
Based on V2.9 Google Drive GLB import and ARCore integration.
- 3D Gallery: reads original glTF2 GLBs from the 13-model user imported ZIP. Adds embedded PNG/JPEG color texture decode and UV mapping, directional diffuse shading with normals, GPU texture size cap (1024 px), bounded ~180k triangles, chunked loader with progress, GPU buffers and object URLs cleanup on pagehide, WebGL context-lost notice, 2-finger pinch zoom and rotate.
- Source asset inventory confirms embedded textures in only Myazedi Stone Inscription (4) and Bagan Entrance Gateway (1). The other original GLBs contain material color data, not actual embedded photographic textures. No unsupported photos are invented.
- Native ARCore scene draws the original imported GLB geometry in device camera space with directional color tint. GLES2 native AR renderer still does not display embedded JPG/PNG textures and does not implement full physically based rendering. Triangle sample limit lowered to 90k.
- Models remain outside the APK in the separately imported GLB ZIP. Existing 30-site bilingual content, quizzes, progress and offline concept GLBs retained.
- Build validation is compile/static only; test on real Samsung Android device needed. Native 3D and AR output may still have model-dependent orientation, shading and performance differences.
