package asia.aaii.baganmuseum

import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** User-controlled export: packages installed educational GLBs and catalog data
 * to a ZIP via Android Storage Access Framework. This is NOT an auto-installer;
 * the APK already includes the 3D assets and text for offline use.
 */
internal object V26LearningPacks {
    fun entries(items:List<V17Monument>,category:String):List<V17Monument>{
        return if(category=="All")items else items.filter{it.category.equals(category,ignoreCase=true)}
    }
    fun verifyBundled(items:List<V17Monument>,ctx:android.content.Context):Pair<Int,Int>{
        var valid=0
        items.forEach { model->
            try{
                ctx.assets.open("models/"+model.slug+".glb").use{ input->
                    val sig=ByteArray(4)
                    if(input.read(sig)==4 && String(sig,Charsets.US_ASCII)=="glTF")valid++
                }
            }catch(_:Exception){}
        }
        return valid to items.size
    }
    fun export(ctx:android.content.Context,uri:Uri,items:List<V17Monument>):Int{
        require(items.isNotEmpty())
        val out=ctx.contentResolver.openOutputStream(uri,"w")
            ?:error("Android could not open the chosen destination")
        out.use { stream->
            ZipOutputStream(stream).use { zip->
                val array=JSONArray()
                items.forEach { entry->
                    val obj=JSONObject().put("slug",entry.slug).put("name_en",entry.name)
                        .put("name_my",entry.mm).put("category",entry.category)
                        .put("description_my",entry.detail).put("glb","models/"+entry.slug+".glb")
                    array.put(obj)
                }
                val json=JSONObject()
                    .put("title","Bagan Museum Offline Learning Pack")
                    .put("version","2.6")
                    .put("purpose","Educational concept models, not archaeological scans")
                    .put("monuments",array).toString(2)
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(json.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                val notes="""BAGAN MUSEUM V2.6 — OFFLINE LEARNING PACK

This export includes the selected 3D GLB concept models and bilingual
monument names, with Myanmar museum description data in manifest.json.
It can be inspected with a GLB viewer on a compatible device.
This is NOT a replacement Android application or a verified archaeological
reconstruction. No external photographs or proprietary .blend files included.

Models are generated educational interpretations included in the app.
For original Blender projects, consult the linked Google Drive collection.
"""
                zip.putNextEntry(ZipEntry("README.txt"))
                zip.write(notes.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                items.forEach { item->
                    val path="models/"+item.slug+".glb"
                    zip.putNextEntry(ZipEntry(path))
                    ctx.assets.open(path).use{it.copyTo(zip,64*1024)}
                    zip.closeEntry()
                }
            }
        }
        return items.size
    }
}

@Composable
internal fun V26OfflinePacks(items:List<V17Monument>){
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    var category by remember{mutableStateOf("All")}
    var status by remember{mutableStateOf("All offline concept models are already bundled with the APK.")}
    var active by remember{mutableStateOf(false)}
    var verify by remember{mutableStateOf<Pair<Int,Int>?>(null)}
    val selected=V26LearningPacks.entries(items,category)
    var pending by remember{mutableStateOf<List<V17Monument>>(emptyList())}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri->
        if(uri==null){status="Export cancelled";return@rememberLauncherForActivityResult}
        val contents=pending.toList()
        active=true
        scope.launch {
            status=try{
                val written=withContext(Dispatchers.IO){V26LearningPacks.export(ctx,uri,contents)}
                "Export completed: "+written+" offline GLB files and manifest."
            }catch(e:Exception){"Could not export pack: "+(e.javaClass.simpleName)}
            active=false
        }
    }
    Text("OFFLINE HERITAGE LEARNING PACKAGES",fontSize=22.sp,fontWeight=FontWeight.Black,color=V17Ink)
    Text("Organize and export selected built-in 3D GLBs and lesson metadata as a ZIP using Android's file picker.",
        color=V17Mute,fontSize=12.sp)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        V17Stat("Bundled GLBs","10",Modifier.weight(1f))
        V17Stat("Selected",selected.size.toString(),Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Column{
        listOf("All","Temple","Pagoda","Gate","Inscription").chunked(3).forEach{line->
            Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                line.forEach{label-> V17Pill(label,category==label){category=label} }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
    Spacer(Modifier.height(7.dp))
    V17GoldenButton("Export "+selected.size+" models as ZIP",Modifier.fillMaxWidth(),enabled=!active && selected.isNotEmpty()){
        pending=selected
        launcher.launch("Bagan_Museum_"+category+"_V2_6.zip")
    }
    Spacer(Modifier.height(7.dp))
    V17NeutralButton("Verify bundled GLB headers",Modifier.fillMaxWidth(),onClick={
        verify=V26LearningPacks.verifyBundled(items,ctx)
    })
    if(verify!=null){
        val (ok,total)=verify!!
        Text("Verified locally: "+ok+" / "+total+" GLB headers",color=V17Gold,fontSize=12.sp)
    }
    Spacer(Modifier.height(8.dp))
    Text(status,color=V17Mute,fontSize=12.sp)
    selected.forEach{item->
        Surface(color=Color.White,shape=RoundedCornerShape(12.dp),
            modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){
            Row(Modifier.padding(12.dp)){
                Text(item.mm,color=V17Ink,modifier=Modifier.weight(1f),fontWeight=FontWeight.Bold)
                Text(".glb",color=V17Gold)
            }
        }
    }
    Text("No network needed to export the built-in models. Exported ZIPs cannot install or update this app.",
        color=V17Mute,fontSize=11.sp)
}
