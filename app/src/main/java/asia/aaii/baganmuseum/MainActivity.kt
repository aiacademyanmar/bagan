package asia.aaii.baganmuseum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val night = Color(0xFF130E0C)
private val earth = Color(0xFF302018)
private val gold = Color(0xFFF3BD69)
private val cream = Color(0xFFFFE8CB)
private val grey = Color(0xFFC9B7A5)
private data class Exhibit(val icon:String,val title:String,val category:String,val description:String)
private val samples = listOf(
    Exhibit("🏛️","ပုဂံဗိသုကာ","Architecture","ပုဂံဘုရားပုထိုးများ၏ အုတ်စီပညာ၊ မုခ်ခုံးနှင့် ရှေးဟောင်းဒီဇိုင်းကို လေ့လာနိုင်သည်။"),
    Exhibit("🎨","နံရံဆေးရေး","Art","ပုဂံခေတ် နံရံဆေးရေးပန်းချီသည် အနုပညာနှင့် လူမှုသမိုင်းကို ဖော်ပြထားသည်။"),
    Exhibit("📜","ကျောက်စာနှင့်မှတ်တမ်း","History","ပုဂံခေတ် ကျောက်စာအမွေအနှစ်သည် ရှေးဟောင်းစာပေသမိုင်းကို လေ့လာရန် အရေးပါသည်။"),
    Exhibit("🏺","မြေအိုးလက်မှု","Culture","ရှေးခေတ် လူနေမှုဘဝတွင် အသုံးပြုသော ရိုးရာ လက်မှုပစ္စည်းများအကြောင်း မိတ်ဆက်ထားသည်။"),
    Exhibit("🪷","ပန်းပုလက်ရာ","Art","ပုဂံခေတ် ကျောက်ဆစ်နှင့် ပန်းတမော့အနုပညာလက်ရာများ၏ အခြေခံသမိုင်း။"),
    Exhibit("🛕","ဘုရားပုထိုးများ","Architecture","ပုဂံဒေသ၏ ထင်ရှားသော ဗိသုကာလက်ရာများနှင့် ဘာသာရေးအမွေအနှစ်များ။"),
    Exhibit("☸️","ယဉ်ကျေးမှုအမွေ","Culture","ပုဂံခေတ် ဗုဒ္ဓဘာသာအနုပညာနှင့် ယဉ်ကျေးမှုဆိုင်ရာ မိတ်ဆက်စာ။"),
    Exhibit("🧵","ရိုးရာလက်မှုပညာ","Culture","မြန်မာ့ရိုးရာ လက်မှုလက်ရာများနှင့် ယဉ်ကျေးမှုဆက်နွယ်မှုများ။")
)

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme=darkColorScheme(primary=gold,background=night,surface=earth,onSurface=cream,onBackground=cream,onPrimary=night)) {
                Museum()
            }
        }
    }
}

