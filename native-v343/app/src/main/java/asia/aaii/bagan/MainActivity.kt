package asia.aaii.bagan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val night = Color(0xFF11172B)
private val gold = Color(0xFFEDBD6B)
private val panel = Color(0xFF212B45)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val items = ModelRepository.catalog(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = gold, background = night, surface = panel)) {
                Museum(items) { item -> openAr(item) }
            }
        }
    }
    private fun openAr(item: Temple) {
        val url = "https://arvr.google.com/scene-viewer/1.0?file=" +
            Uri.encode(item.url) + "&mode=ar_preferred"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (_: Exception) {
            android.widget.Toast.makeText(this, "AR viewer unavailable", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun Museum(temples: List<Temple>, onAr: (Temple) -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<Temple?>(null) }
    var fileUri by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var percentage by remember { mutableIntStateOf(0) }
    var retry by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(selected, retry) {
        val item = selected ?: return@LaunchedEffect
        fileUri = null
        error = null
        percentage = 0
        loading = true
        try {
            val file = withContext(Dispatchers.IO) {
                ModelRepository.download(context, item) { percentage = it }
            }
            fileUri = Uri.fromFile(file).toString()
        } catch (e: Exception) {
            error = e.localizedMessage ?: "3D file download failed"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(night)) {
        Row(Modifier.fillMaxWidth().background(panel).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏛", fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("BAGAN 3D MUSEUM", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = gold)
                Text("Native Android • V3.4.3", color = Color.LightGray, fontSize = 11.sp)
            }
        }
        if (selected == null) {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text("ဘုရားအမည် ရှာရန် / Search") },
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(temples.filter {
                    query.isBlank() || it.nameMm.contains(query, ignoreCase = true) ||
                        it.nameEn.contains(query, ignoreCase = true)
                }) { item ->
                    Column(
                        Modifier.fillMaxWidth().background(panel, RoundedCornerShape(14.dp))
                            .clickable { selected = item }.padding(16.dp)
                    ) {
                        Text(item.nameMm, color = gold, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(item.nameEn, color = Color.White, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("3D ကြည့်ရန် →", color = gold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            val item = selected!!
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { selected = null }) { Text("← Back") }
                Spacer(Modifier.weight(1f))
                Text(item.nameMm, color = gold, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF0C1321))) {
                if (fileUri != null && !loading) {
                    key(fileUri) {
                        SceneView(modifier = Modifier.fillMaxSize()) {
                            rememberModelInstance(modelLoader, fileUri!!)?.let { model ->
                                ModelNode(modelInstance = model, scaleToUnits = 1.25f, autoAnimate = true)
                            }
                        }
                    }
                }
                if (loading) {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(progress = { percentage / 100f }, color = gold)
                        Spacer(Modifier.height(12.dp))
                        Text("3D Model Download…", color = Color.White)
                        Text("$percentage%", color = gold, fontSize = 22.sp)
                    }
                }
                if (error != null) {
                    Column(Modifier.align(Alignment.Center).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error!!, color = Color.White)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { retry++ }) { Text("Retry") }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { retry++ }, modifier = Modifier.weight(1f)) { Text("Refresh") }
                Button(onClick = { onAr(item) }, modifier = Modifier.weight(1f)) { Text("AR") }
            }
            Text("Drag to rotate • Pinch to zoom • Offline cache", color = Color.LightGray,
                fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp))
        }
    }
}
