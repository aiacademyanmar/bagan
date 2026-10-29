package asia.aaii.bagan

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Temple(
    val id: String, val nameMm: String, val nameEn: String,
    val description: String, val fileName: String
) {
    val url: String
        get() = "https://aaii.asia/bagan/3d/models/" + fileName
}

object ModelRepository {
    fun catalog(context: Context): List<Temple> {
        val text = context.assets.open("catalog.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            Temple(
                item.getString("id"), item.getString("name"),
                item.optString("english"), item.optString("description"),
                item.getString("modelUrl").substringAfterLast("/")
            )
        }
    }
    fun cached(context: Context, item: Temple): File =
        File(File(context.filesDir, "models"), item.id + ".glb")

    /** Bundled offline stylized CGI is loaded from Android assets; never through the network. */
    fun optimizedAsset(context: Context, item: Temple): String {
        val path = "models/optimized/${item.id}.glb"
        val buffer = context.assets.open(path).use { it.readBytes() }
        if (buffer.size < 28 || buffer.size > 1_000_000)
            throw IOException("Optimized CGI GLB file is missing or oversized")
        val bytes = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        if (bytes.int != 0x46546c67 || bytes.int != 2 || bytes.int != buffer.size)
            throw IOException("Optimized CGI has invalid GLB 2.0 header or length")
        val jsonLength = bytes.int
        val jsonType = bytes.int
        if (jsonType != 0x4e4f534a || jsonLength <= 0 ||
            20L + jsonLength + 8 > buffer.size.toLong())
            throw IOException("Optimized CGI GLB JSON chunk is invalid")
        bytes.position(20 + jsonLength)
        val binaryLength = bytes.int
        val binaryType = bytes.int
        if (binaryType != 0x004e4942 || binaryLength < 0 ||
            28L + jsonLength + binaryLength != buffer.size.toLong())
            throw IOException("Optimized CGI GLB BIN chunk is invalid")
        return path
    }

    /** Prepared lightweight CGI for native AR. Every Bagan site has a bundled GLB.
     * ARCore / Filament accept a local file URI; copying once avoids asset URI ambiguity.
     * These are stylized approximations, not scanned archaeological geometry.
     */
    fun lightweightAr(context: Context, temple: Temple): File {
        val asset = optimizedAsset(context, temple)
        val directory = File(context.filesDir, "ar_lightweight")
        if(!directory.exists() && !directory.mkdirs())throw IOException("Cannot initialize AR model cache")
        val cached = File(directory, temple.id + ".glb")
        if(cached.exists()){
            try {validate(cached);return cached}
            catch(_:IOException){cached.delete()}
        }
        val part = File(directory, temple.id + ".part")
        try {
            context.assets.open(asset).use { input ->
                part.outputStream().use { output -> input.copyTo(output) }
            }
            validate(part)
            if(!part.renameTo(cached))part.copyTo(cached, overwrite=true)
            validate(cached)
            return cached
        } finally { part.delete() }
    }

    fun validate(file: File) {
        if (!file.isFile || file.length() < 20 || file.length() > 250_000_000L)
            throw IOException("Invalid GLB size")
        val header = ByteArray(12)
        file.inputStream().use {
            var offset = 0
            while (offset < 12) {
                val n = it.read(header, offset, 12 - offset)
                if (n < 0) throw IOException("Incomplete GLB header")
                offset += n
            }
        }
        if (!header.copyOfRange(0, 4).contentEquals("glTF".toByteArray(Charsets.US_ASCII)))
            throw IOException("The server returned HTML or invalid binary data")
        val bytes = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        bytes.position(4)
        val version = bytes.int
        val declared = bytes.int.toLong() and 0xFFFFFFFFL
        if (version != 2 || declared != file.length())
            throw IOException("GLB version or length mismatch")
    }
    fun demo(context: Context): File {
        val file = File(context.filesDir, "bagan_demo_pagoda.glb")
        if (!file.exists()) {
            context.assets.open("models/demo_pagoda.glb").use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        validate(file)
        return file
    }
    /** The original source folder is the user-provided Google Drive Bagan 3D folder.
     * Optimized original-derived GLBs can be installed on the same cPanel host under
     * /bagan/3d/models/optimized_authentic/<original filename>.
     * Fail safely to the user's existing original GLB, never to a mislabeled stylized model.
     */
    private val originalDriveIds = mapOf(
        "shwezigon" to "1Q_veLIGE00yUxV9UNINYXoiAi2QARbOS",
        "ananda" to "1CGuPzEh1j1G2y8rdOJGNYYm3xJnKBwie",
        "htilominlo" to "13-55kND78INyGKvdybrqmHV-3kSRdemZ",
        "thatbyinnyu" to "1bgEDfLffGtW58hp_skd5q9prSLWHVzzC",
        "dhammayangyi" to "1G7autBQod2JgapM00t0FugQ3mZUqOnuB",
        "shwesandaw" to "1_BfOf9Vt5kmsvIy4Q8i8G_btfOc_OPT8",
        "bupaya" to "1CbMY2BsQmHGVFXF49zmHf60WATXZFfv9",
        "gawdawpalin" to "1T24oBJKJ4O_kUlF7kpZ7Z89_QH-GJMze",
        "alodawpyae" to "1UoRssPa2TXBanmqusuVW9aU1zao_VdZE",
        "lawka-hteik-pan" to "1rNlI9kNuD-r4hWBEnXrIKCHw2eNE0lkZ",
        "myazedi" to "1ITsD-E0d1USfF2N_xyyrAmT1qJrU0bWb",
        "tharabar-gate" to "1qypW62d-WJta7vZVE-WhEh1CruWP8ZyD",
        "bagan-entrance" to "1ewXW_x2RLeil4JQ7DmAh8ploq0RApr7V"
    )
    fun authenticCached(context: Context, item: Temple): File =
        File(File(context.filesDir, "authentic"), item.id + ".glb")

    /**
     * Import a user-selected ZIP of original-derived Bagan GLBs into private offline storage.
     * SAF picker avoids storage permission. Only exact catalog filenames are accepted;
     * all archives entries are streamed (not extracted by path), preventing zip-slip.
     * Each model is validated as GLB 2.0 before replacing its cached copy.
     * Accepts the V3.7.7 archive at models/optimized_authentic/ (GLB files).
     */
    fun importAuthenticZip(context: Context, uri: Uri, progress:(Int,Int)->Unit):Int {
        val catalog=catalog(context)
        val known=catalog.associateBy{it.fileName}
        val dir=File(context.filesDir,"authentic")
        if(!dir.exists() && !dir.mkdirs())throw IOException("Cannot create 3D cache directory")
        val input=context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to read selected ZIP")
        var imported=0
        var totalUnpacked=0L
        val seen=HashSet<String>()
        ZipInputStream(BufferedInputStream(input)).use{zip->
            while(true){
                val entry=zip.nextEntry ?: break
                if(entry.isDirectory){zip.closeEntry();continue}
                val fileName=entry.name.replace('\\','/').substringAfterLast('/')
                val item=known[fileName]
                if(item==null || !seen.add(fileName)){zip.closeEntry();continue}
                val dst=authenticCached(context,item)
                val tmp=File(dir,item.id+".part")
                try{
                    var size=0L
                    tmp.outputStream().use{out->
                        val buf=ByteArray(64*1024)
                        while(true){
                            val read=zip.read(buf)
                            if(read<0)break
                            size+=read
                            totalUnpacked+=read
                            if(size>250_000_000L || totalUnpacked>350_000_000L)
                                throw IOException("ZIP GLB exceeds safety limit")
                            out.write(buf,0,read)
                        }
                    }
                    validate(tmp)
                    if(!tmp.renameTo(dst))tmp.copyTo(dst,overwrite=true)
                    validate(dst)
                    imported++
                    progress(imported,catalog.size)
                }finally{tmp.delete();zip.closeEntry()}
            }
        }
        if(imported==0)throw IOException("ZIP does not contain any recognized Bagan GLB files")
        return imported
    }

    fun downloadAuthentic(context: Context, item: Temple, progress:(Int)->Unit):File {
        val saved=authenticCached(context,item)
        if(saved.exists()) try {validate(saved);progress(100);return saved} catch(_:IOException){saved.delete()}
        saved.parentFile?.mkdirs()
        val driveId=originalDriveIds[item.id]
        val urls=listOfNotNull(
            "https://aaii.asia/bagan/3d/models/optimized_authentic/" + item.fileName,
            item.url,
            driveId?.let{"https://drive.google.com/uc?export=download&id="+it}
        )
        var lastFailure:Exception=IOException("Authentic model unavailable")
        for(url in urls){
            val temp=File(saved.parentFile,item.id+".download")
            var connection:HttpURLConnection?=null
            try {
                connection=URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout=10000
                connection.readTimeout=30000
                connection.instanceFollowRedirects=true
                connection.setRequestProperty("Accept", "application/octet-stream")
                val status=connection.responseCode
                if(status!=200)throw IOException("HTTP $status at $url")
                val total=connection.contentLengthLong
                var received=0L
                connection.inputStream.use{input->
                    temp.outputStream().use{output->
                        val buf=ByteArray(64*1024)
                        while(true){
                            val count=input.read(buf)
                            if(count<0)break
                            received+=count
                            if(received>250_000_000L)throw IOException("GLB too large")
                            output.write(buf,0,count)
                            if(total>0)progress((received*95/total).toInt().coerceIn(0,95))
                        }
                    }
                }
                validate(temp)
                if(!temp.renameTo(saved)){temp.copyTo(saved,overwrite=true)}
                validate(saved);progress(100);return saved
            }catch(e:Exception){lastFailure=e}
            finally{connection?.disconnect();temp.delete()}
        }
        throw IOException("Optimized/cPanel/Drive GLB inaccessible for "+item.fileName+
            ". Check public sharing or upload verified GLB to cPanel.",lastFailure)
    }

    fun download(context: Context, item: Temple, progress: (Int) -> Unit): File {
        val saved = cached(context, item)
        if (saved.exists()) {
            try { validate(saved); progress(100); return saved }
            catch (_: IOException) { saved.delete() }
        }
        saved.parentFile?.mkdirs()
        val temp = File(saved.parentFile, saved.name + ".part")
        val connection = URL(item.url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "model/gltf-binary,application/octet-stream")
        try {
            val status = connection.responseCode
            if (status != 200) throw IOException("HTTP $status — Check cPanel model: ${item.fileName}")
            val total = connection.contentLengthLong
            var received = 0L
            connection.inputStream.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val size = input.read(buffer)
                        if (size < 0) break
                        received += size
                        if (received > 250_000_000L) throw IOException("GLB too large")
                        output.write(buffer, 0, size)
                        if (total > 0) progress((received * 95 / total).toInt().coerceIn(0, 95))
                    }
                }
            }
            validate(temp)
            temp.copyTo(saved, overwrite = true)
            progress(100)
            return saved
        } finally {
            connection.disconnect()
            temp.delete()
        }
    }
}
