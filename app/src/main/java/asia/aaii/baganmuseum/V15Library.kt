package asia.aaii.baganmuseum

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

private val txt=Color(0xFFFFE8CB)
private val dim=Color(0xFFC9B7A5)
private val accent=Color(0xFFF3BD69)
private data class DriveExhibit(val name:String,val mm:String,val type:String,val folder:String,val model:String,val preview:String,val detail:String)
private fun catalog(ctx:android.content.Context):List<DriveExhibit> {
    val json=ctx.assets.open("bagan_drive_catalog_v15.json").bufferedReader().use{it.readText()}
    val array=JSONObject(json).getJSONArray("items")
    return (0 until array.length()).map {
        val o=array.getJSONObject(it)
        DriveExhibit(o.getString("name"),o.getString("myanmar"),o.getString("category"),
            o.getString("folder_id"),o.getString("model_id"),o.getString("preview_id"),o.getString("description_mm"))
    }
}
private fun drive(id:String) = "https://drive.google.com/file/d/"+id+"/view"
private fun folder(id:String) = "https://drive.google.com/drive/folders/"+id
private fun openUrl(ctx:android.content.Context,url:String){
    ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))
}
@Composable fun DriveMuseumCatalog() {
    val context=LocalContext.current
    val list=remember{ catalog(context) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    var selected by remember { mutableIntStateOf(-1) }
    Text("Bagan Heritage Library",fontSize=23.sp,fontWeight=FontWeight.Bold,color=txt)
    Text("Google Drive collection snapshot • 10 items",color=dim,fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    if(selected>=0) {
        val item=list[selected]
        TextButton(onClick={selected=-1}){Text("‹ Back to Collection")}
        Text(item.mm,color=accent,fontSize=23.sp,fontWeight=FontWeight.Bold)
        Text(item.name,color=txt)
        Spacer(Modifier.height(10.dp))
        Text(item.detail,color=txt)
        Text("3D Source: Blender (.blend) — not yet a mobile GLB",color=dim,fontSize=12.sp)
        Spacer(Modifier.height(12.dp))
        Button(onClick={openUrl(context,folder(item.folder))},modifier=Modifier.fillMaxWidth()){Text("Open Drive Collection")}
        OutlinedButton(onClick={openUrl(context,drive(item.preview))},modifier=Modifier.fillMaxWidth()){Text("View Preview on Drive")}
        OutlinedButton(onClick={openUrl(context,drive(item.model))},modifier=Modifier.fillMaxWidth()){Text("Download Blender Source via Drive")}
        Text("QR: folder link (access permissions apply)",color=dim)
        QrImage(folder(item.folder))
        OutlinedButton(onClick={
            val share=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,folder(item.folder))}
            context.startActivity(Intent.createChooser(share,"Share exhibit"))
        },modifier=Modifier.fillMaxWidth()){Text("Share Exhibit Link")}
        Text("Models are interpreted reconstructions, not precise archaeological scans.",color=dim,fontSize=12.sp)
        return
    }
    OutlinedTextField(value=search,onValueChange={search=it},modifier=Modifier.fillMaxWidth(),label={Text("Search monuments")},singleLine=true)
    Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        listOf("All","Temple","Pagoda","Gate","Inscription").forEach{c->
            FilterChip(selected=category==c,onClick={category=c},label={Text(c,fontSize=10.sp)})
        }
    }
    list.forEachIndexed{idx,item->
        if((category=="All"||category==item.type) &&
           (item.name.contains(search,true)||item.mm.contains(search,true))){
            Surface(onClick={selected=idx},modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),color=Color(0xFF302018)){
                Column(Modifier.padding(13.dp)){
                    Text(item.mm,color=accent,fontWeight=FontWeight.Bold)
                    Text(item.name+" • "+item.type,color=txt,fontSize=12.sp)
                    Text("Open info, QR and Blender files ›",color=dim,fontSize=11.sp)
                }
            }
        }
    }
}
@Composable private fun QrImage(content:String) {
    val bitmap=remember(content) {
        val matrix=MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE,300,300)
        Bitmap.createBitmap(300,300,Bitmap.Config.ARGB_8888).also{ bmp ->
            for(y in 0 until 300) for(x in 0 until 300)
                bmp.setPixel(x,y,if(matrix.get(x,y))AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    AndroidView(factory={ctx->ImageView(ctx).apply{setImageBitmap(bitmap)}},modifier=Modifier.size(190.dp))
}
private data class Quiz(val question:String,val options:List<String>,val answer:Int)
private val questions=listOf(
    Quiz("Myazedi ကျောက်စာနှင့် ဆက်စပ်သော အမျိုးအစားမှာ?",listOf("ကျောက်စာ","တံခါး","ရေကန်"),0),
    Quiz("Ananda Temple ကို Drive Collection တွင် ဘယ် Format ဖြင့် ထည့်ထားသလဲ?",listOf("MP3",".blend","APK"),1),
    Quiz("မိုဘိုင်းမှာ 3D Model လှည့်ကြည့်ရန် မည်သည့် Format ကို Export လုပ်ရမလဲ?",listOf("GLB","DOCX","CSV"),0),
    Quiz("Tharabar Gate သည် မည်သည့်အမျိုးအစားလဲ?",listOf("Pagoda","Gate","Inscription"),1),
    Quiz("ယခု Museum Room Navigation သည်?",listOf("GPS Survey","Concept Route","Live Indoor GPS"),1),
    Quiz("QR Code သည် မည်သို့အသုံးပြုသလဲ?",listOf("Drive Link ဖွင့်ရန်","ARCore Install လုပ်ရန်","အော့ဖ်လိုင်းမြေပုံအစားထိုးရန်"),0)
)
@Composable fun MuseumQuizV15() {
    val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("quiz_v15",0)}
    var step by remember{mutableIntStateOf(0)}
    var choice by remember{mutableIntStateOf(-1)}
    var score by remember{mutableIntStateOf(0)}
    var completed by remember{mutableStateOf(false)}
    Text("Bagan Museum Quiz",color=txt,fontSize=23.sp,fontWeight=FontWeight.Bold)
    Text("6 questions • Offline learning",color=dim)
    Spacer(Modifier.height(12.dp))
    if(completed) {
        Text("Result: "+score+" / "+questions.size,color=accent,fontSize=25.sp)
        Text("Your best result: "+prefs.getInt("best",0),color=txt)
        Button(onClick={step=0;choice=-1;score=0;completed=false}){Text("Retake Quiz")}
    } else {
        LinearProgressIndicator(progress={(step+1)/questions.size.toFloat()},modifier=Modifier.fillMaxWidth())
        Text(questions[step].question,color=txt,fontSize=17.sp,modifier=Modifier.padding(vertical=17.dp))
        questions[step].options.forEachIndexed{i,text->
            Surface(onClick={choice=i},color=if(choice==i)Color(0xFF875330)else Color(0xFF302018),
                modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){
                Text(text,color=txt,modifier=Modifier.padding(15.dp))
            }
        }
        Button(enabled=choice>=0,onClick={
            val next=score+(if(choice==questions[step].answer)1 else 0)
            score=next
            if(step==questions.lastIndex){
                completed=true
                prefs.edit().putInt("best",maxOf(next,prefs.getInt("best",0))).apply()
            } else step++
            choice=-1
        },modifier=Modifier.fillMaxWidth()){Text(if(step==questions.lastIndex)"Finish Quiz" else "Next Question")}
    }
}