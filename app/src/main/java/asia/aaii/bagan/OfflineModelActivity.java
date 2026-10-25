package asia.aaii.bagan;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Toast;
public class OfflineModelActivity extends Activity {
 WebView web;
 @Override public void onCreate(Bundle s){super.onCreate(s);
  web=new WebView(this);web.setBackgroundColor(Color.rgb(9,35,41));
  web.setWebViewClient(new WebViewClient());
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setAllowFileAccess(true);
  web.getSettings().setAllowFileAccessFromFileURLs(true);
  web.getSettings().setAllowUniversalAccessFromFileURLs(false);
  web.getSettings().setDomStorageEnabled(true);
  setContentView(web);
  String page="file:///android_asset/viewer.html";
  if(getIntent().getBooleanExtra("camera",false)) Toast.makeText(this,"AR camera overlay preview only — not tracked AR",Toast.LENGTH_LONG).show();
  web.loadUrl(page);
 }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}