package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val blue382 = Color(0xFF081B2A)
private val panel382 = Color(0xFF112B3F)
private val gold382 = Color(0xFFFFD476)
private val white382 = Color(0xFFF8F5EC)
private val muted382 = Color(0xFFBBCBD7)
private val outline382 = Color(0xFF3A556B)
private val picture382 = listOf(
    R.drawable.bagan_safe_1,R.drawable.bagan_safe_2,R.drawable.bagan_safe_3,
    R.drawable.bagan_safe_4,R.drawable.bagan_safe_5,R.drawable.bagan_safe_6
)

@Composable
private fun Heading382(title:String,subtitle:String){
    Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp)){
        Text(title,fontSize=23.sp,fontWeight=FontWeight.Bold,color=gold382)
        Text(subtitle,fontSize=12.sp,color=muted382)
    }
}

@Composable
private fun Container382(modifier:Modifier=Modifier,contents:@Composable ColumnScope.()->Unit){
    Column(modifier.clip(RoundedCornerShape(16.dp))
        .background(panel382).border(1.dp,outline382,RoundedCornerShape(16.dp))
        .padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp),content=contents)
}

@Composable
private fun Action382(title:String,onClick:()->Unit,primary:Boolean=false,modifier:Modifier=Modifier){
    if(primary) Button(onClick=onClick,modifier=modifier.heightIn(min=49.dp),
        shape=RoundedCornerShape(12.dp),
        colors=ButtonDefaults.buttonColors(containerColor=gold382,contentColor=blue382)){
        Text(title,fontSize=12.sp,fontWeight=FontWeight.Bold,maxLines=2)
    } else OutlinedButton(onClick=onClick,modifier=modifier.heightIn(min=49.dp),
        shape=RoundedCornerShape(12.dp),border=BorderStroke(1.dp,outline382)){
        Text(title,fontSize=12.sp,color=white382,maxLines=2)
    }
}

@Composable
fun BaganGalleryV382(temple:Temple?,onThreeD:()->Unit,onAr:()->Unit){
    var filter by remember { mutableStateOf("All") }
    var selected by remember { mutableIntStateOf(0) }
    val filters=listOf("All","Exterior","Art","Illustrations")
    // Bundled illustrations are concept art, NOT verified archive photographs.
    val itemsForFilter=when(filter){
        "Exterior"->listOf(0,1,3,5)
        "Art"->listOf(2,4)
        else->(0..5).toList()
    }
    val name=temple?.nameEn ?: "Bagan"
    LazyColumn(Modifier.fillMaxSize().background(blue382),
        contentPadding=PaddingValues(bottom=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Heading382("Gallery & Heritage Views","ဓာတ်ပုံနှင့် ရုပ်ပုံပြခန်း • $name")}
        item{
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=12.dp),
                horizontalArrangement=Arrangement.spacedBy(8.dp)){
                filters.forEach { label ->
                    val chosen=label==filter
                    Surface(onClick={filter=label; selected=itemsForFilter.firstOrNull()?:0},
                        shape=RoundedCornerShape(12.dp),
                        color=if(chosen)gold382 else panel382,
                        border=BorderStroke(1.dp,if(chosen)gold382 else outline382)){
                        Text(label,Modifier.padding(horizontal=13.dp,vertical=12.dp),
                            color=if(chosen)blue382 else white382,fontSize=12.sp)
                    }
                }
            }
        }
        item{
            Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
                Image(painterResource(picture382[selected%picture382.size]),
                    contentDescription="Stylized Bagan heritage illustration",
                    modifier=Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale=ContentScale.Crop)
                Text("$name • Heritage illustration",color=white382,fontWeight=FontWeight.Bold)
                Text("Local artwork preview — not a verified historical photograph.",
                    color=muted382,fontSize=11.sp)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Action382("3D Viewer",onThreeD,true,Modifier.weight(1f))
                    Action382("AR Mode",onAr,false,Modifier.weight(1f))
                }
            }
        }
        item{
            Row(Modifier.fillMaxWidth().padding(horizontal=14.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween){
                Text("Browse Images / ရုပ်ပုံများ",color=gold382,fontWeight=FontWeight.Bold)
                Text("${itemsForFilter.size} illustrations",color=muted382,fontSize=11.sp)
            }
        }
        items(itemsForFilter.chunked(2)){pair->
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),
                horizontalArrangement=Arrangement.spacedBy(10.dp)){
                pair.forEach { index ->
                    Container382(Modifier.weight(1f).clickable{selected=index}){
                        Image(painterResource(picture382[index]),contentDescription="Bagan concept image ${index+1}",
                            modifier=Modifier.fillMaxWidth().height(124.dp).clip(RoundedCornerShape(9.dp)),
                            contentScale=ContentScale.Crop)
                        Text("Bagan View ${index+1}",color=if(index==selected)gold382 else white382,
                            fontWeight=FontWeight.Medium,fontSize=12.sp)
                    }
                }
                if(pair.size==1)Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class HistoryEntry382(val period:String,val title:String,val description:String)

@Composable
fun BaganHistoryV382(onSpeak:(String)->Unit){
    val facts=listOf(
        HistoryEntry382("9th–10th centuries","Early Bagan","Bagan developed into a major settlement in central Myanmar."),
        HistoryEntry382("11th century","Royal patronage","Religious monuments and institutional Buddhist patronage expanded."),
        HistoryEntry382("12th–13th centuries","Temple architecture","Extensive brick temples and stupas shaped Bagan's landscape."),
        HistoryEntry382("2019","UNESCO World Heritage","Bagan was inscribed on the UNESCO World Heritage List.")
    )
    LazyColumn(Modifier.fillMaxSize().background(blue382),
        contentPadding=PaddingValues(bottom=24.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Heading382("Bagan History & Timeline","ပုဂံသမိုင်း အချိန်ဇယား")}
        item{Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
            Image(painterResource(R.drawable.bagan_safe_4),contentDescription="Illustration of Bagan temples",
                modifier=Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(11.dp)),
                contentScale=ContentScale.Crop)
            Text("Explore • Learn • Preserve",color=gold382,fontWeight=FontWeight.Bold)
            Text("A concise timeline for learning. Dates are broad historical periods, not precise construction dates for every temple.",
                color=muted382,fontSize=12.sp)
        }}
        item{Heading382("Historical Milestones","မှတ်တမ်းတင်ထားသော အဓိကခေတ်ကာလများ")}
        items(facts){fact->
            Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(gold382),
                        contentAlignment=Alignment.Center){
                        Text("●",color=blue382,fontSize=23.sp)
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)){
                        Text(fact.period,color=gold382,fontSize=12.sp,fontWeight=FontWeight.Bold)
                        Text(fact.title,color=white382,fontWeight=FontWeight.SemiBold)
                    }
                }
                Text(fact.description,color=muted382,fontSize=12.sp)
            }
        }
        item{Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
            Text("Audio narration",color=gold382,fontWeight=FontWeight.Bold)
            Text("Device text-to-speech (English). The spoken voice depends on Android TTS installation.",
                color=muted382,fontSize=12.sp)
            Action382("▶ Listen to Timeline",{
                onSpeak(facts.joinToString(". "){"${it.period}. ${it.title}. ${it.description}"})
            },true,Modifier.fillMaxWidth())
        }}
    }
}

