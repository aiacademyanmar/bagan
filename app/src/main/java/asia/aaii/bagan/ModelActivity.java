package asia.aaii.bagan;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.widget.Toast;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class ModelActivity extends Activity {
  private static final String HOST = "bagan.local";
  private WebView web;

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    web = new WebView(this);
    web.setBackgroundColor(0xff0e252b);
    WebSettings cfg = web.getSettings();
    cfg.setJavaScriptEnabled(true);
    cfg.setDomStorageEnabled(true);
    cfg.setAllowFileAccess(false);
    cfg.setAllowContentAccess(false);
    cfg.setMediaPlaybackRequiresUserGesture(false);
    web.setWebChromeClient(new WebChromeClient());
    web.setWebViewClient(new WebViewClient(){
      @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request){
        return intercept(request == null || request.getUrl() == null ? null : request.getUrl().toString());
      }
      @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url){
        return intercept(url);
      }
      @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error){
        if (Build.VERSION.SDK_INT >= 23 && request != null && request.isForMainFrame()) {
          Toast.makeText(ModelActivity.this,"3D Viewer load error: "+error.getDescription(),Toast.LENGTH_LONG).show();
        }
      }
    });
    setContentView(web);
    web.loadUrl("https://bagan.local/viewer.html");
  }

  private WebResourceResponse intercept(String raw){
    if(raw==null) return null;
    try {
      Uri uri=Uri.parse(raw);
      if(!"https".equalsIgnoreCase(uri.getScheme()) || !HOST.equalsIgnoreCase(uri.getHost())) return null;
      String path=uri.getPath();
      String asset;
      String mime;
      if("/viewer.html".equals(path)){asset="viewer.html";mime="text/html";}
      else if("/bupaya_offline.glb".equals(path)){asset="bupaya_offline.glb";mime="model/gltf-binary";}
      else return new WebResourceResponse("text/plain", "UTF-8",
          new java.io.ByteArrayInputStream(new byte[0]));
      InputStream stream=getAssets().open(asset);
      WebResourceResponse response=new WebResourceResponse(mime, "text/html".equals(mime)?"UTF-8":null,stream);
      if(Build.VERSION.SDK_INT>=21){
        Map<String,String> headers=new HashMap<>();
        headers.put("Access-Control-Allow-Origin","https://bagan.local");
        headers.put("Cache-Control","no-store");
        response.setResponseHeaders(headers);
      }
      return response;
    } catch (Exception failure){
      return null;
    }
  }
  @Override public void onBackPressed(){
    if(web != null && web.canGoBack()) web.goBack();
    else super.onBackPressed();
  }
  @Override protected void onDestroy(){
    if(web!=null){web.stopLoading();web.destroy();web=null;}
    super.onDestroy();
  }
}