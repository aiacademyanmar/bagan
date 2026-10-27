package asia.aaii.baganmuseum

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HeritageOffline3DV16(monumentIndex:Int=0){
    Text("Bagan Heritage 3D • 10 Monuments",color=Color(0xFFFFE8CB),fontSize=21.sp,fontWeight=FontWeight.Bold)
    Text("Offline original concept GLBs • Swipe to rotate / pinch to zoom",color=Color(0xFFC9B7A5),fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    key(Unit){
        AndroidView(factory={ctx ->
            WebView(ctx).apply{
                settings.javaScriptEnabled=true
                settings.allowFileAccess=true
                settings.allowContentAccess=false
                settings.domStorageEnabled=false
                setBackgroundColor(android.graphics.Color.rgb(23,15,12))
                webViewClient=object:WebViewClient(){
                    override fun onPageFinished(view:WebView?,url:String?){
                        view?.evaluateJavascript("selectMonument(${monumentIndex.coerceIn(0,9)})",null)
                    }
                }
                loadUrl("file:///android_asset/heritage_viewer_v16.html")
            }
        },update={v->v.evaluateJavascript("selectMonument(${monumentIndex.coerceIn(0,9)})",null)},
          modifier=Modifier.fillMaxWidth().height(590.dp),
          onRelease={v->v.stopLoading();v.loadUrl("about:blank");v.destroy()})
    }
    Spacer(Modifier.height(10.dp))
    Text("Concept 3D models are included in the APK. Drive .blend files have not yet been converted; those original files remain in your Drive folders.",color=Color(0xFFC9B7A5),fontSize=12.sp)
}
