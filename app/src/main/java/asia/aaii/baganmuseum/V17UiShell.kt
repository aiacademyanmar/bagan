package asia.aaii.baganmuseum

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.delay
import org.json.JSONObject

internal val V17Brown=Color(0xFF322012)
internal val V17Night=Color(0xFF1D1510)
internal val V17Gold=Color(0xFFD49D47)
internal val V17Yellow=Color(0xFFF9CD78)
internal val V17Cream=Color(0xFFFFF7EA)
internal val V17Linen=Color(0xFFF3EBE0)
internal val V17Ink=Color(0xFF342617)
internal val V17Mute=Color(0xFF806B5B)
internal val V17White=Color(0xFFFFFFFF)
internal val V17Surface=Color(0xFFFFFAF4)
internal val V17Green=Color(0xFF42864C)

internal data class V17Monument(
    val slug:String,val name:String,val mm:String,val category:String,
    val detail:String,val folder:String,val model:String,val preview:String
)
internal fun v17Catalog(context:Context):List<V17Monument>{
    val raw=context.assets.open("bagan_drive_catalog_v15.json").bufferedReader().use { it.readText() }
    val items=JSONObject(raw).getJSONArray("items")
    return (0 until items.length()).map { i ->
        val o=items.getJSONObject(i)
        V17Monument(o.getString("slug"),o.getString("name"),o.getString("myanmar"),
            o.getString("category"),o.getString("description_mm"),o.getString("folder_id"),
            o.getString("model_id"),o.getString("preview_id"))
    }
}
internal fun v17Open(context:Context,url:String){
    try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}
    catch(_:Exception){}
}

