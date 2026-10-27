package asia.aaii.bagan

import android.content.Context
import org.json.JSONArray
import java.io.File
import java.io.IOException
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