@Composable
private fun Museum() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { context.getSharedPreferences("museum",0) }
    var page by rememberSaveable { mutableStateOf("Home") }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var tour by rememberSaveable { mutableIntStateOf(store.getInt("tour",0)) }
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var favs by remember { mutableStateOf(store.getStringSet("favs",emptySet())?.toSet() ?: emptySet<String>()) }
    var first by rememberSaveable { mutableStateOf(!store.getBoolean("entered",false)) }
    BackHandler(!first && page!="Home") { page="Home" }
    if(first) {
        Column(Modifier.fillMaxSize().background(night).padding(20.dp),verticalArrangement=Arrangement.Center) {
            Banner()
            Spacer(Modifier.height(24.dp))
            Text("ပုဂံပြတိုက်မှ ကြိုဆိုပါသည်",color=cream,fontSize=23.sp,fontWeight=FontWeight.Bold)
            Text("Myanmar History • Art • Heritage",color=grey)
            Spacer(Modifier.height(24.dp))
            Button(onClick={store.edit().putBoolean("entered",true).apply();first=false},modifier=Modifier.fillMaxWidth()) {Text("Enter Museum • ဝင်ရောက်ရန်")}
        }
        return
    }
    Column(Modifier.fillMaxSize().background(night)) {
        Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("BAGAN MUSEUM",color=gold,fontWeight=FontWeight.Black,fontSize=21.sp)
                Text("Digital Heritage Experience • V1.5",color=grey,fontSize=11.sp)
            }
            TextButton(onClick={page="Settings"}) { Text("⚙") }
        }
        HorizontalDivider(color=earth)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
            when(page) {
                "Home" -> {
                    Banner()
                    Title("Explore Bagan")
                    Text("ပုဂံသမိုင်း၊ ယဉ်ကျေးမှုနှင့် ရှေးဟောင်းပစ္စည်းများ",color=grey)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={page="Gallery"},modifier=Modifier.fillMaxWidth()) {Text("Explore Galleries")}
                    OutlinedButton(onClick={page="Floor Plan"},modifier=Modifier.fillMaxWidth()) {Text("Interactive Museum Floor Plan")}
                    OutlinedButton(onClick={page="History"},modifier=Modifier.fillMaxWidth()) {Text("Bagan History Timeline")}
                    OutlinedButton(onClick={page="3D"},modifier=Modifier.fillMaxWidth()) {Text("Explore Interactive 3D Museum")}
                    OutlinedButton(onClick={page="Audio"},modifier=Modifier.fillMaxWidth()) {Text("မြန်မာ / English Audio Guide")}
                    Button(onClick={page="AR"},modifier=Modifier.fillMaxWidth()) {Text("AR Camera • Place 3D Museum")}
                    OutlinedButton(onClick={page="Artifacts"},modifier=Modifier.fillMaxWidth()) {Text("3D Artifact Collection • 4 Interactive Models")}
                    OutlinedButton(onClick={page="Rooms"},modifier=Modifier.fillMaxWidth()) {Text("Virtual Indoor Navigation • 5 Rooms")}
                    Button(onClick={page="Drive Library"},modifier=Modifier.fillMaxWidth()) {Text("Bagan Drive Heritage Library • 10 Models")}
                    OutlinedButton(onClick={page="Quiz"},modifier=Modifier.fillMaxWidth()) {Text("Offline Learning & Quiz")}
                    OutlinedButton(onClick={page="Favorites"},modifier=Modifier.fillMaxWidth()) {Text("My Saved Exhibitions")}
                    Title("Featured Exhibitions")
                    samples.take(3).forEachIndexed { i,_ -> ExhibitRow(i){index=i;page="Detail"} }
                }
                "Gallery" -> {
                    Title("Museum Galleries")
                    OutlinedTextField(value=search,onValueChange={search=it},label={Text("Search Exhibits")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                    Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        listOf("All","Art","History","Culture").forEach { c ->
                            FilterChip(selected=category==c,onClick={category=c},label={Text(c,fontSize=11.sp)})
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    samples.indices.filter { (category=="All" || samples[it].category==category) && (samples[it].title.contains(search,true)||samples[it].category.contains(search,true)) }.forEach { i ->
                        ExhibitRow(i){index=i;page="Detail"}
                    }
                    Note()
                }
                "Floor Plan" -> {
                    Title("Museum Floor Plan")
                    Text("ပြခန်း ၅ ခန်း — Interactive conceptual layout",color=grey)
                    Spacer(Modifier.height(12.dp))
                    listOf(0,2,1,4,3).forEachIndexed { n,i ->
                        Surface(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable {index=i;page="Detail"},color=earth,shape=RoundedCornerShape(14.dp)) {
                            Row(Modifier.padding(17.dp),verticalAlignment=Alignment.CenterVertically) {
                                Text("ROOM "+(n+1),color=gold,fontSize=12.sp)
                                Spacer(Modifier.width(16.dp))
                                Text(samples[i].icon+"  "+samples[i].title,color=cream)
                            }
                        }
                    }
                    Text("※ အထက်ပါ Layout သည် Concept Design ဖြစ်ပြီး တကယ့်ပြတိုက်၏ Floor Plan မဟုတ်ပါ။",color=grey,fontSize=12.sp)
                }
                "3D" -> { ThreeDMuseumScreen() }
                "Artifacts" -> { ArtifactCollectionV14() }
                "Drive Library" -> { DriveMuseumCatalog() }
                "Quiz" -> { MuseumQuizV15() }
                "Rooms" -> { MuseumIndoorNavigatorV14(onExplore3d={page="3D"},onArtifact={page="Artifacts"}) }
                "AR" -> {
                    Title("Full Museum AR Placement")
                    Text("ARCore plane tracking: find a floor and tap to place the Bagan Museum GLB concept model.",color=grey)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={context.startActivity(android.content.Intent(context,ArMuseumActivity::class.java))},modifier=Modifier.fillMaxWidth()) {Text("📷 Place Museum in AR")}
                    Spacer(Modifier.height(12.dp))
                    Text("An ARCore-compatible phone and Google Play Services for AR are required. The app loads the included GLB. If it cannot load, a simplified fallback sculpture is shown.",color=grey)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick={page="3D"},modifier=Modifier.fillMaxWidth()) {Text("Open Offline 3D Museum Instead")}
                }
                "Audio" -> { NativeGuideScreen() }
                "History" -> {
                    Title("Bagan History Timeline")
                    listOf(
                        "9th–10th century" to "ပုဂံနိုင်ငံတော်၏ အစောပိုင်း ဖွံ့ဖြိုးတိုးတက်မှု",
                        "11th century" to "အနော်ရထာမင်းလက်ထက် ပုဂံခေတ် အရေးပါလာမှု",
                        "12th century" to "ဗိသုကာ၊ ပန်းချီနှင့် အနုပညာ ထွန်းကားမှု",
                        "13th century" to "ပုဂံခေတ် ယဉ်ကျေးမှုအမွေအနှစ်များ",
                        "2019" to "Bagan UNESCO World Heritage List ဝင်ရောက်မှု"
                    ).forEach { (date,label) ->
                        Surface(Modifier.fillMaxWidth().padding(vertical=5.dp),color=earth,shape=RoundedCornerShape(12.dp)){
                            Column(Modifier.padding(16.dp)){
                                Text(date,color=gold,fontWeight=FontWeight.Bold)
                                Text(label,color=cream)
                            }
                        }
                    }
                }
                "Tour" -> {
                    Title("Digital Guided Tour")
                    Text("Stop "+(tour+1)+" / 5",color=gold)
                    LinearProgressIndicator(progress={(tour+1)/5f},modifier=Modifier.fillMaxWidth())
                    val i=listOf(0,2,1,4,3)[tour]
                    Spacer(Modifier.height(18.dp))
                    Text(samples[i].icon,fontSize=64.sp)
                    Text(samples[i].title,color=cream,fontSize=24.sp,fontWeight=FontWeight.Bold)
                    Text(samples[i].description,color=grey)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick={index=i;page="Detail"}) {Text("View Exhibition")}
                    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        OutlinedButton(enabled=tour>0,onClick={tour=(tour-1).coerceAtLeast(0);store.edit().putInt("tour",tour).apply()},modifier=Modifier.weight(1f)) {Text("Previous")}
                        Button(enabled=tour<4,onClick={tour=(tour+1).coerceAtMost(4);store.edit().putInt("tour",tour).apply()},modifier=Modifier.weight(1f)) {Text("Next")}
                    }
                    Text("Progress saved locally on this device",color=grey,fontSize=12.sp)
                }
                "Favorites" -> {
                    Title("Saved Exhibitions")
                    val saved=samples.indices.filter{it.toString() in favs}
                    if(saved.isEmpty()) Text("Favorites မရှိသေးပါ",color=grey)
                    saved.forEach{ i-> ExhibitRow(i){index=i;page="Detail"} }
                }
                "Detail" -> {
                    TextButton(onClick={page="Gallery"}) {Text("‹ Back to galleries")}
                    Text(samples[index].icon,fontSize=72.sp)
                    Text(samples[index].category,color=gold)
                    Title(samples[index].title)
                    Text(samples[index].description,color=cream)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick={
                        val id=index.toString()
                        favs=if(id in favs) favs-id else favs+id
                        store.edit().putStringSet("favs",favs).apply()
                    }) { Text(if(index.toString() in favs) "♥ Remove Favorite" else "♡ Add Favorite") }
                    OutlinedButton(onClick={page="3D"},modifier=Modifier.fillMaxWidth()) {Text("Explore Museum 3D Concept / Hotspots")}
                    OutlinedButton(onClick={page="Audio"},modifier=Modifier.fillMaxWidth()) {Text("Hear Related Audio Guide")}
                    OutlinedButton(onClick={page="Artifacts"},modifier=Modifier.fillMaxWidth()) {Text("Explore 3D Artifact Collection")}
                    OutlinedButton(onClick={page="Audio"},modifier=Modifier.fillMaxWidth()) {Text("Hear Related Audio Guide")}
                    Note()
                }
                "Settings" -> {
                    Title("Settings")
                    Text("Bagan Museum Native V1.5",color=cream)
                    Text("Offline-first educational demo",color=grey)
                    Spacer(Modifier.height(18.dp))
                    OutlinedButton(onClick={store.edit().remove("entered").apply();first=true}) {Text("Show Welcome Screen")}
                    Title("Future Updates")
                    Text("3D and Audio Guide are in V1.5. AR world tracking and GPS Maps need a future update.",color=grey)
                    Note()
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        HorizontalDivider(color=earth)
        Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("Home" to "⌂","Drive Library" to "▦","3D" to "◈","Quiz" to "✓","Rooms" to "▤").forEach { (item,icon) ->
                TextButton(onClick={page=item},contentPadding=PaddingValues(horizontal=3.dp,vertical=2.dp)) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(icon,color=if(page==item) gold else grey,fontSize=19.sp)
                        Text(item,color=if(page==item) gold else grey,fontSize=10.sp)
                    }
                }
            }
        }
    }
}
@Composable private fun Banner() {
    Box(
        Modifier.fillMaxWidth().height(208.dp),
        contentAlignment=Alignment.BottomStart
    ) {
        Image(
            painter=painterResource(id=R.drawable.museum_hero),
            contentDescription="Bagan Museum architectural illustration",
            contentScale=ContentScale.Crop,
            modifier=Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color(0x9E130E0C)))
        ))
        Column(Modifier.padding(17.dp)) {
            Text("✧ BAGAN MUSEUM ✧",color=cream,fontSize=25.sp,fontWeight=FontWeight.Black)
            Text("A journey through Myanmar heritage",color=cream)
            Text("ART • HISTORY • INTERACTIVE 3D",color=gold,fontSize=10.sp)
        }
    }
}
@Composable private fun Title(s:String) {
    Spacer(Modifier.height(14.dp))
    Text(s,color=cream,fontSize=22.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
}
@Composable private fun ExhibitRow(i:Int,open:()->Unit) {
    Surface(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable(onClick=open),color=earth,shape=RoundedCornerShape(14.dp)) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(samples[i].icon,fontSize=30.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(samples[i].title,color=cream,fontWeight=FontWeight.Bold)
                Text(samples[i].category,color=grey,fontSize=12.sp)
            }
            Text("›",color=gold,fontSize=22.sp)
        }
    }
}
@Composable private fun Note() {
    Spacer(Modifier.height(12.dp))
    Text("Educational sample content, not an inventory of real museum artifacts.",color=grey,fontSize=11.sp)
}
