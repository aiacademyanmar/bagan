package asia.aaii.baganmuseum

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/*
 * V2.0 is an educational content directory, NOT an official surveyed
 * floor plan or a repository of historical photographs.
 *
 * Online media: Wikimedia Commons search results show original works and
 * source licensing, rather than copying images of unknown ownership into APK.
 * Maps: Google Maps query via a regular ACTION_VIEW deep link; no API key,
 * GPS tracking, or fabricated point coordinates.
 */
private val ink=Color(0xFF3C2718)
private val brown=Color(0xFF4A2A19)
private val gold=Color(0xFFC48C3C)
private val ivory=Color(0xFFFFF9F1)
private val mute=Color(0xFF856E58)
private val soft=Color(0xFFF0E4D1)

private data class HeritageArticle(
    val titleEn:String,val titleMm:String,
    val overviewEn:String,val overviewMm:String,
    val placeEn:String,val placeMm:String,
    val highlights:List<Pair<String,String>>,
    val mapSearch:String?,
    val photoSearch:String
)
private val heritageContent = mapOf(
    "thatbyinnyu" to HeritageArticle(
        "Thatbyinnyu Temple","သဗ္ဗညုဘုရား",
        "A landmark of Bagan's monumental brick architecture. Its multi-storey silhouette stands prominently across the temple plain.",
        "သဗ္ဗညုဘုရားသည် ပုဂံမြို့ဟောင်း၏ ထင်ရှားသော အထပ်ဆင့်အုတ်တည်ဗိသုကာလက်ရာတစ်ခုဖြစ်ပြီး ပုဂံမြေပြန့်မှ ထင်ရှားစွာ မြင်တွေ့နိုင်သည်။",
        "Old Bagan","ပုဂံမြို့ဟောင်း",
        listOf("Architecture" to "Rising terraces and tiered massing","Materials" to "Brick construction and surface finish","Heritage" to "Protection of historic masonry"),
        "Thatbyinnyu Temple Bagan Myanmar","Thatbyinnyu Temple Bagan"
    ),
    "myazedi" to HeritageArticle(
        "Myazedi Inscription","မြစေတီကျောက်စာ",
        "An important inscription associated with the Bagan period. The famous inscription preserves texts in Pali, Pyu, Mon and Old Burmese.",
        "မြစေတီကျောက်စာသည် ပုဂံခေတ်နှင့်ဆက်စပ်သော အရေးပါသည့် ကျောက်စာဖြစ်ပြီး ပါဠိ၊ ပျူ၊ မွန်နှင့် မြန်မာဘာသာအဟောင်းတို့ဖြင့် ရေးထိုးထားသည်။",
        "Myazedi / Myinkaba area","မြစေတီ၊ မြင်းကပါအနီး",
        listOf("Languages" to "Four historic languages","Epigraphy" to "Study of carved inscriptions","Documentation" to "Photographs and historical transcription"),
        "Myazedi Pagoda Bagan Myanmar","Myazedi inscription Bagan"
    ),
    "entrance" to HeritageArticle(
        "Bagan Concept Gateway","ပုဂံဝင်ပေါက် Concept",
        "An imaginative gateway model supplied in the project source collection. It is not an independently verified historic gate or geolocatable monument.",
        "ဤဝင်ပေါက်သည် Project တွင် ပါဝင်သည့် စိတ်ကူးဖန်တီးထားသော Gateway Model ဖြစ်ပြီး အမှန်တကယ် သမိုင်းဝင်နေရာတစ်ခုအဖြစ် အတည်ပြုထားခြင်း မရှိပါ။",
        "Virtual museum exhibit","ဒစ်ဂျစ်တယ်ပြတိုက် Concept",
        listOf("Design" to "Gateway arch and tower composition","3D Model" to "Editable Blender project reference","Context" to "Illustrative — no physical site pin"),
        null,"Bagan city gates Myanmar"
    ),
    "tharabar" to HeritageArticle(
        "Tharabar Gate","သရပါတံခါး",
        "A surviving historic gateway associated with the old city of Bagan. It evokes the city's former defensive walls and entrances.",
        "သရပါတံခါးသည် ပုဂံမြို့ဟောင်း၏ သမိုင်းဝင် တံခါးပေါက်အဖြစ် ကျန်ရစ်နေသော အမွေအနှစ်တစ်ခုဖြစ်ပြီး မြို့ရိုးနှင့် ဝင်ပေါက်သမိုင်းကို ထင်ဟပ်စေသည်။",
        "Old Bagan","ပုဂံမြို့ဟောင်း",
        listOf("City Walls" to "Ancient urban enclosure","Architecture" to "Brick gateway composition","Conservation" to "Protecting exposed historic fabric"),
        "Tharabar Gate Old Bagan Myanmar","Tharabar Gate Bagan"
    ),
    "gawdawpalin" to HeritageArticle(
        "Gawdawpalin Temple","ကန်တော့ပလ္လင်ဘုရား",
        "A major Bagan temple recognized for its imposing brick mass, terraces and entrance hall composition.",
        "ကန်တော့ပလ္လင်ဘုရားသည် အုတ်ဖြင့်တည်ဆောက်ထားသည့် အဆောက်အအုံကြီးမားမှု၊ အဆင့်ဆင့်ထပ်ထားသော အဆောင်များနှင့် ဝင်ပေါက်ဖွဲ့စည်းပုံတို့ကြောင့် ထင်ရှားသည်။",
        "Old Bagan","ပုဂံမြို့ဟောင်း",
        listOf("Structure" to "Multi-tier temple profile","Exterior" to "Brickwork and elevation study","Visit" to "Respect cultural and worship contexts"),
        "Gawdawpalin Temple Bagan Myanmar","Gawdawpalin Temple"
    ),
    "ananda" to HeritageArticle(
        "Ananda Temple","အာနန္ဒာဘုရား",
        "One of Bagan's celebrated Buddhist temples, known for a balanced cruciform architectural plan, passageways and its central sanctuary.",
        "အာနန္ဒာဘုရားသည် ပုဂံ၏ ထင်ရှားသော ဘုရားတစ်ဆူဖြစ်ပြီး အရပ်လေးမျက်နှာဆိုင်ရာ ဖွဲ့စည်းပုံ၊ လမ်းကြောင်းများနှင့် ဗဟိုသန့်ရှင်းရာ အဆောင်တို့ကြောင့် လူသိများသည်။",
        "Old Bagan","ပုဂံမြို့ဟောင်း",
        listOf("Plan" to "Cruciform temple concept","Sacred Space" to "Sanctuary and worship setting","Artwork" to "Sculptural and architectural tradition"),
        "Ananda Temple Bagan Myanmar","Ananda Temple Bagan"
    ),
    "htilominlo" to HeritageArticle(
        "Htilominlo Temple","ထီးလိုမင်းလိုဘုရား",
        "A monumental brick temple distinguished by decorative surfaces and the layered silhouette of Bagan temple architecture.",
        "ထီးလိုမင်းလိုဘုရားသည် အုတ်တည်လက်ရာနှင့် အလှဆင်မျက်နှာပြင်များ၊ အဆင့်ဆင့်ထပ်ထားသော ဘုရားဗိသုကာပုံစံတို့ကြောင့် ထင်ရှားသည်။",
        "Bagan archaeological area","ပုဂံရှေးဟောင်းယဉ်ကျေးမှုနယ်မြေ",
        listOf("Facade" to "Decorative brickwork and plaster","Tower" to "Tiered superstructure","Care" to "Conserving temple surfaces"),
        "Htilominlo Temple Bagan Myanmar","Htilominlo Temple"
    ),
    "alodawpyae" to HeritageArticle(
        "Alodawpyae Pagoda","အလိုတော်ပြည့်ဘုရား",
        "A pagoda in the Bagan cultural landscape, presented here as one of the user's historic-site Blender study references.",
        "အလိုတော်ပြည့်ဘုရားသည် ပုဂံရှေးဟောင်းယဉ်ကျေးမှုနယ်မြေအတွင်းရှိ ဘုရားတစ်ဆူဖြစ်ပြီး Project ၏ Blender မော်ဒယ်လေ့လာမှုတွင် ပါဝင်သည်။",
        "Bagan","ပုဂံ",
        listOf("Form" to "Stupa-shaped visual vocabulary","Craft" to "Layers and ornamental finish","Source" to "Blender concept model in Drive"),
        "Alodawpyae Pagoda Bagan Myanmar","Alodawpyae Pagoda Bagan"
    ),
    "shwezigon" to HeritageArticle(
        "Shwezigon Pagoda","ရွှေစည်းခုံဘုရား",
        "A gilded Buddhist stupa in the Bagan area whose form is influential in Myanmar's pagoda-building tradition.",
        "ရွှေစည်းခုံဘုရားသည် ပုဂံဒေသတွင်ရှိသော ရွှေချထားသည့် စေတီတော်တစ်ဆူဖြစ်ပြီး မြန်မာနိုင်ငံ၏ စေတီဗိသုကာအစဉ်အလာတွင် အရေးပါသည်။",
        "Nyaung-U, Bagan region","ညောင်ဦး၊ ပုဂံဒေသ",
        listOf("Stupa" to "Circular gilded form","Devotion" to "Living Buddhist worship practice","Heritage" to "Traditional stupa proportions"),
        "Shwezigon Pagoda Nyaung U Myanmar","Shwezigon Pagoda"
    ),
    "bupaya" to HeritageArticle(
        "Bupaya Pagoda","ဗူးဘုရား",
        "A riverside stupa associated with the Bagan landscape, recognized for its rounded profile and location near the Ayeyarwady River.",
        "ဗူးဘုရားသည် ပုဂံဒေသ ဧရာဝတီမြစ်ကမ်းအနီးရှိ စေတီတော်တစ်ဆူဖြစ်ပြီး လုံးဝန်းသော စေတီပုံစံဖြင့် ထင်ရှားသည်။",
        "Ayeyarwady riverbank, Bagan","ပုဂံ ဧရာဝတီမြစ်ကမ်းနား",
        listOf("Riverside" to "Relationship to Ayeyarwady river","Stupa Form" to "Rounded profile","Landscape" to "Historic river setting"),
        "Bupaya Pagoda Bagan Myanmar","Bupaya Pagoda Bagan"
    )
)
private fun articleOf(slug:String):HeritageArticle?=heritageContent[slug]

