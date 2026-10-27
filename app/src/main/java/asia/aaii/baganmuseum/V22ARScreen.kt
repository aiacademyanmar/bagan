package asia.aaii.baganmuseum
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
internal fun V22ARScreen(items:List<V17Monument>,selected:Int,select:(Int)->Unit,go3D:()->Unit){
 val context=LocalContext.current
 val safe=selected.coerceIn(0,items.lastIndex)
 Text("MONUMENT AR PLACEMENT",fontSize=23.sp,color=V17Ink)
 Text("Choose a concept model to place on a flat surface",color=V17Mute,fontSize=12.sp)
 Spacer(Modifier.height(10.dp))
 items.forEachIndexed{index,item->
   Surface(onClick={select(index)},modifier=Modifier.fillMaxWidth().padding(vertical=3.dp),
       color=if(index==safe)Color(0xFFF1D6AD)else Color.White,shape=RoundedCornerShape(12.dp)){
     Row(Modifier.padding(8.dp)){
       MonumentArtworkV19(item.slug,Modifier.size(56.dp))
       Spacer(Modifier.width(10.dp))
       Column(Modifier.weight(1f)){
          Text(item.mm,color=V17Ink,fontSize=14.sp)
          Text(item.name,color=V17Mute,fontSize=11.sp)
       }
       Text(if(index==safe)"●" else "○",color=V17Gold)
     }
   }
 }
 Spacer(Modifier.height(10.dp))
 V17GoldenButton("◎  Place "+items[safe].name+" in AR",Modifier.fillMaxWidth()){
    val intent=Intent(context,ArMuseumActivity::class.java)
    intent.putExtra("monument_slug",items[safe].slug)
    intent.putExtra("monument_name",items[safe].name)
    context.startActivity(intent)
 }
 Spacer(Modifier.height(8.dp))
 V17NeutralButton("Open offline 3D viewer",Modifier.fillMaxWidth()){go3D()}
 Text("AR requires ARCore-supported hardware. GLB models are educational concepts, not historical scans.",color=V17Mute,fontSize=11.sp)
}
