<?php
declare(strict_types=1);
// Deploy beside v3.4/models.catalog.json as /v3.4/api/model.php.
// GOOGLE_DRIVE_API_KEY must be configured in the server environment, NOT in GitHub.
header('X-Content-Type-Options: nosniff');
$catalog = json_decode(file_get_contents(dirname(__DIR__).'/models.catalog.json'), true);
$id = $_GET['id'] ?? '';
if (!is_string($id) || !preg_match('/^[a-z0-9-]{1,64}$/', $id)) { http_response_code(400); exit('Invalid model ID'); }
$model = null;
foreach (($catalog['models'] ?? []) as $candidate) { if ($candidate['id'] === $id) { $model = $candidate; break; } }
if ($model === null) { http_response_code(404); exit('Model not found'); }
$key = getenv('GOOGLE_DRIVE_API_KEY');
if (!$key) { http_response_code(503); exit('Google Drive API not configured'); }
$cacheDir = dirname(__DIR__).'/private-cache';
if (!is_dir($cacheDir) && !mkdir($cacheDir, 0700, true) && !is_dir($cacheDir)) { http_response_code(500); exit('Cache unavailable'); }
$cacheFile = $cacheDir.'/'.$id.'.glb';
if (!is_file($cacheFile) || filesize($cacheFile) === 0) {
    $url = 'https://www.googleapis.com/drive/v3/files/'.rawurlencode($model['drive_file_id']).'?alt=media&key='.rawurlencode($key);
    $tmp = tempnam($cacheDir, 'glb');
    $stream = fopen($tmp, 'wb');
    $ch = curl_init($url);
    curl_setopt_array($ch, [CURLOPT_FILE=>$stream,CURLOPT_FOLLOWLOCATION=>false,CURLOPT_CONNECTTIMEOUT=>10,CURLOPT_TIMEOUT=>120,CURLOPT_PROTOCOLS=>CURLPROTO_HTTPS]);
    $success = curl_exec($ch);
    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    fclose($stream);
    $head = file_get_contents($tmp, false, null, 0, 4);
    if (!$success || $status !== 200 || $head !== 'glTF') {
        unlink($tmp);
        http_response_code(502);
        exit('Drive file not downloadable. Check sharing/API permission.');
    }
    rename($tmp, $cacheFile);
}
header('Content-Type: model/gltf-binary');
header('Cache-Control: public, max-age=3600');
header('Content-Length: '.filesize($cacheFile));
readfile($cacheFile);
