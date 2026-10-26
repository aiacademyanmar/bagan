package asia.aaii.baganmuseum

import android.speech.tts.TextToSpeech
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

private val sand=Color(0xFFF3BD69)
private val fog=Color(0xFFCEBCA8)
private val porcelain=Color(0xFFFFE8CB)
private val panel=Color(0xFF302018)

@Composable
fun ThreeDMuseumScreen() {
    Text("Explore 3D Bagan Museum",fontSize=22.sp,color=porcelain,fontWeight=FontWeight.Bold)
    Text("Offline GLB concept model • Concept walkthrough + exhibit hotspots",color=fog,fontSize=12.sp)
    Spacer(Modifier.height(14.dp))
    AndroidView(
        factory={context ->
            WebView(context).apply {
                settings.javaScriptEnabled=true
                settings.allowFileAccess=true
                settings.allowContentAccess=false
                settings.domStorageEnabled=false
                settings.setSupportZoom(false)
                webViewClient=WebViewClient()
                setBackgroundColor(android.graphics.Color.rgb(25,17,13))
                loadUrl("file:///android_asset/temple_viewer.html")
            }
        },
        update={},
        modifier=Modifier.fillMaxWidth().height(420.dp)
    )
    Spacer(Modifier.height(12.dp))
    Text("Drag to rotate • Pinch to zoom • Walk / Hotspots to explore",color=porcelain,fontSize=12.sp)
    Text("This is an illustrative newly-created museum concept, not a photogrammetric reconstruction of an ancient Bagan monument.",color=fog,fontSize=12.sp)
    Spacer(Modifier.height(18.dp))
    Text("AR Museum Placement",color=sand,fontWeight=FontWeight.Bold)
    Text("Optional ARCore placement is available in the AR tab on supported phones. This 3D viewer works offline without ARCore.",color=fog,fontSize=12.sp)
}
private val narrations= listOf(
    Triple("Introduction / မိတ်ဆက်","ပုဂံဟာ မြန်မာ့ သမိုင်းဝင် ဘုရားပုထိုးများနဲ့ ယဉ်ကျေးမှုအမွေအနှစ်များကြောင့် ထင်ရှားတဲ့ ဒေသတစ်ခု ဖြစ်ပါတယ်။","Bagan is celebrated for its ancient monuments and Myanmar cultural heritage."),
    Triple("Architecture / ဗိသုကာ","ပုဂံဗိသုကာမှာ အုတ်စီပညာ၊ မုခ်ခုံးနဲ့ ဘုရားပုထိုး အဆောက်အအုံ အနုပညာတွေကို တွေ့နိုင်ပါတယ်။","Bagan architecture is notable for its brick structures, arches and temples."),
    Triple("Murals / နံရံဆေးရေး","ပုဂံခေတ် နံရံဆေးရေးပန်းချီတွေက အနုပညာနဲ့ ဘာသာရေး ယဉ်ကျေးမှုကို ရောင်ပြန်ဟပ်ပါတယ်။","Ancient Bagan murals offer a glimpse into art, religion and historical life."),
    Triple("Inscriptions / ကျောက်စာ","ကျောက်စာတွေဟာ ပုဂံခေတ် စာပေ၊ လူမှုရေးနဲ့ အလှူဒါန မှတ်တမ်းတွေကို သိရှိစေပါတယ်။","Historical inscriptions are valuable records of language, society and donations."),
    Triple("Conservation / ထိန်းသိမ်းရေး","ပုဂံအမွေအနှစ်တွေကို အနာဂတ် မျိုးဆက်တွေအတွက် စနစ်တကျ ထိန်းသိမ်းဖို့ လိုအပ်ပါတယ်။","Conservation helps protect Bagan's heritage for future generations."),
    Triple("Temple Studies / ဘုရားပုထိုး","ပုဂံမြို့ဟောင်းမှာ ထင်ရှားသော ဘုရားပုထိုးများနှင့် ခေတ်အဆက်ဆက် ဗိသုကာပညာတို့ကို လေ့လာနိုင်ပါတယ်။","The temples of Bagan showcase centuries of architecture and craftsmanship."),
    Triple("Archaeology / ရှေးဟောင်းသုတေသန","မြေပြင်သုတေသနနှင့် မှတ်တမ်းတင်ခြင်းတို့က ရှေးဟောင်းအမွေအနှစ်တွေကို နားလည်အောင် ကူညီပါတယ်။","Archaeological research and documentation help us understand cultural heritage."),
    Triple("Museum Tour / ပြတိုက်လှည့်လည်ခြင်း","ဤဒစ်ဂျစ်တယ်ပြတိုက်မှာ ပြခန်းအယူအဆများ၊ အနုပညာလက်ရာများနှင့် သမိုင်းဇာတ်ကြောင်းများကို စူးစမ်းနိုင်ပါတယ်။","Explore the museum concept galleries and their historical themes.")
)
@Composable
fun NativeGuideScreen() {
    val context=LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Initializing speech engine…") }
    var language by remember { mutableStateOf("English") }
    var selected by remember { mutableIntStateOf(0) }
    val tts=remember(context) { TextToSpeech(context) { status ->
        ttsReady=status==TextToSpeech.SUCCESS
        message=if(ttsReady) "Speech engine ready" else "Speech engine unavailable on this phone"
    }}
    DisposableEffect(tts) { onDispose { tts.stop(); tts.shutdown() } }
    Text("Museum Audio Guide",fontSize=23.sp,fontWeight=FontWeight.Bold,color=porcelain)
    Text("Native Text-to-Speech • No pre-recorded audio is required",fontSize=12.sp,color=fog)
    Spacer(Modifier.height(18.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("English","မြန်မာ").forEach{ option ->
            FilterChip(
                selected=language==option,
                onClick={language=option;tts.stop()},
                label={Text(option)}
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    narrations.forEachIndexed { i,entry ->
        Surface(
            onClick={selected=i;tts.stop()},
            color=if(selected==i) Color(0xFF754329) else panel,
            modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(entry.first,color=porcelain,fontWeight=FontWeight.Bold)
                Text(if(language=="မြန်မာ")entry.second else entry.third,color=fog,fontSize=12.sp,maxLines=if(selected==i)10 else 2)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Button(
        modifier=Modifier.fillMaxWidth(),
        enabled=ttsReady,
        onClick={
            val loc=if(language=="မြန်မာ") Locale.forLanguageTag("my-MM") else Locale.US
            val result=tts.setLanguage(loc)
            if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                message="This phone has no installed "+language+" TTS voice. Install a supported speech voice in Android Settings."
            }else{
                tts.setSpeechRate(0.9f)
                val script=if(language=="မြန်မာ") narrations[selected].second else narrations[selected].third
                tts.speak(script,TextToSpeech.QUEUE_FLUSH,null,"museum-v12")
                message="Playing: "+narrations[selected].first
            }
        }
    ) { Text("▶ Play Audio Guide") }
    OutlinedButton(modifier=Modifier.fillMaxWidth(),onClick={tts.stop();message="Stopped"}) { Text("■ Stop") }
    Text(message,color=sand,fontSize=12.sp)
    Spacer(Modifier.height(8.dp))
    Text("Voice availability depends on installed Android TTS engines. Burmese audio may require a supported Myanmar voice pack.",color=fog,fontSize=12.sp)
}
