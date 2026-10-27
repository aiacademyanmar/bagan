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

@Composable internal fun V24OfflinePhotos(items:List<V17Monument>,open:(Int)->Unit){
 val ctx=LocalContext.current
 var refreshed by remember{mutableIntStateOf(0)}
 Text("OFFLINE PHOTO GALLERY",fontSize=22.sp,fontWeight=FontWeight.Bold,color=V17Ink)
 Text("Previously cached Wikimedia photos are shown offline. If not cached, a concept illustration appears.",fontSize=12.sp,color=V17Mute)
 V17NeutralButton("Refresh cached photos",Modifier.fillMaxWidth()){refreshed++}
 key(refreshed){
  items.forEachIndexed { index,item ->
   Surface(color=Color.White,shape=RoundedCornerShape(13.dp),
     modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){
    Column(Modifier.padding(10.dp)){
     V21HeritagePhoto(item,Modifier.fillMaxWidth().height(160.dp),showLabel=true)
     Row {
      Column(Modifier.weight(1f)){
       Text(item.mm,fontWeight=FontWeight.Bold,color=V17Ink)
       Text(item.name,fontSize=11.sp,color=V17Mute)
      }
      TextButton(onClick={open(index)}){Text("Details ›",color=V17Gold)}
     }
     V21CommonsLicense(item)
    }
   }
  }
 }
}
@Composable internal fun V24TourProgress(items:List<V17Monument>,open:(Int)->Unit){
 val ctx=LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("bagan_tour_v24",0)}
 var visited by remember{mutableStateOf(prefs.getStringSet("visited",emptySet())?.toSet()?:emptySet<String>())}
 Text("VIRTUAL MUSEUM TOUR",fontSize=22.sp,fontWeight=FontWeight.Bold,color=V17Ink)
 Text("Self-recorded learning stops only; not GPS verification of a physical visit.",fontSize=12.sp,color=V17Mute)
 Text(visited.size.toString()+" / "+items.size+" completed",fontSize=22.sp,color=V17Gold)
 LinearProgressIndicator(progress={visited.size/items.size.toFloat()},modifier=Modifier.fillMaxWidth().padding(vertical=10.dp))
 items.forEachIndexed{i,item->
  Surface(color=Color.White,shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){
   Row(Modifier.padding(9.dp)){
    MonumentArtworkV19(item.slug,Modifier.size(61.dp))
    Spacer(Modifier.width(9.dp))
    Column(Modifier.weight(1f)){
     Text(item.mm,color=V17Ink,fontSize=14.sp,fontWeight=FontWeight.Bold)
     TextButton(onClick={open(i)},contentPadding=PaddingValues(0.dp)){Text("Explore 3D ›",color=V17Gold)}
    }
    Checkbox(checked=item.slug in visited,onCheckedChange={value->
     visited=if(value)visited+item.slug else visited-item.slug
     prefs.edit().putStringSet("visited",visited).apply()
    })
   }
  }
 }
}
@Composable internal fun V24SamsungChecklist(){
 val ctx=LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("bagan_qa_v24",0)}
 val tests=listOf("Install and launch APK","Splash, welcome and homepage","Bottom navigation and Back",
 "Gallery filter and favorites","10 offline 3D models","Rotate, pinch and hotspots","Offline cached photo preview",
 "ARCore permissions / plane tracking","AR size rotate and reset","Myanmar and English voice",
 "Audio progress saved","Resume after screen lock")
 var pass by remember{mutableStateOf(prefs.getStringSet("pass",emptySet())?.toSet()?:emptySet<String>())}
 Text("SAMSUNG RUNTIME QA",color=V17Ink,fontSize=23.sp,fontWeight=FontWeight.Bold)
 Text("Manual test checklist for your device. No device tests were performed by the build runner.",color=V17Mute,fontSize=12.sp)
 Text(pass.size.toString()+" / "+tests.size+" marked passed",color=V17Gold,fontSize=20.sp)
 tests.forEach{test->
  Row(Modifier.fillMaxWidth().padding(vertical=4.dp)){
   Checkbox(checked=test in pass,onCheckedChange={yes->
    pass=if(yes)pass+test else pass-test
    prefs.edit().putStringSet("pass",pass).apply()
   })
   Text(test,color=V17Ink,modifier=Modifier.padding(top=13.dp),fontSize=13.sp)
  }
 }
}
