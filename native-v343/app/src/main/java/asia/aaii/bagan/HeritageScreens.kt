
package asia.aaii.bagan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

private val Navy=Color(0xFF081B2A)
private val Navy2=Color(0xFF10283B)
private val Gold=Color(0xFFE1AA42)
private val Pale=Color(0xFFFFF7EA)
private val Ink=Color(0xFF252C36)
private val Muted=Color(0xFF716B63)
private val CardBorder=Color(0xFFE6D9C2)
private val SoftGold=Color(0xFFFFDF96)

private val labels=listOf(
 "01 Splash","02 Home","03 Pagodas","04 Site Details","05 3D Viewer",
 "06 AR Camera","07 Bagan Map","08 Gallery","09 History","10 Audio Guide",
 "11 Routes","12 Favorites","13 Search","14 Nearby Places",
 "15 Offline Downloads","16 Settings","17 Help & Support","18 About Bagan",
 "19 3D Loading","20 Error Handling"
)

@Composable
fun BaganHeritageApp(
 temples:List<Temple>,
 onMap:(Temple)->Unit,
 onAR:(Temple)->Unit,
 onSpeak:(String)->Unit
) {
 val ctx=LocalContext.current
 val prefs=remember {ctx.getSharedPreferences("bagan_heritage_350",0)}
 val scope=rememberCoroutineScope()
 var route by remember {mutableIntStateOf(2)}
 var selected by remember {mutableStateOf<Temple?>(temples.firstOrNull())}
 var fav by remember {mutableStateOf(prefs.getStringSet("fav",emptySet())!!.toSet())}
 var query by remember {mutableStateOf("")}
 var category by remember {mutableStateOf("All")}
 var infoTab by remember {mutableStateOf("Overview")}
 var imageTab by remember {mutableStateOf("All")}
 var showLang by remember {mutableStateOf(false)}
 var language by remember {mutableStateOf(prefs.getString("language", "မြန်မာ") ?: "မြန်မာ")}
 var quality by remember {mutableStateOf("High")}
 var onlineOnly by remember {mutableStateOf(false)}
 var loadProgress by remember {mutableIntStateOf(0)}
 var modelFile by remember {mutableStateOf<String?>(null)}
 var modelDemo by remember {mutableStateOf(false)}
 var loadError by remember {mutableStateOf("")}
 var renderReady by remember {mutableStateOf(false)}
 var retryCount by remember {mutableIntStateOf(0)}
 var bulkStart by remember {mutableIntStateOf(0)}
 var bulkStatus by remember {mutableStateOf("")}

 fun go(n:Int) { route=n.coerceIn(1,27) }
 BackHandler(enabled=route!=2 && route!=27) { go(2) }
 fun openTemple(t:Temple){selected=t;go(4)}
 fun favorite(t:Temple){
  fav=if(t.id in fav)fav-t.id else fav+t.id
  prefs.edit().putStringSet("fav",fav.toSet()).apply()
 }
 fun loadModel(t:Temple){
  selected=t; modelFile=null; modelDemo=false; loadError="";loadProgress=0; renderReady=false
  if(onlineOnly){
   try {
    val cached=ModelRepository.cached(ctx,t)
    ModelRepository.validate(cached)
    modelFile=Uri.fromFile(cached).toString()
    go(5)
   } catch(e:Exception){
    loadError="Offline Mode: ${t.nameEn} is not downloaded. Switch off Offline Mode in Settings and download the GLB first."
    go(20)
   }
   return
  }
  retryCount++;go(19)
 }
 fun demoModel(){
  try{
   ModelRepository.demo(ctx)
   modelDemo=true; modelFile="models/demo_pagoda.glb"; renderReady=false;go(5)
  }catch(e:Exception){loadError="Demo GLB: "+(e.localizedMessage ?: "Error");go(20)}
 }
 LaunchedEffect(route,retryCount) {
  if(route==19 && selected!=null){
   val t=selected!!
   try{
    val f=withContext(Dispatchers.IO){
     ModelRepository.download(ctx,t){p->
      (ctx as? android.app.Activity)?.runOnUiThread { loadProgress=p }
     }
    }
    modelFile=Uri.fromFile(f).toString();modelDemo=false;go(5)
   }catch(e:Exception){
    loadError=(e.localizedMessage ?: "Download failed")+"\nModel: "+t.fileName
    go(20)
   }
  }
 }
 LaunchedEffect(bulkStart) {
  if(bulkStart>0){
   if(onlineOnly){
    bulkStatus="Offline Mode is ON. Disable it in Settings to download models."
    return@LaunchedEffect
   }
   bulkStatus="Downloading models..."
   var ok=0
   withContext(Dispatchers.IO){
    temples.forEachIndexed {ix,t->
     try{ModelRepository.download(ctx,t){};ok++}catch(e:Exception){}
     withContext(Dispatchers.Main){ bulkStatus="${ix+1}/${temples.size} scanned • $ok cached" }
    }
   }
   bulkStatus="$ok / ${temples.size} offline models available"
  }
 }
 Column(Modifier.fillMaxSize().background(Pale).windowInsetsPadding(WindowInsets.safeDrawing)) {
  if(route!=1){
   Row(Modifier.fillMaxWidth().background(Navy).padding(horizontal=15.dp,vertical=9.dp),
       verticalAlignment=Alignment.CenterVertically) {
    Text("♜",fontSize=30.sp,color=Gold)
    Spacer(Modifier.width(8.dp))
    Column(Modifier.weight(1f)) {
     Text("BAGAN",fontSize=24.sp,fontWeight=FontWeight.Black,color=SoftGold,letterSpacing=2.sp)
     Text("HERITAGE MUSEUM",fontSize=11.sp,color=SoftGold,letterSpacing=1.sp)
    }
    Text("⋮",color=Gold,fontSize=25.sp,modifier=Modifier.clickable{go(16)})
   }

  }
  Box(Modifier.weight(1f).fillMaxWidth()){
   when(route){
    1->Splash(onStart={go(2)})
    2->V361Home(temples,onSearch={go(13)},onNavigate={p-> if(p==5) selected?.let{loadModel(it)} ?: go(3) else go(p)},onTemple={openTemple(it)})
    3->PagodaList(temples,query,{query=it},category,{category=it},{openTemple(it)},{favorite(it)},{loadModel(it)},{onMap(it)},fav)
    4->TempleDetail(selected ?: temples.first(),infoTab,{infoTab=it},
      onModel={selected?.let{loadModel(it)}},onAR={go(6)},onMap={selected?.let{onMap(it)}},
      onFav={selected?.let{favorite(it)}},saved=selected?.id in fav,
      onPage={go(it)})
    5->ModelScreen(selected,modelFile,modelDemo,{renderReady=true},
      {selected?.let{loadModel(it)}},{demoModel()},{go(6)},{go(4)})
    6->ArInstructions(selected,onStart={selected?.let{onAR(it)}},onBack={go(4)},onModel={selected?.let{loadModel(it)} ?: go(3)})
    7->MapPage(temples,onDirections={onMap(it)},onDetail={openTemple(it)})
    8->GalleryPage(selected,imageTab,{imageTab=it},onDetail={go(4)})
    9->TimelineV356(onSpeak={onSpeak(it)})
    10->AudioPage(selected,onSpeak={onSpeak(it)})
    11->RoutesV356(temples,details={openTemple(it)},directions={onMap(it)})
    12->FavoritesV356(temples.filter{it.id in fav},details={openTemple(it)},remove={favorite(it)})
    13->V354Search(temples,query,{query=it},onDetails={openTemple(it)},onThreeD={loadModel(it)})
    14->NearbyPage(temples,{onMap(it)},{openTemple(it)})
    15->OfflinePage(temples,bulkStatus,onBatch={bulkStart++},onView={loadModel(it)})
    16->SettingsV357(language,onLanguage={showLang=true},offline=onlineOnly,onOffline={onlineOnly=it},onHelp={go(17)},onProfile={go(26)},onDiagnostics={go(21)})
    17->HelpV357(onThreeD={selected?.let{loadModel(it)} ?: go(3)},onAR={go(6)},onMap={go(7)})
    18->AboutV357(onHome={go(2)})
    19->LoadingPage(selected,loadProgress,onCancel={go(4)})
    20->ErrorPage(loadError,onRetry={selected?.let{loadModel(it)}},onDemo={demoModel()},onBack={go(3)})
    21->BaganDiagnosticsScreen(temples,onBack={go(16)})
    22->V354Categories(temples,onCategory={category=it;go(3)},onTemple={openTemple(it)})
    23->MuseumInfoV356(onHistory={go(9)},onArtifacts={go(24)})
    24->ArtifactsV356(onBack={go(23)})
    25->RelatedPage(temples,selected,onSelect={openTemple(it)})
    26->ProfileV357(savedCount=fav.size,onFavorites={go(12)},onHome={go(2)})
    27->ExitV357(onCancel={go(2)},onExit={(ctx as? android.app.Activity)?.finish()})
   }
  }
  if(route!=1) V361BottomBar(route,onNavigate={go(it)})
 }
 if(showLang) AlertDialog(onDismissRequest={showLang=false},title={Text("Language / ဘာသာစကား")},
  text={Column {
   listOf("မြန်မာ","English").forEach{l->TextButton(onClick={language=l;prefs.edit().putString("language",l).apply();showLang=false}){Text(l)}}
  }},confirmButton={TextButton(onClick={showLang=false}){Text("Close")}})
}

