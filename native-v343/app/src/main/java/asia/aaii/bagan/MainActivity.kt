package asia.aaii.bagan

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ar.core.ArCoreApk
import java.io.File
import java.util.Locale

/**
 * Recovery-first Android entrypoint for Samsung and other Android devices.
 * Heavy 3D/AR code is entered only on user request, never during startup.
 * Fatal crash details are saved privately on-device for the next launch.
 */
class MainActivity : ComponentActivity() {
    private var speech: TextToSpeech? = null
    private var speechReady = false
    private val crashPrefName = "bagan_crash_report_v367"

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogger()
        super.onCreate(savedInstanceState)

        val crashStore = getSharedPreferences(crashPrefName, Context.MODE_PRIVATE)
        val previousCrash = crashStore.getString("last_error", null)
        val dataResult = runCatching { ModelRepository.catalog(this) }
        val catalogError = dataResult.exceptionOrNull()?.let {
            "Catalog load error: ${it.javaClass.simpleName}: ${it.message}"
        }
        val temples = dataResult.getOrElse { emptyList() }

        setContent {
            MaterialTheme {
                var showRecovery by rememberSaveable { mutableStateOf(previousCrash != null || catalogError != null || temples.isEmpty()) }
                var safeMode by rememberSaveable { mutableStateOf(false) }
                val message = catalogError ?: previousCrash ?: "No temple records were found."
                when {
                    safeMode -> BaganSafeMode(temples, onMap = ::launchMap, onExitSafe = { safeMode = false; showRecovery = false })
                    showRecovery -> BaganRecoveryScreen(
                        message = message,
                        onRetry = {
                            crashStore.edit().remove("last_error").apply()
                            showRecovery = false
                        },
                        onSafeMode = { safeMode = true }
                    )
                    temples.isEmpty() -> BaganRecoveryScreen(
                        message = "Catalog is empty. Please reinstall the APK.",
                        onRetry = { showRecovery = true },
                        onSafeMode = { safeMode = true }
                    )
                    else -> BaganEntryFlow(temples, ::launchMap, ::launchAr, ::speakGuide)
                }
            }
        }
    }

    private fun installCrashLogger() {
        val earlier = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val stack = Log.getStackTraceString(error).take(10000)
                getSharedPreferences(crashPrefName, Context.MODE_PRIVATE)
                    .edit().putString("last_error", stack).commit()
                Log.e("BaganNative", "Uncaught error: ${error.javaClass.simpleName}", error)
            } catch (_: Throwable) {
                // Always hand control back to Android's crash handler.
            }
            if (earlier != null) earlier.uncaughtException(thread, error)
            else android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun speakGuide(text: String) {
        try {
            if (speech == null) {
                speech = TextToSpeech(this) { status ->
                    speechReady = status == TextToSpeech.SUCCESS
                    if (speechReady) speech?.language = Locale.US
                }
                Toast.makeText(this, "အသံဖတ်စနစ်ကို စတင်နေပါသည်။ ထပ်နှိပ်ပါ။", Toast.LENGTH_SHORT).show()
            } else if (speechReady) {
                speech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bagan-guide")
            } else {
                Toast.makeText(this, "Text-to-Speech မရနိုင်သေးပါ။", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Audio မရနိုင်ပါ: ${e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        runCatching {
            speech?.stop()
            speech?.shutdown()
            speech = null
        }
        super.onDestroy()
    }

    private fun launchMap(t: Temple) {
        val url = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode(t.nameEn + " Bagan Myanmar")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "မြေပုံကို ယခုဖွင့်မရနိုင်ပါ။", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchAr(t: Temple) {
        val available = try { ArCoreApk.getInstance().checkAvailability(this) }
            catch (e: Exception) { null }
        if (available?.isSupported != true) {
            AlertDialog.Builder(this)
                .setTitle("AR Camera")
                .setMessage("ဤ Samsung ဖုန်းတွင် ARCore မရနိုင်သေးပါ။ 3D Viewer ကို အသုံးပြုနိုင်ပါသည်။")
                .setPositiveButton("OK", null).show()
            return
        }
        val cached = ModelRepository.cached(this, t)
        val actual = try { ModelRepository.validate(cached); true } catch (_: Exception) { false }
        val source = if (actual) cached else try { ModelRepository.demo(this) } catch (e: Exception) {
            AlertDialog.Builder(this).setTitle("3D File Error")
                .setMessage(e.localizedMessage ?: "GLB file unavailable")
                .setPositiveButton("OK", null).show()
            return
        }
        val open = {
            try {
                startActivity(Intent(this, NativeARActivity::class.java)
                    .putExtra("file_uri", Uri.fromFile(source).toString())
                    .putExtra("model_name", if (actual) t.nameMm else "Bagan Demo Pagoda")
                    .putExtra("demo_mode", !actual))
            } catch (e: Exception) {
                AlertDialog.Builder(this).setTitle("AR Camera Error")
                    .setMessage(e.localizedMessage ?: "AR Camera unavailable")
                    .setPositiveButton("OK", null).show()
            }
        }
        if (actual) open() else AlertDialog.Builder(this)
            .setTitle("Demo 3D")
            .setMessage("မူရင်း GLB ဖိုင်မရှိသေးပါ။ နမူနာပုံစံဖြင့် AR ကြည့်မလား။")
            .setPositiveButton("ဖွင့်ရန်") { _, _ -> open() }
            .setNegativeButton("မဖွင့်ပါ", null).show()
    }
}

@Composable
private fun BaganRecoveryScreen(message: String, onRetry: () -> Unit, onSafeMode: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("BAGAN HERITAGE MUSEUM", style = MaterialTheme.typography.headlineSmall) }
        item { Text("App Recovery / အက်ပ်ပြန်လည်စတင်ရန်", style = MaterialTheme.typography.titleMedium) }
        item { Text("ယခင်ဖွင့်ရာတွင် အက်ပ်အမှားတစ်ခု တွေ့ရှိခဲ့ပါသည်။ Error စာသားကို Screenshot ရိုက်၍ ပေးပို့နိုင်ပါသည်။") }
        item {
            SelectionContainer {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(message.take(3000), Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Button(onClick = onSafeMode, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Safe Mode ဖြင့် ဖွင့်ရန်") } }
        item { OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("ပုံမှန် ပြန်ဖွင့်ရန်") } }
    }
}

@Composable
private fun BaganSafeMode(temples: List<Temple>, onMap: (Temple) -> Unit, onExitSafe: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item { Text("BAGAN — Safe Mode", style = MaterialTheme.typography.headlineSmall) }
        item { Text("3D နှင့် AR ကို ယာယီပိတ်ထားပြီး ဘုရားစာရင်းကို ကြည့်ရှုနိုင်ပါသည်။") }
        item { OutlinedButton(onClick = onExitSafe) { Text("ပုံမှန် Home သို့ပြန်ရန်") } }
        items(temples) { site ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(site.nameMm, style = MaterialTheme.typography.titleMedium)
                    Text(site.nameEn)
                    TextButton(onClick = { onMap(site) }) { Text("မြေပုံတွင်ကြည့်ရန်") }
                }
            }
        }
    }
}