private val chapters382=listOf(
    "Introduction" to "Welcome to Bagan, an ancient cultural landscape in central Myanmar.",
    "Historical Timeline" to "Bagan flourished across the eleventh, twelfth and thirteenth centuries. Its archaeological sites preserve important Buddhist art and architecture.",
    "Architecture" to "Explore Bagan's temples, brick vaults, stupas and architectural decoration. Each monument is unique.",
    "Cultural Heritage" to "Bagan is a World Heritage Site, inscribed in 2019. Heritage conservation is essential for future generations.",
    "Explore in 3D" to "Use the lightweight CGI viewer to explore stylized temple shapes. These illustrations are not archaeological scans."
)

@Composable
fun BaganAudioV382(temple:Temple?,onSpeak:(String)->Unit,onStop:()->Unit,onRate:(Float)->Unit){
    var active by remember(temple?.id){mutableIntStateOf(-1)}
    var speed by remember {mutableFloatStateOf(1f)}
    val name=temple?.nameEn ?: "Bagan"
    LazyColumn(Modifier.fillMaxSize().background(blue382),
        contentPadding=PaddingValues(bottom=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Heading382("Audio Guide & Narration","အသံလမ်းညွှန် • $name")}
        item{Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
            Image(painterResource(R.drawable.bagan_safe_2),contentDescription="Bagan temple art",
                modifier=Modifier.fillMaxWidth().height(178.dp).clip(RoundedCornerShape(12.dp)),
                contentScale=ContentScale.Crop)
            Text("BAGAN • English Voice Guide",color=gold382,fontSize=19.sp,fontWeight=FontWeight.Bold)
            Text("Android Text-to-Speech • Requires an installed English voice. This is not a pre-recorded MP3.",
                color=muted382,fontSize=11.sp)
            Text(if(active<0)"Select a chapter below" else "Now speaking: ${chapters382[active].first}",
                color=white382,fontSize=12.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
                Action382("◀ Previous",{
                    active=if(active<=0)chapters382.lastIndex else active-1
                    onSpeak("$name. ${chapters382[active].second}")
                },false,Modifier.weight(1f))
                Action382("▶ Play",{
                    active=if(active<0)0 else active
                    onSpeak("$name. ${chapters382[active].second}")
                },true,Modifier.weight(1f))
                Action382("■ Stop",{
                    onStop();active=-1
                },false,Modifier.weight(1f))
            }
            Text("Playback speed: ${speed}×",color=white382,fontSize=12.sp)
            Slider(value=speed,onValueChange={speed=it;onRate(it)},valueRange=.75f..1.5f,
                steps=2,modifier=Modifier.fillMaxWidth())
        }}
        item{Heading382("Audio Chapters","အသံဖတ်မည့် အခန်းများ")}
        items(chapters382.indices.toList()){index->
            val chapter=chapters382[index]
            Container382(Modifier.fillMaxWidth().padding(horizontal=12.dp)
                .clickable{active=index;onSpeak("$name. ${chapter.second}")}){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text("${index+1}.",color=gold382,fontSize=18.sp,fontWeight=FontWeight.Bold)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)){
                        Text(chapter.first,color=if(active==index)gold382 else white382,
                            fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                        Text(chapter.second,color=muted382,fontSize=11.sp,maxLines=2)
                    }
                    Text("▶",color=gold382,fontSize=18.sp)
                }
            }
        }
    }
}