internal fun mapsUrl(slug:String):String?{
    val query=articleOf(slug)?.mapSearch ?: return null
    return "https://www.google.com/maps/search/?api=1&query="+Uri.encode(query)
}
internal fun commonsUrl(slug:String):String{
    val term=articleOf(slug)?.photoSearch?:"Bagan Myanmar monuments"
    return "https://commons.wikimedia.org/w/index.php?title=Special:MediaSearch&type=image&search="+Uri.encode(term)
}
private fun openExternal(context:Context,url:String){
    try{
        context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))
    }catch(_:ActivityNotFoundException){}
}

@Composable
internal fun V20HeritageDetail(exhibit:V17Monument){
    val context=LocalContext.current
    val info=articleOf(exhibit.slug)?:return
    val prefs=remember{context.getSharedPreferences("bagan_v20_language",Context.MODE_PRIVATE)}
    var language by remember { mutableStateOf(prefs.getString("language","my")?:"my") }
    Text("HERITAGE INFORMATION",color=ink,fontSize=19.sp,fontWeight=FontWeight.Black)
    Row(Modifier.padding(vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
        listOf("my" to "မြန်မာ","en" to "English").forEach{(code,label)->
            Surface(onClick={language=code;prefs.edit().putString("language",code).apply()},
                color=if(language==code)gold else soft,shape=RoundedCornerShape(18.dp)){
                Text(label,color=if(language==code)Color.White else ink,
                    modifier=Modifier.padding(horizontal=14.dp,vertical=9.dp),fontSize=12.sp)
            }
        }
    }
    Text(if(language=="my")info.titleMm else info.titleEn,color=ink,fontWeight=FontWeight.Bold,fontSize=16.sp)
    Spacer(Modifier.height(6.dp))
    Text(if(language=="my")info.overviewMm else info.overviewEn,color=ink,lineHeight=23.sp,fontSize=13.sp)
    Spacer(Modifier.height(9.dp))
    Text("AREA • "+if(language=="my")info.placeMm else info.placeEn,color=mute,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    if(mapsUrl(exhibit.slug)!=null){
        V17GoldenButton("▤  Open on Google Maps",Modifier.fillMaxWidth()){
            openExternal(context,mapsUrl(exhibit.slug)!!)
        }
    } else {
        Text("Virtual concept gateway: no verified physical location for map routing.",
            color=mute,fontSize=12.sp)
    }
    Spacer(Modifier.height(6.dp))
    V17NeutralButton("▧  Browse real photos on Wikimedia Commons",Modifier.fillMaxWidth()){
        openExternal(context,commonsUrl(exhibit.slug))
    }
    Text("Photos open online with their individual source and license details. They are not bundled offline.",
        color=mute,fontSize=11.sp)
}
@Composable
internal fun V20MapDirectory(items:List<V17Monument>,openDetail:(Int)->Unit){
    val context=LocalContext.current
    Text("BAGAN HERITAGE MAP",color=ink,fontWeight=FontWeight.Black,fontSize=23.sp)
    Text("Google Maps location search • no API key or GPS permission",color=mute,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    Surface(color=ivory,shape=RoundedCornerShape(15.dp),border=BorderStroke(1.dp,soft)){
        Column(Modifier.fillMaxWidth().padding(14.dp)){
            Text("Find monuments around Bagan",color=ink,fontWeight=FontWeight.Bold)
            Text("Select a location to open its Google Maps search. Check the place result before navigating.",
                color=mute,fontSize=12.sp)
            Spacer(Modifier.height(10.dp))
            V17GoldenButton("Open Bagan in Google Maps",Modifier.fillMaxWidth()){
                openExternal(context,"https://www.google.com/maps/search/?api=1&query="+Uri.encode("Bagan Myanmar historic temples"))
            }
        }
    }
    Spacer(Modifier.height(13.dp))
    items.forEachIndexed { i,item ->
        val available=mapsUrl(item.slug)!=null
        Surface(color=ivory,shape=RoundedCornerShape(14.dp),
            modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
            Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                MonumentArtworkV19(item.slug,Modifier.size(77.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).clickable{openDetail(i)}){
                    Text(item.mm,color=ink,fontWeight=FontWeight.Bold,fontSize=13.sp)
                    Text(item.name,color=mute,fontSize=10.sp)
                    Text(if(available)"Map search available" else "Virtual concept only",
                        color=gold,fontSize=10.sp)
                }
                if(available)TextButton(onClick={openExternal(context,mapsUrl(item.slug)!!)}){
                    Text("MAP ›",color=gold,fontWeight=FontWeight.Bold,fontSize=11.sp)
                }
            }
        }
    }
    Text("Map results are provided by Google Maps. No precise coordinates, travel times, live opening hours or directions have been independently verified in-app.",
        color=mute,fontSize=11.sp)
}

@Composable
internal fun V20PhotoGallery(items:List<V17Monument>,openDetail:(Int)->Unit){
    val context=LocalContext.current
    Text("HERITAGE PHOTO DIRECTORY",color=ink,fontSize=22.sp,fontWeight=FontWeight.Black)
    Text("Discover public historical-site photographs and their attribution online.",color=mute,fontSize=12.sp)
    Spacer(Modifier.height(11.dp))
    items.forEachIndexed { i,item ->
        Surface(color=ivory,modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),
            shape=RoundedCornerShape(13.dp)){
            Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                MonumentArtworkV19(item.slug,Modifier.size(80.dp))
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)){
                    Text(item.mm,color=ink,fontWeight=FontWeight.Bold,fontSize=14.sp)
                    Text(item.name,color=mute,fontSize=11.sp)
                    TextButton(onClick={openExternal(context,commonsUrl(item.slug))},
                        contentPadding=PaddingValues(0.dp)){
                        Text("View real photos ↗",color=gold,fontSize=12.sp)
                    }
                }
                TextButton(onClick={openDetail(i)}){Text("Info ›",color=gold)}
            }
        }
    }
    Text("Thumbnails displayed here are original concept drawings; real photos are browsed on Wikimedia Commons with licenses displayed per file.",
        color=mute,fontSize=11.sp)
}
@Composable
internal fun V20Hotspots(item:V17Monument){
    val article=articleOf(item.slug)?:return
    val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("bagan_v20_language",Context.MODE_PRIVATE)}
    var selected by remember(item.slug){mutableIntStateOf(0)}
    var lang by remember {mutableStateOf(prefs.getString("language","my")?:"my")}
    Text("3D INFORMATION HOTSPOTS",color=Color(0xFFF2C979),fontSize=18.sp,fontWeight=FontWeight.Bold)
    Text("Concept learning topics • not surveyed 3D anchors",color=Color(0xFFDDC6A8),fontSize=11.sp)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        article.highlights.forEachIndexed { index,pair ->
            Surface(onClick={selected=index},color=if(selected==index)gold else brown,
                shape=RoundedCornerShape(12.dp)){
                Text("◉ "+pair.first,color=Color.White,
                    modifier=Modifier.padding(horizontal=12.dp,vertical=9.dp),fontSize=11.sp)
            }
        }
    }
    Spacer(Modifier.height(11.dp))
    Surface(color=Color(0xFF453020),shape=RoundedCornerShape(15.dp)){
        Column(Modifier.padding(14.dp)){
            Text(article.highlights[selected].first,color=Color(0xFFF2C979),fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(article.highlights[selected].second,color=Color.White,fontSize=12.sp,lineHeight=20.sp)
            Spacer(Modifier.height(8.dp))
            Text(if(lang=="my")article.overviewMm else article.overviewEn,
                color=Color(0xFFE9D4B9),fontSize=12.sp,lineHeight=21.sp)
        }
    }
    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
        TextButton(onClick={lang=if(lang=="my")"en" else "my";prefs.edit().putString("language",lang).apply()}){
            Text(if(lang=="my")"English" else "မြန်မာ",color=Color(0xFFF2C979))
        }
        TextButton(onClick={openExternal(context,commonsUrl(item.slug))}){
            Text("Photos ↗",color=Color(0xFFF2C979))
        }
    }
}
