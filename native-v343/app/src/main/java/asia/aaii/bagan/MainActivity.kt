package asia.aaii.bagan
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.google.ar.core.ArCoreApk
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val night=Color(0xFF151C30)
private val gold=Color(0xFFECC276)
private val panel=Color(0xFF29354B)

class MainActivity:ComponentActivity(){
 private var speech: TextToSpeech? = null
 private var speechReady = false
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  speech = TextToSpeech(this) { status ->
   speechReady = status == TextToSpeech.SUCCESS
   if(speechReady) speech?.language = Locale.US
  }
  val data=ModelRepository.catalog(this)
  setContent { MaterialTheme(colorScheme=darkColorScheme(primary=gold,background=night,surface=panel)){
   BaganHeritageApp(data,{t->launchMap(t)},{t->launchAr(t)},{text->speakGuide(text)})
  } }
 }
 private fun speakGuide(text:String){
  if(speechReady) speech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bagan-guide")
  else android.widget.Toast.makeText(this,"Text-to-Speech engine not ready. Check Android TTS settings.",android.widget.Toast.LENGTH_LONG).show()
 }
 override fun onDestroy(){
  speech?.stop()
  speech?.shutdown()
  speech = null
  super.onDestroy()
 }
 private fun launchMap(t:Temple){
  val uri=Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(t.nameEn+" Bagan Myanmar"))
  try { startActivity(Intent(Intent.ACTION_VIEW,uri)) }
  catch(e:Exception){android.widget.Toast.makeText(this,"Map app unavailable",1).show()}
 }
 private fun launchAr(t:Temple){
  val availability=try { ArCoreApk.getInstance().checkAvailability(this) }
    catch(e:Exception){null}
  if(availability?.isSupported != true){
    AlertDialog.Builder(this).setTitle("ARCore Support Check")
      .setMessage(if(availability?.isTransient==true)
        "ARCore availability is still being checked. Tap AR again shortly."
       else "This phone may not support Google Play Services for AR. You can use the Native 3D Viewer instead. Status: $availability")
      .setPositiveButton("OK",null).show()
    return
  }
  val modelFile=ModelRepository.cached(this,t)
  val actual=try {ModelRepository.validate(modelFile);true}catch(_:Exception){false}
  val source=if(actual)modelFile else try {ModelRepository.demo(this)} catch(e:Exception){
    AlertDialog.Builder(this).setTitle("AR Model Error").setMessage(e.localizedMessage?:"No usable GLB")
      .setPositiveButton("OK",null).show()
    return
  }
  fun openModel(){
    try {
      startActivity(Intent(this,NativeARActivity::class.java)
        .putExtra("file_uri",Uri.fromFile(source).toString())
        .putExtra("model_name",if(actual)t.nameMm else "Bagan Demo Pagoda")
        .putExtra("demo_mode",!actual))
    }catch(e:Exception){
      AlertDialog.Builder(this).setTitle("AR Error")
        .setMessage(e.localizedMessage?:"AR camera cannot start")
        .setPositiveButton("OK",null).show()
    }
  }
  if(actual)openModel()
  else AlertDialog.Builder(this).setTitle("Offline Demo AR")
    .setMessage("The original ${t.nameEn} GLB is not downloaded. Open AR using the bundled DEMO pagoda instead? The demo is not a scan of the real temple.")
    .setPositiveButton("Demo AR"){_,_->openModel()}
    .setNegativeButton("Cancel",null).show()
 }

}
@Composable
private fun Museum(data:List<Temple>,ar:(Temple)->Unit,map:(Temple)->Unit){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("bagan_saved",0)}
 var favorites by remember{mutableStateOf(prefs.getStringSet("ids",emptySet())!!.toSet())}
 var page by remember{mutableStateOf("home")}
 var current by remember{mutableStateOf<Temple?>(null)}
 var search by remember{mutableStateOf("")}
 fun open(t:Temple){current=t;page="viewer"}
 fun save(t:Temple){favorites=if(t.id in favorites)favorites-t.id else favorites+t.id;prefs.edit().putStringSet("ids",favorites).apply()}
 Column(Modifier.fillMaxSize().background(night)){
  Row(Modifier.fillMaxWidth().background(panel).padding(14.dp),verticalAlignment=Alignment.CenterVertically){
   Text("🏛",fontSize=26.sp); Spacer(Modifier.width(9.dp))
   Column{Text("BAGAN HERITAGE MUSEUM",color=gold,fontWeight=FontWeight.Bold,fontSize=18.sp)
     Text("History • Guide • Map • 3D • AR | V3.4.5",fontSize=11.sp,color=Color.White)}
  }
  Row(Modifier.fillMaxWidth().background(panel).horizontalScroll(rememberScrollState()).padding(horizontal=6.dp,vertical=4.dp),
    horizontalArrangement=Arrangement.spacedBy(4.dp)){
   listOf("home" to "🏠 Home","guide" to "🏛 Guide/3D","maps" to "🗺 Map","saved" to "★ Saved","more" to "☰ More").forEach{(id,name)->
    val active=page==id||(page=="viewer"&&id=="guide")
    TextButton(onClick={page=id;current=null}){Text(name,color=if(active)gold else Color.White,fontSize=12.sp)}
   }
  }
  if(page=="viewer" && current!=null){
   ModelPage(current!!,{page="guide"},ar)
  } else {
   Box(Modifier.weight(1f)){
    when(page){
     "home"->LazyColumn(contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
      item{Column(Modifier.background(panel,RoundedCornerShape(15.dp)).fillMaxWidth().padding(18.dp)){
       Text("ပုဂံယဉ်ကျေးမှု အမွေအနှစ်",color=gold,fontSize=21.sp,fontWeight=FontWeight.Bold)
       Text("ပထမ App ၏ Guide / History / Map / Favorites ကို Native 3D Viewer နှင့် ပြန်ပေါင်းထားပါသည်။",color=Color.White,fontSize=13.sp)
       Button(onClick={page="guide"}){Text("ဘုရား 3D ပြခန်း ဖွင့်ရန်")}
      }}
      item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
       Button(onClick={page="guide"},modifier=Modifier.weight(1f)){Text("3D Models")}
       OutlinedButton(onClick={page="maps"},modifier=Modifier.weight(1f)){Text("Maps")}
      }}
      item{Text("ထင်ရှားသော ပုဂံဘုရားများ",color=gold,fontSize=17.sp)}
      items(data.take(6)){t->Item(t,t.id in favorites,{open(t)},{save(t)},{map(t)})}
     }
     "guide"->Column{
      OutlinedTextField(search,{search=it},label={Text("Search pagodas / ဘုရားအမည်")},modifier=Modifier.fillMaxWidth().padding(10.dp),singleLine=true)
      LazyColumn(contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
       items(data.filter{search.isBlank()||it.nameMm.contains(search,true)||it.nameEn.contains(search,true)}){
        t->Item(t,t.id in favorites,{open(t)},{save(t)},{map(t)})
       }
      }
     }
     "saved"->LazyColumn(contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      item{Text("Favorites ★",color=gold,fontSize=20.sp)}
      items(data.filter{it.id in favorites}){t->Item(t,true,{open(t)},{save(t)},{map(t)})}
     }
     "maps"->LazyColumn(contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
      item{Text("Map & Directions",color=gold,fontSize=20.sp)}
      items(data){t->Item(t,t.id in favorites,{open(t)},{save(t)},{map(t)})}
     }
     else->LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
      item{Text("ပုဂံသမိုင်း / Information",color=gold,fontSize=22.sp)}
      item{Info("History","ပုဂံသည် မြန်မာ့ရှေးဟောင်းယဉ်ကျေးမှု အမွေအနှစ်ဒေသဖြစ်ပြီး ၂၀၁၉ ခုနှစ်တွင် UNESCO ကမ္ဘာ့အမွေအနှစ်စာရင်းဝင်ခဲ့သည်။")}
      item{Info("Visitor Guide","ဘုရားပုထိုးများပေါ် မတက်ပါနှင့်။ ရှေးဟောင်းနံရံနှင့် ပန်းချီများကို မထိပါနှင့်။ နေရာအသီးသီး၏ စည်းကမ်းကို လိုက်နာပါ။")}
      item{Info("Offline 3D","GLB ဖိုင် Download အောင်မြင်ပါက Cache သိမ်းထားမည်။ Server GLB မရလျှင် Demo 3D ကိုပြပါမည်။")}
      item{Info("AR","AR လုပ်ဆောင်ရန် Google App, ARCore Support နှင့် Public HTTPS GLB လိုအပ်နိုင်သည်။")}
     }
    }
   }
  }
 }
}
@Composable private fun Info(title:String,body:String){
 Column(Modifier.fillMaxWidth().background(panel,RoundedCornerShape(12.dp)).padding(16.dp)){
  Text(title,color=gold,fontSize=18.sp); Spacer(Modifier.height(6.dp));Text(body,color=Color.White)
 }
}
@Composable private fun Item(t:Temple,saved:Boolean,view:()->Unit,save:()->Unit,map:()->Unit){
 Column(Modifier.fillMaxWidth().background(panel,RoundedCornerShape(12.dp)).padding(15.dp)){
  Text(t.nameMm,color=gold,fontSize=17.sp,fontWeight=FontWeight.Bold)
  Text(t.nameEn,color=Color.White,fontSize=12.sp)
  Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
   Button(onClick=view){Text("3D")}
   OutlinedButton(onClick=map){Text("Map")}
   TextButton(onClick=save){Text(if(saved)"★ Saved" else "☆ Save")}
  }
 }
}
@Composable private fun ModelPage(t:Temple,back:()->Unit,ar:(Temple)->Unit){
 val context=LocalContext.current
 var link by remember(t.id){mutableStateOf<String?>(null)}
 var loading by remember(t.id){mutableStateOf(true)}
 var progress by remember(t.id){mutableIntStateOf(0)}
 var failure by remember(t.id){mutableStateOf<String?>(null)}
 var demo by remember(t.id){mutableStateOf(false)}
 var retry by remember(t.id){mutableIntStateOf(0)}
 var rendererReady by remember(t.id){mutableStateOf(false)}
 var slow by remember(t.id){mutableStateOf(false)}
 var arHelp by remember{mutableStateOf(false)}
 var demoNow by remember(t.id){mutableIntStateOf(0)}
 LaunchedEffect(t.id,retry,demoNow){
  link=null;failure=null;loading=true;progress=0;demo=false;rendererReady=false;slow=false
  try {
   if(demoNow>0) {
    withContext(Dispatchers.IO){ModelRepository.demo(context)}
    demo=true;link="models/demo_pagoda.glb"
   } else {
   val f=withContext(Dispatchers.IO){
    ModelRepository.download(context,t){p->(context as? android.app.Activity)?.runOnUiThread{progress=p}}
   }
   link=Uri.fromFile(f).toString()
   }
  } catch(e:Exception){
   failure=e.message?:"cPanel GLB file unavailable"
   try{
    withContext(Dispatchers.IO){ModelRepository.demo(context)}
    demo=true;link="models/demo_pagoda.glb"
   }catch(x:Exception){failure=failure+" | Demo: "+x.message}
  }finally{loading=false}
 }
 LaunchedEffect(link){if(link!=null){kotlinx.coroutines.delay(15000);if(!rendererReady)slow=true}}
 Column(Modifier.fillMaxSize()){
  Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick=back){Text("← Back")};Text(t.nameMm,color=gold)}
  Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF07111E))){
   if(link!=null){
    key(link){
     SceneView(modifier=Modifier.fillMaxSize()){
      rememberModelInstance(modelLoader,link!!)?.let{instance->
       ModelNode(modelInstance=instance,scaleToUnits=1.25f,autoAnimate=true,isEditable=true)
       LaunchedEffect(instance){rendererReady=true}
      }
     }
    }
   }
   if(loading) Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
    CircularProgressIndicator(progress={progress/100f},color=gold)
    Text("3D Loading… $progress%",color=gold)
   }
   else if(link==null) Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
    Text(failure?:"Model unavailable",color=gold);Button(onClick={retry++}){Text("Retry")}
   }
   else if(!rendererReady)Column(Modifier.align(Alignment.Center).padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){
    CircularProgressIndicator(color=gold)
    Text(if(slow)"Renderer ကြာနေပါသည် — Demo 3D စမ်းပါ" else "3D Renderer ပြင်ဆင်နေပါသည်",color=gold)
    if(slow)Button(onClick={demoNow++}){Text("Offline Demo 3D")}
   }
   if(demo && !loading)Text("DEMO 3D — မူရင်းဘုရား Model မဟုတ်ပါ",color=gold,
      modifier=Modifier.align(Alignment.BottomCenter).background(panel).padding(6.dp))
  }
  if(failure!=null)Text(failure!!,color=gold,fontSize=11.sp,modifier=Modifier.padding(8.dp))
  Row(Modifier.padding(10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   OutlinedButton(onClick={retry++},modifier=Modifier.weight(1f)){Text("↻ Retry")}
   TextButton(onClick={demoNow++}){Text("Demo")}
   Button(onClick={if(demo||loading||link==null)arHelp=true else ar(t)},modifier=Modifier.weight(1f)){Text("AR")}
  }
  Text("Drag: Rotate   •   Pinch: Zoom",color=Color.White,fontSize=11.sp,modifier=Modifier.align(Alignment.CenterHorizontally))
 }
 if(arHelp)androidx.compose.material3.AlertDialog(onDismissRequest={arHelp=false},title={Text("AR မရနိုင်ပါ")},
  text={Text("မူရင်း GLB ဖိုင်ကို cPanel မှ မရရှိသေးပါ။ ${t.url} သို့ GLB ဖိုင်တင်ပြီး ပြန်စမ်းပါ။")},
  confirmButton={TextButton(onClick={arHelp=false}){Text("OK")}})
}
