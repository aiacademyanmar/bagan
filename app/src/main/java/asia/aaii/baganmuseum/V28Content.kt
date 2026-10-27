package asia.aaii.baganmuseum

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

internal data class V28Site(
 val slug:String,val mm:String,val en:String,val group:String,val type:String,
 val descriptionMm:String,val descriptionEn:String,val area:String,val glb:String?,
 val mapQuery:String?,val needsReview:Boolean
)
internal fun v28Catalog(ctx:Context):List<V28Site>{
 val text=ctx.assets.open("bagan_content_v28_30.json").bufferedReader().use{it.readText()}
 val arr=JSONObject(text).getJSONArray("items")
 return (0 until arr.length()).map{i->
  val j=arr.getJSONObject(i)
  V28Site(j.getString("slug"),j.getString("name_mm"),j.getString("name_en"),
    j.getString("group"),j.getString("type"),j.getString("description_mm"),
    j.getString("description_en"),j.getString("area_mm"),
    j.optString("drive3d_slug").takeUnless{it.isBlank()||it=="null"},
    j.optString("map_search").takeUnless{it.isBlank()||it=="null"},
    j.optBoolean("verification_needed"))
 }
}
private fun v28Open(ctx:Context,url:String){
 try{ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}
}
private fun v28Commons(site:V28Site)=
 "https://commons.wikimedia.org/w/index.php?title=Special:MediaSearch&type=image&search="+Uri.encode(site.en+" Bagan")
private fun v28Google(query:String)=
 "https://www.google.com/maps/search/?api=1&query="+Uri.encode(query)
private fun v28Label(group:String)=when(group){
 "temples"->"ထင်ရှားသည့် ဘုရားပုထိုးများ"
 "landmarks"->"အထင်ကရ နေရာများ"
 else->"အားလုံး"
}
@Composable
internal fun V28Directory(
 items:List<V28Site>,group:String,changeGroup:(String)->Unit,lang:String,
 open:(Int)->Unit
){
 var search by rememberSaveable{mutableStateOf("")}
 var type by rememberSaveable{mutableStateOf("All")}
 val filtered=items.withIndex().filter{(_,item)->
  (group=="all"||item.group==group) &&
  (type=="All"||item.type==type) &&
  (search.isBlank()||item.en.contains(search,true)||item.mm.contains(search,true)
     ||item.area.contains(search,true))
 }
 Text(if(lang=="my")"ပုဂံ အမွေအနှစ်စာရင်း" else "BAGAN HERITAGE DIRECTORY",
   color=V17Ink,fontWeight=FontWeight.Black,fontSize=24.sp)
 Text(if(lang=="my")"ဘုရားပုထိုး ၂၅ ခု • အထင်ကရနေရာ ၅ ခု" else
   "25 famous temples and pagodas • 5 landmarks",color=V17Mute,fontSize=12.sp)
 Spacer(Modifier.height(9.dp))
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
  V17Stat("Pagodas / Temples","25",Modifier.weight(1f))
  V17Stat("Landmarks","5",Modifier.weight(1f))
 }
 Spacer(Modifier.height(12.dp))
 OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,
   placeholder={Text(if(lang=="my")"အမည် သို့မဟုတ် နေရာရှာရန်" else "Search name or area")},
   shape=RoundedCornerShape(14.dp))
 Spacer(Modifier.height(10.dp))
 Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){
  listOf("all" to "All • 30","temples" to "ဘုရားပုထိုး • 25",
     "landmarks" to "နေရာ • 5").forEach{(code,label)->
      V17Pill(label,group==code){changeGroup(code);type="All"}
  }
 }
 Spacer(Modifier.height(7.dp))
 Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
  listOf("All","Temple","Pagoda","Gate","Inscription","Area","Ordination Hall").forEach{filter->
    V17Pill(filter,type==filter){type=filter}
  }
 }
 Text("${filtered.size} result(s)",color=V17Mute,fontSize=11.sp,
    modifier=Modifier.padding(vertical=7.dp))
 filtered.forEach { (index,item)->
  Surface(onClick={open(index)},shape=RoundedCornerShape(16.dp),
     color=Color.White,shadowElevation=1.dp,
     modifier=Modifier.fillMaxWidth().padding(bottom=9.dp)){
   Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
    Box(Modifier.size(73.dp).background(
      Brush.linearGradient(listOf(Color(0xFF65432B),Color(0xFFB18753))),RoundedCornerShape(12.dp)),
      contentAlignment=Alignment.Center){
      Text(when(item.type){"Pagoda"->"♜";"Gate"->"▥";"Inscription"->"▤";"Area"->"⌖";else->"♙"},
       fontSize=34.sp,color=Color(0xFFFFE0A5))
    }
    Spacer(Modifier.width(11.dp))
    Column(Modifier.weight(1f)){
      Text(if(lang=="my")item.mm else item.en,color=V17Ink,fontSize=15.sp,
        fontWeight=FontWeight.Bold,maxLines=2,overflow=TextOverflow.Ellipsis)
      Text(if(lang=="my")item.en else item.mm,color=V17Mute,fontSize=11.sp,
        maxLines=1,overflow=TextOverflow.Ellipsis)
      Text(item.type+" • "+item.area,color=V17Gold,fontSize=10.sp)
      if(item.glb!=null)Text("3D model available by ZIP import",color=V17Green,fontSize=10.sp)
      if(item.needsReview)Text("Site details need verification",color=Color(0xFFB16E28),fontSize=10.sp)
    }
    Text("›",fontSize=24.sp,color=V17Gold)
   }
  }
 }
 if(filtered.isEmpty())Text("No matching heritage sites.",color=V17Mute)
 Spacer(Modifier.height(8.dp))
 Text("Introductory educational descriptions. Precise coordinates, periods and some names require individual verification.",
  color=V17Mute,fontSize=11.sp)
}

