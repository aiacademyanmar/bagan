package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsV383(
 language:String,onLanguage:()->Unit,offline:Boolean,onOffline:(Boolean)->Unit,
 onHelp:()->Unit,onProfile:()->Unit,onDiagnostics:()->Unit,
 onStorage:()->Unit,onDownload:()->Unit,
 fastMode:Boolean,onFastMode:(Boolean)->Unit
){
 val gold=Color(0xFFFFD476)
 val light=Color(0xFFF7F5F0)
 @Composable fun SettingAction(label:String,description:String,action:()->Unit){
  Surface(onClick=action,color=Color(0xFF142D40),shape=RoundedCornerShape(13.dp),
    border=BorderStroke(1.dp,Color(0xFF38516A)),modifier=Modifier.fillMaxWidth()){
   Column(Modifier.padding(15.dp)){
    Text(label,color=gold,fontWeight=FontWeight.SemiBold)
    Text(description,color=light,style=MaterialTheme.typography.bodySmall)
   }
  }
 }
 LazyColumn(Modifier.fillMaxSize().background(Color(0xFF081B2A)),
  contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{
   Text("Settings • ဆက်တင်များ",color=gold,style=MaterialTheme.typography.headlineSmall)
   Text("BAGAN Native V${BuildConfig.VERSION_NAME}",color=light)
  }
  item{SettingAction("Language / ဘာသာစကား","Current: $language",onLanguage)}
  item{
   Surface(color=Color(0xFF142D40),shape=RoundedCornerShape(13.dp),modifier=Modifier.fillMaxWidth()){
    Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
     Row{
      Column(Modifier.weight(1f)){
       Text("Fast 3D CGI",color=gold,fontWeight=FontWeight.Bold)
       Text("Recommended for Samsung / lower GPU load",color=light,style=MaterialTheme.typography.bodySmall)
      }
      Switch(checked=fastMode,onCheckedChange=onFastMode)
     }
     HorizontalDivider(color=Color(0xFF38516A))
     Row{
      Column(Modifier.weight(1f)){
       Text("Offline Mode",color=gold,fontWeight=FontWeight.Bold)
       Text("Use locally cached originals; bundled CGI always works offline",color=light,style=MaterialTheme.typography.bodySmall)
      }
      Switch(checked=offline,onCheckedChange=onOffline)
     }
    }
   }
  }
  item{SettingAction("Manage Offline GLB Storage","View cache sizes and clear original/authentic/AR caches safely",onStorage)}
  item{SettingAction("Download / Import GLB ZIP","Go to offline model downloads and GLB import",onDownload)}
  item{SettingAction("AR Camera Diagnostics","Check ARCore support, camera permission and GLB data",onDiagnostics)}
  item{SettingAction("Help & User Guide","3D, AR and offline troubleshooting",onHelp)}
  item{SettingAction("My Saved / Profile","Preferences and saved places",onProfile)}
 }
}
