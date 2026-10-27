package asia.aaii.baganmuseum

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Self-contained native illustration library; not historic photos, verified
 * architectural elevations, nor derived from the private Blender source.
 * Unlike a repeated photo placeholder, each monument renders an identifiable
 * category-specific composition and shape entirely in Compose Canvas.
 */
private data class MonumentPaint(
    val skyTop:Color,val skyLow:Color,val brick:Color,val shadow:Color,val trim:Color,
    val floor:Color,val label:String
)
private val palettes=listOf(
    MonumentPaint(Color(0xFF6F7E97),Color(0xFFE5B883),Color(0xFFB87B50),Color(0xFF804F39),Color(0xFFFFDC98),Color(0xFF865F49),"THATBYINNYU"),
    MonumentPaint(Color(0xFF5B849B),Color(0xFFF1CA91),Color(0xFF918077),Color(0xFF635E58),Color(0xFFDDC1A4),Color(0xFF656058),"MYAZEDI"),
    MonumentPaint(Color(0xFF8A7882),Color(0xFFDFB78F),Color(0xFFB77A53),Color(0xFF794531),Color(0xFFFFD499),Color(0xFF9B785C),"GATEWAY"),
    MonumentPaint(Color(0xFF777785),Color(0xFFFFD194),Color(0xFFAA7350),Color(0xFF62432D),Color(0xFFEAC39B),Color(0xFF80583F),"THARABAR"),
    MonumentPaint(Color(0xFF6B91A5),Color(0xFFF7CF8A),Color(0xFFAE7755),Color(0xFF76523B),Color(0xFFEDC397),Color(0xFF8A654D),"GAWDAWPALIN"),
    MonumentPaint(Color(0xFF728795),Color(0xFFEDCBA2),Color(0xFFE3CAA5),Color(0xFFA78B69),Color(0xFFF4EAD9),Color(0xFF9E8467),"ANANDA"),
    MonumentPaint(Color(0xFF766F78),Color(0xFFF5BC91),Color(0xFFBB7954),Color(0xFF825037),Color(0xFFFFCA81),Color(0xFFA57854),"HTILOMINLO"),
    MonumentPaint(Color(0xFF769397),Color(0xFFF6D9A4),Color(0xFFD4A24F),Color(0xFF916F32),Color(0xFFFFE199),Color(0xFF9F845F),"ALODAWPYAE"),
    MonumentPaint(Color(0xFF5E738A),Color(0xFFFFC986),Color(0xFFCCA74D),Color(0xFF9E7835),Color(0xFFFFE8AB),Color(0xFF938063),"SHWEZIGON"),
    MonumentPaint(Color(0xFF6D89A0),Color(0xFFF7CC98),Color(0xFFDFB660),Color(0xFFAC7C39),Color(0xFFFFE2A1),Color(0xFF8F7555),"BUPAYA")
)
private val slugOrder=listOf("thatbyinnyu","myazedi","entrance","tharabar","gawdawpalin",
    "ananda","htilominlo","alodawpyae","shwezigon","bupaya")
