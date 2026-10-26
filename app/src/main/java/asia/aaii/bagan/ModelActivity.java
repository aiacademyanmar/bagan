package asia.aaii.bagan;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import java.io.InputStream;
public class ModelActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state){super.onCreate(state);
   web=new WebView(this);web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,String url){
     try{
       Uri u=Uri.parse(url);
       if(!"https".equals(u.getScheme())||!"bagan.local".equals(u.getHost()))return null;
       String p=u.getPath();
       if(p==null)return null;
       if(p.equals("/viewer.html"))return new WebResourceResponse("text/html","UTF-8",getAssets().open("viewer.html"));
       if(p.equals("/bupaya_offline.glb"))return new WebResourceResponse("model/gltf-binary",null,getAssets().open("bupaya_offline.glb"));
     }catch(Exception ex){}return null;
   }});
   web.setBackgroundColor(0xff0e252b);
   WebSettings settings=web.getSettings();
   settings.setJavaScriptEnabled(true);
   settings.setDomStorageEnabled(true);
   settings.setAllowFileAccess(false);
   settings.setAllowContentAccess(false);
   setContentView(web);
   web.loadUrl("https://bagan.local/viewer.html");
 }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}