package asia.aaii.baganmuseum
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
internal fun V19Museum3D(selected:Int,items:List<V17Monument>,select:(Int)->Unit,ar:()->Unit,detail:()->Unit){
 val safe=selected.coerceIn(0,items.lastIndex)
 Text("3D HERITAGE EXPLORER",color=Color(0xFFF2C46E),fontSize=21.sp)
 Text("10 independent offline concept models",color=Color.White)
 Spacer(Modifier.height(10.dp))
 Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){
  items.forEachIndexed{index,item->
   Surface(onClick={select(index)},color=if(index==safe)Color(0xFF8B592E) else Color(0xFF3D2B1D),shape=RoundedCornerShape(12.dp),modifier=Modifier.width(112.dp)){
    Column(Modifier.padding(6.dp)){
     MonumentArtworkV19(item.slug,Modifier.fillMaxWidth().height(69.dp))
     Text(item.mm,color=Color.White,maxLines=1,fontSize=11.sp)
    }
   }
  }
 }
 Spacer(Modifier.height(12.dp))
 HeritageOffline3DV16(safe)
 Text(items[safe].detail,color=Color(0xFFF2D6AD),fontSize=12.sp)
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
  V17GoldenButton("Museum AR",Modifier.weight(1f),onClick=ar)
  V17NeutralButton("Info",Modifier.weight(1f),onClick=detail)
 }
 V20Hotspots(items[safe])
 Text("AR places the museum concept, not the selected monument. Illustrations are not archaeological scans.",color=Color(0xFFCCB6A2),fontSize=11.sp)
}
