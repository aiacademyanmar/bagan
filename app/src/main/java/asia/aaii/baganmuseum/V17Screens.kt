package asia.aaii.baganmuseum

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun V17Home(items:List<V17Monument>,go:(String)->Unit,open:(Int)->Unit){
    V17BrandHero(193,"10 Heritage Sites • Offline 3D • Museum Tour")
    V17Section("Explore Bagan Heritage","View all"){go("Gallery")}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        listOf("▣" to "3D Gallery","◎" to "AR View","▤" to "Museum Map").forEach{(icon,label)->
            Surface(onClick={go(when(label){"3D Gallery"->"Heritage 3D";"AR View"->"AR View";else->"Museum Map"})},
                modifier=Modifier.weight(1f),color=V17Yellow,shape=RoundedCornerShape(13.dp)){
                Column(Modifier.padding(vertical=15.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Text(icon,fontSize=25.sp,color=V17Brown)
                    Text(label,fontSize=11.sp,color=V17Brown)
                }
            }
        }
    }
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        listOf("▧" to "History Timeline","◉" to "Collections","✓" to "Quiz").forEach{(icon,label)->
            Surface(onClick={go(label)},modifier=Modifier.weight(1f),color=V17Linen,shape=RoundedCornerShape(13.dp)){
                Column(Modifier.padding(vertical=15.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Text(icon,fontSize=25.sp,color=V17Gold)
                    Text(label,fontSize=10.sp,color=V17Ink,maxLines=1)
                }
            }
        }
    }
    V17Section("Featured Monuments","See all"){go("Gallery")}
    items.take(4).forEachIndexed{i,item->V17MonumentTile(item,i,onClick={open(i)})}
    V17Section("Heritage Learning")
    V17GoldenButton("▶  Start Museum Quiz",Modifier.fillMaxWidth()){go("Quiz")}
    Spacer(Modifier.height(9.dp))
    V17NeutralButton("View offline 3D collection",Modifier.fillMaxWidth()){go("Heritage 3D")}
}
@Composable
internal fun V17Gallery(items:List<V17Monument>,category:String,setCategory:(String)->Unit,
    search:String,setSearch:(String)->Unit,favorites:Set<String>,open:(Int)->Unit,onFavorite:(String)->Unit){
    V17Section("Monument Galleries")
    OutlinedTextField(search,setSearch,modifier=Modifier.fillMaxWidth(),singleLine=true,
        placeholder={Text("Search monuments / ဘုရားအမည်",fontSize=12.sp)},
        shape=RoundedCornerShape(14.dp))
    Spacer(Modifier.height(12.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        listOf("All","Temple","Pagoda","Gate","Inscription").forEach{key->
            V17Pill(key,category==key){setCategory(key)}
        }
    }
    Spacer(Modifier.height(9.dp))
    var visible=0
    items.forEachIndexed{i,item->
        if((category=="All"||item.category==category)&&
            (item.mm.contains(search,true)||item.name.contains(search,true))){
            visible++
            V17MonumentTile(item,i,{open(i)}){
                TextButton(onClick={onFavorite(item.slug)}){
                    Text(if(item.slug in favorites)"♥" else "♡",fontSize=23.sp,
                        color=if(item.slug in favorites)Color.Red else V17Gold)
                }
            }
        }
    }
    if(visible==0)Text("No matching monument.",color=V17Mute)
}
@Composable
internal fun V17Detail(item:V17Monument,index:Int,isSaved:Boolean,toggle:()->Unit,go:(String)->Unit){
    V17BrandHero(225,item.name)
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f)){
            Text(item.mm,color=V17Ink,fontSize=22.sp,fontWeight=FontWeight.Black)
            Text(item.name,color=V17Mute)
        }
        TextButton(onClick=toggle){Text(if(isSaved)"♥" else "♡",fontSize=30.sp,
            color=if(isSaved)Color.Red else V17Gold)}
    }
    V17Pill(item.category,true){}
    V17Section("About this monument")
    Text(item.detail,color=V17Ink,fontSize=14.sp,lineHeight=25.sp)
    Spacer(Modifier.height(13.dp))
    V17GoldenButton("◈  View offline 3D model",Modifier.fillMaxWidth()){go("Heritage 3D")}
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("◎  Place museum concept in AR",Modifier.fillMaxWidth()){go("AR View")}
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("♫  Audio Guide",Modifier.fillMaxWidth()){go("Audio Guide")}
    val ctx=LocalContext.current
    V17Section("Original Blender reference")
    V17NeutralButton("Open Google Drive source",Modifier.fillMaxWidth()){
        v17Open(ctx,"https://drive.google.com/drive/folders/"+item.folder)
    }
    Text("Illustrated concept models are not measured archaeological scans.",color=V17Mute,fontSize=11.sp)
}
@Composable
internal fun V17AR(go:(String)->Unit){
    val ctx=LocalContext.current
    V17Section("AR Museum Experience")
    V17BrandHero(218,"Native ARCore camera placement")
    Spacer(Modifier.height(12.dp))
    Text("Point your phone at a flat surface, then tap to place the Bagan Museum concept GLB.",
        color=V17Ink)
    Spacer(Modifier.height(10.dp))
    V17GoldenButton("◎  Launch AR Camera",Modifier.fillMaxWidth()){
        try{ctx.startActivity(Intent(ctx,ArMuseumActivity::class.java))}
        catch(_:Exception){}
    }
    Spacer(Modifier.height(10.dp))
    V17NeutralButton("Use offline 3D viewer instead",Modifier.fillMaxWidth()){go("Heritage 3D")}
    Text("AR requires a supported Android phone and Google Play Services for AR. Actual device tracking is not verified.",
        fontSize=11.sp,color=V17Mute)
}
@Composable
internal fun V17Map(go:(String)->Unit){
    V17Section("Museum Map")
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
        V17Pill("2D Map",true){}
        V17Pill("3D View",false){go("Heritage 3D")}
        V17Pill("Indoor Tour",false){go("Indoor Tour")}
    }
    Spacer(Modifier.height(12.dp))
    Surface(color=Color(0xFFE9DABF),shape=RoundedCornerShape(18.dp)){
        Column(Modifier.fillMaxWidth().padding(13.dp)){
            Text("BAGAN VIRTUAL MUSEUM",color=V17Brown,fontWeight=FontWeight.Black)
            Text("Illustrative 5-room museum plan",color=V17Mute,fontSize=11.sp)
            Spacer(Modifier.height(12.dp))
            listOf("01 • MAIN HALL","02 • HISTORY HALL","03 • ART GALLERY",
                "04 • ARCHITECTURE","05 • CONSERVATION").forEachIndexed{index,name->
                Surface(onClick={go("Indoor Tour")},color=if(index%2==0)V17Surface else V17Yellow,
                    modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),shape=RoundedCornerShape(13.dp)){
                    Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){
                        Text("◆",color=V17Gold)
                        Spacer(Modifier.width(9.dp))
                        Text(name,color=V17Ink,modifier=Modifier.weight(1f))
                        Text("›",color=V17Ink)
                    }
                }
            }
        }
    }
    Text("This is a conceptual layout, not an official surveyed floor plan or live navigation.",
        fontSize=11.sp,color=V17Mute)
}
@Composable
internal fun V17Collections(open:(Int)->Unit){
    V17Section("Museum Collections")
    V17Pill("Artifacts • 4 models",true){}
    Spacer(Modifier.height(11.dp))
    val items=listOf("Stone Inscription","Terracotta Pottery","Buddha Sculpture","Temple Bell")
    val icons=listOf("▤","◒","♟","♧")
    items.chunked(2).forEachIndexed { rowIndex,pair->
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
            pair.forEachIndexed{i,title->
                val idx=rowIndex*2+i
                Surface(onClick={open(idx)},modifier=Modifier.weight(1f).padding(vertical=5.dp),
                    color=V17Surface,shape=RoundedCornerShape(14.dp),shadowElevation=2.dp){
                    Column(Modifier.padding(11.dp),horizontalAlignment=Alignment.CenterHorizontally){
                        Box(Modifier.fillMaxWidth().height(126.dp).background(V17Linen,RoundedCornerShape(12.dp)),
                            contentAlignment=Alignment.Center){
                            Text(icons[idx],fontSize=70.sp,color=V17Gold)
                        }
                        Text(title,fontSize=12.sp,color=V17Ink,fontWeight=FontWeight.Bold,maxLines=1)
                        Text("3D educational model",fontSize=10.sp,color=V17Mute)
                    }
                }
            }
        }
    }
    Text("Illustrative models, not original museum collection scans.",fontSize=11.sp,color=V17Mute)
}
@Composable
internal fun V17Artifact(index:Int,go:(String)->Unit){
    val names=listOf("Stone Inscription","Terracotta Pottery","Buddha Sculpture","Temple Bell")
    V17Section(names[index.coerceIn(0,3)])
    V17DarkPanel{ArtifactCollectionV14()}
    V17NeutralButton("Return to collections",Modifier.fillMaxWidth()){go("Collections")}
}
@Composable
internal fun V17History(){
    V17Section("History of Bagan")
    val timeline=listOf(
        "849" to "Traditional founding date of Bagan",
        "1044" to "Anawrahta's reign began",
        "1105" to "Kyanzittha's later reign and Bagan cultural development",
        "12th century" to "Expansion of Bagan art and temple architecture",
        "13th century" to "Bagan's classical heritage",
        "2019" to "Bagan inscribed on the UNESCO World Heritage List"
    )
    timeline.forEach{(date,detail)->
        Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(13.dp).background(V17Gold,CircleShape))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)){
                Text(date,color=V17Ink,fontWeight=FontWeight.Black,fontSize=20.sp)
                Text(detail,color=V17Mute,fontSize=12.sp)
            }
            Text("🏛",fontSize=33.sp)
        }
        HorizontalDivider(color=V17Linen)
    }
}
private val questionsV17=listOf(
    Pair("မြစေတီကျောက်စာသည် မည်သည့်အမျိုးအစားလဲ?",listOf("Inscription","Gate","Temple","Pagoda")),
    Pair("Which file format is used by the offline 3D viewer?",listOf("DOCX","GLB","PDF","MP3")),
    Pair("Ananda Temple is in which region?",listOf("Yangon","Bagan","Mandalay city","Bago")),
    Pair("ARCore mainly uses the phone's...",listOf("Camera and motion tracking","Calculator","Contacts","Phone calls")),
    Pair("Which site is a gateway?",listOf("Shwezigon","Ananda","Tharabar Gate","Bupaya")),
    Pair("The V1.7 museum room map is...",listOf("Official survey","Live GPS","A 2D satellite scan","A concept layout"))
)
private val answersV17=listOf(0,1,1,0,2,3)
@Composable
internal fun V17Quiz(index:Int,choice:Int,score:Int,done:Boolean,
    select:(Int)->Unit,next:(Boolean)->Unit,restart:()->Unit,go:(String)->Unit){
    V17Section(if(done)"Heritage Quiz Result" else "Bagan Heritage Quiz")
    if(done){
        Column(Modifier.fillMaxWidth().background(V17Brown,RoundedCornerShape(22.dp)).padding(25.dp),
            horizontalAlignment=Alignment.CenterHorizontally){
            Text("✦",color=V17Yellow,fontSize=67.sp)
            Text("Great Job!",color=V17Yellow,fontSize=27.sp,fontWeight=FontWeight.Black)
            Text(score.toString()+" / 6",color=V17White,fontSize=46.sp,fontWeight=FontWeight.Black)
            Text("${score*100/6}% • Museum Learning",color=V17White)
        }
        Spacer(Modifier.height(13.dp))
        V17GoldenButton("Try Again",Modifier.fillMaxWidth(),onClick=restart)
        Spacer(Modifier.height(8.dp))
        V17NeutralButton("View Certificate",Modifier.fillMaxWidth()){go("Certificate")}
    }else{
        Text("Question "+(index+1)+" of 6",color=V17Gold,fontWeight=FontWeight.Bold)
        LinearProgressIndicator(progress={(index+1)/6f},modifier=Modifier.fillMaxWidth().padding(vertical=12.dp),
            color=V17Gold,trackColor=V17Linen)
        V17BrandHero(160,"Bagan Heritage Learning")
        Spacer(Modifier.height(16.dp))
        Text(questionsV17[index].first,color=V17Ink,fontWeight=FontWeight.Bold,fontSize=19.sp)
        Spacer(Modifier.height(12.dp))
        questionsV17[index].second.forEachIndexed { i,label ->
            Surface(onClick={select(i)},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),
                color=if(choice==i)Color(0xFFD9EDD8) else V17Surface,
                shape=RoundedCornerShape(13.dp),border=BorderStroke(1.dp,
                    if(choice==i)V17Green else V17Linen)){
                Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(if(choice==i)"◉" else "○",color=if(choice==i)V17Green else V17Mute,fontSize=22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(label,color=V17Ink)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        V17GoldenButton(if(index==5)"See Results →" else "Next Question →",
            Modifier.fillMaxWidth(),enabled=choice>=0){next(choice==answersV17[index])}
    }
}
@Composable
internal fun V17Favorites(items:List<V17Monument>,saved:Set<String>,open:(Int)->Unit){
    V17Section("My Favorites")
    val filtered=items.withIndex().filter{it.value.slug in saved}
    if(filtered.isEmpty())Text("No favorites yet. Tap ♡ on any monument to save it.",color=V17Mute)
    filtered.forEach{(i,item)->V17MonumentTile(item,i,onClick={open(i)})}
}
@Composable
internal fun V17Search(items:List<V17Monument>,search:String,setSearch:(String)->Unit,open:(Int)->Unit){
    V17Section("Search Heritage")
    OutlinedTextField(search,setSearch,modifier=Modifier.fillMaxWidth(),
        label={Text("Search monuments, artifacts...")},singleLine=true)
    items.forEachIndexed{i,item->
        if(search.isEmpty()||item.name.contains(search,true)||item.mm.contains(search,true)){
            V17MonumentTile(item,i,onClick={open(i)})
        }
    }
}
@Composable
internal fun V17Settings(lang:String,setLang:(String)->Unit,voice:Boolean,setVoice:(Boolean)->Unit,
    theme:String,setTheme:(String)->Unit,go:(String)->Unit){
    V17Section("Settings")
    Surface(color=V17Surface,shape=RoundedCornerShape(15.dp)){
        Column(Modifier.padding(13.dp)){
            Text("Language  •  ဘာသာစကား",fontWeight=FontWeight.Bold,color=V17Ink)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                V17Pill("မြန်မာ",lang=="my"){setLang("my")}
                V17Pill("English",lang=="en"){setLang("en")}
            }
            HorizontalDivider(Modifier.padding(vertical=10.dp),color=V17Linen)
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("Auto-play audio guide",modifier=Modifier.weight(1f),color=V17Ink)
                Switch(checked=voice,onCheckedChange=setVoice)
            }
            Text("Audio playback requires installed Android speech voices.",color=V17Mute,fontSize=11.sp)
            HorizontalDivider(Modifier.padding(vertical=10.dp),color=V17Linen)
            Text("Theme",fontWeight=FontWeight.Bold,color=V17Ink)
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
                listOf("Gold","Classic").forEach{option->
                    V17Pill(option,theme==option){setTheme(option)}
                }
            }
            Text("Gold / Classic preference is saved; full recoloring is planned.",fontSize=10.sp,color=V17Mute)
        }
    }
    Spacer(Modifier.height(8.dp))
    listOf("Audio Guide","Profile","Offline Learning","Certificate").forEach{label->
        V17NeutralButton(label+"   ›",Modifier.fillMaxWidth().padding(vertical=4.dp)){go(label)}
    }
    Text("Bagan Museum UI V1.7 — Educational prototype",color=V17Mute,fontSize=12.sp)
}
@Composable
internal fun V17Profile(name:String,setName:(String)->Unit,saved:Int,best:Int,go:(String)->Unit){
    V17Section("My Profile")
    Column(Modifier.fillMaxWidth().background(V17Surface,RoundedCornerShape(20.dp)).padding(23.dp),
        horizontalAlignment=Alignment.CenterHorizontally){
        Text("◉",fontSize=58.sp,color=V17Gold)
        OutlinedTextField(value=name,onValueChange=setName,
            singleLine=true,label={Text("Visitor / ခရီးသွားအမည်")})
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            V17Stat("Favorites",saved.toString(),Modifier.weight(1f))
            V17Stat("Best Quiz",best.toString()+"/6",Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(10.dp))
    V17GoldenButton("View Certificate",Modifier.fillMaxWidth()){go("Certificate")}
    V17NeutralButton("My Favorites",Modifier.fillMaxWidth()){go("Favorites")}
}
@Composable
internal fun V17Certificate(name:String,best:Int,go:(String)->Unit){
    V17Section("Achievement Certificate")
    Column(Modifier.fillMaxWidth().background(V17Surface,RoundedCornerShape(17.dp))
        .border(2.dp,V17Gold,RoundedCornerShape(17.dp)).padding(24.dp),
        horizontalAlignment=Alignment.CenterHorizontally){
        Text("✧ BAGAN MUSEUM ✧",color=V17Gold,fontWeight=FontWeight.Black,fontSize=24.sp)
        Spacer(Modifier.height(18.dp))
        Text("CERTIFICATE",color=V17Brown,fontSize=29.sp,fontWeight=FontWeight.Black)
        Text("OF ACHIEVEMENT",color=V17Gold)
        Spacer(Modifier.height(16.dp))
        Text("This certificate records the best quiz performance of",color=V17Mute,fontSize=12.sp)
        Text(name,color=V17Ink,fontWeight=FontWeight.Black,fontSize=23.sp)
        Spacer(Modifier.height(12.dp))
        Text("Bagan Heritage Quiz",color=V17Brown,fontSize=18.sp)
        Text("Best Score: "+best+" / 6",fontSize=16.sp,color=V17Gold)
        Text("✦",color=V17Gold,fontSize=48.sp)
        Text("Unofficial educational app achievement",color=V17Mute,fontSize=11.sp)
    }
    Spacer(Modifier.height(12.dp))
    V17NeutralButton("Return to Quiz",Modifier.fillMaxWidth()){go("Quiz")}
    Text("Certificate is an on-screen educational record; no official issuer or PDF export.",
        color=V17Mute,fontSize=11.sp)
}
@Composable
internal fun V17Menu(go:(String)->Unit){
    V17Section("Explore Bagan Museum")
    listOf("Home","Gallery","Heritage 3D","AR View","Museum Map","Indoor Tour",
        "Collections","History Timeline","Audio Guide","Audio Dashboard","Quiz","Favorites","Offline Multimedia",
        "Search","Profile","Certificate","Settings","Offline Learning").forEach{label->
        Surface(onClick={go(label)},color=V17Surface,shape=RoundedCornerShape(14.dp),
            modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
            Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){
                Text("✧",color=V17Gold)
                Spacer(Modifier.width(12.dp))
                Text(label,color=V17Ink,modifier=Modifier.weight(1f))
                Text("›",color=V17Gold)
            }
        }
    }
}
@Composable
internal fun V17Offline(go:(String)->Unit){
    V17Section("Offline Learning")
    V17BrandHero(185,"10 locally generated 3D concept monuments")
    Spacer(Modifier.height(14.dp))
    Text("Offline 3D models and quiz questions are bundled with the APK.",
        color=V17Ink)
    V17GoldenButton("Explore offline 3D",Modifier.fillMaxWidth()){go("Heritage 3D")}
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("Take Heritage Quiz",Modifier.fillMaxWidth()){go("Quiz")}
    Spacer(Modifier.height(10.dp))
    Text("Original Google Drive Blender files and private previews require internet and your Drive account.",
        color=V17Mute,fontSize=12.sp)
}
