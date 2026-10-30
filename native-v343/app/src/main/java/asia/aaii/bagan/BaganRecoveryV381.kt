package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val recoveryNavy = Color(0xFF081B2A)
private val recoveryPanel = Color(0xFF142D40)
private val recoveryGold = Color(0xFFFFD476)

@Composable
fun BaganRecoveryV381(
    error:String,
    onRetry:()->Unit,
    onLightweight:()->Unit,
    onDiagnostics:()->Unit,
    onHelp:()->Unit,
    onBack:()->Unit
){
  Column(Modifier.fillMaxSize().background(recoveryNavy).verticalScroll(rememberScrollState())
    .padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    Text("⚠  3D / AR Error & Recovery",fontWeight=FontWeight.Bold,fontSize=23.sp,color=recoveryGold)
    Text("3D မပေါ်ခြင်း၊ AR ဖွင့်မရခြင်းတို့ကို စစ်ဆေးနိုင်ပါတယ်။",
      color=Color.White,fontSize=13.sp)
    Surface(color=recoveryPanel,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
      Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text("Model could not load",fontWeight=FontWeight.Bold,color=Color.White,fontSize=18.sp)
        Text(error.ifBlank{"3D Model unavailable. Try the lightweight offline CGI or run diagnostics."},
          color=Color(0xFFD1DDEA),fontSize=12.sp)
      }
    }
    Text("RECOVERY ACTIONS • ပြန်လည်ဖွင့်ရန်",fontWeight=FontWeight.Bold,color=recoveryGold)
    Button(onClick=onLightweight,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
      colors=ButtonDefaults.buttonColors(containerColor=recoveryGold,contentColor=recoveryNavy)){
      Text("⬡  Use Lightweight CGI (Offline)",fontWeight=FontWeight.Bold)
    }
    OutlinedButton(onClick=onRetry,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){
      Text("↻ Retry Original GLB",color=recoveryGold)
    }
    OutlinedButton(onClick=onDiagnostics,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){
      Text("⚙  3D / AR Diagnostics",color=recoveryGold)
    }
    Surface(color=recoveryPanel,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
      Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
        Text("COMMON ERRORS & SOLUTIONS",fontWeight=FontWeight.Bold,color=recoveryGold)
        listOf(
          "GLB 404 / No Internet" to "Use offline CGI or import verified authentic GLB ZIP.",
          "GLB Invalid / Black Model" to "Check GLB 2.0, geometry, textures and cache status.",
          "ARCore not supported" to "Use 3D Viewer; AR needs compatible hardware.",
          "Camera Permission" to "Allow Camera when opening AR Mode.",
          "Low Storage / Out of Memory" to "Use lightweight CGI; remove old downloaded caches."
        ).forEach{ (cause,solution)->
          Text(cause,color=Color.White,fontWeight=FontWeight.SemiBold,fontSize=13.sp)
          Text(solution,color=Color(0xFFBFCFDB),fontSize=12.sp)
          HorizontalDivider(color=Color(0xFF304D61))
        }
      }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
      OutlinedButton(onClick=onHelp,modifier=Modifier.weight(1f)){Text("Help",color=recoveryGold)}
      OutlinedButton(onClick=onBack,modifier=Modifier.weight(1f)){Text("Pagodas",color=recoveryGold)}
    }
    Text("Diagnostics reflect actual device checks when run; design mockup statuses are not live results.",
      textAlign=TextAlign.Center,color=Color.LightGray,fontSize=11.sp)
  }
}
