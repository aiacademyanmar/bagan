package asia.aaii.bagan

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray

/**
 * Plain Android launcher (no Compose, ARCore, SceneView, GLB, or TTS dependencies).
 * If the museum fails before its first frame, this screen remains a known-good
 * diagnostic entrypoint on the next launch.
 */
class BootstrapActivity : Activity() {
    private val reportStore by lazy {
        getSharedPreferences("bagan_crash_report_v367", Context.MODE_PRIVATE)
    }
    private lateinit var reportView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        installEarlyCrashLogger()
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(18, 37, 53)
        window.navigationBarColor = Color.rgb(18, 37, 53)

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(24))
            setBackgroundColor(Color.rgb(255, 247, 232))
        }
        scroll.addView(column)
        addText(column, "BAGAN HERITAGE MUSEUM", 24, true)
        addText(column, "ပုဂံအမွေအနှစ်ပြတိုက်", 20, true)
        addText(column, "Startup Diagnostic • V3.6.8", 14, false)
        addText(column, "Samsung ဖုန်းတွင် အက်ပ်ပြန်ပိတ်ပါက ဤစာမျက်နှာမှ Crash Report ကို ကူးယူပေးပို့နိုင်ပါသည်။", 15, false)

        addButton(column, "OPEN MUSEUM / အက်ပ်ဖွင့်ရန်") {
            reportStore.edit().putBoolean("attempted_launch", true).apply()
            try {
                startActivity(Intent().setClassName(packageName, "asia.aaii.bagan.MainActivity"))
            } catch (e: Throwable) {
                saveError("MainActivity launch failed:\n" + Log.getStackTraceString(e))
                refreshReport()
            }
        }
        addButton(column, "SAFE MODE / အခြေခံစာရင်း") {
            startActivity(Intent(this, SafeModeActivity::class.java))
        }
        addButton(column, "COPY CRASH REPORT / Error ကူးရန်") {
            val report = reportStore.getString("last_error", "") ?: ""
            if (report.isBlank()) {
                AlertDialog.Builder(this).setMessage("Crash Report မရှိသေးပါ။").setPositiveButton("OK", null).show()
            } else {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Bagan crash report", report))
                AlertDialog.Builder(this).setMessage("Error ကို Copy လုပ်ပြီးပါပြီ။ ChatGPT ထဲ Paste လုပ်ပေးပါ။")
                    .setPositiveButton("OK", null).show()
            }
        }
        addButton(column, "SHARE ERROR / Error မျှဝေရန်") {
            val report = reportStore.getString("last_error", "") ?: ""
            if (report.isNotBlank()) {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, report)
                }, "Share Bagan crash report"))
            }
        }
        reportView = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.rgb(74, 45, 24))
            setPadding(dp(10), dp(16), dp(10), dp(18))
            setTextIsSelectable(true)
        }
        column.addView(reportView, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(scroll)
        refreshReport()
    }

    override fun onResume() {
        super.onResume()
        if (::reportView.isInitialized) refreshReport()
    }

    private fun refreshReport() {
        val message = reportStore.getString("last_error", "") ?: ""
        reportView.text = if (message.isBlank())
            "Startup Status: Ready.\nNo fatal error has been captured on this installation."
        else "CRASH REPORT (copy to share):\n\n" + message.take(8000)
    }

    private fun installEarlyCrashLogger() {
        val prior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try { saveError("Fatal startup error:\n" + Log.getStackTraceString(throwable)) }
            catch (_: Throwable) {}
            if (prior != null) prior.uncaughtException(thread, throwable)
            else android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun saveError(message: String) {
        reportStore.edit().putString("last_error", message.take(12000)).commit()
    }

    private fun addText(container: LinearLayout, text: String, size: Int, bold: Boolean) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(Color.rgb(36, 42, 52))
            if (bold) setTypeface(typeface, 1)
            setPadding(0, dp(4), 0, dp(9))
        })
    }

    private fun addButton(container: LinearLayout, label: String, action: () -> Unit) {
        container.addView(Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(58)).apply { bottomMargin = dp(11) })
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density + 0.5f).toInt()
}

/** Text-only, Android framework-only mode, independent of 3D/AR and Compose. */
class SafeModeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wrapper = ScrollView(this)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 28)
            setBackgroundColor(Color.rgb(255, 247, 232))
        }
        wrapper.addView(column)
        fun add(title: String, detail: String = "") {
            column.addView(TextView(this).apply {
                text = title + if (detail.isBlank()) "" else "\n" + detail
                textSize = 18f
                setTextColor(Color.rgb(60, 39, 23))
                setPadding(10, 15, 10, 16)
            })
        }
        add("BAGAN • SAFE MODE", "3D နှင့် AR မပါဘဲ ဘုရားစာရင်းကို ကြည့်ရှုနိုင်ပါသည်။")
        column.addView(Button(this).apply {
            text = "BACK / ပြန်သွားရန်"
            setOnClickListener { finish() }
        })
        try {
            val text = assets.open("catalog.json").bufferedReader().use { it.readText() }
            val records = JSONArray(text)
            add("ဘုရားများ — ${records.length()} ခု")
            for (i in 0 until records.length()) {
                val site = records.getJSONObject(i)
                add(site.optString("name", "ဘုရား"), site.optString("english", ""))
            }
        } catch (e: Exception) {
            add("Catalog Error", e.javaClass.simpleName + ": " + (e.message ?: "Unknown"))
        }
        setContentView(wrapper)
    }
}
