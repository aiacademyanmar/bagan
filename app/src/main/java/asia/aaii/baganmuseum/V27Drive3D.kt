package asia.aaii.baganmuseum

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

internal data class V27Model(val slug:String,val filename:String,val title:String)
internal val V27Models=listOf(
 V27Model("lawka_hteik_pan","lawka_hteik_pan.glb","Lawka Hteik Pan"),
 V27Model("dhammayangyi","Dhammayangyi_Temple.glb","Dhammayangyi Temple"),
 V27Model("shwesandaw","Shwesandaw_Bagan.glb","Shwesandaw Pagoda"),
 V27Model("thatbyinnyu","Thatbyinnyu_Temple.glb","Thatbyinnyu Temple"),
 V27Model("myazedi","Myazedi_Stone_Inscription.glb","Myazedi Inscription"),
 V27Model("entrance","Bagan_Entrance_Gateway.glb","Bagan Entrance Gateway"),
 V27Model("tharabar","Tharabar_Gate.glb","Tharabar Gate"),
 V27Model("gawdawpalin","Gawdawpalin_Temple.glb","Gawdawpalin Temple"),
 V27Model("ananda","Ananda_Temple.glb","Ananda Temple"),
 V27Model("htilominlo","Htilominlo_Temple.glb","Htilominlo Temple"),
 V27Model("alodawpyae","Alodawpyae_Pagoda.glb","Alodawpyae Pagoda"),
 V27Model("shwezigon","Shwezigon_Pagoda.glb","Shwezigon Pagoda"),
 V27Model("bupaya","Bupaya_Realistic.glb","Bupaya Pagoda")
)
internal object V27Import {
    private fun folder(ctx:Context)=File(ctx.filesDir,"drive3d/models").apply{mkdirs()}
    fun file(ctx:Context,model:V27Model):File = File(folder(ctx),model.filename)
    fun present(ctx:Context,model:V27Model):Boolean {
        val f=file(ctx,model)
        if(!f.isFile||f.length()<20||f.length()>65_000_000L)return false
        return try {
            FileInputStream(f).use{
                val head=ByteArray(12)
                it.read(head)==12 &&
                  String(head,0,4,Charsets.US_ASCII)=="glTF" &&
                  java.nio.ByteBuffer.wrap(head,4,4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int==2 &&
                  java.nio.ByteBuffer.wrap(head,8,4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int.toLong()==f.length()
            }
        }catch(_:Exception){false}
    }
    fun count(ctx:Context)=V27Models.count{present(ctx,it)}
    fun importZip(ctx:Context,uri:Uri):String{
        var imported=0
        var bytes=0L
        val expected=V27Models.associateBy{"models/"+it.filename}
        val seen=mutableSetOf<String>()
        val src=ctx.contentResolver.openInputStream(uri)?:error("Cannot open selected ZIP")
        src.use { stream->
            ZipInputStream(stream).use { zip->
                var entry=zip.nextEntry
                while(entry!=null){
                    val model=expected[entry.name]
                    if(model!=null && entry.name !in seen && !entry.isDirectory){
                        seen.add(entry.name)
                        val out=file(ctx,model)
                        val tmp=File(out.parentFile,out.name+".pending")
                        try {
                            FileOutputStream(tmp).use { target->
                                val buf=ByteArray(64*1024)
                                var fileBytes=0L
                                while(true){
                                    val n=zip.read(buf)
                                    if(n<0)break
                                    fileBytes+=n;bytes+=n
                                    if(fileBytes>65_000_000L||bytes>200_000_000L)error("ZIP size limit exceeded")
                                    target.write(buf,0,n)
                                }
                            }
                            // Verify file bytes including GLB length before installing.
                            val header=FileInputStream(tmp).use{it.readNBytes(12)}
                            if(header.size==12 && String(header,0,4,Charsets.US_ASCII)=="glTF" &&
                                java.nio.ByteBuffer.wrap(header,4,4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int==2 &&
                                java.nio.ByteBuffer.wrap(header,8,4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int.toLong()==tmp.length()){
                                if(out.exists())out.delete()
                                if(!tmp.renameTo(out))error("Could not save "+model.filename)
                                imported++
                            } else tmp.delete()
                        }catch(e:Exception){tmp.delete();throw e}
                    }
                    zip.closeEntry()
                    entry=zip.nextEntry
                }
            }
        }
        return "Imported "+imported+" original Drive GLB models. Total installed: "+count(ctx)+" / 13."
    }
}

@Composable
internal fun V27Drive3DGallery() {
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    var imported by remember{mutableIntStateOf(V27Import.count(ctx))}
    var status by remember{mutableStateOf("Import the matching Drive 3D ZIP once to view the original models offline.")}
    var busy by remember{mutableStateOf(false)}
    var selected by remember{mutableIntStateOf(-1)}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        if(uri!=null){
            busy=true
            scope.launch{
                status=try{withContext(Dispatchers.IO){V27Import.importZip(ctx,uri)}}
                    catch(e:Exception){"Import failed: "+e.javaClass.simpleName}
                imported=V27Import.count(ctx)
                busy=false
            }
        }
    }
    Text("BAGAN • GOOGLE DRIVE 3D",fontSize=21.sp,fontWeight=FontWeight.Black,color=V17Ink)
    Text("Original supplied GLB models • Offline after ZIP import • Rotate / Zoom",fontSize=12.sp,color=V17Mute)
    Spacer(Modifier.height(10.dp))
    V17Stat("Imported 3D Models", "$imported / 13",Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    V17GoldenButton(if(busy)"Importing…" else "Import Google Drive 3D ZIP",Modifier.fillMaxWidth(),enabled=!busy){
        launcher.launch(arrayOf("application/zip","application/octet-stream","*/*"))
    }
    Spacer(Modifier.height(7.dp))
    Text(status,color=V17Mute,fontSize=12.sp)
    Spacer(Modifier.height(10.dp))
    V27Models.forEachIndexed{idx,m->
        val ready=V27Import.present(ctx,m)
        Surface(color=Color.White,shape=RoundedCornerShape(11.dp),
            modifier=Modifier.fillMaxWidth().padding(vertical=3.dp),
            onClick={if(ready)selected=idx}){
            Row(Modifier.padding(11.dp)){
                Text(m.title,modifier=Modifier.weight(1f),color=V17Ink,fontWeight=FontWeight.Bold,fontSize=13.sp)
                Text(if(ready)"View 3D ›" else "Import needed",
                     color=if(ready)V17Gold else V17Mute,fontSize=11.sp)
            }
        }
    }
    if(selected in V27Models.indices && V27Import.present(ctx,V27Models[selected])){
        Spacer(Modifier.height(14.dp))
        Text(V27Models[selected].title,fontSize=19.sp,fontWeight=FontWeight.Bold,color=V17Ink)
        V27ImportedGLBViewer(V27Models[selected])
    }
    Spacer(Modifier.height(9.dp))
    Text("These are user-supplied original 3D assets. Import extracts only the 13 expected GLB filenames to app-private storage and does not access your Drive account.",fontSize=11.sp,color=V17Mute)
}

@Composable
private fun V27ImportedGLBViewer(model:V27Model){
    val ctx=LocalContext.current
    key(model.slug){
        AndroidView(factory={context->
            val assetLoader=WebViewAssetLoader.Builder()
                .addPathHandler("/assets/",WebViewAssetLoader.AssetsPathHandler(context))
                .addPathHandler("/drive3d/",WebViewAssetLoader.InternalStoragePathHandler(context,File(context.filesDir,"drive3d")))
                .build()
            WebView(context).apply{
                settings.javaScriptEnabled=true
                settings.allowFileAccess=false
                settings.allowContentAccess=false
                settings.domStorageEnabled=false
                setBackgroundColor(android.graphics.Color.rgb(25,18,14))
                webViewClient=object:WebViewClient(){
                    override fun shouldInterceptRequest(view:WebView?,request:WebResourceRequest?) =
                        if(request!=null)assetLoader.shouldInterceptRequest(request.url) else null
                }
                loadUrl("https://appassets.androidplatform.net/assets/drive3d_viewer_v27.html?model="+Uri.encode(model.filename))
            }
        },modifier=Modifier.fillMaxWidth().height(500.dp),
            onRelease={it.stopLoading();it.loadUrl("about:blank");it.destroy()})
    }
}
