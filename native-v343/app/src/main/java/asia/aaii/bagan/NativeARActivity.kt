package asia.aaii.bagan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * BAGAN V3.8.0: Lightweight-only native ARCore viewer.
 * NEVER loads high-resolution/downloaded original GLBs into the AR scene.
 * Source must be prepared via ModelRepository.lightweightAr() and validated.
 * Model placement happens on an explicit tap with a real AR hit test.
 */
class NativeARActivity : ComponentActivity() {
    private var cameraGranted by mutableStateOf(false)
    private var arInstalled by mutableStateOf(false)
    private var arMessage by mutableStateOf("Checking Google Play Services for AR…")
    private var askedToInstall = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

        val targetUri = intent.getStringExtra("file_uri")
        val lightweight = intent.getBooleanExtra("lightweight_cgi", false)
        val modelName = intent.getStringExtra("model_name") ?: "Bagan CGI"
        val localModel = runCatching {
            require(lightweight) { "AR accepts only bundled lightweight CGI models" }
            val uri = Uri.parse(requireNotNull(targetUri))
            require(uri.scheme == "file") { "AR needs a locally validated CGI GLB" }
            val file = File(requireNotNull(uri.path)).canonicalFile
            val folder = File(filesDir, "ar_lightweight").canonicalFile
            require(file.parentFile == folder) { "This model is not a prepared lightweight CGI" }
            ModelRepository.validate(file)
            uri.toString()
        }
        val modelError = localModel.exceptionOrNull()?.localizedMessage
        val modelUri = localModel.getOrNull()

        setContent {
            MaterialTheme {
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFF081B2A)).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { finish() }) { Text("‹ Back", color = Color(0xFFE4B359)) }
                        Column(Modifier.weight(1f)) {
                            Text("BAGAN • LIGHTWEIGHT AR", color = Color.White)
                            Text(modelName, color = Color(0xFFF7D99A), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    when {
                        modelUri == null -> ArErrorPanel(
                            "CGI model invalid: " + (modelError ?: "File unavailable"),
                            onBack = { finish() }
                        )
                        !arInstalled -> ArErrorPanel(arMessage, onBack = { finish() },
                            onRetry = { checkArCore() })
                        !cameraGranted -> Column(Modifier.padding(18.dp)) {
                            Text("AR Camera Permission လိုအပ်ပါတယ်။")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Allow Camera")
                            }
                            OutlinedButton(onClick = { finish() }) { Text("Use 3D Viewer") }
                        }
                        else -> LightweightArScene(
                            modelUri = modelUri,
                            onExit = { finish() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkArCore()
    }

    private fun checkArCore() {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(this)
            if (availability.isTransient) {
                arInstalled = false
                arMessage = "ARCore availability စစ်ဆေးနေပါတယ်။ Retry နှိပ်ပါ။"
                return
            }
            if (!availability.isSupported) {
                arInstalled = false
                arMessage = "ARCore မပံ့ပိုးသော ဖုန်းဖြစ်နိုင်ပါတယ်။ ပုံမှန် Native 3D Viewer ကို သုံးနိုင်ပါတယ်။"
                return
            }
            val result = ArCoreApk.getInstance().requestInstall(this, !askedToInstall)
            askedToInstall = true
            arInstalled = result == ArCoreApk.InstallStatus.INSTALLED
            arMessage = if(arInstalled) "" else
                "Google Play Services for AR တပ်ဆင်ပြီး App ထဲ ပြန်ဝင်ပါ။"
        } catch (e: Exception) {
            arInstalled = false
            arMessage = "ARCore initialization failed: " + (e.localizedMessage ?: e.javaClass.simpleName)
        }
    }
}

@Composable
private fun ArErrorPanel(
    message: String,
    onBack: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AR Camera Status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (onRetry != null) Button(onClick = onRetry) { Text("Retry ARCore Check") }
        OutlinedButton(onClick = onBack) { Text("Back to 3D Viewer") }
    }
}

