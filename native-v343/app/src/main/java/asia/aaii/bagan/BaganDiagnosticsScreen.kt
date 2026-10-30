package asia.aaii.bagan

import android.Manifest
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * On-device diagnostics. Reports hardware/permission/cache & HTTP HEAD status.
 * No API key, password, location, device ID or other secrets are collected.
 * "HTTP 200" only validates URL accessibility; downloading/decoding is a separate test.
 */
@Composable
fun BaganDiagnosticsScreen(temples: List<Temple>, onBack: () -> Unit) {
    val context= LocalContext.current
    var running by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf("Run checks to inspect phone support, cached GLB files and cPanel model URLs.") }
    var serial by remember { mutableIntStateOf(0) }
    LaunchedEffect(serial) {
        if(serial>0) {
            running=true
            report=withContext(Dispatchers.IO) { diagnosticReport(context,temples) }
            running=false
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF081B2A))) {
        Row(Modifier.fillMaxWidth().background(Color(0xFF0B2031)).padding(12.dp),
            verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick=onBack) { Text("‹ Back", color=Color(0xFFE5B656)) }
            Column(Modifier.weight(1f)) {
                Text("3D / AR Diagnostics",color=Color.White,fontWeight=FontWeight.Bold)
                Text("V3.8.4 • phone & cPanel checks",color=Color.LightGray)
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Check why 3D or AR is not working",color=Color(0xFFFFD476),fontWeight=FontWeight.Bold)
            Text("This report helps identify missing GLB files, invalid cache, ARCore support and camera permission. It does not prove on-device AR tracking.",color=Color(0xFFCDDAE5))
            Button(onClick={serial++},enabled=!running,modifier=Modifier.fillMaxWidth(),
                colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFD5A143))) {
                Text(if(running)"Checking device & cPanel…" else "Run Diagnostics",color=Color(0xFF102B3D))
            }
            if(running) LinearProgressIndicator(Modifier.fillMaxWidth())
            OutlinedButton(onClick={
                val clip=context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("Bagan Diagnostics",report))
                Toast.makeText(context,"Report copied",Toast.LENGTH_SHORT).show()
            },modifier=Modifier.fillMaxWidth(),enabled=!running){Text("Copy Diagnostic Report")}
            Surface(color=Color(0xFF142D40),shape=RoundedCornerShape(12.dp),shadowElevation=2.dp){
                Text(report,modifier=Modifier.fillMaxWidth().padding(16.dp),
                    color=Color.White,style=MaterialTheme.typography.bodySmall)
            }
            Text("GLB Server Path: https://aaii.asia/bagan/3d/models/",color=Color.DarkGray)
            Text("Reminder: a valid demo model is not a substitute for the actual temple GLB.",color=Color.DarkGray)
        }
    }
}
private fun diagnosticReport(context: Context, temples: List<Temple>):String {
    val lines=mutableListOf<String>()
    lines+="BAGAN Heritage Museum • V3.8.4"
    lines+="Checked: "+SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(Date())
    lines+="Android: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")"
    lines+="Device: "+Build.MANUFACTURER+" "+Build.MODEL
    val cam=context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    val permission=androidx.core.content.ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
    lines+="Camera hardware: "+if(cam)"YES" else "NO"
    lines+="Camera permission: "+if(permission)"GRANTED" else "NOT GRANTED (requested on AR screen)"
    val gl=(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.glEsVersion
    lines+="OpenGL ES reported: "+gl
    try {
        val available=ArCoreApk.getInstance().checkAvailability(context)
        lines+="ARCore: "+available.toString()+" (supported="+available.isSupported+")"
        if(!available.isSupported)lines+="AR: install/update Google Play Services for AR or use 3D mode."
    }catch(e:Exception){lines+="ARCore availability error: "+e.javaClass.simpleName+" "+e.message}
    try {
        val file=ModelRepository.demo(context)
        lines+="Offline demo GLB: VALID ("+file.length()+" bytes)"
    }catch(e:Exception){lines+="Offline demo GLB: INVALID - "+e.localizedMessage}
    lines+="----- OFFLINE LIGHTWEIGHT AR CGI -----"
    var lightCount=0
    temples.forEach { t ->
        try { val file=ModelRepository.lightweightAr(context,t);lightCount++;lines+=t.nameEn+": CGI VALID ("+file.length()+" bytes)" }
        catch(e:Exception){lines+=t.nameEn+": CGI ERROR: "+(e.localizedMessage ?: e.javaClass.simpleName)}
    }
    lines+="Lightweight CGI ready: $lightCount / "+temples.size
    var cached=0
    lines+="----- CACHED 3D MODELS -----"
    temples.forEach { t ->
        val file=ModelRepository.cached(context,t)
        if(file.exists()){
            try {ModelRepository.validate(file);cached++;lines+=t.fileName+": VALID ("+file.length()+" bytes)"}
            catch(e:Exception){lines+=t.fileName+": INVALID - "+e.message}
        } else lines+=t.fileName+": NOT DOWNLOADED"
    }
    lines+="Valid cached temple GLBs: $cached / "+temples.size
    lines+="----- cPANEL HTTPS TESTS -----"
    lines+="URL checks use HTTP HEAD; status 200 does not guarantee a renderable GLB."
    val executor=Executors.newFixedThreadPool(4)
    try {
        val calls=temples.map { t->Callable {
            var connection:HttpURLConnection?=null
            try {
                connection=(URL(t.url).openConnection() as HttpURLConnection).apply {
                    requestMethod="HEAD"
                    connectTimeout=4500
                    readTimeout=4500
                    instanceFollowRedirects=true
                }
                val status=connection.responseCode
                t.fileName+": HTTP "+status+
                    (if(status==200)" (accessible; validate GLB after download)"
                     else if(status==405)" (HEAD unsupported; try 3D download)"
                     else if(status==404)" (missing from cPanel folder)"
                     else if(status==403)" (permission denied)"
                     else " (server response)")
            }catch(e:Exception){t.fileName+": "+e.javaClass.simpleName+" "+(e.message ?: "connection failed")}
            finally {connection?.disconnect()}
        }}
        val futures=executor.invokeAll(calls,18000,TimeUnit.MILLISECONDS)
        futures.forEachIndexed { ix,f ->
            lines+=if(f.isCancelled)temples[ix].fileName+": TIMEOUT" else try{f.get()}catch(e:Exception){temples[ix].fileName+": CHECK ERROR"}
        }
    }finally {executor.shutdownNow()}
    lines+="----- TROUBLESHOOT -----"
    lines+="HTTP 404/403: upload model with exact filename and correct permissions."
    lines+="GLB invalid: uploaded file must begin with glTF and match GLB 2.0 byte length."
    lines+="AR unavailable: requires supported phone, camera permission and ARCore services."
    lines+="UI builds are not a substitute for physical-device GLB/AR testing."
    return lines.joinToString("\n")
}
