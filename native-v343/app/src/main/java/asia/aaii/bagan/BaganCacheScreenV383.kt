package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun BaganCacheScreenV383(onBack:()->Unit){
 val ctx=LocalContext.current
 val scope=rememberCoroutineScope()
 var stats by remember { mutableStateOf(emptyList<BaganCacheStat>()) }
 var busy by remember { mutableStateOf(false) }
 var message by remember { mutableStateOf("Bundled stylized CGI models are safe and cannot be deleted here.") }
 var confirm by remember { mutableStateOf<BaganCacheGroup?>(null) }
 LaunchedEffect(Unit){
  stats=withContext(Dispatchers.IO){BaganCacheV383.scan(ctx)}
 }
 Column(Modifier.fillMaxSize().background(Color(0xFF081B2A)).padding(14.dp)){
  TextButton(onClick=onBack){Text("‹ Settings",color=Color(0xFFFFD476))}
  Text("3D Offline Storage",color=Color(0xFFFFD476),
    fontWeight=FontWeight.Bold,style=MaterialTheme.typography.headlineSmall)
  Text("မူရင်း GLB၊ Authentic GLB နှင့် AR Cache ကို စီမံရန်",
    color=Color.White,style=MaterialTheme.typography.bodySmall)
  Spacer(Modifier.height(12.dp))
  LazyColumn(verticalArrangement=Arrangement.spacedBy(11.dp)){
   items(stats.size){index->
    val item=stats[index]
    Surface(color=Color(0xFF142D40),shape=RoundedCornerShape(14.dp),
     modifier=Modifier.fillMaxWidth(),
     border=BorderStroke(1.dp,Color(0xFF345268))){
     Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
      Text(item.group.caption,color=Color.White,fontWeight=FontWeight.SemiBold)
      Text("${item.files} models • ${BaganCacheV383.formatSize(item.bytes)}",
       color=Color(0xFFFFD476))
      OutlinedButton(onClick={confirm=item.group},enabled=!busy && item.bytes>0){
       Text("Clear this cache",color=Color(0xFFFFD476))
      }
     }
    }
   }
   item{
    Text(message,color=Color(0xFFC1CFD9),style=MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(8.dp))
    Button(onClick={
     scope.launch{
      busy=true
      stats=withContext(Dispatchers.IO){BaganCacheV383.scan(ctx)}
      message="Cache status refreshed."
      busy=false
     }
    },enabled=!busy){Text("Refresh Storage")}
   }
  }
 }
 if(confirm!=null){
  val target=confirm!!
  AlertDialog(onDismissRequest={confirm=null},title={Text("Clear ${target.caption}?")},
   text={Text("Downloaded models in this cache will be deleted. Bundled offline CGI stays inside the APK.")},
   confirmButton={
    TextButton(onClick={
     confirm=null
     scope.launch{
      busy=true
      try{
       val cleared=withContext(Dispatchers.IO){BaganCacheV383.clear(ctx,target)}
       stats=withContext(Dispatchers.IO){BaganCacheV383.scan(ctx)}
       message="Removed ${cleared.cleared} files; freed ${BaganCacheV383.formatSize(cleared.freedBytes)}."
      }catch(e:Exception){message="Cache error: ${e.localizedMessage}"}
      busy=false
     }
    }){Text("Clear",color=Color(0xFFB7323D))}
   },dismissButton={TextButton(onClick={confirm=null}){Text("Cancel")}})
 }
}
