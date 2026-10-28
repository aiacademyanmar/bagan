package asia.aaii.bagan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.ArCoreApk

class NativeARActivity:ComponentActivity() {
 private var permitted by mutableStateOf(false)
 private var arReady by mutableStateOf(false)
 private var installMessage by mutableStateOf<String?>(null)
 private var installAsked = false
 override fun onResume(){
  super.onResume()
  try {
   val result=ArCoreApk.getInstance().requestInstall(this,!installAsked)
   installAsked=true
   arReady=result==ArCoreApk.InstallStatus.INSTALLED
   installMessage=if(arReady)null else "Google Play Services for AR installation requested. Complete installation and return."
  }catch(e:Exception){
   arReady=false
   installMessage="Unable to initialize ARCore: "+(e.localizedMessage ?: e.javaClass.simpleName)
  }
 }
 private val ask = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permitted=it }
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  permitted=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
  val modelUri=intent.getStringExtra("file_uri")
  val modelName=intent.getStringExtra("model_name") ?: "Bagan Temple"
  val demoMode=intent.getBooleanExtra("demo_mode",false)
  setContent {
   MaterialTheme {
    Column {
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
      TextButton(onClick={finish()}){Text("← Back")}
      Text(modelName,modifier=Modifier.padding(12.dp))
     }
     if(demoMode) Text("DEMO MODEL — not the original Bagan monument",modifier=Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.error)
     if(!arReady){
      Text(installMessage ?: "Checking Google Play Services for AR…",Modifier.padding(16.dp))
      TextButton(onClick={finish()}){Text("Return to 3D Museum")}
     }else if(!permitted){
      Text("AR အတွက် Camera Permission ခွင့်ပြုပါ",Modifier.padding(16.dp))
      Button(onClick={ask.launch(Manifest.permission.CAMERA)}){Text("Allow Camera")}
     }else if(modelUri==null){
      Text("AR Model file missing",Modifier.padding(16.dp))
     }else{
      var anchor by remember { mutableStateOf<Anchor?>(null) }
      var issue by remember { mutableStateOf<String?>(null) }
      Box(Modifier.fillMaxSize()) {
       ARSceneView(
        modifier=Modifier.fillMaxSize(),
        onSessionFailed={error->issue=error.localizedMessage ?: "ARCore session failed"},
        onSessionUpdated={_,frame->
          if(anchor==null) {
           anchor=frame.getUpdatedTrackables(Plane::class.java)
             .firstOrNull {it.type==Plane.Type.HORIZONTAL_UPWARD_FACING}
             ?.let { plane -> plane.createAnchor(plane.centerPose) }
          }
        }
       ) {
        anchor?.let {a->
         AnchorNode(anchor=a) {
          rememberModelInstance(modelLoader,modelUri)?.let {instance->
           ModelNode(modelInstance=instance,scaleToUnits=0.45f)
          }
         }
        }
       }
       if(issue!=null){
        AlertDialog(onDismissRequest={issue=null},title={Text("AR Unavailable")},
          text={Text(issue!!+"\nGoogle Play Services for AR ကိုစစ်ပါ။")},
          confirmButton={TextButton(onClick={finish()}){Text("Back")}})
       }
      }
     }
    }
   }
  }
 }
}
