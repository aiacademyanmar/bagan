package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalContext

/** Native UI based on BAGAN 1080x1920 design references.
 * Reference PNGs are visual specifications, NOT interactive app screens.
 * All controls below are Compose nodes, with real navigation callbacks.
 */
private val bg385 = Color(0xFFFFF8EC)
private val navy385 = Color(0xFF071B2A)
private val gold385 = Color(0xFFD5A044)
private val cream385 = Color(0xFFFFE2A1)
private val ink385 = Color(0xFF243341)
private val muted385 = Color(0xFF67788A)
private val edge385 = Color(0xFFE9DDC8)
private val img385 = listOf(R.drawable.bagan_safe_1,R.drawable.bagan_safe_2,
 R.drawable.bagan_safe_3,R.drawable.bagan_safe_4,R.drawable.bagan_safe_5,R.drawable.bagan_safe_6)

@Composable
private fun KPanel385(modifier:Modifier=Modifier, content:@Composable ColumnScope.()->Unit){
 Column(modifier.clip(RoundedCornerShape(16.dp))
  .background(Color.White).border(1.dp,edge385,RoundedCornerShape(16.dp))
  .padding(12.dp),verticalArrangement=Arrangement.spacedBy(7.dp),content=content)
}

@Composable
private fun KHeader385(title:String,number:Int,description:String=""){
 Column(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=10.dp),
  verticalArrangement=Arrangement.spacedBy(7.dp)){
  Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(navy385)
   .padding(horizontal=13.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically){
   Box(Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)).background(gold385),
    contentAlignment=Alignment.Center){
    Text(number.toString(),color=navy385,fontWeight=FontWeight.Bold)
   }
   Spacer(Modifier.width(11.dp))
   Text(title,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold)
  }
  if(description.isNotBlank())Text(description,color=muted385,fontSize=12.sp)
 }
}

@Composable
private fun KGold385(text:String,action:()->Unit,modifier:Modifier=Modifier){
 Button(onClick=action,modifier=modifier.heightIn(min=43.dp),shape=RoundedCornerShape(11.dp),
  colors=ButtonDefaults.buttonColors(containerColor=gold385,contentColor=navy385),
  contentPadding=PaddingValues(horizontal=9.dp,vertical=7.dp)){
  Text(text,fontSize=11.sp,fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.Center)
 }
}

@Composable
private fun KOutline385(text:String,action:()->Unit,modifier:Modifier=Modifier){
 OutlinedButton(onClick=action,modifier=modifier.heightIn(min=43.dp),shape=RoundedCornerShape(11.dp),
  border=BorderStroke(1.dp,edge385),contentPadding=PaddingValues(horizontal=8.dp,vertical=7.dp)){
  Text(text,fontSize=11.sp,color=ink385,maxLines=2,textAlign=TextAlign.Center)
 }
}

@Composable
private fun KThumb385(index:Int,modifier:Modifier=Modifier){
 Image(painterResource(img385[index.mod(img385.size)]),
  contentDescription="Illustrative Bagan heritage image",
  modifier=modifier.clip(RoundedCornerShape(11.dp)),contentScale=ContentScale.Crop)
}

@Composable
fun BaganNavTopV385(active:Int,onGo:(Int)->Unit){
 val tabs=listOf(
  Triple(2,"⌂","Home"),Triple(3,"♜","Pagodas"),Triple(7,"◆","Map"),
  Triple(5,"⬡","3D"),Triple(6,"◎","AR"),Triple(16,"☰","More")
 )
 Surface(color=navy385,modifier=Modifier.fillMaxWidth(),
  border=BorderStroke(1.dp,gold385)){
  Row(Modifier.fillMaxWidth().padding(horizontal=3.dp,vertical=4.dp),
   horizontalArrangement=Arrangement.spacedBy(2.dp)){
   tabs.forEach{(route,glyph,label)->
    val current=route==active || (route==3 && active in listOf(4,11,12,13,14)) ||
     (route==16 && active in listOf(15,17,18,21,28))
    Column(Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
     .background(if(current)Color(0xFF273B45) else Color.Transparent)
     .clickable{onGo(route)}.padding(vertical=5.dp),
     horizontalAlignment=Alignment.CenterHorizontally){
     Text(glyph,fontSize=23.sp,color=cream385)
     Text(label,fontSize=10.sp,color=if(current)cream385 else Color.White,maxLines=1)
    }
   }
  }
 }
}