@Composable
private fun Head(t:String,number:Int,sub:String?=null){
 Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp)){
  Text(t,color=Navy,fontWeight=FontWeight.Bold,fontSize=21.sp)
  if(sub!=null) Text(sub,color=Muted,fontSize=12.sp)
 }
}
@Composable private fun GButton(text:String,action:()->Unit,modifier:Modifier=Modifier){
 Button(onClick=action,modifier=modifier.heightIn(min=48.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Navy),
  shape=RoundedCornerShape(11.dp),contentPadding=PaddingValues(horizontal=13.dp,vertical=10.dp)){
  Text(text,fontWeight=FontWeight.Bold,fontSize=12.sp)
 }
}
@Composable private fun Outline(text:String,action:()->Unit,modifier:Modifier=Modifier){
 OutlinedButton(onClick=action,modifier=modifier.heightIn(min=48.dp),shape=RoundedCornerShape(11.dp),contentPadding=PaddingValues(horizontal=11.dp,vertical=8.dp)){
  Text(text,color=Navy,fontSize=12.sp)
 }
}
@Composable
private fun Art(modifier:Modifier=Modifier,seed:Int=0){
 Canvas(modifier.background(Brush.verticalGradient(listOf(Color(0xFF284A62),Color(0xFFF1A65F),Color(0xFF4E402C))))){
  val w=size.width;val h=size.height
  val accent=if(seed%2==0)Color(0xFFBD7B37) else Color(0xFFE3BA65)
  drawCircle(Color(0xFFFFDC9D),radius=w*.11f,center=Offset(w*.82f,h*.31f))
  for(i in 0..5){
   val x=w*(i*.19f-.05f)
   val base=h*(.82f+((i*3)%3)*.03f)
   val ht=h*(.23f+((i*7)%3)*.06f)
   drawRect(Color(0xFF584D37).copy(alpha=.55f),Offset(x,base-ht*.5f),Size(w*.09f,ht*.5f))
   val p=Path().apply{moveTo(x-w*.01f,base-ht*.5f);lineTo(x+w*.11f,base-ht*.5f);lineTo(x+w*.05f,base-ht);close()}
   drawPath(p,Color(0xFF9F7C49).copy(alpha=.6f))
  }
  val cx=w*.50f;val by=h*.92f
  drawRect(accent,Offset(cx-w*.25f,by-h*.20f),Size(w*.50f,h*.20f))
  drawRect(Color(0xFFFFD28A),Offset(cx-w*.29f,by-h*.23f),Size(w*.58f,h*.04f))
  drawRect(accent,Offset(cx-w*.19f,by-h*.36f),Size(w*.38f,h*.13f))
  drawRect(Color(0xFFFFD28A),Offset(cx-w*.21f,by-h*.38f),Size(w*.42f,h*.035f))
  val roof=Path().apply{moveTo(cx-w*.15f,by-h*.38f);lineTo(cx,by-h*.79f);lineTo(cx+w*.15f,by-h*.38f);close()}
  drawPath(roof,accent)
  drawLine(Gold,Offset(cx,by-h*.89f),Offset(cx,by-h*.76f),strokeWidth=w*.009f)
  for(i in 0..4){
   val x=cx-w*.225f+i*w*.11f
   drawRect(Color(0xFF5B3727),Offset(x,by-h*.16f),Size(w*.055f,h*.14f))
  }
 }
}
@Composable private fun PillGroup(names:List<String>,chosen:String,set:(String)->Unit){
 Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=12.dp,vertical=6.dp),
  horizontalArrangement=Arrangement.spacedBy(7.dp)){
  names.forEach{n->
   Box(Modifier.background(if(n==chosen)Gold else Color(0xFFF0E9DD),RoundedCornerShape(22.dp))
     .clickable{set(n)}.heightIn(min=48.dp).padding(horizontal=15.dp,vertical=9.dp)){
    Text(n,fontSize=11.sp,color=Ink,fontWeight=if(n==chosen)FontWeight.Bold else FontWeight.Normal)
   }
  }
 }
}
@Composable private fun Panel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){
 Column(modifier.background(Color.White,RoundedCornerShape(14.dp))
  .border(1.dp,CardBorder,RoundedCornerShape(14.dp)).padding(12.dp),content=content)
}
@Composable private fun TempleItem(t:Temple,index:Int,saved:Boolean,open:()->Unit,fav:()->Unit,model:()->Unit,map:()->Unit){
 Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=3.dp)){
  Row(verticalAlignment=Alignment.CenterVertically){
   Art(Modifier.size(90.dp,82.dp).clip(RoundedCornerShape(9.dp)),index)
   Spacer(Modifier.width(11.dp))
   Column(Modifier.weight(1f)){
    Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold,fontSize=14.sp,maxLines=2)
    Text(t.nameMm,color=Muted,fontSize=12.sp)
    Text("ပုဂံ ရှေးဟောင်းအမွေအနှစ်နေရာ",color=Muted,fontSize=10.sp)
    Row(verticalAlignment=Alignment.CenterVertically){
     Text("⬡ 3D   ◉ Map",color=Color(0xFFA36E2A),fontSize=11.sp,modifier=Modifier.clickable{model()})
    }
   }
   Text(if(saved)"♥" else "♡",color=Color(0xFFC45A28),fontSize=24.sp,
    modifier=Modifier.clickable{fav()}.padding(4.dp))
  }
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Outline("Details",open,Modifier.weight(1f))
   GButton("View 3D",model,Modifier.weight(1f))
   Outline("Map",map,Modifier.weight(1f))
  }
 }
}

