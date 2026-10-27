package asia.aaii.baganmuseum

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val cream14=Color(0xFFFFE8CB)
private val gold14=Color(0xFFF3BD69)
private val muted14=Color(0xFFC9B7A5)
private val panel14=Color(0xFF302018)

@Composable
fun ArtifactCollectionV14() {
    Text("3D Artifact Collection",color=cream14,fontWeight=FontWeight.Bold,fontSize=23.sp)
    Spacer(Modifier.height(6.dp))
    Text("Rotate four locally generated 3D teaching models; tap each object for Myanmar and English information.",color=muted14,fontSize=12.sp)
    Spacer(Modifier.height(14.dp))
    AndroidView(
        factory={context -> WebView(context).apply {
            settings.javaScriptEnabled=true
            settings.allowFileAccess=true
            settings.allowContentAccess=false
            settings.domStorageEnabled=false
            webViewClient=WebViewClient()
            setBackgroundColor(android.graphics.Color.rgb(24,15,13))
            loadUrl("file:///android_asset/artifacts.html")
        }},
        modifier=Modifier.fillMaxWidth().height(585.dp)
    )
    Spacer(Modifier.height(12.dp))
    Text("These are original illustrative models — not scans or verified museum collection objects.",color=muted14,fontSize=12.sp)
}
private data class MuseumRoom(val name:String,val description:String,val action:String)
private val rooms14=listOf(
    MuseumRoom("Entrance & Welcome Hall","ပုဂံအမွေအနှစ်များကို မိတ်ဆက်ပေးသော အဓိက ဝင်ပေါက်ခန်း","Explore museum overview and introduction"),
    MuseumRoom("History Gallery","ပုဂံခေတ် သမိုင်းမှတ်တမ်းနှင့် ကျောက်စာဆိုင်ရာ အကြောင်းအရာများ","History exhibits and inscriptions"),
    MuseumRoom("Art & Mural Gallery","နံရံဆေးရေးပန်းချီနှင့် မြန်မာ့အနုပညာလက်ရာများ","Paintings and artistic heritage"),
    MuseumRoom("Architecture & 3D Hall","ဘုရားပုထိုး ဗိသုကာ၊ အုတ်စီပညာနှင့် 3D မော်ဒယ်များ","Architectural forms and building study"),
    MuseumRoom("Conservation & Exit","အမွေအနှစ်ထိန်းသိမ်းရေး၊ ပြခန်းပြီးဆုံးမှုနှင့် အထွက်","Heritage preservation and completion")
)
@Composable
fun MuseumIndoorNavigatorV14(onExplore3d:()->Unit,onArtifact:()->Unit) {
    val context=LocalContext.current
    val prefs=remember {context.getSharedPreferences("bagan_v14_route",0)}
    var stop by remember {mutableIntStateOf(prefs.getInt("stop",0).coerceIn(0,4))}
    fun setStop(i:Int){
        stop=i.coerceIn(0,4)
        prefs.edit().putInt("stop",stop).apply()
    }
    Text("Museum Indoor Navigation",color=cream14,fontWeight=FontWeight.Bold,fontSize=23.sp)
    Text("Concept route • 5 connected virtual rooms",color=muted14,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    LinearProgressIndicator(progress={(stop+1)/rooms14.size.toFloat()},modifier=Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Column(Modifier.fillMaxWidth().background(panel14,RoundedCornerShape(18.dp)).padding(14.dp)) {
        Text("ENTRANCE ↓",color=gold14,fontSize=12.sp)
        rooms14.forEachIndexed { i,room ->
            Surface(
                modifier=Modifier.fillMaxWidth().padding(vertical=3.dp).clickable {setStop(i)},
                color=if(stop==i) Color(0xFF89502F) else Color(0xFF513626),
                shape=RoundedCornerShape(10.dp)
            ) {
                Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text((i+1).toString(),color=gold14,fontWeight=FontWeight.Black)
                    Spacer(Modifier.width(12.dp))
                    Text(room.name,color=cream14,fontSize=13.sp,modifier=Modifier.weight(1f))
                    if(stop==i) Text("●",color=gold14)
                }
            }
        }
        Text("↓ EXIT",color=gold14,fontSize=12.sp)
    }
    Spacer(Modifier.height(14.dp))
    Text("Stop "+(stop+1)+" / 5 — "+rooms14[stop].name,color=gold14,fontWeight=FontWeight.Bold)
    Text(rooms14[stop].description,color=cream14)
    Text(rooms14[stop].action,color=muted14,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
        OutlinedButton(onClick={setStop(stop-1)},enabled=stop>0,modifier=Modifier.weight(1f)){Text("Previous")}
        Button(onClick={setStop(stop+1)},enabled=stop<4,modifier=Modifier.weight(1f)){Text("Next Room")}
    }
    Spacer(Modifier.height(10.dp))
    OutlinedButton(onClick=onExplore3d,modifier=Modifier.fillMaxWidth()) {Text("Open 3D Walkthrough")}
    OutlinedButton(onClick=onArtifact,modifier=Modifier.fillMaxWidth()) {Text("Explore 3D Artifacts")}
    Text("Saved locally. This is a conceptual tour route, not real-time indoor positioning, GPS directions or accessibility-certified wayfinding.",color=muted14,fontSize=11.sp)
}
