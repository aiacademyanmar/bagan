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
import androidx.compose.ui.unit.sp

@Composable internal fun V23OfflineMultimedia(items:List<V17Monument>,open3D:(Int)->Unit,openAudio:(Int)->Unit,openPhotos:()->Unit){
 val ctx=LocalContext.current
 var photoCount by remember{mutableIntStateOf(V23CachedPhotoCount(ctx))}
 Text("OFFLINE MULTIMEDIA LIBRARY",fontSize=23.sp,fontWeight=FontWeight.Black,color=V17Ink)
 Text("10 locally bundled concept GLBs. Photos need initial internet access.",fontSize=12.sp,color=V17Mute)
 Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
  V17Stat("3D Models","10",Modifier.weight(1f))
  V17Stat("Cached Photos",photoCount.toString(),Modifier.weight(1f))
 }
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
  V17NeutralButton("Photos",Modifier.weight(1f),onClick=openPhotos)
  V17NeutralButton("Refresh",Modifier.weight(1f)){photoCount=V23CachedPhotoCount(ctx)}
 }
 items.forEachIndexed{index,item->
  Surface(color=Color.White,shape=RoundedCornerShape(13.dp),modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
   Row(Modifier.padding(9.dp)){
    MonumentArtworkV19(item.slug,Modifier.size(67.dp))
    Column(Modifier.weight(1f).padding(start=10.dp)){
     Text(item.mm,fontWeight=FontWeight.Bold,color=V17Ink)
     Text(item.name,fontSize=11.sp,color=V17Mute)
     Row{
      TextButton(onClick={open3D(index)}){Text("3D",color=V17Gold)}
      TextButton(onClick={openAudio(index)}){Text("Audio",color=V17Gold)}
     }
    }
   }
  }
 }
}
@Composable internal fun V23AudioDashboard(items:List<V17Monument>,openAudio:(Int)->Unit){
 val ctx=LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("bagan_audio_v22",0)}
 var completed by remember{mutableStateOf(prefs.getStringSet("completed",emptySet())?.toSet()?:emptySet<String>())}
 Text("AUDIO LEARNING DASHBOARD",fontSize=22.sp,fontWeight=FontWeight.Black,color=V17Ink)
 Text("Speech playback completion saved on this phone.",fontSize=12.sp,color=V17Mute)
 Text(completed.size.toString()+" / "+items.size+" completed",fontSize=22.sp,color=V17Gold)
 LinearProgressIndicator(progress={completed.size/items.size.toFloat()},modifier=Modifier.fillMaxWidth().padding(vertical=10.dp))
 V17NeutralButton("Refresh progress",Modifier.fillMaxWidth()){completed=prefs.getStringSet("completed",emptySet())?.toSet()?:emptySet()}
 items.forEachIndexed{i,item->
  Surface(onClick={openAudio(i)},color=if(item.slug in completed)Color(0xFFE5F0DD)else Color.White,shape=RoundedCornerShape(11.dp),modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
   Row(Modifier.padding(13.dp)){
    Text(if(item.slug in completed)"✓" else "♫",color=V17Gold)
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)){
     Text(item.mm,fontWeight=FontWeight.Bold,color=V17Ink)
     Text(if(item.slug in completed)"Completed" else "Not yet played",fontSize=11.sp,color=V17Mute)
    }
    Text("›",color=V17Gold)
   }
  }
 }
 var confirm by remember{mutableStateOf(false)}
 if(confirm){
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
   V17NeutralButton("Cancel",Modifier.weight(1f)){confirm=false}
   V17GoldenButton("Confirm reset",Modifier.weight(1f)){prefs.edit().remove("completed").apply();completed=emptySet();confirm=false}
  }
 } else V17NeutralButton("Reset saved audio progress",Modifier.fillMaxWidth()){confirm=true}
}
