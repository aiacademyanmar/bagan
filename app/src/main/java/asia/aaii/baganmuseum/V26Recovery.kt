package asia.aaii.baganmuseum

import android.content.Context
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

/** A crash cannot safely be resumed in-process. After a subsequent launch,
 * offer a restricted recovery mode, avoiding a restart loop or auto-upload.
 * The existing V2.5 private crash report remains available to the user.
 */
internal object V26Recovery {
    private const val PREF="bagan_recovery_v26"
    fun isSafe(ctx:Context)=ctx.getSharedPreferences(PREF,0).getBoolean("safe",false)
    fun setSafe(ctx:Context,safe:Boolean){
        ctx.getSharedPreferences(PREF,0).edit().putBoolean("safe",safe).apply()
    }
    fun newCrashReport(ctx:Context):String?{
        val log=V25LocalDiagnostics.read(ctx)?:return null
        if(!log.startsWith("Bagan Museum V2.5 • Local crash summary"))return null
        val seen=ctx.getSharedPreferences(PREF,0).getInt("ack",0)
        return log.takeIf{it.hashCode()!=seen}
    }
    fun acknowledge(ctx:Context,log:String){
        ctx.getSharedPreferences(PREF,0).edit().putInt("ack",log.hashCode()).apply()
    }
}

@Composable
internal fun V26RecoveryGate(content:@Composable ()->Unit){
    val ctx=LocalContext.current
    var report by remember{mutableStateOf(V26Recovery.newCrashReport(ctx))}
    if(report==null){
        content()
    }else{
        Column(Modifier.fillMaxSize().padding(horizontal=22.dp,vertical=45.dp),
            verticalArrangement=Arrangement.Center){
            Text("✧ BAGAN MUSEUM",fontSize=23.sp,fontWeight=FontWeight.Black,color=V17Gold)
            Spacer(Modifier.height(18.dp))
            Text("RECOVERY OPTIONS",fontSize=24.sp,fontWeight=FontWeight.Black,color=V17Ink)
            Spacer(Modifier.height(13.dp))
            Text("A previous launch ended unexpectedly. You can use Safe Mode to avoid opening the 3D/AR features while checking this device.",
                color=V17Ink,fontSize=14.sp,lineHeight=23.sp)
            Spacer(Modifier.height(12.dp))
            V17GoldenButton("Open in Safe Mode",Modifier.fillMaxWidth()){
                val log=report
                if(log!=null)V26Recovery.acknowledge(ctx,log)
                V26Recovery.setSafe(ctx,true)
                report=null
            }
            Spacer(Modifier.height(9.dp))
            V17NeutralButton("Continue Normally",Modifier.fillMaxWidth()){
                val log=report
                if(log!=null)V26Recovery.acknowledge(ctx,log)
                V26Recovery.setSafe(ctx,false)
                report=null
            }
            Spacer(Modifier.height(13.dp))
            Text("Diagnostic summary remains in app-private storage. Nothing is uploaded automatically.",
                fontSize=11.sp,color=V17Mute)
        }
    }
}
@Composable
internal fun V26SafeModeNotice(onDisable:()->Unit){
    Text("SAFE MODE",fontSize=22.sp,color=V17Gold,fontWeight=FontWeight.Black)
    Spacer(Modifier.height(9.dp))
    Text("3D WebGL and AR camera are temporarily disabled. Home, lessons, quizzes, maps and offline text can still be used.",
        color=V17Ink,fontSize=13.sp,lineHeight=22.sp)
    Spacer(Modifier.height(12.dp))
    V17GoldenButton("Enable 3D & AR",Modifier.fillMaxWidth(),onClick=onDisable)
    Spacer(Modifier.height(7.dp))
    Text("Only re-enable after inspecting Crash Diagnostics if the app repeatedly crashes.",fontSize=11.sp,color=V17Mute)
}
@Composable
internal fun V26RecoverySettings(){
    val ctx=LocalContext.current
    var safe by remember{mutableStateOf(V26Recovery.isSafe(ctx))}
    Text("RECOVERY & STABILITY",fontSize=22.sp,fontWeight=FontWeight.Black,color=V17Ink)
    Text("Safe Mode suppresses 3D/AR launch. This is a recovery option, not a guarantee that every crash can be prevented.",color=V17Mute,fontSize=12.sp)
    Row(Modifier.fillMaxWidth().padding(vertical=14.dp)){
        Text("Safe Mode",modifier=Modifier.weight(1f),color=V17Ink,fontWeight=FontWeight.Bold)
        Switch(checked=safe,onCheckedChange={safe=it;V26Recovery.setSafe(ctx,it)})
    }
    Spacer(Modifier.height(8.dp))
    V25DiagnosticsScreen()
}