@Composable
internal fun V28Detail(site:V28Site,lang:String,favorite:Boolean,toggle:()->Unit,
    openModel:(String)->Unit){
 val ctx=LocalContext.current
 Column{
  Box(Modifier.fillMaxWidth().height(120.dp).background(
    Brush.horizontalGradient(listOf(Color(0xFF40291A),Color(0xFFAE7B42))),
    RoundedCornerShape(17.dp)).padding(17.dp)){
   Column{
    Text(if(site.group=="temples")"PAGODA & TEMPLE" else "HERITAGE LANDMARK",
      color=Color(0xFFF5CB80),fontSize=11.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(7.dp))
    Text(site.mm,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black,
     maxLines=2)
   }
  }
  Spacer(Modifier.height(13.dp))
  Row(verticalAlignment=Alignment.CenterVertically){
   Column(Modifier.weight(1f)){
    Text(site.en,color=V17Ink,fontSize=20.sp,fontWeight=FontWeight.Black)
    Text(site.type+" • "+v28Label(site.group),color=V17Mute,fontSize=12.sp)
   }
   TextButton(onClick=toggle){Text(if(favorite)"♥" else "♡",color=V17Gold,fontSize=27.sp)}
  }
  Spacer(Modifier.height(11.dp))
  Text(if(lang=="my")"သမိုင်းနှင့် အကြောင်းအရာ" else "OVERVIEW",color=V17Ink,fontSize=16.sp,
     fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(5.dp))
  Text(if(lang=="my")site.descriptionMm else site.descriptionEn,
    color=V17Ink,fontSize=14.sp,lineHeight=23.sp)
  Spacer(Modifier.height(8.dp))
  Text(if(lang=="my")site.descriptionEn else site.descriptionMm,
    color=V17Mute,fontSize=12.sp,lineHeight=20.sp)
  Spacer(Modifier.height(14.dp))
  Surface(color=Color.White,shape=RoundedCornerShape(12.dp)){
   Column(Modifier.fillMaxWidth().padding(13.dp)){
    Text("AREA",color=V17Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)
    Text(site.area,color=V17Ink,fontSize=14.sp)
    Spacer(Modifier.height(8.dp))
    Text("PERIOD",color=V17Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)
    Text("Specific construction date pending verification",color=V17Mute,fontSize=12.sp)
    if(site.needsReview){
     Spacer(Modifier.height(7.dp))
     Text("⚠ Site name / exact location requires verification before precise navigation.",
       color=Color(0xFFAA6832),fontSize=12.sp)
    }
   }
  }
  Spacer(Modifier.height(12.dp))
  if(site.glb!=null){
   V17GoldenButton("▧  Open supplied 3D GLB",Modifier.fillMaxWidth()){
    openModel(site.glb)
   }
   Spacer(Modifier.height(8.dp))
   Text("Import the original 13-file Drive GLB ZIP in the 3D gallery before viewing.",
     color=V17Mute,fontSize=11.sp)
  } else {
   Text("3D model: not yet supplied for this site.",color=V17Mute,fontSize=12.sp)
  }
  Spacer(Modifier.height(9.dp))
  if(site.mapQuery!=null){
   V17NeutralButton("⌖  Search Google Maps",Modifier.fillMaxWidth()){
     v28Open(ctx,v28Google(site.mapQuery))
   }
  }else{
   Text("No map shortcut until this site has been identified more precisely.",color=V17Mute,fontSize=11.sp)
  }
  Spacer(Modifier.height(9.dp))
  V17NeutralButton("▧  Search Wikimedia Commons photos",Modifier.fillMaxWidth()){
   v28Open(ctx,v28Commons(site))
  }
  Spacer(Modifier.height(12.dp))
  Text("Photos and location search are external results; check the original sources. No photograph or invented coordinate is embedded for this record.",
    color=V17Mute,fontSize=11.sp)
 }
}
