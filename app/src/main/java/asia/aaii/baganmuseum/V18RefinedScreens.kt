package asia.aaii.baganmuseum

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val darkBrown=Color(0xFF39240F)
private val softGold=Color(0xFFE6B45B)
private val cardGold=Color(0xFFF2C46E)
private val orangeEarth=Color(0xFFB97435)
private val paleIvory=Color(0xFFFFFBF6)
private val bgIvory=Color(0xFFF7F1E8)
private val fineInk=Color(0xFF3E2B19)
private val quietInk=Color(0xFF8B7460)
private val darkPanel=Color(0xFF24190F)

@Composable
internal fun V18ScreenHeading(title:String,detail:String?=null){
    Text(title,fontSize=22.sp,fontWeight=FontWeight.Black,color=fineInk)
    if(detail!=null)Text(detail,fontSize=12.sp,color=quietInk,modifier=Modifier.padding(top=3.dp,bottom=8.dp))
}
@Composable
private fun PhotoConcept(height:Int,overlay:String?=null,modifier:Modifier=Modifier,tag:String?=null){
    Box(modifier.height(height.dp).clip(RoundedCornerShape(17.dp))){
        Image(painter=painterResource(R.drawable.museum_hero),contentDescription="Bagan architectural concept art",
            modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0x00000000),Color(0x280E0803),Color(0xD7251408)))))
        if(tag!=null){
            Box(Modifier.align(Alignment.TopStart).padding(10.dp)
                .background(darkBrown.copy(alpha=.86f),RoundedCornerShape(16.dp))
                .padding(horizontal=10.dp,vertical=5.dp)){
                Text(tag,color=cardGold,fontSize=10.sp,fontWeight=FontWeight.Bold)
            }
        }
        if(overlay!=null)Text(overlay,color=Color.White,fontWeight=FontWeight.Black,
            fontSize=19.sp,modifier=Modifier.align(Alignment.BottomStart).padding(15.dp))
    }
}
@Composable
private fun MiniMonumentPhoto(index:Int,modifier:Modifier=Modifier){
 val keys=listOf("thatbyinnyu","myazedi","entrance","tharabar","gawdawpalin","ananda","htilominlo","alodawpyae","shwezigon","bupaya")
 MonumentArtworkV19(keys[index.coerceIn(0,9)],modifier)
}
@Composable
private fun FeatureTile(symbol:String,title:String,onClick:()->Unit,modifier:Modifier=Modifier){
    Column(modifier.clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally){
        Box(Modifier.size(55.dp).clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(cardGold,softGold))),
            contentAlignment=Alignment.Center){
            Text(symbol,fontSize=25.sp,color=darkBrown)
        }
        Spacer(Modifier.height(6.dp))
        Text(title,color=fineInk,fontSize=10.sp,fontWeight=FontWeight.SemiBold,maxLines=1)
    }
}

