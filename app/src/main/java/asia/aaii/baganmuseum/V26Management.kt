package asia.aaii.baganmuseum

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.ApplicationInfo
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Only anonymous recent statistics, persisted locally and refreshed after a
 * 3D viewer session. FPS is a sample, not a benchmark or guaranteed performance.
 */
internal object V26ViewerStatsStore {
    private const val KEY="bagan_3d_perf_v26"
    fun save(ctx:Context,data:JSONObject){
        val report=JSONObject()
            .put("model",data.optString("model").take(80))
            .put("fps",data.optDouble("fps",0.0).coerceIn(0.0,240.0))
            .put("triangles",data.optInt("triangles",0).coerceAtLeast(0))
            .put("loadMs",data.optDouble("loadMs",0.0).coerceAtLeast(0.0))
            .put("webgl",data.optBoolean("webgl",false))
            .put("time",System.currentTimeMillis())
        ctx.getSharedPreferences(KEY,0).edit().putString("snapshot",report.toString()).apply()
    }
    fun read(ctx:Context):JSONObject?{
        val json=ctx.getSharedPreferences(KEY,0).getString("snapshot",null) ?:return null
        return try{JSONObject(json)}catch(_:Exception){null}
    }
    fun clear(ctx:Context){
        ctx.getSharedPreferences(KEY,0).edit().remove("snapshot").apply()
    }
}

@Composable
internal fun V26PerformanceDashboard(){
    val ctx=LocalContext.current
    var stats by remember{mutableStateOf(V26ViewerStatsStore.read(ctx))}
    Text("3D GALLERY PERFORMANCE",color=V17Ink,fontSize=22.sp,fontWeight=FontWeight.Black)
    Text("Live WebGL render samples are recorded locally while the offline viewer is open. No analytics are sent to a server.",
        color=V17Mute,fontSize=12.sp)
    Spacer(Modifier.height(9.dp))
    V17NeutralButton("Refresh latest 3D sample",Modifier.fillMaxWidth()){
        stats=V26ViewerStatsStore.read(ctx)
    }
    Spacer(Modifier.height(10.dp))
    if(stats==null){
        Text("No 3D performance sample yet. Open the 3D Gallery, rotate a model for a few seconds, then return here.",
            color=V17Mute,fontSize=13.sp)
    } else {
        val data=stats!!
        V17Stat("Estimated FPS","%.1f".format(java.util.Locale.US,data.optDouble("fps")),Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            V17Stat("Triangles",data.optInt("triangles").toString(),Modifier.weight(1f))
            V17Stat("Load (ms)","%.0f".format(java.util.Locale.US,data.optDouble("loadMs")),Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Text("Model: "+data.optString("model","Unknown"),color=V17Ink,fontSize=14.sp,fontWeight=FontWeight.Bold)
        Text("WebGL: "+(if(data.optBoolean("webgl"))"Available" else "Unavailable"),
            color=V17Mute,fontSize=12.sp)
        Spacer(Modifier.height(9.dp))
        V17NeutralButton("Clear local sample",Modifier.fillMaxWidth()){
            V26ViewerStatsStore.clear(ctx);stats=null
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("FPS readings may change with temperature, phone battery saver, app visibility and device GPU. This is not a formal Samsung performance certification.",
        color=V17Mute,fontSize=11.sp)
}

private data class V26Release(val version:String,val page:String)
private suspend fun fetchRelease():V26Release?=withContext(Dispatchers.IO){
    var connection:HttpURLConnection?=null
    try {
        connection=URL("https://api.github.com/repos/aiacademyanmar/bagan/releases/latest").openConnection() as HttpURLConnection
        connection.connectTimeout=5000;connection.readTimeout=6500
        connection.setRequestProperty("Accept","application/vnd.github+json")
        connection.setRequestProperty("User-Agent","BaganMuseumAndroid/2.6")
        if(connection.responseCode!=200)return@withContext null
        val payload=JSONObject(connection.inputStream.bufferedReader().use{it.readText()})
        val tag=payload.optString("tag_name").take(64)
        val url=payload.optString("html_url")
        if(tag.isEmpty()||!url.startsWith("https://github.com/aiacademyanmar/bagan/releases/"))null
        else V26Release(tag,url)
    }catch(_:Exception){null}
    finally{connection?.disconnect()}
}
@Composable
internal fun V26UpdateManager(){
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    val version=remember{
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(ctx.packageName,0).versionName ?: "Unknown"
    }
    val debug=remember{ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE!=0}
    var release by remember{mutableStateOf<V26Release?>(null)}
    var status by remember{mutableStateOf("No release check performed")}
    var checking by remember{mutableStateOf(false)}
    fun openUrl(url:String){
        try{ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}
    }
    Text("APP INSTALL / UPDATE CENTER",fontSize=22.sp,fontWeight=FontWeight.Black,color=V17Ink)
    Spacer(Modifier.height(9.dp))
    Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
        Column(Modifier.padding(15.dp)){
            Text("BAGAN MUSEUM",color=V17Gold,fontWeight=FontWeight.Black,fontSize=19.sp)
            Text("Installed version: "+version,color=V17Ink)
            Text("Build: "+(if(debug)"Development / Debug" else "Release"),
                color=V17Mute)
            Text("Android package: "+ctx.packageName,fontSize=11.sp,color=V17Mute)
        }
    }
    Spacer(Modifier.height(11.dp))
    V17GoldenButton("Check published GitHub release",Modifier.fillMaxWidth(),enabled=!checking){
        checking=true
        scope.launch {
            val found=fetchRelease()
            release=found
            status=if(found==null)"No public GitHub release was found, or network is unavailable."
              else "Latest published release tag: "+found.version
            checking=false
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(status,color=V17Mute,fontSize=12.sp)
    if(release!=null) {
        V17NeutralButton("Open published release page",Modifier.fillMaxWidth()){
            openUrl(release!!.page)
        }
    }
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("Open project's GitHub Releases",Modifier.fillMaxWidth()){
        openUrl("https://github.com/aiacademyanmar/bagan/releases")
    }
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("Open GitHub Actions APK builds",Modifier.fillMaxWidth()){
        openUrl("https://github.com/aiacademyanmar/bagan/actions")
    }
    Spacer(Modifier.height(12.dp))
    Text("Updates are not installed automatically. Android checks signing certificates when installing a new APK. A Debug APK cannot overwrite one signed with a different key without reinstalling; always back up important local data before reinstalling.",
        color=V17Mute,fontSize=11.sp,lineHeight=19.sp)
}
