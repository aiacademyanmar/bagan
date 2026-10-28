package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val midnight=Color(0xFF102A3D)
private val amber=Color(0xFFBC853D)
private val ivory=Color(0xFFFFF7EA)

@Composable private fun SectionTitle(mm:String,en:String) {
 Column(Modifier.padding(16.dp)) {
  Text(mm,fontSize=22.sp,fontWeight=FontWeight.Bold,color=midnight)
  Text(en,fontSize=13.sp,color=Color.DarkGray)
 }
}
@Composable private fun HeritageTile(title:String,description:String,onClick:()->Unit){
 ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth()){
  Column(Modifier.padding(16.dp)){
   Text(title,color=midnight,fontWeight=FontWeight.Bold,fontSize=17.sp)
   Spacer(Modifier.height(6.dp))
   Text(description,color=Color.DarkGray,fontSize=13.sp)
  }
 }
}
@Composable fun RoutesV356(temples:List<Temple>,details:(Temple)->Unit,directions:(Temple)->Unit){
 val selected=temples.take(6)
 LazyColumn(Modifier.fillMaxSize().background(ivory),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("ခရီးစဉ်အကြံပြုချက်များ","Suggested Routes")}
  item{Text("အောက်ပါစာရင်းသည် ဥပမာခရီးစဉ်ဖြစ်ပြီး လမ်းကြောင်းအကွာအဝေးကို အတည်မပြုရသေးပါ။",fontSize=12.sp,color=midnight)}
  items(selected){t->
   HeritageTile(t.nameMm,t.nameEn){details(t)}
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
    TextButton(onClick={directions(t)}){Text("မြေပုံဖွင့်ရန်")}
   }
  }
 }
}
@Composable fun FavoritesV356(temples:List<Temple>,details:(Temple)->Unit,remove:(Temple)->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivory),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("အကြိုက်ဆုံးများ","Favorites")}
  if(temples.isEmpty())item{Text("သိမ်းဆည်းထားသောနေရာ မရှိသေးပါ။",color=midnight)}
  items(temples,key={it.id}){t->
   ElevatedCard(modifier=Modifier.fillMaxWidth()){
    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f)){
      Text(t.nameMm,fontWeight=FontWeight.Bold,color=midnight)
      Text(t.nameEn,fontSize=12.sp,color=Color.Gray)
     }
     TextButton(onClick={details(t)}){Text("ကြည့်ရန်")}
     TextButton(onClick={remove(t)}){Text("ဖယ်ရှားရန်")}
    }
   }
  }
 }
}
@Composable fun MuseumInfoV356(onHistory:()->Unit,onArtifacts:()->Unit){
 LazyColumn(Modifier.fillMaxSize().background(ivory),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("ပုဂံအမွေအနှစ်ပြတိုက်","Museum Information")}
  item{HeritageTile("ပုဂံယဉ်ကျေးမှုအမွေအနှစ်","သမိုင်းဝင်ဘုရားပုထိုးများနှင့် ဗိသုကာလက်ရာများကို ဒစ်ဂျစ်တယ်ပုံစံဖြင့် လေ့လာနိုင်ပါသည်။",onHistory)}
  item{HeritageTile("ရှေးဟောင်းပစ္စည်းများ","ပြတိုက်ဆိုင်ရာ အကြောင်းအရာနှင့် ပြသမှုပုံစံများကို ကြည့်ရှုရန်။",onArtifacts)}
  item{Text("ဤအက်ပ်သည် ဒစ်ဂျစ်တယ်လေ့လာရေးအတွက်ဖြစ်ပြီး တရားဝင်ပြတိုက်၏ ကိုယ်စားပြုအက်ပ်ဟု မဆိုလိုပါ။",fontSize=12.sp,color=Color.Gray)}
 }
}
@Composable fun ArtifactsV356(onBack:()->Unit){
 val categories=listOf("ဗုဒ္ဓဆင်းတုတော်များ","ကျောက်စာများ","မြေအိုးနှင့် ကြွေထည်များ","ဗိသုကာအလှဆင်လက်ရာများ")
 LazyColumn(Modifier.fillMaxSize().background(ivory),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("ရှေးဟောင်းပစ္စည်းများ","Artifacts")}
  item{Text("အောက်ပါအမျိုးအစားများသည် လေ့လာရေးအတွက် နမူနာစာရင်းဖြစ်ပြီး အတည်ပြုထားသော ပြတိုက်ပစ္စည်းစာရင်း မဟုတ်သေးပါ။",fontSize=12.sp,color=midnight)}
  items(categories){c->HeritageTile(c,"Verified item photographs and descriptions pending"){}}
  item{OutlinedButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text("ပြန်သွားရန်")}}
 }
}
@Composable fun TimelineV356(onSpeak:(String)->Unit){
 val entries=listOf("ပုဂံခေတ်အစောပိုင်း","ဘုရားပုထိုးတည်ဆောက်မှုများ ထွန်းကားခဲ့သောခေတ်","ပုဂံအမွေအနှစ် ထိန်းသိမ်းရေးလုပ်ငန်းများ","၂၀၁၉ — UNESCO ကမ္ဘာ့အမွေအနှစ်စာရင်းဝင်")
 LazyColumn(Modifier.fillMaxSize().background(ivory),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("ပုဂံသမိုင်းအချိန်ဇယား","History Timeline")}
  items(entries){e->HeritageTile(e,"အချိန်ကာလနှင့် သမိုင်းအချက်အလက်များကို ဆက်လက်အတည်ပြုမည်။"){}}
  item{Button(onClick={onSpeak("Bagan was inscribed on the UNESCO World Heritage List in 2019.")},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=amber)){Text("အသံဖြင့် နားထောင်ရန်")}}
 }
}
