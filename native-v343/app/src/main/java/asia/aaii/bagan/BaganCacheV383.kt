package asia.aaii.bagan

import android.content.Context
import java.io.File
import java.util.Locale

/** V3.8.3 cache management. Operates ONLY in app-private GLB cache directories. */
enum class BaganCacheGroup(val folder:String,val caption:String){
    ORIGINAL("models","Original cPanel GLB"),
    AUTHENTIC("authentic","Authentic GLB (ZIP / Drive)"),
    AR("ar_lightweight","Lightweight CGI AR copies")
}
data class BaganCacheStat(val group:BaganCacheGroup,val files:Int,val bytes:Long)
data class BaganCacheResult(val cleared:Int,val freedBytes:Long)

object BaganCacheV383 {
    private fun safeDirectory(context:Context,group:BaganCacheGroup):File{
        val root=context.filesDir.canonicalFile
        val dir=File(root,group.folder).canonicalFile
        require(dir.parentFile==root){"Unsafe model cache directory"}
        return dir
    }
    private fun eligible(file:File):Boolean =
        file.isFile && (file.name.endsWith(".glb",ignoreCase=true) ||
            file.name.endsWith(".part",ignoreCase=true) ||
            file.name.endsWith(".download",ignoreCase=true))

    fun scan(context:Context):List<BaganCacheStat> = BaganCacheGroup.entries.map{group->
        val dir=safeDirectory(context,group)
        val models=dir.listFiles()?.filter(::eligible) ?: emptyList()
        BaganCacheStat(group,models.count{it.name.endsWith(".glb",ignoreCase=true)},
            models.sumOf{it.length()})
    }

    fun clear(context:Context,group:BaganCacheGroup):BaganCacheResult{
        val dir=safeDirectory(context,group)
        var deleted=0
        var freed=0L
        for(file in (dir.listFiles() ?: emptyArray())){
            if(!eligible(file))continue
            val bytes=file.length()
            if(file.delete()){deleted++;freed+=bytes}
        }
        return BaganCacheResult(deleted,freed)
    }

    fun formatSize(bytes:Long):String = when {
        bytes<1024L -> "$bytes B"
        bytes<1024L*1024 -> String.format(Locale.US,"%.1f KB",bytes/1024.0)
        else -> String.format(Locale.US,"%.1f MB",bytes/1024.0/1024.0)
    }
}