@Composable
internal fun V17BrandHero(height:Int=206,subtitle:String="Explore the ancient heritage of Bagan") {
    Box(Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(18.dp))) {
        Image(painter=painterResource(R.drawable.museum_hero),contentDescription="Illustrated Bagan architecture",
            modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0x00734420),Color(0x55190D06),Color(0xE51A0E07)))))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Text("✦ EXPLORE BAGAN HERITAGE",fontSize=11.sp,color=V17Yellow,fontWeight=FontWeight.Bold)
            Text("BAGAN MUSEUM",fontSize=24.sp,color=V17White,fontWeight=FontWeight.Black)
            Text(subtitle,fontSize=12.sp,color=V17White)
        }
    }
}
@Composable
internal fun V17Section(name:String,right:String?=null,onRight:(()->Unit)?=null){
    Row(Modifier.fillMaxWidth().padding(top=17.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically){
        Text(name,fontSize=19.sp,fontWeight=FontWeight.Black,color=V17Ink,modifier=Modifier.weight(1f))
        if(right!=null&&onRight!=null) TextButton(onClick=onRight){Text(right,color=V17Gold,fontSize=12.sp)}
    }
}
@Composable
internal fun V17Pill(label:String,selected:Boolean,modifier:Modifier=Modifier,onClick:()->Unit){
    Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(50),
        color=if(selected)V17Gold else V17Linen) {
        Text(label,modifier=Modifier.padding(horizontal=13.dp,vertical=9.dp),
            fontSize=11.sp,color=if(selected)V17White else V17Ink,
            fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium,maxLines=1)
    }
}
@Composable
internal fun V17GoldenButton(label:String,modifier:Modifier=Modifier,enabled:Boolean=true,onClick:()->Unit){
    Button(onClick=onClick,enabled=enabled,modifier=modifier,
        colors=ButtonDefaults.buttonColors(containerColor=V17Gold,contentColor=V17Night),
        shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(horizontal=14.dp,vertical=14.dp)){
        Text(label,fontWeight=FontWeight.Bold,fontSize=14.sp)
    }
}
@Composable
internal fun V17NeutralButton(label:String,modifier:Modifier=Modifier,onClick:()->Unit){
    OutlinedButton(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(14.dp),
        border=BorderStroke(1.dp,V17Gold),colors=ButtonDefaults.outlinedButtonColors(contentColor=V17Gold)){
        Text(label,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
}
@Composable
internal fun V17MonumentTile(item:V17Monument,index:Int,onClick:()->Unit,extra:(@Composable ()->Unit)?=null){
    Surface(onClick=onClick,modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),
        color=V17Surface,shape=RoundedCornerShape(14.dp),
        shadowElevation=2.dp) {
        Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(74.dp).clip(RoundedCornerShape(10.dp))) {
                Image(painter=painterResource(R.drawable.museum_hero),contentDescription=null,
                    contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.BottomStart).background(V17Brown.copy(alpha=.77f))
                    .padding(horizontal=6.dp,vertical=2.dp)){
                    Text((index+1).toString().padStart(2,'0'),color=V17Yellow,fontSize=10.sp)
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)){
                Text(item.mm,color=V17Ink,fontWeight=FontWeight.Bold,fontSize=14.sp,maxLines=1)
                Text(item.name,color=V17Mute,fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(item.category,color=V17Gold,fontSize=10.sp)
            }
            if(extra!=null)extra() else Text("›",fontSize=27.sp,color=V17Gold)
        }
    }
}
@Composable
internal fun V17Stat(label:String,count:String,modifier:Modifier=Modifier){
    Column(modifier.background(V17Linen,RoundedCornerShape(15.dp)).padding(13.dp),
        horizontalAlignment=Alignment.CenterHorizontally){
        Text(count,color=V17Ink,fontSize=22.sp,fontWeight=FontWeight.Black)
        Text(label,color=V17Mute,fontSize=11.sp)
    }
}
@Composable
internal fun V17Header(screen:String,onBack:()->Unit,onMenu:()->Unit) {
    val home=screen=="Home"
    Row(Modifier.fillMaxWidth().height(59.dp).background(V17Brown)
        .padding(horizontal=13.dp),verticalAlignment=Alignment.CenterVertically){
        TextButton(onClick=if(home)onMenu else onBack,contentPadding=PaddingValues(0.dp),
            modifier=Modifier.width(37.dp)){Text(if(home)"☰" else "‹",color=V17White,fontSize=29.sp)}
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
            Text(if(home)"✧  BAGAN MUSEUM" else screen.uppercase(),color=V17Yellow,
                fontSize=if(home)18.sp else 14.sp,fontWeight=FontWeight.Bold,maxLines=1)
            if(home)Text("ပုဂံပြတိုက်",color=V17White,fontSize=10.sp)
        }
        TextButton(onClick=onMenu,contentPadding=PaddingValues(0.dp),modifier=Modifier.width(38.dp)){
            Text("⋮",color=V17Yellow,fontSize=25.sp)
        }
    }
}
@Composable
internal fun V17Bottom(active:String,go:(String)->Unit) {
    val tabs=listOf(Triple("Home","⌂","Home"),Triple("Gallery","▧","Gallery"),
        Triple("Map","▤","Museum Map"),Triple("Quiz","✧","Quiz"),Triple("More","···","Menu"))
    Row(Modifier.fillMaxWidth().height(64.dp).background(V17Brown)
        .padding(vertical=3.dp),horizontalArrangement=Arrangement.SpaceEvenly){
        tabs.forEach{(label,icon,route)->
            Column(Modifier.weight(1f).fillMaxHeight().clickable {go(route)},
                horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
                Text(icon,color=if(active==route)V17Yellow else V17White,fontSize=23.sp,lineHeight=23.sp)
                Text(label,color=if(active==route)V17Yellow else V17White,fontSize=10.sp)
            }
        }
    }
}
@Composable
internal fun V17Splash(onDone:()->Unit){
    LaunchedEffect(Unit){delay(1400);onDone()}
    Box(Modifier.fillMaxSize().background(V17Night)){
        Image(painter=painterResource(R.drawable.museum_hero),contentDescription=null,
            contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            Color(0xBA160B03),Color(0x53130B07),Color(0xE3160B05)))))
        Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
            Text("✧",fontSize=62.sp,color=V17Yellow)
            Text("BAGAN",fontSize=53.sp,color=V17Yellow,fontWeight=FontWeight.Black)
            Text("MUSEUM",fontSize=40.sp,color=V17Yellow,fontWeight=FontWeight.Bold)
            Text("ပုဂံပြတိုက်",fontSize=28.sp,color=V17White)
            Spacer(Modifier.height(20.dp))
            Text("EXPLORE  •  LEARN  •  PRESERVE",fontSize=11.sp,color=V17White)
        }
        CircularProgressIndicator(modifier=Modifier.size(34.dp).align(Alignment.BottomCenter)
            .padding(bottom=6.dp),color=V17Yellow,strokeWidth=2.dp)
    }
}
@Composable
internal fun V17Welcome(language:String,setLanguage:(String)->Unit,onStart:()->Unit){
    Box(Modifier.fillMaxSize().background(V17Brown)){
        Image(painter=painterResource(R.drawable.museum_hero),contentDescription="Bagan monument illustration",
            contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            Color(0x21000000),Color(0x21000000),Color(0xE91D0D04)))))
        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(22.dp),
            horizontalAlignment=Alignment.CenterHorizontally){
            Text("✧  BAGAN MUSEUM  ✧",fontSize=27.sp,fontWeight=FontWeight.Black,color=V17Yellow)
            Text("ပုဂံပြတိုက်",color=V17White,fontSize=23.sp)
            Spacer(Modifier.height(18.dp))
            Text("Discover the Ancient Heritage of Bagan",color=V17White,fontSize=16.sp)
            Spacer(Modifier.height(14.dp))
            V17GoldenButton("Start Exploring   →",Modifier.fillMaxWidth(),onClick=onStart)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                V17Pill("မြန်မာ",language=="my",Modifier.weight(1f)){setLanguage("my")}
                V17Pill("English",language=="en",Modifier.weight(1f)){setLanguage("en")}
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
@Composable
fun MuseumUiV17(){
    val ctx=LocalContext.current
    val prefs=remember{ctx.getSharedPreferences("bagan_museum_ui_v17",Context.MODE_PRIVATE)}
    val monuments=remember(ctx){v17Catalog(ctx)}
    var current by rememberSaveable{mutableStateOf("Splash")}
    var lang by rememberSaveable{mutableStateOf(prefs.getString("lang","my")?:"my")}
    var chosen by rememberSaveable{mutableIntStateOf(0)}
    var category by rememberSaveable{mutableStateOf("All")}
    var search by rememberSaveable{mutableStateOf("")}
    var openedMenu by rememberSaveable{mutableStateOf(false)}
    var favorites by remember{mutableStateOf(prefs.getStringSet("favorites",emptySet())?.toSet()?:emptySet<String>())}
    var visitor by rememberSaveable{mutableStateOf(prefs.getString("visitor","Visitor")?:"Visitor")}
    var autoVoice by rememberSaveable{mutableStateOf(prefs.getBoolean("autoVoice",false))}
    var chosenTheme by rememberSaveable{mutableStateOf(prefs.getString("theme","Gold")?:"Gold")}
    var quizIndex by rememberSaveable{mutableIntStateOf(0)}
    var quizSelection by rememberSaveable{mutableIntStateOf(-1)}
    var quizScore by rememberSaveable{mutableIntStateOf(0)}
    var quizDone by rememberSaveable{mutableStateOf(false)}
    var artifactIndex by rememberSaveable{mutableIntStateOf(1)}
    fun go(target:String){ current=target;openedMenu=false}
    fun saveFavorite(id:String){
        favorites=if(id in favorites) favorites-id else favorites+id
        prefs.edit().putStringSet("favorites",favorites).apply()
    }
    BackHandler(current!="Splash"&&current!="Welcome"&&current!="Home"){
        if(openedMenu)openedMenu=false else go("Home")
    }
    when(current){
        "Splash" -> V17Splash{go(if(prefs.getBoolean("onboard",false))"Home" else "Welcome")}
        "Welcome" -> V17Welcome(lang,{v->lang=v;prefs.edit().putString("lang",v).apply()}) {
            prefs.edit().putBoolean("onboard",true).apply();go("Home")
        }
        else -> Column(Modifier.fillMaxSize().background(V17Cream)){
            V18Header(current,onBack={go("Home")},onMenu={openedMenu=!openedMenu})
            Box(Modifier.weight(1f)){
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal=14.dp,vertical=12.dp)){
                    when(current){
                        "Home" -> V18Home(monuments,{destination->go(destination)},{chosen=it;go("Monument Detail")})
                        "Gallery" -> V18Gallery(monuments,category,{category=it},search,{search=it},
                            favorites,{i->chosen=i;go("Monument Detail")},{id->saveFavorite(id)},{go("Heritage Photos")})
                        "Monument Detail" -> V18Detail(monuments[chosen],chosen,
                            monuments[chosen].slug in favorites,
                            {saveFavorite(monuments[chosen].slug)},{destination->go(destination)})
                        "Heritage 3D" -> V17DarkPanel { V19Museum3D(chosen,monuments,{index->chosen=index},{go("AR View")},{go("Monument Detail")}) }
                        "3D Viewer" -> V17DarkPanel { V19Museum3D(chosen,monuments,{index->chosen=index},{go("AR View")},{go("Monument Detail")}) }
                        "AR View" -> V22ARScreen(monuments,chosen,{index->chosen=index},{go("Heritage 3D")})
                        "Indoor Tour" -> V17DarkPanel{
                            MuseumIndoorNavigatorV14(onExplore3d={go("Heritage 3D")},
                                onArtifact={go("Collections")})
                        }
                        "Museum Map" -> V20MapDirectory(monuments){i->chosen=i;go("Monument Detail")}
                        "Heritage Photos" -> V21PhotoGallery(monuments){i->chosen=i;go("Monument Detail")}
                        "Collections" -> V17Collections {i->artifactIndex=i;go("Artifact Detail")}
                        "Artifact Detail" -> V17Artifact(artifactIndex,{destination->go(destination)})
                        "History Timeline" -> V17History()
                        "Quiz" -> V17Quiz(quizIndex,quizSelection,quizScore,quizDone,
                            select={quizSelection=it},next={correct->
                                if(correct)quizScore++
                                if(quizIndex==5){
                                    quizDone=true
                                    prefs.edit().putInt("best",maxOf(prefs.getInt("best",0),quizScore)).apply()
                                } else quizIndex++
                                quizSelection=-1
                            },restart={
                                quizIndex=0;quizSelection=-1;quizScore=0;quizDone=false
                            },go={destination->go(destination)})
                        "Favorites" -> V17Favorites(monuments,favorites,{i->chosen=i;go("Monument Detail")})
                        "Audio Guide" -> V17DarkPanel{V21MonumentAudio(monuments,chosen){i->chosen=i}}
                        "Search" -> V17Search(monuments,search,{search=it},{i->chosen=i;go("Monument Detail")})
                        "Settings" -> V17Settings(lang,{lang=it;prefs.edit().putString("lang",it).apply()},
                            autoVoice,{autoVoice=it;prefs.edit().putBoolean("autoVoice",it).apply()},
                            chosenTheme,{chosenTheme=it;prefs.edit().putString("theme",it).apply()},{destination->go(destination)})
                        "Profile" -> V17Profile(visitor,{visitor=it;prefs.edit().putString("visitor",it).apply()},
                            favorites.size,prefs.getInt("best",0),{destination->go(destination)})
                        "Certificate" -> V18Certificate(visitor,prefs.getInt("best",0),{go("Quiz")})
                        "Menu" -> V17Menu { destination->go(destination) }
                        "Drive Library" -> V17DarkPanel{DriveMuseumCatalog()}
                        "Offline Learning" -> V17Offline { destination->go(destination) }
                        else -> V17Home(monuments,{destination->go(destination)},{chosen=it;go("Monument Detail")})
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if(openedMenu){
                    Box(Modifier.fillMaxSize().background(V17Night.copy(alpha=.55f))){
                        Column(Modifier.fillMaxHeight().fillMaxWidth(.78f)
                            .background(V17Brown).verticalScroll(rememberScrollState()).padding(12.dp)){
                            Text("✧ BAGAN MUSEUM",color=V17Yellow,fontSize=23.sp,fontWeight=FontWeight.Black)
                            Text("Explore • Learn • Preserve",color=V17White,fontSize=11.sp)
                            Spacer(Modifier.height(12.dp))
                            listOf("Home","Gallery","Heritage 3D","AR View","Museum Map",
                                "Indoor Tour","Collections","Drive Library","Heritage Photos","History Timeline",
                                "Audio Guide","Quiz","Favorites","Offline Learning",
                                "Profile","Certificate","Settings").forEach{label->
                                TextButton(onClick={go(label)},modifier=Modifier.fillMaxWidth()){
                                    Text("✦  "+label,color=V17White,
                                        modifier=Modifier.fillMaxWidth(),fontSize=15.sp)
                                }
                            }
                        }
                        TextButton(onClick={openedMenu=false},
                            modifier=Modifier.align(Alignment.TopEnd)){Text("✕",color=V17White)}
                    }
                }
            }
            V18Bottom(current,{destination->go(destination)})
        }
    }
}
@Composable
internal fun V17DarkPanel(content:@Composable ()->Unit){
    Column(Modifier.fillMaxWidth().background(V17Night,RoundedCornerShape(18.dp)).padding(14.dp)){
        content()
    }
}