@Composable private fun Splash(onStart:()->Unit){
 Box(Modifier.fillMaxSize()){
  Art(Modifier.fillMaxSize(),2)
  Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xB5081B2A),Color.Transparent,Color(0xEE081B2A)))),
   verticalArrangement=Arrangement.SpaceBetween,horizontalAlignment=Alignment.CenterHorizontally){
   Spacer(Modifier.height(24.dp))
   Column(horizontalAlignment=Alignment.CenterHorizontally){
    Text("♜",fontSize=60.sp,color=Gold)
    Text("BAGAN",fontSize=52.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp,color=SoftGold)
    Text("HERITAGE MUSEUM",fontSize=20.sp,color=SoftGold,letterSpacing=2.sp)
    Text("Explore  •  Learn  •  3D  •  AR",fontSize=13.sp,color=Color.White)
   }
   Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(bottom=45.dp)){
    Text("ပုဂံရှေးဟောင်းယဉ်ကျေးမှုအမွေအနှစ်",color=Color.White,fontSize=18.sp)
    Spacer(Modifier.height(20.dp))
    GButton("Start Exploring Bagan  ➜",onStart,Modifier.fillMaxWidth(.77f))
    Spacer(Modifier.height(10.dp))
    Text("AAII.ASIA • Native Android",color=SoftGold,fontSize=11.sp)
   }
  }
 }
}
@Composable private fun Home(ts:List<Temple>,onTemple:(Temple)->Unit,onNext:(Int)->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
  item{
   Box(Modifier.fillMaxWidth().height(220.dp)){
    Art(Modifier.fillMaxSize(),2)
    Column(Modifier.align(Alignment.BottomStart).padding(16.dp)){
     Text("Explore the Ancient City of",color=Color.White,fontSize=13.sp)
     Text("BAGAN",fontSize=41.sp,fontWeight=FontWeight.Black,color=Color.White)
     Text("Myanmar Heritage in 3D",color=Color.White,fontSize=14.sp)
    }
   }
  }
  item {Head("Discover Bagan",2,"History • Pagodas • Maps • 3D Museum • AR Experience")}
  item{
   Column(Modifier.padding(horizontal=10.dp)){
    val shortcuts=listOf(9 to "📜 History",3 to "♜ Temples",7 to "📍 Map",5 to "⬡ 3D Museum",
     6 to "▣ AR Mode",15 to "↓ Offline",14 to "📍 Nearby",16 to "⚙ Settings")
    shortcuts.chunked(4).forEach{line->
     Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.padding(bottom=8.dp)){
      line.forEach{(id,name)->
       Panel(Modifier.weight(1f).clickable{onNext(id)}) {
        Text(name,color=Navy,fontSize=11.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
       }
      }
     }
    }
   }
  }
  item{Head("Popular Pagodas",2,"Tap a temple to open information, 3D and AR")}
  items(ts.take(5)){ t->
   val i=ts.indexOf(t)
   Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp).clickable{onTemple(t)}){
    Row(verticalAlignment=Alignment.CenterVertically){
     Art(Modifier.size(110.dp,80.dp).clip(RoundedCornerShape(9.dp)),i)
     Column(Modifier.padding(start=12.dp).weight(1f)){Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
       Text(t.nameMm,color=Muted,fontSize=12.sp);Text("Details  →",color=Gold)}
    }
   }
  }
  item{GButton("See All Pagodas →",{onNext(3)},Modifier.fillMaxWidth().padding(horizontal=18.dp))}
 }
}
@Composable private fun PagodaList(ts:List<Temple>,search:String,setSearch:(String)->Unit,cat:String,setCat:(String)->Unit,
 open:(Temple)->Unit,favorite:(Temple)->Unit,model:(Temple)->Unit,map:(Temple)->Unit,favs:Set<String>){
 Column {
  Head("Pagodas & Temples",3,"ဘုရားပုထိုးများ • ${ts.size} 3D catalog entries")
  OutlinedTextField(search,setSearch,modifier=Modifier.fillMaxWidth().padding(horizontal=13.dp),
    singleLine=true,placeholder={Text("Search pagodas…")},leadingIcon={Text("⌕")})
  PillGroup(listOf("All","Famous","Temples","Stupas"),cat,setCat)
  val shown=ts.filter{(search.isBlank()||it.nameEn.contains(search,true)||it.nameMm.contains(search,true)) &&
    (cat=="All"||cat=="Famous"||(cat=="Temples"&&it.nameEn.contains("temple",true))||(cat=="Stupas"&&!it.nameEn.contains("temple",true)))}
  LazyColumn(contentPadding=PaddingValues(bottom=15.dp)){
   items(shown){t->TempleItem(t,ts.indexOf(t),t.id in favs,{open(t)},{favorite(t)},{model(t)},{map(t)})}
  }
 }
}
@Composable private fun TempleDetail(t:Temple,tab:String,setTab:(String)->Unit,
 onModel:()->Unit,onAR:()->Unit,onMap:()->Unit,onFav:()->Unit,saved:Boolean,onPage:(Int)->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Art(Modifier.fillMaxWidth().height(225.dp),t.id.length)}
  item {
   Column(Modifier.padding(horizontal=14.dp)){
    Text(t.nameMm,fontSize=23.sp,fontWeight=FontWeight.Black,color=Ink)
    Text(t.nameEn,color=Muted,fontSize=15.sp)
    Text("Bagan • Myanmar",fontSize=12.sp,color=Muted)
   }
  }
  item {
   Row(Modifier.padding(horizontal=10.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
    listOf("⬡ 3D" to onModel,"▣ AR Camera" to onAR,"📍 Map" to onMap,(if(saved)"♥ Saved" else "♡ Save") to onFav).forEach{(name,action)->
     GButton(name,action,Modifier.weight(1f))
    }
   }
  }
  item {PillGroup(listOf("Overview","History","Architecture","Gallery"),tab,setTab)}
  item{
   Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
    Text(when(tab){"History"->"Historical Background";"Architecture"->"Temple Architecture";"Gallery"->"Photo Gallery";else->"About ${t.nameEn}"},
      fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink)
    Spacer(Modifier.height(8.dp))
    Text(when(tab) {
      "History"->"Bagan archaeological heritage spans many centuries. Learn from authoritative historical records when researching this site."
      "Architecture"->"Observe spires, terraces, vaults and ornate brickwork in the 3D model. Preserved structure varies by monument."
      "Gallery"->"Browse the gallery for heritage imagery and architectural details."
      else->"${t.nameEn} (${t.nameMm}) is part of Bagan's historic landscape. Explore its model, location and cultural context."
    },color=Muted,fontSize=13.sp)
    Spacer(Modifier.height(9.dp))
    GButton(if(tab=="Gallery")"Open Gallery" else "Explore More",{
      onPage(when(tab){"History"->9;"Architecture"->8;"Gallery"->8;else->9})
    })
   }
  }
  item{
   Row(Modifier.padding(horizontal=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    Outline("Audio Guide",{onPage(10)},Modifier.weight(1f))
    Outline("Related Pagodas",{onPage(25)},Modifier.weight(1f))
   }
  }
 }
}
@Composable private fun ModelScreen(t:Temple?,file:String?,demo:Boolean,ready:()->Unit,retry:()->Unit,demoClick:()->Unit,ar:()->Unit,back:()->Unit){
 var rendered by remember(file){mutableStateOf(false)}
 var timedOut by remember(file){mutableStateOf(false)}
 LaunchedEffect(file){
  if(file!=null) {kotlinx.coroutines.delay(20000);if(!rendered)timedOut=true}
 }
 Column(Modifier.fillMaxSize().background(Navy)){
  Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
   TextButton(onClick=back){Text("‹ Back",color=Gold)}
   Text((t?.nameEn ?: "Demo")+" • 3D View",color=Color.White,fontWeight=FontWeight.Bold)
  }
  Box(Modifier.weight(1f).fillMaxWidth()){
   if(file!=null){
    key(file) {
     SceneView(modifier=Modifier.fillMaxSize()){
      rememberModelInstance(modelLoader,file)?.let{inst->
       ModelNode(modelInstance=inst,scaleToUnits=1.15f,autoAnimate=true,isEditable=true)
       LaunchedEffect(inst){rendered=true;ready()}
      }
     }
    }
   }
   if(!rendered){
    Column(Modifier.align(Alignment.Center).padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally){
     CircularProgressIndicator(color=Gold)
     Spacer(Modifier.height(12.dp))
     Text(if(timedOut)"3D renderer not ready" else "Preparing Native 3D…",color=Color.White)
     if(timedOut){
      Text("GLB model could not be rendered on this device.",color=Color.White,fontSize=12.sp)
      Spacer(Modifier.height(8.dp))
      GButton("Try Offline Demo",demoClick)
     }
    }
   }
   if(demo)Text("OFFLINE DEMO — not the actual temple model",color=Gold,fontSize=10.sp,
     modifier=Modifier.align(Alignment.BottomCenter).background(Navy).padding(9.dp))
  }
  Row(Modifier.fillMaxWidth().padding(10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Outline("‹ Guide",back,Modifier.weight(1f))
   GButton("↻ Retry",retry,Modifier.weight(1f))
   GButton("▣ AR Camera",ar,Modifier.weight(1f))
  }
  Text("Drag to rotate  •  Pinch to zoom  •  GLB 3D",color=Color.White,fontSize=12.sp,
    modifier=Modifier.align(Alignment.CenterHorizontally).padding(bottom=12.dp))
 }
}
@Composable private fun ArInstructions(t:Temple?,onStart:()->Unit,onBack:()->Unit,onModel:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(Pale),contentPadding=PaddingValues(15.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Head("AR Camera / AR ကင်မရာ",6,t?.nameEn ?: "Augmented Reality")}
  item{Art(Modifier.fillMaxWidth().height(270.dp),7)}
  item{Panel(Modifier.fillMaxWidth()){
   Text("AR ဖြင့် ဘုရားပုံစံကို ကြည့်ရှုရန်",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Ink)
   Text("AR requires a compatible Android phone, camera permission and Google Play Services for AR.",color=Muted)
  }}
  items(listOf("1  Allow camera permission","2  Point camera at a flat surface","3  Move the phone slowly to detect a plane",
   "4  Place the downloaded 3D model")){instruction->
   Panel(Modifier.fillMaxWidth()){Text(instruction,color=Ink)}
  }
  item{GButton("Start AR Camera",onStart,Modifier.fillMaxWidth())}
  item{Outline("Continue in 3D Viewer",onModel,Modifier.fillMaxWidth())}
  item{Outline("‹ Back",onBack,Modifier.fillMaxWidth())}
 }
}
@Composable private fun MapPage(ts:List<Temple>,onDirections:(Temple)->Unit,onDetail:(Temple)->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Head("Bagan Map / ပုဂံမြေပုံ",7,"Directions open in Google Maps. Live in-app maps require a Maps API key.")}
  item{Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
   Box(Modifier.fillMaxWidth().height(170.dp).background(Color(0xFFDCF0DB),RoundedCornerShape(12.dp))) {
    Text("🗺",fontSize=65.sp,modifier=Modifier.align(Alignment.Center))
    Text("OPEN GOOGLE MAPS FOR LIVE DIRECTIONS",modifier=Modifier.align(Alignment.BottomCenter).padding(10.dp),color=Navy,fontSize=10.sp)
   }
  }}
  items(ts){t->
   Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
    Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     GButton("Directions",{onDirections(t)})
     Outline("Temple Info",{onDetail(t)})
    }
   }
  }
 }
}
@Composable private fun GalleryPage(t:Temple?,filter:String,setFilter:(String)->Unit,onDetail:()->Unit){
 Column{
  Head("Gallery & Photos",8,t?.nameEn ?: "Bagan Gallery")
  PillGroup(listOf("All","Exterior","Interior","Details"),filter,setFilter)
  LazyColumn(contentPadding=PaddingValues(10.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
   item{Art(Modifier.fillMaxWidth().height(225.dp),3)}
   items((0..5).toList().chunked(2)){row->
    Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){
     row.forEach{i->Art(Modifier.weight(1f).height(150.dp).clip(RoundedCornerShape(12.dp)),i)}
    }
   }
   item{Text("Illustrative artwork — replace with verified monument photos.",fontSize=11.sp,color=Muted)}
   item{GButton("Temple Details",onDetail,Modifier.fillMaxWidth())}
  }
 }
}
@Composable private fun HistoryPage(t:Temple?,onSpeak:(String)->Unit,onBack:()->Unit){
 val title=t?.nameEn?:"Bagan"
 val events=listOf("Early Bagan kingdom","Monument construction and patronage",
 "Religious and cultural development","Conservation of Bagan heritage","2019 — UNESCO World Heritage inscription")
 LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Head("Bagan History",9,title)}
  item{Art(Modifier.fillMaxWidth().height(195.dp),1)}
  item{Panel(Modifier.fillMaxWidth()){Text("Timeline of Bagan",color=Ink,fontSize=19.sp,fontWeight=FontWeight.Bold)}}
  items(events.withIndex().toList()){(i,e)->
   Panel(Modifier.fillMaxWidth()){
    Row{Text("●",color=Gold,fontSize=20.sp);Spacer(Modifier.width(12.dp))
     Column{Text(e,color=Ink,fontWeight=FontWeight.Bold);Text("Heritage information",color=Muted,fontSize=11.sp)}}
   }
  }
  item{GButton("🔊 Listen to history",{onSpeak("Bagan is a UNESCO World Heritage Site in Myanmar, known for its historic temples and cultural heritage.")},Modifier.fillMaxWidth())}
  item{Outline("Back to Temple",onBack,Modifier.fillMaxWidth())}
 }
}
@Composable private fun AudioPage(t:Temple?,onSpeak:(String)->Unit){
 val title=t?.nameEn?:"Bagan"
 val chapters=listOf("Introduction","History","Architecture","Cultural Significance","Visitor Information")
 var playIndex by remember{mutableIntStateOf(-1)}
 Column(Modifier.fillMaxSize().background(Navy)){
  Box(Modifier.fillMaxWidth().height(220.dp)){Art(Modifier.fillMaxSize(),4)}
  Column(Modifier.padding(16.dp)){
   Text("Audio Guide  •  $title",color=Gold,fontSize=21.sp,fontWeight=FontWeight.Bold)
   Text("Local device text-to-speech • Availability depends on installed voices",color=Color.White,fontSize=11.sp)
   Spacer(Modifier.height(12.dp))
   GButton(if(playIndex>=0)"▶ Play Next Chapter" else "▶ Start Audio Guide",{
    playIndex=(playIndex+1)%chapters.size
    onSpeak("$title. ${chapters[playIndex]}. Explore the historic architecture of Bagan and learn about its cultural heritage.")
   },Modifier.fillMaxWidth())
   Spacer(Modifier.height(15.dp))
   chapters.forEachIndexed{i,chapter->
    Row(Modifier.fillMaxWidth().clickable{
     playIndex=i;onSpeak("$title. $chapter. Bagan is a historic archaeological city in Myanmar.")
    }.padding(vertical=13.dp),horizontalArrangement=Arrangement.SpaceBetween){
     Text("${i+1}.  $chapter",color=if(i==playIndex)Gold else Color.White)
     Text("▶",color=Gold)
    }
    HorizontalDivider(color=Color.DarkGray)
   }
  }
 }
}
@Composable private fun RelatedPage(ts:List<Temple>,current:Temple?,onSelect:(Temple)->Unit){
 Column{
  Head("Related Pagodas",11,"Explore more Bagan monuments")
  LazyColumn(contentPadding=PaddingValues(bottom=16.dp)){
   items(ts.filter{it.id!=current?.id}){t->
    Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=5.dp).clickable{onSelect(t)}){
     Row(verticalAlignment=Alignment.CenterVertically){
      Art(Modifier.size(85.dp,75.dp).clip(RoundedCornerShape(8.dp)),ts.indexOf(t))
      Column(Modifier.padding(start=12.dp).weight(1f)){Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
       Text(t.nameMm,color=Muted,fontSize=11.sp)}
      Text("›",fontSize=25.sp,color=Gold)
     }
    }
   }
  }
 }
}
@Composable private fun FavoritesPage(ts:List<Temple>,onSelect:(Temple)->Unit,onDelete:(Temple)->Unit){
 Column{
  Head("My Favorites",12,"${ts.size} saved pagodas")
  if(ts.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("No favorites yet. Tap ♡ on pagodas.",color=Muted)}
  else LazyColumn{items(ts){t->Panel(Modifier.fillMaxWidth().padding(10.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){
    Art(Modifier.size(82.dp,72.dp),t.id.length)
    Column(Modifier.padding(start=12.dp).weight(1f).clickable{onSelect(t)}){
     Text(t.nameEn,fontWeight=FontWeight.Bold,color=Ink);Text(t.nameMm,color=Muted,fontSize=11.sp)}
    Text("♥",color=Color.Red,fontSize=23.sp,modifier=Modifier.clickable{onDelete(t)})
   }
  }}}
 }
}
@Composable private fun SearchPage(ts:List<Temple>,query:String,set:(String)->Unit,info:(Temple)->Unit,model:(Temple)->Unit){
 Column{
  Head("Search Bagan",13,"Find pagodas, 3D models and heritage sites")
  OutlinedTextField(query,set,modifier=Modifier.fillMaxWidth().padding(12.dp),singleLine=true,
   placeholder={Text("Search temples…")},leadingIcon={Text("⌕")})
  LazyColumn {
   items(ts.filter{query.isBlank()||it.nameEn.contains(query,true)||it.nameMm.contains(query,true)}){t->
    Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=4.dp)){
     Row(verticalAlignment=Alignment.CenterVertically){
      Art(Modifier.size(80.dp,70.dp),ts.indexOf(t))
      Column(Modifier.weight(1f).padding(start=10.dp)){
       Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
       Row{TextButton(onClick={info(t)}){Text("Info")};TextButton(onClick={model(t)}){Text("3D")}}
      }
     }
    }
   }
  }
 }
}
@Composable private fun NearbyPage(ts:List<Temple>,directions:(Temple)->Unit,info:(Temple)->Unit){
 LazyColumn(contentPadding=PaddingValues(bottom=16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
  item{Head("Nearby Places",14,"Browse temples and open maps for travel distances")}
  item{Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
   Text("📍 Live distance requires location permission and mapping services.",color=Muted,fontSize=12.sp)
  }}
  items(ts){t->
   Panel(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){
     Art(Modifier.size(75.dp,70.dp),ts.indexOf(t))
     Column(Modifier.weight(1f).padding(start=10.dp)){Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
      Text("Bagan, Myanmar",color=Muted,fontSize=12.sp)}
    }
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){GButton("Directions",{directions(t)});Outline("Details",{info(t)})}
   }
  }
 }
}
@Composable private fun OfflinePage(ts:List<Temple>,status:String,onBatch:()->Unit,onView:(Temple)->Unit){
 val ctx=LocalContext.current
 val cached=ts.count{ModelRepository.cached(ctx,it).exists()}
 LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Head("Offline Model Library",15,"Download & cache GLBs from cPanel")}
  item{Panel(Modifier.fillMaxWidth()){
   Text("Offline models ready: $cached / ${ts.size}",fontWeight=FontWeight.Bold,color=Ink)
   LinearProgressIndicator(progress={if(ts.isEmpty())0f else cached.toFloat()/ts.size},
     modifier=Modifier.fillMaxWidth().padding(vertical=11.dp))
   if(status.isNotEmpty())Text(status,color=Muted,fontSize=12.sp)
   GButton("↓ Download All Available Models",onBatch,Modifier.fillMaxWidth())
  }}
  items(ts){t->
   Panel(Modifier.fillMaxWidth()){
    Row(verticalAlignment=Alignment.CenterVertically){
     Text("⬡",fontSize=24.sp,color=Gold)
     Column(Modifier.weight(1f).padding(start=9.dp)){
      Text(t.nameEn,color=Ink,fontWeight=FontWeight.Bold)
      Text(if(ModelRepository.cached(ctx,t).exists())"Cached" else "Not downloaded",color=Muted,fontSize=11.sp)
     }
     GButton("View / ↓",{onView(t)})
    }
   }
  }
 }
}
@Composable private fun SettingsPage(language:String,quality:String,offline:Boolean,onLang:()->Unit,onQuality:(String)->Unit,
 onOffline:(Boolean)->Unit,onDiagnostics:()->Unit,onClear:()->Unit,onHelp:()->Unit,onAbout:()->Unit){
 var showQuality by remember{mutableStateOf(false)}
 LazyColumn(contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
  item{Head("Settings",16,"App settings & content")}
  item{Panel(Modifier.fillMaxWidth()){
   Text("App Settings",fontWeight=FontWeight.Bold,color=Ink)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    Text("Language",color=Ink);TextButton(onClick=onLang){Text(language)}
   }
   HorizontalDivider(color=CardBorder)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    Text("3D Model Quality",color=Ink);TextButton(onClick={showQuality=true}){Text(quality)}
   }
   HorizontalDivider(color=CardBorder)
   Row(verticalAlignment=Alignment.CenterVertically){Text("Offline Mode",modifier=Modifier.weight(1f),color=Ink)
    Switch(checked=offline,onCheckedChange=onOffline)}
  }}
  item{Panel(Modifier.fillMaxWidth()){
   GButton("Clear Downloaded GLB Cache",onClear,Modifier.fillMaxWidth())
   Text("Demo model remains bundled in the APK.",color=Muted,fontSize=12.sp)
  }}
  item{Panel(Modifier.fillMaxWidth()){
   Outline("3D / AR Diagnostics",onDiagnostics,Modifier.fillMaxWidth())
   Spacer(Modifier.height(9.dp))
   Outline("Help & User Guide",onHelp,Modifier.fillMaxWidth())
   Spacer(Modifier.height(9.dp))
   Outline("About Bagan",onAbout,Modifier.fillMaxWidth())
  }}
 }
 if(showQuality)AlertDialog(onDismissRequest={showQuality=false},title={Text("3D Quality")},
  text={Column{listOf("High","Medium","Low").forEach{v->TextButton(onClick={onQuality(v);showQuality=false}){Text(v)}}}},
  confirmButton={TextButton(onClick={showQuality=false}){Text("Close")}})
}
@Composable private fun HelpPage(onNavigate:(Int)->Unit){
 LazyColumn(contentPadding=PaddingValues(13.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Head("Help & Guide",17,"Using 3D, AR and Offline Mode")}
  items(listOf(5 to "How to use 3D Viewer",6 to "How to use AR Camera",15 to "How to download GLB files",
    7 to "Using Google Maps",20 to "Fix 3D / AR errors")){
    (p,t)->Panel(Modifier.fillMaxWidth().clickable{onNavigate(p)}){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
      Text(t,color=Ink);Text("›",color=Gold,fontSize=20.sp)
     }
    }
  }
  item{Panel(Modifier.fillMaxWidth()){
   Text("3D not showing?",fontWeight=FontWeight.Bold,color=Ink)
   Text("Check whether the corresponding file is present under https://aaii.asia/bagan/3d/models/ . You can use Offline Demo when a server model is unavailable.",color=Muted)
  }}
 }
}
@Composable private fun AboutPage(onHome:()->Unit){
 LazyColumn(contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Art(Modifier.fillMaxWidth().height(220.dp),5)}
  item{Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){
   Text("BAGAN",color=Navy,fontWeight=FontWeight.Black,fontSize=34.sp)
   Text("HERITAGE MUSEUM",color=Gold,fontSize=20.sp,fontWeight=FontWeight.Bold)
   Text("Native Android • V3.5.3",color=Muted)
  }}
  item{Panel(Modifier.fillMaxWidth()){
   Text("Explore Bagan's ancient architectural heritage with native 3D models, AR, guides, favorites and offline caches.",color=Ink)
   Spacer(Modifier.height(9.dp))
   Text("Powered by AAII.ASIA",color=Muted,fontSize=12.sp)
  }}
  item{Panel(Modifier.fillMaxWidth()){
   Text("Important",color=Ink,fontWeight=FontWeight.Bold)
   Text("3D models require accessible cPanel GLB files. AR requires a supported Android camera and ARCore services. Gallery imagery is illustrative until approved photographic assets are added.",color=Muted)
  }}
  item{GButton("Return Home",onHome,Modifier.fillMaxWidth())}
 }
}
@Composable private fun LoadingPage(t:Temple?,progress:Int,onCancel:()->Unit){
 Box(Modifier.fillMaxSize().background(Navy),contentAlignment=Alignment.Center){
  Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(24.dp)){
   Text("⬡",color=Gold,fontSize=55.sp)
   Spacer(Modifier.height(20.dp))
   CircularProgressIndicator(progress={progress.coerceIn(0,100)/100f},
    modifier=Modifier.size(115.dp),color=Gold,strokeWidth=7.dp)
   Spacer(Modifier.height(14.dp))
   Text("$progress%",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Gold)
   Text("Loading 3D Model",color=Color.White,fontSize=20.sp)
   Text(t?.nameEn?:"Bagan",color=Color.LightGray)
   Spacer(Modifier.height(20.dp))
   Text("Downloading and validating cPanel GLB data",fontSize=12.sp,color=Color.White,textAlign=TextAlign.Center)
   Spacer(Modifier.height(30.dp))
   Outline("Cancel",onCancel,Modifier.fillMaxWidth())
  }
 }
}
@Composable private fun ErrorPage(message:String,onRetry:()->Unit,onDemo:()->Unit,onBack:()->Unit){
 Box(Modifier.fillMaxSize().background(Navy),contentAlignment=Alignment.Center){
  Column(Modifier.fillMaxWidth().padding(25.dp),horizontalAlignment=Alignment.CenterHorizontally){
   Text("!",color=Color.White,fontSize=34.sp,modifier=Modifier.background(Color(0xFFB7323D),RoundedCornerShape(100.dp)).padding(horizontal=23.dp,vertical=7.dp))
   Spacer(Modifier.height(15.dp))
   Text("3D Model Not Available",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold)
   Spacer(Modifier.height(8.dp))
   Text(message.ifBlank{"Model could not be loaded"},color=Color.LightGray,textAlign=TextAlign.Center,fontSize=12.sp)
   Spacer(Modifier.height(20.dp))
   GButton("↻ Retry Download",onRetry,Modifier.fillMaxWidth())
   Spacer(Modifier.height(10.dp))
   Outline("Use Offline Demo Model",onDemo,Modifier.fillMaxWidth())
   Spacer(Modifier.height(10.dp))
   Outline("Go Back",onBack,Modifier.fillMaxWidth())
  }
 }
}