private fun paletteFor(slug:String):MonumentPaint{
    val idx=slugOrder.indexOf(slug).coerceAtLeast(0)
    return palettes[idx]
}
private fun DrawScope.rect(x:Float,y:Float,w:Float,h:Float,c:Color){
    drawRect(c,topLeft=Offset(x*size.width,y*size.height),size=Size(w*size.width,h*size.height))
}
private fun DrawScope.polygon(c:Color,vararg coords:Float){
    val path=Path()
    var i=0
    while(i+1<coords.size){
        val x=coords[i]*size.width
        val y=coords[i+1]*size.height
        if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
        i+=2
    }
    path.close()
    drawPath(path,c)
}
private fun DrawScope.archedDoor(x:Float,y:Float,w:Float,h:Float){
    val path=Path().apply{
        moveTo((x)*size.width,(y+h)*size.height)
        lineTo(x*size.width,(y+h*.26f)*size.height)
        quadraticTo((x+w*.5f)*size.width,(y-h*.10f)*size.height, (x+w)*size.width,(y+h*.26f)*size.height)
        lineTo((x+w)*size.width,(y+h)*size.height)
        close()
    }
    drawPath(path,Color(0xFF3E2C22))
}
private fun DrawScope.tower(x:Float,y:Float,w:Float,h:Float,p:MonumentPaint,accent:Boolean=false){
    rect(x,y,w,h,if(accent)p.trim else p.brick)
    rect(x+.02f,y+.03f,w-.04f,h*.075f,p.shadow.copy(alpha=.72f))
    rect(x-.018f,y-.055f,w+.036f,.045f,p.trim)
    polygon(if(accent)p.trim else p.shadow,
        x-.03f,y-.055f,x+w+.03f,y-.055f,x+w*.5f,y-.26f)
    rect(x+w*.465f,y-.31f,w*.07f,.08f,p.trim)
    archedDoor(x+w*.30f,y+h*.43f,w*.4f,h*.45f)
}
private fun DrawScope.pagoda(x:Float,y:Float,w:Float,h:Float,p:MonumentPaint,layers:Int){
    rect(x,y+h*.74f,w,h*.13f,p.brick)
    rect(x+w*.10f,y+h*.64f,w*.8f,h*.12f,p.trim)
    for(i in 0 until layers){
        val inset=(i.toFloat()/layers)*w*.30f
        val yy=y+h*(.48f-i*.07f)
        rect(x+inset,yy,w-inset*2,h*.065f,if(i%2==0)p.trim else p.brick)
    }
    polygon(p.brick,x+w*.18f,y+h*.47f,x+w*.82f,y+h*.47f,x+w*.57f,y+h*.04f,
        x+w*.43f,y+h*.04f)
    polygon(p.trim,x+w*.43f,y+h*.04f,x+w*.57f,y+h*.04f,x+w*.50f,y-h*.12f)
    rect(x+w*.486f,y-h*.22f,w*.028f,h*.13f,p.trim)
    rect(x-.08f,y+h*.84f,w+.16f,h*.085f,p.shadow)
}
private fun DrawScope.drawMonumentShape(slug:String,p:MonumentPaint){
    val gate=slug=="entrance"||slug=="tharabar"
    val tablet=slug=="myazedi"
    if(tablet){
        rect(.35f,.70f,.31f,.09f,p.shadow)
        rect(.39f,.27f,.23f,.45f,p.shadow)
        rect(.405f,.285f,.20f,.40f,p.brick)
        rect(.42f,.255f,.17f,.045f,p.trim)
        for(row in 0..9)for(col in 0..3){
            rect(.425f+col*.039f,.34f+row*.031f,.025f,.008f,p.shadow)
        }
    }else if(gate){
        rect(.08f,.72f,.84f,.10f,p.floor)
        rect(.19f,.34f,.22f,.41f,p.brick)
        rect(.60f,.34f,.22f,.41f,p.brick)
        rect(.27f,.36f,.47f,.12f,p.brick)
        rect(.26f,.31f,.48f,.06f,p.trim)
        polygon(p.shadow,.27f,.31f,.73f,.31f,.68f,.23f,.32f,.23f)
        rect(.24f,.22f,.12f,.09f,p.trim)
        rect(.64f,.22f,.12f,.09f,p.trim)
        polygon(p.brick,.22f,.22f,.38f,.22f,.30f,.08f)
        polygon(p.brick,.62f,.22f,.78f,.22f,.70f,.08f)
        polygon(p.trim,.43f,.31f,.57f,.31f,.50f,.14f)
        archedDoor(.405f,.46f,.20f,.29f)
    }else if(slug=="shwezigon"||slug=="bupaya"||slug=="alodawpyae"){
        val r=if(slug=="bupaya") .31f else .42f
        val x=.5f-r*.5f
        pagoda(x,.30f,r,.51f,p,if(slug=="shwezigon")5 else 3)
    }else{
        val isAnanda=slug=="ananda"
        val tall=slug=="thatbyinnyu"
        val wide=if(isAnanda).47f else if(tall).33f else .39f
        val x=.5f-wide/2
        val y=if(tall).31f else .42f
        val h=if(tall).46f else .38f
        rect(x-.08f,.72f,wide+.16f,.10f,p.floor)
        rect(x,y,wide,h,p.brick)
        rect(x-.03f,y-.04f,wide+.06f,.060f,p.trim)
        archedDoor(.47f,.59f,.06f,.16f)
        rect(x+.05f,y+.12f,.055f,.13f,p.shadow)
        rect(x+wide-.10f,y+.12f,.055f,.13f,p.shadow)
        if(isAnanda){
            rect(.16f,.57f,.22f,.17f,p.brick)
            rect(.62f,.57f,.22f,.17f,p.brick)
        }
        rect(.40f,y-.145f,.20f,.10f,p.brick)
        rect(.385f,y-.16f,.23f,.045f,p.trim)
        polygon(p.brick,.41f,y-.16f,.59f,y-.16f,.50f,y-.35f)
        polygon(p.trim,.485f,y-.34f,.515f,y-.34f,.50f,y-.49f)
        rect(.493f,y-.53f,.014f,.075f,p.trim)
        for(dx in listOf(.04f,wide-.10f)){
            polygon(p.trim,x+dx,y-.04f,x+dx+.06f,y-.04f,x+dx+.03f,y-.16f)
        }
    }
}
@Composable
internal fun MonumentArtworkV19(slug:String,modifier:Modifier=Modifier,caption:Boolean=false){
    val p=paletteFor(slug)
    Box(modifier.clip(RoundedCornerShape(12.dp))){
        Canvas(Modifier.fillMaxSize()){
            drawRect(brush=Brush.verticalGradient(listOf(p.skyTop,p.skyLow)))
            drawCircle(Color(0xFFFFEDBF).copy(alpha=.91f),radius=size.minDimension*.13f,
                center=Offset(size.width*.78f,size.height*.22f))
            polygon(p.floor.copy(alpha=.43f),0f,.59f,.15f,.55f,.45f,.62f,.65f,.56f,1f,.61f,1f,1f,0f,1f)
            rect(0f,.75f,1f,.25f,p.floor)
            drawMonumentShape(slug,p)
            rect(0f,.90f,1f,.10f,p.shadow.copy(alpha=.43f))
        }
        if(caption){
            Box(Modifier.align(Alignment.BottomStart).padding(7.dp)
                .background(Color(0xCC2C1C14),RoundedCornerShape(7.dp))
                .padding(horizontal=7.dp,vertical=4.dp)){
                Text(p.label,color=Color(0xFFF7D38D),fontSize=9.sp,fontWeight=FontWeight.Bold)
            }
        }
    }
}
