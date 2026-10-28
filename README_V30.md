# Bagan Museum V3.0 (Drive 3D + AR refinement)

- Based on V2.9; users can import the original 13 supplied Drive GLB files via ZIP.
- 3D Gallery opens imported GLB geometry with touch rotate and zoom.
- ARCore places imported original Drive GLB geometry on a detected plane, with size, rotation and new Light+/Light- material brightness controls.
- Added WebGL context loss status message to the model preview.
- GPU triangle sampling and local cache keep mobile payload manageable.
- **Limitations:** Original GLB texture maps and physically based material rendering are not yet supported in these custom GLES/WebGL viewers. Light controls adjust base material brightness, not physically correct AR environmental lighting. On-device Samsung AR performance and complex file rendering remain untested. Do not confuse Compile PASS with device QA. Original GLBs are not baked into the APK; ZIP import remains required.
