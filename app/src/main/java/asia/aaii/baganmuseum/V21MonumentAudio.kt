package asia.aaii.baganmuseum

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val englishNarration=mapOf(
 "thatbyinnyu" to "Thatbyinnyu is a prominent multi-storey brick temple in Old Bagan. Observe its layered roof and soaring spire.",
 "myazedi" to "The Myazedi inscription is associated with four historic languages: Pali, Pyu, Mon, and Old Burmese.",
 "entrance" to "This Bagan entrance gateway is an imaginative museum concept model, not a verified historic monument.",
 "tharabar" to "Tharabar Gate is a surviving gateway of the old city of Bagan, associated with the ancient city walls.",
 "gawdawpalin" to "Gawdawpalin Temple is an imposing Bagan structure known for multi-tier brick architecture.",
 "ananda" to "Ananda Temple is celebrated for its cruciform architectural plan and central sanctuary.",
 "htilominlo" to "Htilominlo Temple is distinguished by its imposing brickwork and layered architecture.",
 "alodawpyae" to "Alodawpyae Pagoda is part of the cultural landscape of Bagan.",
 "shwezigon" to "Shwezigon is a gilded Buddhist pagoda and a landmark of Myanmar's stupa-building tradition.",
 "bupaya" to "Bupaya Pagoda is located by the Ayeyarwady river in Bagan and is known for its rounded profile."
)
@Composable
internal fun V21MonumentAudio(items:List<V17Monument>,selected:Int,onSelect:(Int)->Unit){
 val ctx=LocalContext.current
 val current=selected.coerceIn(0,items.lastIndex)
 val prefs=remember{ctx.getSharedPreferences("bagan_audio_v22",0)}
 var completed by remember {mutableStateOf(prefs.getStringSet("completed",emptySet())?.toSet()?:emptySet<String>())}
 var myanmar by remember{mutableStateOf(true)}
 var ready by remember{mutableStateOf(false)}
 var message by remember{mutableStateOf("Initializing voice engine…")}
 var rate by remember{mutableFloatStateOf(1f)}
 val tts=remember(ctx){
    TextToSpeech(ctx){result->
       ready=result==TextToSpeech.SUCCESS
       message=if(ready)"Voice engine ready" else "Voice engine unavailable on this phone"
    }
 }
 DisposableEffect(tts) {
     tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
        override fun onStart(utteranceId:String?){}
        override fun onError(utteranceId:String?){}
        override fun onDone(utteranceId:String?){
            val slug=utteranceId?.removePrefix("v22-")?.substringBefore("-my")?.substringBefore("-en")?:return
            if(utteranceId.startsWith("v22-")){
                val next=(prefs.getStringSet("completed",emptySet())?.toSet()?:emptySet())+slug
                prefs.edit().putStringSet("completed",next).apply()
                completed=next
            }
        }
     })
     onDispose{tts.stop();tts.shutdown()}
 }
 val item=items[current]
 Text("MONUMENT AUDIO GUIDE",color=Color(0xFFF4C373),fontSize=21.sp,fontWeight=FontWeight.Black)
 Text(item.mm,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold)
 Text(item.name,color=Color(0xFFD2BCA0),fontSize=13.sp)
 Text("Listening completed: "+completed.size+" / "+items.size,color=Color(0xFFF4C373),fontSize=12.sp)
 LinearProgressIndicator(progress={completed.size/items.size.toFloat()},modifier=Modifier.fillMaxWidth().padding(vertical=8.dp))
 Spacer(Modifier.height(12.dp))
 V21HeritagePhoto(item,Modifier.fillMaxWidth().height(180.dp),showLabel=true)
 Spacer(Modifier.height(9.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
  V17GoldenButton("မြန်မာ",Modifier.weight(1f)){myanmar=true;tts.stop()}
  V17NeutralButton("English",Modifier.weight(1f)){myanmar=false;tts.stop()}
 }
 Spacer(Modifier.height(10.dp))
 val content=if(myanmar)item.detail else englishNarration[item.slug].orEmpty()
 Text(content,color=Color.White,fontSize=13.sp,lineHeight=23.sp)
 Spacer(Modifier.height(12.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
  V17NeutralButton("‹ Previous",Modifier.weight(1f)){
   tts.stop();onSelect((current+items.size-1)%items.size)
  }
  V17GoldenButton("▶ Speak",Modifier.weight(1.3f),enabled=ready){
   val result=tts.setLanguage(if(myanmar)Locale.forLanguageTag("my-MM") else Locale.US)
   if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
      message=if(myanmar)"Myanmar voice data not installed" else "English voice unavailable"
   }else{
      tts.setSpeechRate(rate)
      tts.speak(content,TextToSpeech.QUEUE_FLUSH,null,"v22-"+item.slug+(if(myanmar)"-my" else "-en"))
      message="Speaking: "+item.name
   }
  }
  V17NeutralButton("■ Stop",Modifier.weight(1f)){tts.stop();message="Stopped"}
  V17NeutralButton("Next ›",Modifier.weight(1f)){
   tts.stop();onSelect((current+1)%items.size)
  }
 }
 Text("Speech speed",color=Color(0xFFE2CAA8),fontSize=12.sp)
 Slider(value=rate,onValueChange={rate=it},valueRange=.65f..1.3f)
 Text(message,color=Color(0xFFF4C373),fontSize=11.sp)
 Spacer(Modifier.height(10.dp))
 V21CommonsLicense(item)
 Text("The guide uses generated speech, not a professional recording. Language voice support depends on your Android TTS engine.",
    color=Color(0xFFD2BCA0),fontSize=11.sp)
}
