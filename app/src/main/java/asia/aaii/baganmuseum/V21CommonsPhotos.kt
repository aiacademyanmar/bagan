package asia.aaii.baganmuseum

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/*
 * Wikimedia Commons photo thumbnails are loaded over HTTPS, *only on demand*
 * and never bundled/republished as permanent private assets. Description
 * links show original photographer and the current license/conditions.
 * Catalog images are search results, not independently authenticated photos
 * of individual archaeological objects. Offline: fallback to original art.
 */
internal data class CommonsPhoto(
    val bitmap:Bitmap,val filePage:String,val title:String,
    val credit:String,val license:String
)
private val commonsPhotoCache=object:LruCache<String,CommonsPhoto>(14){}
private fun searchTerms(item:V17Monument):String{
    return when(item.slug){
        "myazedi" -> "Myazedi inscription Bagan"
        "tharabar" -> "Tharabar Gate Bagan"
        "shwezigon" -> "Shwezigon Pagoda Bagan"
        "bupaya" -> "Bupaya Pagoda Bagan"
        "alodawpyae" -> "Alodawpyae Pagoda Bagan"
        else -> item.name+" Bagan Myanmar"
    }
}
private fun uriEncode(text:String)=URLEncoder.encode(text,"UTF-8")
private fun fetchThumb(item:V17Monument):CommonsPhoto? {
    if(item.slug=="entrance")return null // imaginary gateway, not a real landmark
    synchronized(commonsPhotoCache){commonsPhotoCache.get(item.slug)?.let{return it}}
    val api="https://commons.wikimedia.org/w/api.php?"+
        "action=query&format=json&formatversion=2&generator=search"+
        "&gsrnamespace=6&gsrlimit=5&gsrsearch="+uriEncode(searchTerms(item))+
        "&prop=imageinfo&iiprop=url%7Cextmetadata"+
        "&iiextmetadatafilter=LicenseShortName%7CArtist%7CCredit"+
        "&iiurlwidth=480"
    fun connect(url:String):HttpURLConnection{
        val c=URL(url).openConnection() as HttpURLConnection
        c.connectTimeout=5500;c.readTimeout=6500
        c.setRequestProperty("User-Agent","BaganMuseumEducation/2.1 (Android app; Wikimedia Commons attribution)")
        return c
    }
    try {
        val response=connect(api).run{
            try{inputStream.bufferedReader().use{it.readText()}}finally{disconnect()}
        }
        val pages=JSONObject(response).optJSONObject("query")?.optJSONArray("pages")?:return null
        for(i in 0 until pages.length()){
            val page=pages.optJSONObject(i)?:continue
            val info=page.optJSONArray("imageinfo")?.optJSONObject(0)?:continue
            val thumb=info.optString("thumburl").takeIf{it.startsWith("https://")}?:continue
            val filename=page.optString("title")
            val filePage=info.optString("descriptionurl").takeIf{it.startsWith("https://")}
                ?:("https://commons.wikimedia.org/wiki/"+Uri.encode(filename.replace(' ','_')))
            val meta=info.optJSONObject("extmetadata")
            fun field(name:String)=meta?.optJSONObject(name)?.optString("value")?:""
            val license=field("LicenseShortName").replace(Regex("<[^>]*>"),"").ifBlank{"See file page"}
            val artist=field("Artist").replace(Regex("<[^>]*>"),"").trim().take(80)
            val credit=artist.ifBlank{"Credit on file page"}
            val bmp=connect(thumb).run{
                try{inputStream.use{BitmapFactory.decodeStream(it)}}finally{disconnect()}
            }?:continue
            val result=CommonsPhoto(bmp,filePage,filename,credit,license)
            synchronized(commonsPhotoCache){commonsPhotoCache.put(item.slug,result)}
            return result
        }
    } catch(_:Exception){ /* Network failure returns offline artwork. */ }
    return null
}
@Composable
internal fun V21HeritagePhoto(item:V17Monument,modifier:Modifier=Modifier,showLabel:Boolean=false){
    val photo by produceState<CommonsPhoto?>(initialValue=null,key1=item.slug){
        value=withContext(Dispatchers.IO){fetchThumb(item)}
    }
    Box(modifier){
        if(photo!=null){
            AndroidView(
                factory={ctx->ImageView(ctx).apply{scaleType=ImageView.ScaleType.CENTER_CROP}},
                update={view->view.setImageBitmap(photo?.bitmap)},
                modifier=Modifier.fillMaxSize()
            )
        } else {
            MonumentArtworkV19(item.slug,Modifier.fillMaxSize(),caption=false)
        }
        if(showLabel){
            Text(if(photo==null)"Offline concept art" else "Photo: Wikimedia Commons",
                color=Color.White,fontSize=10.sp,
                modifier=Modifier.align(androidx.compose.ui.Alignment.BottomStart)
                    .background(Color(0xC82F2016),RoundedCornerShape(6.dp))
                    .padding(horizontal=8.dp,vertical=4.dp))
        }
    }
}
@Composable
internal fun V21CommonsLicense(item:V17Monument){
    val context=LocalContext.current
    val photo by produceState<CommonsPhoto?>(initialValue=null,key1=item.slug){
        value=withContext(Dispatchers.IO){fetchThumb(item)}
    }
    if(photo!=null){
        val found=photo!!
        Text("Wikimedia Commons • "+found.license+" • "+found.credit,
            fontSize=10.sp,color=Color(0xFF86694D))
        TextButton(onClick={openResource(context,found.filePage)},contentPadding=PaddingValues(0.dp)){
            Text("View original photo, author & license ↗",color=Color(0xFFAD7839),fontSize=11.sp)
        }
    }else{
        Text(if(item.slug=="entrance")"Concept gateway — no verified site photograph."
            else "Offline artwork shown. Photos require internet.",
            color=Color(0xFF886E55),fontSize=10.sp)
        TextButton(onClick={openResource(context,commonsUrl(item.slug))},contentPadding=PaddingValues(0.dp)){
            Text("Search real photos on Wikimedia Commons ↗",fontSize=11.sp)
        }
    }
}
private fun openResource(context:Context,url:String){
    try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}
}
@Composable
internal fun V21PhotoGallery(items:List<V17Monument>,openDetail:(Int)->Unit){
    val context=LocalContext.current
    Text("HERITAGE PHOTO COLLECTION",color=Color(0xFF3D2919),
        fontSize=22.sp,fontWeight=FontWeight.Black)
    Text("Internet photo search • Wikimedia Commons links with file licenses.",
        color=Color(0xFF86694D),fontSize=12.sp)
    Spacer(Modifier.height(12.dp))
    items.forEachIndexed{index,item->
        Surface(shape=RoundedCornerShape(16.dp),color=Color(0xFFFFF9F0),
            modifier=Modifier.fillMaxWidth().padding(bottom=13.dp)){
            Column(Modifier.padding(12.dp)){
                V21HeritagePhoto(item,Modifier.fillMaxWidth().height(175.dp),showLabel=true)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text(item.mm,color=Color(0xFF382416),fontSize=17.sp,fontWeight=FontWeight.Bold)
                        Text(item.name,color=Color(0xFF90785F),fontSize=12.sp)
                    }
                    TextButton(onClick={openDetail(index)}){
                        Text("Info ›",color=Color(0xFFB6803E))
                    }
                }
                V21CommonsLicense(item)
            }
        }
    }
    Text("Commons search thumbnails are illustrative discovery results. Confirm each image's subject and licensing on its file page before reuse.",
        color=Color(0xFF90785F),fontSize=11.sp)
}
