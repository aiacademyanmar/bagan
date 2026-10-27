# BAGAN V3.4 Model Integration
Verified Google Drive folder: 1d9vs81uKgfKc9xcfaTmIBnN5J0-o2nCG
13 GLB IDs indexed in models.catalog.json.

## Deployment
1. Deploy v3.4 as the web root or adjust path appropriately.
2. Set GOOGLE_DRIVE_API_KEY as a server environment variable. Do NOT put it in source files.
3. Turn on the Google Drive API in the Cloud project and restrict the new key to Google Drive API / server IP, rotate the previously exposed key.
4. Models must be publicly readable for API-key access. Private models require OAuth/service account and cannot be accessed by API key alone.
5. Request /v3.4/api/model.php?id=ananda and verify binary GLB response.
6. Point the WebGL model viewer or Android model cache at the server endpoint.
7. Confirm ARCore/device support separately. External Scene Viewer is not embedded ARCore.

Security: protect the private-cache directory from direct HTTP access using web-server rules, or relocate it outside the web root. Implement rate limiting for public API use. This endpoint is NOT live-deployed or tested against the Drive API.