@Composable
internal fun V18Home(items:List<V17Monument>,go:(String)->Unit,open:(Int)->Unit) {
    PhotoConcept(179,overlay="Explore Bagan Heritage",tag="BAGAN MUSEUM  •  ယဉ်ကျေးမှုအမွေအနှစ်")
    Spacer(Modifier.height(9.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround){
        FeatureTile("▣","3D Gallery",{go("Heritage 3D")})
        FeatureTile("◎","AR View",{go("AR View")})
        FeatureTile("▤","Museum Map",{go("Museum Map")})
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround){
        FeatureTile("▥","History",{go("History Timeline")})
        FeatureTile("🏺","Collections",{go("Collections")})
        FeatureTile("✓","Quiz",{go("Quiz")})
    }
    Spacer(Modifier.height(8.dp))
    V17Section("Featured Monuments","See all") {go("Gallery")}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
        items.take(3).forEachIndexed{index,item->
            Surface(onClick={open(index)},modifier=Modifier.weight(1f),color=paleIvory,
                shape=RoundedCornerShape(14.dp),shadowElevation=1.dp){
                Column(Modifier.padding(6.dp)){
                    V21HeritagePhoto(item,Modifier.fillMaxWidth().height(95.dp))
                    Spacer(Modifier.height(7.dp))
                    Text(item.mm,fontSize=10.sp,fontWeight=FontWeight.Bold,color=fineInk,maxLines=1)
                    Text(item.name,fontSize=9.sp,color=quietInk,maxLines=1,overflow=TextOverflow.Ellipsis)
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        V17NeutralButton("▤  Heritage Map",Modifier.weight(1f)){go("Museum Map")}
        V17NeutralButton("▧  Real Photos ↗",Modifier.weight(1f)){go("Heritage Photos")}
    }
    V17Section("Heritage Learning","Explore") {go("Offline Learning")}
    Row(Modifier.fillMaxWidth().background(Color(0xFFFAE9CC),RoundedCornerShape(17.dp)).padding(12.dp),
        verticalAlignment=Alignment.CenterVertically){
        Text("🎓",fontSize=37.sp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)){
            Text("Learn Bagan History",fontWeight=FontWeight.Bold,color=fineInk)
            Text("Quiz • Audio Guide • Offline 3D",fontSize=11.sp,color=quietInk)
        }
        TextButton(onClick={go("Quiz")}){Text("Start ›",color=orangeEarth,fontWeight=FontWeight.Bold)}
    }
}
@Composable
private fun ListCard(item:V17Monument,index:Int,selected:Boolean,open:()->Unit,toggle:()->Unit){
    Surface(onClick=open,modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),color=paleIvory,
        shape=RoundedCornerShape(14.dp),shadowElevation=1.dp){
        Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){
            V21HeritagePhoto(item,Modifier.size(width=90.dp,height=78.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)){
                Text(item.mm,color=fineInk,fontWeight=FontWeight.Bold,fontSize=13.sp,maxLines=1)
                Text(item.name,color=quietInk,fontSize=11.sp,maxLines=1)
                Text(item.category+"  •  Concept 3D available",
                    color=orangeEarth,fontSize=9.sp,maxLines=1)
            }
            TextButton(onClick=toggle,contentPadding=PaddingValues(0.dp),
                modifier=Modifier.width(40.dp)){
                Text(if(selected)"♥" else "♡",color=if(selected)Color(0xFFBC3434) else orangeEarth,fontSize=25.sp)
            }
        }
    }
}
@Composable
internal fun V18Gallery(items:List<V17Monument>,category:String,setCategory:(String)->Unit,
    search:String,setSearch:(String)->Unit,saved:Set<String>,open:(Int)->Unit,toggle:(String)->Unit,openPhoto:()->Unit){
    V18ScreenHeading("Explore Monuments","10 locations from your Drive heritage collection")
    OutlinedTextField(value=search,onValueChange=setSearch,modifier=Modifier.fillMaxWidth(),
        leadingIcon={Text("⌕",fontSize=27.sp,color=quietInk)},singleLine=true,
        placeholder={Text("Search monuments, artifacts…",fontSize=12.sp)},
        shape=RoundedCornerShape(15.dp),
        colors=OutlinedTextFieldDefaults.colors(focusedContainerColor=paleIvory,
            unfocusedContainerColor=paleIvory))
    Spacer(Modifier.height(10.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        listOf("All","Temple","Pagoda","Gate","Inscription").forEach{V17Pill(it,category==it){setCategory(it)}}
    }
    Spacer(Modifier.height(10.dp))
    var visible=0
    items.forEachIndexed{index,item->
        if((category=="All"||item.category==category) &&
            (search.isBlank()||item.name.contains(search,true)||item.mm.contains(search,true))){
            visible++
            ListCard(item,index,item.slug in saved,{open(index)},{toggle(item.slug)})
        }
    }
    if(visible==0)Text("No results — try a different category.",color=quietInk)
    V17NeutralButton("Browse heritage photographs online",Modifier.fillMaxWidth()){ openPhoto() }
    Text("Images are illustrative, not verified photos of the referenced monuments.",
        color=quietInk,fontSize=10.sp,modifier=Modifier.padding(top=10.dp))
}
@Composable
internal fun V18Detail(item:V17Monument,index:Int,saved:Boolean,toggle:()->Unit,go:(String)->Unit){
    val ctx=LocalContext.current
    V21HeritagePhoto(item,Modifier.fillMaxWidth().height(235.dp),showLabel=true)
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f)){
            Text(item.mm,color=fineInk,fontSize=22.sp,fontWeight=FontWeight.Black)
            Text(item.name,color=quietInk,fontSize=15.sp)
        }
        TextButton(onClick=toggle){Text(if(saved)"♥" else "♡",fontSize=28.sp,
            color=if(saved)Color(0xFFCA3540) else orangeEarth)}
    }
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        V17Pill(item.category,true){}
        V17Pill("Offline 3D",false){go("Heritage 3D")}
        V17Pill("Audio",false){go("Audio Guide")}
    }
    V17Section("About")
    Text(item.detail,color=fineInk,fontSize=14.sp,lineHeight=24.sp)
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){
        V17GoldenButton("◈  View 3D",Modifier.weight(1f)){go("Heritage 3D")}
        V17NeutralButton("♫  Audio",Modifier.weight(1f)){go("Audio Guide")}
    }
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("◎  AR Museum Concept",Modifier.fillMaxWidth()){go("AR View")}
    V17Section("3D Reference & Source")
    Surface(color=paleIvory,shape=RoundedCornerShape(15.dp)){
        Column(Modifier.padding(13.dp)){
            Text("Blender source collection",color=fineInk,fontWeight=FontWeight.Bold)
            Text("The authentic source remains on your Google Drive. The app's offline GLB is an educational concept model.",
                color=quietInk,fontSize=11.sp)
            Spacer(Modifier.height(7.dp))
            V17GoldenButton("Open Google Drive",Modifier.fillMaxWidth()){
                v17Open(ctx,"https://drive.google.com/drive/folders/"+item.folder)
            }
        }
    }
    Text("Photos open online; maps search locations, and the 3D artworks are illustrative.",color=quietInk,fontSize=11.sp)
    V20HeritageDetail(item)
    V21CommonsLicense(item)

}
@Composable
internal fun V18Bottom(current:String,onSelect:(String)->Unit){
    val options=listOf(Triple("Home","⌂","Home"),Triple("Gallery","▣","Gallery"),
        Triple("Map","▤","Museum Map"),Triple("Quiz","✧","Quiz"),Triple("More","•••","Menu"))
    Row(Modifier.fillMaxWidth().height(67.dp).background(
        Brush.verticalGradient(listOf(Color(0xFF4C300E),darkBrown)))
        .padding(horizontal=4.dp,vertical=5.dp),horizontalArrangement=Arrangement.SpaceEvenly){
        options.forEach{(label,symbol,route)->
            val active=current==route || (label=="Gallery"&&current=="Monument Detail") ||
                (label=="More"&&current in listOf("Settings","Profile","Certificate","Audio Guide"))
            Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(11.dp))
                .background(if(active)Color(0x584F330C) else Color.Transparent)
                .clickable{onSelect(route)},
                horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
                Text(symbol,color=if(active)cardGold else Color(0xFFE8D4AD),fontSize=22.sp,lineHeight=23.sp)
                Text(label,color=if(active)cardGold else Color(0xFFE8D4AD),
                    fontSize=10.sp,fontWeight=if(active)FontWeight.Black else FontWeight.Normal)
            }
        }
    }
}
@Composable
internal fun V18Header(page:String,onBack:()->Unit,onMenu:()->Unit){
    val isHome=page=="Home"
    Row(Modifier.fillMaxWidth().height(58.dp).background(
        Brush.horizontalGradient(listOf(darkBrown,Color(0xFF4E320F),darkBrown)))
        .padding(horizontal=13.dp),verticalAlignment=Alignment.CenterVertically){
        TextButton(onClick=if(isHome)onMenu else onBack,contentPadding=PaddingValues(0.dp),
            modifier=Modifier.width(37.dp)){
            Text(if(isHome)"☰" else "‹",color=Color.White,fontSize=27.sp)
        }
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
            if(isHome){
                Text("♜ BAGAN MUSEUM",color=cardGold,fontSize=17.sp,fontWeight=FontWeight.Black)
                Text("ပုဂံပြတိုက်",color=Color(0xFFF5E9D7),fontSize=10.sp)
            }else{
                Text(page.uppercase(),color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Bold,
                    maxLines=1,overflow=TextOverflow.Ellipsis)
            }
        }
        TextButton(onClick=onMenu,contentPadding=PaddingValues(0.dp),modifier=Modifier.width(38.dp)){
            Text("⋮",color=cardGold,fontSize=26.sp)
        }
    }
}
@Composable
internal fun V18Museum3D(selected:Int,onAR:()->Unit,onInfo:()->Unit){
    Text("3D HERITAGE EXPLORER",color=cardGold,fontWeight=FontWeight.Black,fontSize=21.sp)
    Text("10 Offline models • Rotate / Zoom / Switch • No internet needed",color=Color(0xFFDCC3A2),fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    // Keep the full-functionality local GLB WebGL viewer rather than a static mockup.
    HeritageOffline3DV16(selected)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        V17GoldenButton("◎  Place in AR",Modifier.weight(1f),onClick=onAR)
        V17NeutralButton("ⓘ  Model Info",Modifier.weight(1f),onClick=onInfo)
    }
    Text("Original 3D concept geometry; Drive .blend files are not exported to this APK.",
        fontSize=11.sp,color=Color(0xFFDCC3A2))
}
private val audioTopics18=listOf(
    Triple("Introduction / မိတ်ဆက်","ပုဂံဟာ မြန်မာ့သမိုင်းဝင် ဘုရားပုထိုးများနဲ့ ယဉ်ကျေးမှုအမွေအနှစ်များကြောင့် ထင်ရှားတဲ့ ဒေသတစ်ခုဖြစ်ပါတယ်။","Bagan is celebrated for its ancient monuments and Myanmar cultural heritage."),
    Triple("Architecture / ဗိသုကာ","ပုဂံဗိသုကာမှာ အုတ်စီပညာ မုခ်ခုံးနဲ့ ဘုရားပုထိုး အဆောက်အအုံ အနုပညာတွေကို တွေ့နိုင်ပါတယ်။","Bagan architecture is notable for its brick structures arches and temples."),
    Triple("Temple Studies / ဘုရားပုထိုး","ပုဂံဘုရားပုထိုးများဟာ အနုပညာလက်ရာတွေကို လေ့လာနိုင်တဲ့ သမိုင်းဝင် အမွေအနှစ်များဖြစ်ပါတယ်။","The temples of Bagan showcase centuries of architecture and craftsmanship."),
    Triple("Inscriptions / ကျောက်စာ","ကျောက်စာတွေဟာ ပုဂံခေတ် စာပေ လူမှုရေးနဲ့ အလှူဒါန မှတ်တမ်းတွေကို သိရှိစေပါတယ်။","Historical inscriptions preserve information on language society and donations."),
    Triple("Conservation / ထိန်းသိမ်းရေး","ပုဂံအမွေအနှစ်တွေကို အနာဂတ်မျိုးဆက်တွေအတွက် စနစ်တကျ ထိန်းသိမ်းဖို့ လိုအပ်ပါတယ်။","Conservation protects Bagan's cultural heritage for future generations.")
)
@Composable
internal fun V18AudioGuide(){
    val ctx=LocalContext.current
    var ready by remember{mutableStateOf(false)}
    var message by remember{mutableStateOf("Initializing Android speech engine…")}
    var lang by remember{mutableStateOf("မြန်မာ")}
    var current by remember{mutableIntStateOf(0)}
    var slow by remember{mutableStateOf(false)}
    val tts=remember(ctx){TextToSpeech(ctx){status->
        ready=status==TextToSpeech.SUCCESS
        message=if(ready)"Speech engine ready" else "Speech engine unavailable"
    }}
    DisposableEffect(tts){onDispose{tts.stop();tts.shutdown()}}
    Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(17.dp))){
        Image(painter=painterResource(R.drawable.museum_hero),contentDescription=null,
            modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color.Transparent,Color(0xE6221408)))))
        Column(Modifier.align(Alignment.BottomStart).padding(15.dp)){
            Text("MUSEUM AUDIO GUIDE",color=cardGold,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text(audioTopics18[current].first,color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Black)
        }
    }
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
        V17Pill("မြန်မာ",lang=="မြန်မာ",Modifier.weight(1f)){lang="မြန်မာ";tts.stop()}
        V17Pill("English",lang=="English",Modifier.weight(1f)){lang="English";tts.stop()}
    }
    Spacer(Modifier.height(12.dp))
    Surface(color=Color(0xFF382617),shape=RoundedCornerShape(17.dp)){
        Column(Modifier.fillMaxWidth().padding(16.dp)){
            Text("▶  "+audioTopics18[current].first,color=cardGold,fontWeight=FontWeight.Bold)
            Text(if(lang=="မြန်မာ")audioTopics18[current].second else audioTopics18[current].third,
                color=Color(0xFFEEE0CB),fontSize=12.sp,lineHeight=21.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically){
                V17NeutralButton("|‹",Modifier.weight(1f)){
                    tts.stop();current=(current+audioTopics18.size-1)%audioTopics18.size
                }
                V17GoldenButton("▶ Play",Modifier.weight(2f),enabled=ready){
                    val locale=if(lang=="မြန်မာ")Locale.forLanguageTag("my-MM") else Locale.US
                    val result=tts.setLanguage(locale)
                    if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED)
                        message=lang+" TTS voice not installed on this phone"
                    else{
                        tts.setSpeechRate(if(slow).75f else 1f)
                        tts.speak(if(lang=="မြန်မာ")audioTopics18[current].second else audioTopics18[current].third,
                            TextToSpeech.QUEUE_FLUSH,null,"bagan-v18")
                        message="Playing "+audioTopics18[current].first
                    }
                }
                V17NeutralButton("■",Modifier.weight(1f)){tts.stop();message="Stopped"}
                V17NeutralButton("›|",Modifier.weight(1f)){
                    tts.stop();current=(current+1)%audioTopics18.size
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("Slow voice",color=Color(0xFFDCC3A2),modifier=Modifier.weight(1f))
                Switch(checked=slow,onCheckedChange={slow=it})
            }
            Text(message,color=cardGold,fontSize=11.sp)
        }
    }
    Spacer(Modifier.height(9.dp))
    Text("Audio chapters",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold)
    audioTopics18.forEachIndexed{i,topic->
        Surface(onClick={tts.stop();current=i},color=if(i==current)Color(0xFF75512C)else Color(0xFF352417),
            shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){
            Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){
                Text(if(i==current)"▶" else "♫",color=cardGold)
                Spacer(Modifier.width(11.dp))
                Text(topic.first,color=Color.White,modifier=Modifier.weight(1f),fontSize=12.sp)
                Text("${(i+1).toString().padStart(2,'0')}",color=cardGold,fontSize=11.sp)
            }
        }
    }
    Text("Uses Android TTS; speech may require the correct installed Burmese/English voices. There are no pre-recorded tracks or estimated durations.",
        color=Color(0xFFCDB89B),fontSize=11.sp)
}
@Composable
internal fun V18Certificate(visitor:String,best:Int,onQuiz:()->Unit) {
    val ctx=LocalContext.current
    V18ScreenHeading("Learning Certificate","Bagan Heritage Quiz — educational achievement")
    Box(Modifier.fillMaxWidth().background(paleIvory,RoundedCornerShape(14.dp))
        .border(3.dp,softGold,RoundedCornerShape(14.dp)).padding(8.dp)){
        Column(Modifier.fillMaxWidth().border(1.dp,Color(0xFFC9A66F),RoundedCornerShape(9.dp))
            .padding(horizontal=13.dp,vertical=26.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Text("✧",fontSize=47.sp,color=softGold)
            Text("BAGAN MUSEUM",color=darkBrown,fontWeight=FontWeight.Black,fontSize=25.sp)
            Text("ပုဂံပြတိုက်",color=orangeEarth,fontSize=17.sp)
            Spacer(Modifier.height(20.dp))
            Text("CERTIFICATE",color=darkBrown,fontSize=29.sp,fontWeight=FontWeight.Black)
            Text("OF ACHIEVEMENT",color=orangeEarth,fontSize=13.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(19.dp))
            Text("This commemorates the heritage quiz progress of",fontSize=12.sp,color=quietInk)
            Text(visitor.ifBlank{"Visitor"},fontSize=28.sp,fontWeight=FontWeight.Bold,color=darkBrown)
            Spacer(Modifier.height(12.dp))
            Text("Bagan Heritage Learning",fontSize=16.sp,color=orangeEarth)
            Text("Best Quiz Score  •  "+best+" / 6",fontSize=16.sp,color=fineInk)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color=softGold)
            Text("✧  EXPLORE  •  LEARN  •  PRESERVE  ✧",
                modifier=Modifier.padding(top=12.dp),color=orangeEarth,fontSize=11.sp)
            Text("Unofficial in-app learning record",fontSize=10.sp,color=quietInk)
        }
    }
    Spacer(Modifier.height(13.dp))
    V17GoldenButton("Share achievement text",Modifier.fillMaxWidth()){
        val message="Bagan Museum • Heritage Learning\n"+
            "Visitor: "+visitor.ifBlank{"Visitor"}+"\nBest quiz score: "+best+"/6\n"+
            "Unofficial educational app achievement."
        val send=Intent(Intent.ACTION_SEND).apply{
            type="text/plain";putExtra(Intent.EXTRA_TEXT,message)
        }
        ctx.startActivity(Intent.createChooser(send,"Share heritage achievement"))
    }
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("Retake Quiz",Modifier.fillMaxWidth(),onClick=onQuiz)
    Text("This screen displays a decorative, unofficial certificate; PDF generation and institutional verification are not included.",
        color=quietInk,fontSize=11.sp)
}
