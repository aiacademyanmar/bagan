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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

private val BaganBrown=Color(0xFF59300F)
private val BaganGold=Color(0xFFC38A3D)
private val BaganCream=Color(0xFFFFF8EE)

@Composable
fun V354Search(
  temples:List<Temple>,query:String,onQuery:(String)->Unit,
  onDetails:(Temple)->Unit,onThreeD:(Temple)->Unit
){
 Column(Modifier.fillMaxSize().background(BaganCream).padding(horizontal=14.dp)){
  Text("Search / ရှာဖွေရန်",style=MaterialTheme.typography.headlineSmall,
    fontWeight=FontWeight.Bold,color=BaganBrown,modifier=Modifier.padding(vertical=14.dp))
  OutlinedTextField(query,onQuery,modifier=Modifier.fillMaxWidth(),
    singleLine=true,placeholder={Text("Search temples / ဘုရားအမည် ရှာရန်")},
    leadingIcon={Text("⌕")},shape=RoundedCornerShape(14.dp))
  val matches=temples.filter{query.isBlank()||it.nameEn.contains(query,true)||it.nameMm.contains(query,true)}
  Text("Results / ရလဒ်များ — ${matches.size}",modifier=Modifier.padding(vertical=12.dp),color=BaganBrown)
  LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),
    contentPadding=PaddingValues(bottom=20.dp)){
    items(matches){t->
      Card(colors=CardDefaults.cardColors(containerColor=Color.White)){
        Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
          Image(painter=painterResource(id=R.drawable.bagan_illustration_1),contentDescription="Bagan illustration",modifier=Modifier.size(75.dp,78.dp),contentScale=ContentScale.Crop)
          Spacer(Modifier.width(10.dp))
          Column(Modifier.weight(1f)){
          Text(t.nameMm,fontSize=17.sp,fontWeight=FontWeight.Bold,color=BaganBrown)
          Text(t.nameEn,fontSize=12.sp,color=Color.DarkGray)
          Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){
            TextButton(onClick={onDetails(t)}){Text("Details")}
            Button(onClick={onThreeD(t)},colors=ButtonDefaults.buttonColors(containerColor=BaganGold)){
              Text("View 3D")
            }
          }
          }
        }
      }
    }
    if(matches.isEmpty())item { Text("မတွေ့ရှိပါ။ အခြားအမည်ဖြင့် ပြန်ရှာပါ။",color=BaganBrown) }
  }
 }
}
@Composable
fun V354Categories(
  temples:List<Temple>,onCategory:(String)->Unit,onTemple:(Temple)->Unit
){
 val groups=listOf(
   Triple("All","အားလုံး",temples.size),
   Triple("Famous","ထင်ရှားသောဘုရားများ",temples.size),
   Triple("Temples","ဂူဘုရားများ",temples.count{it.nameEn.contains("temple",true)}),
   Triple("Stupas","စေတီပုထိုးများ",temples.count{!it.nameEn.contains("temple",true)})
 )
 LazyColumn(Modifier.fillMaxSize().background(BaganCream),
   contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{
     Text("Categories / အမျိုးအစားများ",style=MaterialTheme.typography.headlineSmall,
       fontWeight=FontWeight.Bold,color=BaganBrown)
   }
   items(groups){(key,name,count)->
     Card(onClick={onCategory(key)},colors=CardDefaults.cardColors(containerColor=Color.White),
       modifier=Modifier.fillMaxWidth()){
       Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){
         Image(painter=painterResource(id=R.drawable.bagan_illustration_2),contentDescription=null,modifier=Modifier.size(64.dp),contentScale=ContentScale.Crop)
         Spacer(Modifier.width(14.dp))
         Column(Modifier.weight(1f)){
           Text(name,fontWeight=FontWeight.Bold,color=BaganBrown)
           Text("$count records",fontSize=12.sp,color=Color.Gray)
         }
         Text("›",fontSize=25.sp,color=BaganGold)
       }
     }
   }
   item{Text("Featured / အထူးရွေးချယ်ထားသောနေရာများ",fontWeight=FontWeight.Bold,color=BaganBrown)}
   items(temples.take(4)){t->
     OutlinedCard(onClick={onTemple(t)},modifier=Modifier.fillMaxWidth()){
       Column(Modifier.padding(12.dp)){
         Text(t.nameMm,fontWeight=FontWeight.Bold,color=BaganBrown)
         Text(t.nameEn,color=Color.Gray,fontSize=12.sp)
       }
     }
   }
 }
}