@Composable
private fun LightweightArScene(
    modelUri: String,
    onExit: () -> Unit
) {
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var tracking by remember { mutableStateOf(false) }
    var detectedPlane by remember { mutableStateOf(false) }
    var loadReady by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    val latestFrame = remember { AtomicReference<Frame?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            anchor?.detach()
            latestFrame.set(null)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Native SceneView/Filament + ARCore. All model data stays local and low-poly.
        ARSceneView(
            modifier = Modifier.fillMaxSize().onSizeChanged { viewSize = it },
            onSessionFailed = { e ->
                problem = e.localizedMessage ?: "AR session could not start"
            },
            onSessionUpdated = { session, frame ->
                latestFrame.set(frame)
                val nowTracking = frame.camera.trackingState == TrackingState.TRACKING
                if (nowTracking != tracking) tracking = nowTracking
                val found = nowTracking && session.getAllTrackables(Plane::class.java)
                    .any {
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        it.trackingState == TrackingState.TRACKING &&
                        it.subsumedBy == null
                    }
                if (found != detectedPlane) detectedPlane = found
            }
        ) {
            anchor?.let { a ->
                AnchorNode(anchor = a) {
                    val instance = rememberModelInstance(modelLoader, modelUri)
                    if (instance != null) {
                        ModelNode(
                            modelInstance = instance,
                            scaleToUnits = 0.45f,
                            autoAnimate = false
                        )
                        LaunchedEffect(instance) { loadReady = true }
                    }
                }
            }
        }

        // Center reticle indicates where the manual hit test will attempt placement.
        Text(
            "+",
            modifier = Modifier.align(Alignment.Center),
            color = Color(0xFFE0B056),
            style = MaterialTheme.typography.displaySmall
        )

        Column(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth().background(Color(0xDD091B2A), RoundedCornerShape(topStart=16.dp, topEnd=16.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when {
                    problem != null -> problem!!
                    anchor != null && loadReady -> "CGI placed • Walk around to explore"
                    anchor != null -> "Placing CGI • Loading model…"
                    !tracking -> "Move phone slowly until camera tracking starts"
                    !detectedPlane -> "Point at a flat floor or table"
                    else -> "Surface detected • Tap Place CGI"
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = tracking && detectedPlane && problem == null,
                    onClick = {
                        try {
                            val frame = latestFrame.get()
                                ?: throw IllegalStateException("Camera frame not ready")
                            require(viewSize.width > 0 && viewSize.height > 0) {
                                "AR camera view size not ready"
                            }
                            val x = viewSize.width.toFloat() * 0.5f
                            val y = viewSize.height.toFloat() * 0.5f
                            val hit = frame.hitTest(x, y).firstOrNull { result ->
                                val p = result.trackable as? Plane
                                p != null &&
                                    p.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                    p.trackingState == TrackingState.TRACKING &&
                                    p.isPoseInPolygon(result.hitPose)
                            }
                            if(hit == null) {
                                problem = "No flat surface under crosshair. Aim at floor and retry."
                            } else {
                                val newAnchor = hit.createAnchor()
                                anchor?.detach()
                                anchor = newAnchor
                                loadReady = false
                                problem = null
                            }
                        } catch (e: Exception) {
                            problem = "Placement failed: " +
                                (e.localizedMessage ?: e.javaClass.simpleName)
                        }
                    }
                ) { Text(if(anchor == null) "Place CGI" else "Move CGI") }
                OutlinedButton(
                    onClick = {
                        anchor?.detach()
                        anchor = null
                        loadReady = false
                        problem = null
                    },
                    enabled = anchor != null || problem != null
                ) { Text("Reset") }
                TextButton(onClick = onExit) { Text("Close", color = Color.White) }
            }
            Text(
                "OFFLINE STYLIZED CGI • 13 lightweight models • Not a historical 3D scan",
                color = Color(0xFFF5D99C),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