@Composable
fun BaganPagodasV385(temples:List<Temple>,search:String,onSearch:(String)->Unit,
 category:String,onCategory:(String)->Unit,onDetail:(Temple)->Unit,
 on3D:(Temple)->Unit,onAR:(Temple)->Unit,onMap:(Temple)->Unit){
 val matches=BaganCategoryFilter.matching(temples,category,search)
 Column(Modifier.fillMaxSize().background(bg385)){
  OutlinedTextField(value=search,onValueChange=onSearch,
   modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=11.dp),
   singleLine=true,shape=RoundedCornerShape(28.dp),
   placeholder={Text("Search pagodas...")},
   leadingIcon={Text("⌕",fontSize=24.sp,color=gold385)},
   colors=OutlinedTextFieldDefaults.colors(
    focusedContainerColor=Color.White,unfocusedContainerColor=Color.White,
    focusedBorderColor=gold385,unfocusedBorderColor=edge385))
  Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=12.dp),
   horizontalArrangement=Arrangement.spacedBy(7.dp)){
   listOf("All","Famous","Temples","Stupas","Others").forEach{name->
    Surface(onClick={onCategory(name)},color=if(name==category)gold385 else Color(0xFFF0E9DE),
     shape=RoundedCornerShape(13.dp)){
     Text(name,Modifier.padding(horizontal=14.dp,vertical=11.dp),
      color=if(name==category)Color.White else ink385,
      fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
   }
  }
  LazyColumn(Modifier.fillMaxSize().padding(top=7.dp),
   contentPadding=PaddingValues(start=12.dp,end=12.dp,bottom=18.dp),
   verticalArrangement=Arrangement.spacedBy(9.dp)){
   items(matches,key={it.id}){temple->
    val index=temples.indexOf(temple)
    Surface(onClick={onDetail(temple)},modifier=Modifier.fillMaxWidth(),
     color=Color.White,shape=RoundedCornerShape(15.dp),
     border=BorderStroke(1.dp,edge385),shadowElevation=1.dp){
     Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
      KThumb385(index,Modifier.size(102.dp,104.dp))
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
       Text(temple.nameEn,color=ink385,fontWeight=FontWeight.Bold,fontSize=14.sp,maxLines=2)
       Text(temple.nameMm,color=ink385,fontSize=12.sp,maxLines=1)
       Text("Bagan heritage site",color=muted385,fontSize=11.sp)
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Text("⬡ 3D",Modifier.clickable{on3D(temple)}.padding(vertical=6.dp),
         color=gold385,fontSize=12.sp)
        Text("◎ AR",Modifier.clickable{onAR(temple)}.padding(vertical=6.dp),
         color=gold385,fontSize=12.sp)
        Text("◆ Map",Modifier.clickable{onMap(temple)}.padding(vertical=6.dp),
         color=gold385,fontSize=12.sp)
       }
      }
      Text("›",fontSize=22.sp,color=ink385)
     }
    }
   }
   if(matches.isEmpty())item{
    Text("No matching pagodas. Try a different name.",
     Modifier.fillMaxWidth().padding(25.dp),color=ink385,textAlign=TextAlign.Center)
   }
  }
 }
}

