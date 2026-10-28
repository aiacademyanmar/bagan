package asia.aaii.bagan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val darkBlue=Color(0xFF112A3D)
private val goldV357=Color(0xFFBC863D)
private val ivoryV357=Color(0xFFFFF7EA)

@Composable private fun TitleV357(mm:String,en:String) {
 Column(Modifier.padding(vertical=12.dp)){
  Text(mm,fontSize=23.sp,fontWeight=FontWeight.Bold,color=darkBlue)
  Text(en,fontSize=13.sp,color=Color.DarkGray)
 }
}
@Composable private fun ActionV357(mm:String,en:String,click:()->Unit) {
 ElevatedCard(onClick=click,modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.fillMaxWidth().padding(16.dp)){
   Text(mm,fontWeight=FontWeight.Bold,color=darkBlue)
   Text(en,color=Color.DarkGray,fontSize=12.sp)
  }
 }
}
@Composable fun SettingsV357(language:String,onLanguage:()->Unit,offline:Boolean,onOffline:(Boolean)->Unit,onHelp:()->Unit,onProfile:()->Unit,onDiagnostics:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivoryV357),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{TitleV357("ဆက်တင်များ","Settings")}
  item{ActionV357("ဘာသာစကား — $language","Language",onLanguage)}
  item{ElevatedCard(Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
   Column(Modifier.weight(1f)){Text("အင်တာနက်မလိုသည့်ပုံစံ",color=darkBlue,fontWeight=FontWeight.Bold);Text("Offline Mode",fontSize=12.sp)}
   Switch(checked=offline,onCheckedChange=onOffline)
  }}}
  item{ActionV357("ကိုယ်ရေးအချက်အလက်","Profile",onProfile)}
  item{ActionV357("အကူအညီ","Help",onHelp)}
  item{ActionV357("3D / AR စစ်ဆေးရန်","Diagnostics",onDiagnostics)}
  item{Text("3D အရည်အသွေးနှင့် Cache ထိန်းချုပ်မှုအတွက် မူလ Settings စာမျက်နှာကို နောက်ထပ် ချိတ်ဆက်နိုင်ပါသည်။",fontSize=12.sp,color=Color.Gray)}
 }
}
@Composable fun ProfileV357(savedCount:Int,onFavorites:()->Unit,onHome:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivoryV357),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{TitleV357("ကိုယ်ရေးအချက်အလက်","Profile")}
  item{Text("Guest / ဧည့်သည်",fontWeight=FontWeight.Bold,fontSize=20.sp,color=darkBlue)}
  item{Text("လက်ရှိဗားရှင်းတွင် Account Login မပါဝင်သေးပါ။",color=Color.DarkGray)}
  item{ActionV357("အကြိုက်ဆုံးများ ($savedCount)","Saved places",onFavorites)}
  item{ActionV357("ပင်မစာမျက်နှာ","Return Home",onHome)}
 }
}
@Composable fun HelpV357(onThreeD:()->Unit,onAR:()->Unit,onMap:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivoryV357),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{TitleV357("အကူအညီနှင့် ပံ့ပိုးမှု","Help & Support")}
  item{ActionV357("3D ပုံစံကြည့်ရှုနည်း","3D Viewer guide",onThreeD)}
  item{ActionV357("AR ကင်မရာ အသုံးပြုနည်း","AR Camera guide",onAR)}
  item{ActionV357("မြေပုံကြည့်ရှုနည်း","Bagan Map guide",onMap)}
  item{Text("AR မရနိုင်သောဖုန်းများတွင် 3D Viewer ကို အသုံးပြုနိုင်ပါသည်။ GLB ဖိုင် မရှိပါက Download သို့မဟုတ် Offline Demo ကို စစ်ဆေးပါ။",fontSize=13.sp,color=darkBlue)}
 }
}
@Composable fun AboutV357(onHome:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivoryV357),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{TitleV357("အက်ပ်အကြောင်း","About")}
  item{Text("BAGAN HERITAGE MUSEUM",fontWeight=FontWeight.Black,fontSize=24.sp,color=darkBlue)}
  item{Text("Version 3.5.7 • AAII.ASIA",color=goldV357)}
  item{Text("ပုဂံအမွေအနှစ်များကို သမိုင်း၊ 3D၊ AR နှင့် မြေပုံများဖြင့် လေ့လာနိုင်ရန် ရည်ရွယ်ထားသော ဒစ်ဂျစ်တယ်လေ့လာရေးအက်ပ် ဖြစ်ပါသည်။",color=darkBlue)}
  item{Text("တရားဝင်ပြတိုက်၏ ကိုယ်စားပြုအက်ပ်မဟုတ်ပါ။",fontSize=12.sp,color=Color.DarkGray)}
  item{ActionV357("ပင်မစာမျက်နှာ","Home",onHome)}
 }
}
@Composable fun ExitV357(onCancel:()->Unit,onExit:()->Unit){
 BackHandler {onCancel()}
 Column(Modifier.fillMaxSize().background(ivoryV357).padding(24.dp),
  verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
  Text("အက်ပ်မှ ထွက်မလား။",fontWeight=FontWeight.Bold,fontSize=24.sp,color=darkBlue)
  Spacer(Modifier.height(12.dp))
  Text("Do you want to exit Bagan Heritage Museum?",color=Color.DarkGray)
  Spacer(Modifier.height(20.dp))
  Button(onClick=onCancel,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=goldV357)){Text("မထွက်သေးပါ")}
  OutlinedButton(onClick=onExit,modifier=Modifier.fillMaxWidth()){Text("ထွက်ရန်")}
 }
}
