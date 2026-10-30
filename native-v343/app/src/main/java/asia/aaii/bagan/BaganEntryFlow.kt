package asia.aaii.bagan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

private val InkBlue = Color(0xFF112438)
private val HeritageGold = Color(0xFFB9792E)
private val Parchment = Color(0xFFFFF6E7)

@Composable
fun BaganEntryFlow(
    temples: List<Temple>,
    onMap: (Temple)->Unit,
    onAR: (Temple)->Unit,
    onSpeak: (String)->Unit,
    onStopSpeech: ()->Unit,
    onSpeechRate: (Float)->Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("bagan_onboarding_v353",0) }
    var step by remember { mutableIntStateOf(if(prefs.getBoolean("completed",false)) 4 else 1) }
    var language by remember { mutableStateOf(prefs.getString("language","မြန်မာ") ?: "မြန်မာ") }
    BackHandler(enabled=step in 2..3) { step -= 1 }
    when(step) {
        1 -> BaganWelcome(
            title="BAGAN HERITAGE MUSEUM",
            subtitle="ပုဂံအမွေအနှစ်ပြတိုက်",
            description="သမိုင်းဝင် ပုဂံမြို့တော်ကို လေ့လာကြမယ်",
            action="စတင်လေ့လာရန်",
            onNext={step=2}
        )
        2 -> BaganWelcome(
            title="EXPLORE BAGAN",
            subtitle="ပုဂံကို လေ့လာကြမယ်",
            description="ရှေးဟောင်းဘုရားပုထိုးများ၊ သမိုင်းကြောင်း၊ 3D နှင့် AR နည်းပညာများကို တစ်နေရာတည်းတွင် လေ့လာနိုင်ပါသည်။",
            action="ရှေ့ဆက်ရန်",
            onNext={step=3}
        )
        3 -> Column(
            Modifier.fillMaxSize().background(Parchment)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal=22.dp,vertical=30.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment=Alignment.CenterHorizontally){
                Text("BAGAN",fontSize=35.sp,color=HeritageGold,fontWeight=FontWeight.Black)
                Text("ဘာသာစကားရွေးချယ်ရန်",fontSize=22.sp,color=InkBlue,fontWeight=FontWeight.Bold)
                Text("Select Language",color=InkBlue)
            }
            Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                listOf("မြန်မာ" to "မြန်မာ (Unicode)","English" to "English").forEach{ (value,label)->
                    Surface(
                        onClick={language=value},
                        modifier=Modifier.fillMaxWidth().heightIn(min=60.dp),
                        shape=RoundedCornerShape(14.dp),
                        color=if(language==value)Color(0xFFF1D6A7) else Color.White,
                        border=BorderStroke(1.dp,HeritageGold)
                    ){ Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                        RadioButton(selected=language==value,onClick={language=value})
                        Text(label,color=InkBlue,fontSize=17.sp)
                    }}
                }
                Text("အခြားဘာသာစကားများကို နောက်ပိုင်းတွင် ထည့်သွင်းပါမည်။",fontSize=12.sp,color=InkBlue)
            }
            Button(onClick={
                prefs.edit().putString("language",language).putBoolean("completed",true).apply()
                context.getSharedPreferences("bagan_heritage_350",0).edit().putString("language",language).apply()
                step=4
            },modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),
                colors=ButtonDefaults.buttonColors(containerColor=HeritageGold)) {
                Text("ပင်မစာမျက်နှာသို့",fontSize=17.sp)
            }
        }
        else -> BaganHeritageApp(temples,onMap,onAR,onSpeak,onStopSpeech,onSpeechRate)
    }
}

@Composable
private fun BaganWelcome(title:String,subtitle:String,description:String,action:String,onNext:()->Unit){
    Box(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Parchment,Color(0xFFE8BF7D),InkBlue))
    ).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.SpaceBetween
        ){
            Column(horizontalAlignment=Alignment.CenterHorizontally){
                Text("♜",fontSize=72.sp,color=HeritageGold)
                Text(title,fontWeight=FontWeight.Black,color=InkBlue,fontSize=26.sp,textAlign=TextAlign.Center)
                Text(subtitle,fontSize=18.sp,color=InkBlue)
            }
            Column(horizontalAlignment=Alignment.CenterHorizontally){
                Text("✦",fontSize=105.sp,color=HeritageGold)
                Text(description,fontSize=17.sp,color=Color.White,textAlign=TextAlign.Center,lineHeight=27.sp)
            }
            Button(onClick=onNext,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
                colors=ButtonDefaults.buttonColors(containerColor=HeritageGold),
                shape=RoundedCornerShape(14.dp)){
                Text(action,fontWeight=FontWeight.Bold,fontSize=18.sp)
            }
        }
    }
}