@Composable
fun BaganTempleV385(temple:Temple,selectedTab:String,onTab:(String)->Unit,
 onBack:()->Unit,on3D:()->Unit,onAR:()->Unit,onMap:()->Unit,onSaved:()->Unit,
 saved:Boolean,onHistory:()->Unit,onGallery:()->Unit,onAudio:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(bg385),
  contentPadding=PaddingValues(bottom=22.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
  item{
   Box(Modifier.fillMaxWidth().height(270.dp)){
    KThumb385(temple.id.length,Modifier.fillMaxSize())
    Row(Modifier.fillMaxWidth().padding(12.dp),
     horizontalArrangement=Arrangement.SpaceBetween){
     Surface(onClick=onBack,shape=RoundedCornerShape(50),
      color=Color(0xD1081B2A)){Text("‹ Back",Modifier.padding(12.dp),color=Color.White)}
     Surface(onClick=onSaved,shape=RoundedCornerShape(50),
      color=Color(0xD1081B2A)){
      Text(if(saved)"♥ Saved" else "♡ Save",
       Modifier.padding(12.dp),color=Color.White)
     }
    }
   }
  }
  item{
   Column(Modifier.fillMaxWidth().padding(horizontal=15.dp),
    verticalArrangement=Arrangement.spacedBy(5.dp)){
    Text(temple.nameEn,color=ink385,fontSize=24.sp,fontWeight=FontWeight.Bold)
    Text(temple.nameMm,color=ink385,fontSize=18.sp)
    Text("Bagan • Myanmar • Architectural heritage",
     color=muted385,fontSize=12.sp)
   }
  }
  item{
   Row(Modifier.fillMaxWidth().padding(horizontal=10.dp),
    horizontalArrangement=Arrangement.spacedBy(7.dp)){
    listOf(Triple("⬡","3D View",on3D),Triple("◎","AR Mode",onAR),
      Triple("◆","Map",onMap),Triple(if(saved)"♥" else "♡","Save",onSaved)).forEach{
     (icon,text,action)->
     Surface(onClick=action,modifier=Modifier.weight(1f),
      color=Color(0xFFF5D18A),shape=RoundedCornerShape(12.dp)){
      Column(Modifier.fillMaxWidth().padding(vertical=12.dp),
       horizontalAlignment=Alignment.CenterHorizontally){
       Text(icon,color=Color(0xFF734710),fontSize=23.sp)
       Text(text,color=ink385,fontSize=10.sp,fontWeight=FontWeight.Bold)
      }
     }
    }
   }
  }
  item{
   Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=12.dp),
    horizontalArrangement=Arrangement.spacedBy(6.dp)){
    listOf("Overview","History","Architecture","Gallery").forEach{tab->
     Surface(onClick={onTab(tab)},color=if(tab==selectedTab)Color(0xFF996217) else Color(0xFFF0E9DE),
      shape=RoundedCornerShape(20.dp)){
      Text(tab,Modifier.padding(horizontal=13.dp,vertical=9.dp),
       color=if(tab==selectedTab)Color.White else ink385,fontSize=12.sp)
     }
    }
   }
  }
  item{
   KPanel385(Modifier.fillMaxWidth().padding(horizontal=12.dp)){
    Text(when(selectedTab){
     "History"->"Historical Background"
     "Architecture"->"Architectural Features"
     "Gallery"->"Temple Gallery"
     else->"About ${temple.nameEn}"
    },color=ink385,fontWeight=FontWeight.Bold,fontSize=15.sp)
    Text(when(selectedTab){
     "History"->"Explore Bagan's chronology and cultural heritage. Monument dates should be checked against authoritative records."
     "Architecture"->"Explore this monument's rooflines, brickwork, vaults and ornamental details. Images shown here are illustrative."
     "Gallery"->"Browse illustrated views and open the gallery."
     else->temple.description.ifBlank{"${temple.nameEn} is part of Bagan's architectural landscape in Myanmar."}
    },color=ink385,fontSize=13.sp,lineHeight=19.sp)
    KGold385(when(selectedTab){"History"->"Read History";"Gallery"->"Open Gallery";else->"Learn More"},
     when(selectedTab){"Gallery"->onGallery;"History"->onHistory;else->onAudio})
   }
  }
  item{
   Row(Modifier.padding(horizontal=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    KOutline385("🔊 Audio Guide",onAudio,Modifier.weight(1f))
    KOutline385("Gallery",onGallery,Modifier.weight(1f))
   }
  }
  item{
   Text("Pictures are illustrative; the bundled low-poly CGI is not an archaeological scan.",
    Modifier.fillMaxWidth().padding(13.dp),color=muted385,fontSize=11.sp,
    textAlign=TextAlign.Center)
  }
 }
}

