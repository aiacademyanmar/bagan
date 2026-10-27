package asia.aaii.baganmuseum
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable internal fun V25Downloads(items:List<V17Monument>){
 val ctx=LocalContext.current
 val scope=rememberCoroutineScope()
 var running by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("Ready to cache Wikimedia Commons thumbnails")}
 var progress by remember{mutableIntStateOf(0)}
 var refreshed by remember{mutableIntStateOf(0)}
 Text("OFFLINE CONTENT DOWNLOAD MANAGER",color=V17Ink,fontWeight=FontWeight.Bold)
 Text("Downloads public photo previews only. 10 concept GLBs and lesson descriptions are already inside the APK.",color=V17Mute)
 Spacer(Modifier.height(9.dp))
 LinearProgressIndicator(progress={progress/10f},modifier=Modifier.fillMaxWidth())
 Text(status,color=V17Mute)
 V17GoldenButton(if(running)"Downloading..." else "Cache heritage photo previews",Modifier.fillMaxWidth(),enabled=!running){
  running=true;scope.launch{
   var saved=0
   for((i,item) in items.withIndex()){
    if(V25DownloadPhoto(ctx,item))saved++
    progress=i+1
    status="Checked "+progress+" / "+items.size+" • "+saved+" photo(s) available"
   }
   running=false;refreshed++
  }
 }
 key(refreshed){
  items.forEach { item ->
   Surface(color=Color.White,shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
    Row(Modifier.padding(12.dp)){
     Text(item.mm,color=V17Ink,modifier=Modifier.weight(1f))
     Text(if(V25PhotoCached(ctx,item))"Cached ✓" else "Not cached",color=V17Gold)
    }
   }
  }
 }
 Text("Requires an internet connection for first-time loading. Search photos may be inaccurate; verify each file and its license.",color=V17Mute)
}
@Composable internal fun V25Achievements(items:List<V17Monument>,quiz:Int){
 val ctx=LocalContext.current
 val tour=remember{ctx.getSharedPreferences("bagan_tour_v24",0)}
 val audio=remember{ctx.getSharedPreferences("bagan_audio_v22",0)}
 var refresh by remember{mutableIntStateOf(0)}
 val visited=remember(refresh){tour.getStringSet("visited",emptySet())?.size?:0}
 val listened=remember(refresh){audio.getStringSet("completed",emptySet())?.size?:0}
 Text("MUSEUM LEARNING ACHIEVEMENTS",color=V17Ink,fontWeight=FontWeight.Bold)
 Text("Achievements are self-reported in-app learning records, not official awards.",color=V17Mute)
 V17NeutralButton("Refresh achievements",Modifier.fillMaxWidth()){refresh++}
 listOf(
   Triple("Heritage Explorer",visited,10),
   Triple("Audio Scholar",listened,10),
   Triple("Bagan Quiz",quiz,6)
 ).forEach { (name,current,total) ->
   Surface(color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){
    Column(Modifier.padding(14.dp)){
     Text(name+"  •  "+current+"/"+total,color=V17Ink,fontWeight=FontWeight.Bold)
     LinearProgressIndicator(progress={current.toFloat()/total},modifier=Modifier.fillMaxWidth().padding(vertical=9.dp))
     Text(if(current>=total)"Achievement completed ✓" else "Continue learning",color=V17Gold)
    }
   }
 }
}
