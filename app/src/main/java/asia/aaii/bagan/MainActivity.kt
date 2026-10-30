package asia.aaii.bagan
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val navy=Color(0xFF031624)
private val gold=Color(0xFFFFCB57)
private val temples=listOf("Shwezigon Pagoda" to "ရွှေစည်းခုံစေတီ","Ananda Temple" to "အာနန္ဒာဘုရား","Thatbyinnyu Temple" to "သဗ္ဗညုဘုရား","Dhammayangyi Temple" to "ဓမ္မရံကြီးဘုရား","Shwesandaw Pagoda" to "ရွှေဆံတော်စေတီ")
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { BaganUi() }}}
@Composable fun BaganUi(){
 var page by remember { mutableStateOf("Home") }
 var selected by remember { mutableStateOf(temples[0]) }
 var search by remember { mutableStateOf("") }
 val saved=remember { mutableStateListOf<String>() }
 MaterialTheme(colorScheme=darkColorScheme(background=navy,primary=gold,surface=Color(0xFF082333))){
 Column(Modifier.fillMaxSize().background(navy)){
 Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween){
 Text("BAGAN",fontSize=32.sp,color=gold,fontWeight=FontWeight.Bold)
 Text("MM | EN",color=Color.White)
 }
 Text("HERITAGE MUSEUM   •   "+page,modifier=Modifier.padding(horizontal=18.dp),color=gold)
 Spacer(Modifier.height(10.dp))
 LazyColumn(Modifier.weight(1f).padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
 item { if(page=="Home"){
 Text("Explore • Learn • Experience",color=Color.White,fontSize=21.sp)
 Spacer(Modifier.height(12.dp))
 TextField(search,{search=it},label={Text("Search pagodas, history, places...")},modifier=Modifier.fillMaxWidth())
 Spacer(Modifier.height(12.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ listOf("Pagodas","3D Viewer","AR Experience","History").forEach { b-> OutlinedButton(onClick={page=b},modifier=Modifier.weight(1f),contentPadding=PaddingValues(4.dp)){Text(b,fontSize=10.sp)} } }
 Spacer(Modifier.height(10.dp))
 Text("Discover the Ancient City of BAGAN",fontSize=22.sp,color=gold)
 Text("Famous Pagodas",color=Color.White,fontSize=22.sp)
 } else {
 Text(page,color=gold,fontSize=26.sp)
 if(page=="Pagoda Detail"){Text(selected.first,color=Color.White,fontSize=24.sp);Text(selected.second,color=gold)}
 if(page=="3D Viewer" || page=="AR Experience"){Text("3D/AR model requires a downloadable GLB model and supported device.",color=Color.White);Button(onClick={page="3D Downloads"}){Text("Download 3D Models")}}
 }}
 items(temples.size){i->val temple=temples[i]
 if(search.isBlank() || temple.first.contains(search,true) || temple.second.contains(search)){
 Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF102B3D)),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
 Column(Modifier.padding(16.dp)){
 Text(temple.first,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold)
 Text(temple.second,color=gold,fontSize=17.sp)
 Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
 TextButton(onClick={selected=temple;page="Pagoda Detail"}){Text("Details")}
 TextButton(onClick={selected=temple;page="3D Viewer"}){Text("3D")}
 TextButton(onClick={selected=temple;page="AR Experience"}){Text("AR")}
 TextButton(onClick={if(saved.contains(temple.first))saved.remove(temple.first) else saved.add(temple.first)}){Text(if(saved.contains(temple.first))"♥" else "♡")}
 }
 }
 }
 }
 }
 }
 Row(Modifier.fillMaxWidth().padding(7.dp),horizontalArrangement=Arrangement.SpaceEvenly){listOf("Home","Pagodas","Scan","Map","Saved").forEach {item->
 TextButton(onClick={page=item}){Text(item,color=if(page==item)gold else Color.White,fontSize=12.sp)}
 }}
 }
 }
}