@Composable
fun BaganSettingsV385(language:String,onLanguage:()->Unit,offline:Boolean,onOffline:(Boolean)->Unit,
 fast:Boolean,onFast:(Boolean)->Unit,onStorage:()->Unit,onImport:()->Unit,
 onARCheck:()->Unit,onHelp:()->Unit,onAbout:()->Unit,
 wifiOnly:Boolean,onWifi:(Boolean)->Unit,showProgress:Boolean,onProgress:(Boolean)->Unit){
 var quality by remember{mutableStateOf(false)}
 var theme by remember{mutableStateOf(false)}
 var promptAr by remember{mutableStateOf(false)}
 LazyColumn(Modifier.fillMaxSize().background(bg385),
  contentPadding=PaddingValues(bottom=20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
  item{KHeader385("Settings / ဆက်တင်များ",16)}
  item{Text("App appearance & language",Modifier.padding(start=18.dp,top=8.dp),
   color=ink385,fontWeight=FontWeight.Bold,fontSize=17.sp)}
  val rows=listOf(
   Triple("Language / ဘာသာစကား","$language / English",onLanguage),
   Triple("App theme","Dark Navy + Gold header, Ivory content",{theme=true}),
   Triple("3D rendering quality",if(fast)"Fast CGI (recommended)" else "High Detail",{quality=true}),
   Triple("Native AR mode","Supported phones • Requires ARCore",{promptAr=true}),
   Triple("Model cache","Manage offline files",onStorage),
   Triple("Import Original GLB ZIP","Open Offline Downloads",onImport),
   Triple("AR & Camera Diagnostics","Check hardware and permission",onARCheck),
   Triple("Help & User Guide","How to use 3D and AR",onHelp),
   Triple("About BAGAN","App version and heritage information",onAbout)
  )
  items(rows.size){i->
   if(i==3){
    KSettingsRow385(4,"Show download progress",
      if(showProgress)"Enabled" else "Disabled",toggle=showProgress,onToggle=onProgress)
    KSettingsRow385(5,"Download on Wi-Fi only",
      if(wifiOnly)"Enabled" else "Disabled",toggle=wifiOnly,onToggle=onWifi)
    KSettingsRow385(6,"Offline mode",
      if(offline)"Enabled" else "Disabled",toggle=offline,onToggle=onOffline)
   }
   val (title,sub,onClick)=rows[i]
   KSettingsRow385(i+1+if(i>=3)3 else 0,title,sub,onClick=onClick)
  }
  item{
   Text("BAGAN Heritage Museum • V${BuildConfig.VERSION_NAME}",
    Modifier.fillMaxWidth().padding(14.dp),color=muted385,fontSize=12.sp,
    textAlign=TextAlign.Center)
  }
 }
 if(quality)AlertDialog(onDismissRequest={quality=false},title={Text("3D Rendering")},
  text={Text("Fast CGI reduces GPU load. High Detail may use more power.")},
  confirmButton={TextButton(onClick={onFast(true);quality=false}){Text("Fast")}},
  dismissButton={TextButton(onClick={onFast(false);quality=false}){Text("High Detail")}})
 if(theme)AlertDialog(onDismissRequest={theme=false},title={Text("BAGAN Theme")},
  text={Text("The current official BAGAN design uses a Navy + Gold header with an Ivory content area.")},
  confirmButton={TextButton(onClick={theme=false}){Text("OK")}})
 if(promptAr)AlertDialog(onDismissRequest={promptAr=false},title={Text("AR Mode")},
  text={Text("AR needs a compatible Android phone, Camera permission and Google Play Services for AR.")},
  confirmButton={TextButton(onClick={onARCheck();promptAr=false}){Text("Diagnostics")}},
  dismissButton={TextButton(onClick={promptAr=false}){Text("Close")}})
}

@Composable
private fun KSettingsRow385(index:Int,title:String,description:String,
 onClick:(()->Unit)?=null,toggle:Boolean?=null,onToggle:((Boolean)->Unit)?=null){
 Surface(onClick={onClick ?: {}},modifier=Modifier.fillMaxWidth().padding(horizontal=13.dp,vertical=3.dp),
  color=Color.White,shape=RoundedCornerShape(14.dp),
  border=BorderStroke(1.dp,edge385)){
  Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
   Box(Modifier.size(39.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFF7EFE1)),
    contentAlignment=Alignment.Center){
    Text("$index",color=Color(0xFFC98D28),fontWeight=FontWeight.Bold)
   }
   Spacer(Modifier.width(12.dp))
   Column(Modifier.weight(1f)){
    Text(title,color=ink385,fontWeight=FontWeight.Bold,fontSize=14.sp)
    Text(description,color=muted385,fontSize=12.sp)
   }
   if(toggle!=null)Switch(checked=toggle,onCheckedChange=onToggle ?: {},
    colors=SwitchDefaults.colors(checkedTrackColor=gold385))
   else Text("›",fontSize=24.sp,color=gold385)
  }
 }
}

