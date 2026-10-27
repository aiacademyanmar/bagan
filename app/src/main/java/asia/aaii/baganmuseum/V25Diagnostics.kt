package asia.aaii.baganmuseum

import android.content.Context
import android.content.Intent
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
import java.io.File
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

/**
 * V2.5 crash records remain ONLY in app-private files; there is no background
 * transmission or telemetry endpoint. Does not capture error messages, file
 * paths, personal identifiers, network requests, or user contents.
 */
internal object V25LocalDiagnostics {
    private val installed=AtomicBoolean(false)
    private const val FILE="crash_summary_v25.txt"
    private fun file(context:Context)=File(context.filesDir,FILE)
    fun install(context:Context){
        if(!installed.compareAndSet(false,true))return
        val app=context.applicationContext
        val original=Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val summary=buildString{
                    appendLine("Bagan Museum V2.5 • Local crash summary")
                    appendLine("UTC date: "+java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",java.util.Locale.US).apply{
                        timeZone=java.util.TimeZone.getTimeZone("UTC")
                    }.format(Date()))
                    appendLine("Exception: "+throwable.javaClass.simpleName.take(80))
                    appendLine("Stack entrypoints:")
                    throwable.stackTrace.take(7).forEach { frame ->
                        appendLine(frame.className.take(110)+"."+frame.methodName.take(70))
                    }
                    appendLine("No crash message, names or user content recorded.")
                }.take(3000)
                file(app).writeText(summary)
            }catch(_:Throwable){}
            if(original!=null)original.uncaughtException(thread,throwable)
            else android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
    fun read(context:Context):String? = try {
        file(context).takeIf{it.exists()}?.readText()
    }catch(_:Exception){null}
    fun clear(context:Context):Boolean = try{file(context).delete()}catch(_:Exception){false}
    fun logViewerError(context:Context,code:Int){
        try {
            file(context).writeText("Bagan Museum V2.5 • 3D Viewer load error\n"+
                "Date: "+Date().toString()+"\nError code: "+code+"\n"+
                "No URL, user content or device identifier recorded.")
        }catch(_:Exception){}
    }
}
@Composable
internal fun V25DiagnosticsScreen(){
    val context=LocalContext.current
    var report by remember{mutableStateOf(V25LocalDiagnostics.read(context))}
    Text("SAMSUNG / ANDROID CRASH DIAGNOSTICS",fontSize=20.sp,fontWeight=FontWeight.Black,color=V17Ink)
    Spacer(Modifier.height(8.dp))
    Text("Crash summaries are kept locally on this phone only. Nothing is automatically uploaded or shared.",
        fontSize=12.sp,color=V17Mute)
    Spacer(Modifier.height(12.dp))
    Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()){
        Column(Modifier.padding(15.dp)){
            Text(if(report==null)"No crash record" else "Last recorded diagnostic",fontWeight=FontWeight.Bold,color=V17Ink)
            Spacer(Modifier.height(7.dp))
            Text(report?:"No previous uncaught exception or WebView load error has been recorded by this version.",
                fontSize=12.sp,color=V17Mute,lineHeight=19.sp)
        }
    }
    Spacer(Modifier.height(12.dp))
    V17NeutralButton("Refresh local diagnostics",Modifier.fillMaxWidth()){
        report=V25LocalDiagnostics.read(context)
    }
    Spacer(Modifier.height(8.dp))
    V17GoldenButton("Share diagnostic text (manual)",Modifier.fillMaxWidth(),enabled=report!=null){
        val content=report?:return@V17GoldenButton
        val intent=Intent(Intent.ACTION_SEND).apply{
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT,content)
        }
        context.startActivity(Intent.createChooser(intent,"Share Bagan crash report"))
    }
    Spacer(Modifier.height(8.dp))
    V17NeutralButton("Delete local diagnostic",Modifier.fillMaxWidth()){
        V25LocalDiagnostics.clear(context)
        report=V25LocalDiagnostics.read(context)
    }
    Text("This minimal log cannot replace Logcat or testing on a physical Samsung device.",
        color=V17Mute,fontSize=11.sp)
}