@Composable
fun BaganHelpV385(on3D:()->Unit,onAR:()->Unit,onMap:()->Unit,
 onDownloads:()->Unit,onDiagnostics:()->Unit){
 val help=listOf(
  Triple("How to view 3D models","Choose a pagoda, tap 3D, then drag to rotate.",on3D),
  Triple("3D model is not showing","Try Stylized CGI, or run Diagnostics.",onDiagnostics),
  Triple("AR camera does not open","Allow Camera, check ARCore support.",onAR),
  Triple("Offline download guide","Download original GLB or import a verified ZIP.",onDownloads),
  Triple("Maps and directions","Choose a temple and open Google Maps.",onMap),
  Triple("Contact technical support","Run Diagnostics and copy the report.",onDiagnostics)
 )
 LazyColumn(Modifier.fillMaxSize().background(bg385),
  verticalArrangement=Arrangement.spacedBy(7.dp),
  contentPadding=PaddingValues(bottom=24.dp)){
  item{KHeader385("Help & Guide / အကူအညီ",17)}
  item{Text("How can we help you?",Modifier.padding(horizontal=20.dp),
    color=ink385,fontWeight=FontWeight.Bold,fontSize=17.sp)}
  item{Text("Search help, 3D, AR, map and support…",
    Modifier.fillMaxWidth().padding(horizontal=17.dp,vertical=10.dp),
    color=muted385,fontSize=13.sp)}
  item{Text("Quick Help",Modifier.padding(horizontal=20.dp),
    color=ink385,fontSize=14.sp,fontWeight=FontWeight.Bold)}
  items(help){(title,sub,onClick)->
   Surface(onClick=onClick,modifier=Modifier.fillMaxWidth().padding(horizontal=13.dp),
    color=Color.White,shape=RoundedCornerShape(13.dp),
    border=BorderStroke(1.dp,edge385)){
    Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
     Text("◉",color=gold385,fontSize=20.sp)
     Spacer(Modifier.width(11.dp))
     Column(Modifier.weight(1f)){
      Text(title,color=ink385,fontWeight=FontWeight.Bold,fontSize=13.sp)
      Text(sub,color=muted385,fontSize=11.sp)
     }
     Text("›",color=gold385,fontSize=22.sp)
    }
   }
  }
  item{Text("Actual AR availability and model loading must be verified on the target device.",
    Modifier.padding(17.dp),color=muted385,fontSize=12.sp)}
 }
}

@Composable
fun BaganAboutV385(onHome:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(bg385),
  contentPadding=PaddingValues(bottom=22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{KHeader385("About BAGAN / အကြောင်း",18)}
  item{
   Box(Modifier.fillMaxWidth().height(250.dp).padding(horizontal=12.dp)){
    KThumb385(4,Modifier.fillMaxSize())
    Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
     Text("BAGAN",color=cream385,fontWeight=FontWeight.Black,fontSize=32.sp)
     Text("HERITAGE MUSEUM",color=Color.White,fontSize=15.sp)
     Text("Explore • Learn • Preserve",color=cream385,fontSize=12.sp)
    }
   }
  }
  item{KPanel385(Modifier.fillMaxWidth().padding(horizontal=13.dp)){
   Text("Our Mission",color=ink385,fontWeight=FontWeight.Bold,fontSize=16.sp)
   Text("Make Bagan's historical and architectural heritage easier to explore through educational content, 3D models, native AR, maps and offline access.",
    color=ink385,fontSize=12.sp)
   Text("This is an independent educational app, not an official museum or UNESCO app.",
    color=muted385,fontSize=11.sp)
  }}
  item{
   KPanel385(Modifier.fillMaxWidth().padding(horizontal=13.dp)){
    Text("BAGAN Heritage Museum • V${BuildConfig.VERSION_NAME}",color=ink385,fontWeight=FontWeight.Bold)
    Text("Produced for AAII.ASIA",color=muted385)
    KGold385("Return Home",onHome,Modifier.fillMaxWidth())
   }
  }
 }
}
