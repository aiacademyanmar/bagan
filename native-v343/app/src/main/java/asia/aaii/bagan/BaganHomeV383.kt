package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val v383Navy = Color(0xFF081B2A)
private val v383Panel = Color(0xFF122B3F)
private val v383Gold = Color(0xFFFFD476)
private val v383Ivory = Color(0xFFFFF8EB)
private val v383Muted = Color(0xFFBACAD8)
private val v383Outline = Color(0xFF38516A)
private val v383Artwork = listOf(
    R.drawable.bagan_safe_1,R.drawable.bagan_safe_2,R.drawable.bagan_safe_3,
    R.drawable.bagan_safe_4,R.drawable.bagan_safe_5,R.drawable.bagan_safe_6
)

@Composable
fun BaganBottomBarV383(active:Int,navigate:(Int)->Unit){
    val routes=listOf(
        Triple(2,"⌂","Home"),Triple(3,"♜","Pagodas"),
        Triple(6,"◉","Scan / AR"),Triple(7,"◆","Map"),
        Triple(12,"♥","Saved"),Triple(16,"⋯","More")
    )
    Surface(color=v383Navy,shadowElevation=12.dp,
        modifier=Modifier.fillMaxWidth(),
        border=BorderStroke(1.dp,v383Outline)){
        Row(Modifier.fillMaxWidth().heightIn(min=74.dp).padding(horizontal=4.dp),
            horizontalArrangement=Arrangement.spacedBy(1.dp),
            verticalAlignment=Alignment.CenterVertically){
            routes.forEach { (route,symbol,label)->
                val chosen=when(route){
                    2->active==2
                    3->active in listOf(3,4,13,22)
                    6->active in listOf(5,6)
                    7->active in listOf(7,14)
                    12->active==12
                    else->active in listOf(15,16,17,18,20,21,26)
                }
                val isAr=route==6
                Column(
                    Modifier.weight(1f).clickable{navigate(route)}
                        .padding(vertical=4.dp),
                    horizontalAlignment=Alignment.CenterHorizontally){
                    Box(Modifier.size(if(isAr)48.dp else 36.dp)
                        .clip(RoundedCornerShape(if(isAr)24.dp else 13.dp))
                        .background(if(isAr)v383Gold else if(chosen)v383Panel else Color.Transparent),
                        contentAlignment=Alignment.Center){
                        Text(symbol,fontSize=if(isAr)27.sp else 23.sp,
                            color=if(isAr)v383Navy else if(chosen)v383Gold else v383Ivory)
                    }
                    Text(label,color=if(chosen)v383Gold else v383Muted,fontSize=9.sp,
                        maxLines=1,softWrap=false,textAlign=TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun HomeActionV383(label:String,onClick:()->Unit,primary:Boolean=false,modifier:Modifier=Modifier){
    if(primary){
        Button(onClick=onClick,modifier=modifier.heightIn(min=46.dp),
            shape=RoundedCornerShape(10.dp),
            contentPadding=PaddingValues(horizontal=8.dp,vertical=8.dp),
            colors=ButtonDefaults.buttonColors(containerColor=v383Gold,contentColor=v383Navy)){
            Text(label,fontSize=11.sp,maxLines=1,fontWeight=FontWeight.Bold)
        }
    } else {
        OutlinedButton(onClick=onClick,modifier=modifier.heightIn(min=46.dp),
            contentPadding=PaddingValues(horizontal=7.dp,vertical=8.dp),
            shape=RoundedCornerShape(10.dp),
            border=BorderStroke(1.dp,v383Outline)){
            Text(label,color=v383Ivory,fontSize=11.sp,maxLines=1)
        }
    }
}

/**
 * Page 2 design aligned to the Navy/Gold concept.
 * Photographs are bundled decorative concept assets; do not present as evidence of a monument.
 */
@Composable
fun BaganHomeV383(
    temples:List<Temple>, onSearch:()->Unit, navigate:(Int)->Unit,
    onDetails:(Temple)->Unit,on3D:(Temple)->Unit,onAR:(Temple)->Unit,
    onFavorite:(Temple)->Unit, favorites:Set<String>
){
    val shortcuts=listOf(
        Triple("♜","Pagodas",3),Triple("⬡","3D Models",15),
        Triple("◉","AR Camera",6),Triple("◆","Map",7),
        Triple("▤","History",9),Triple("♥","Saved",12)
    )
    LazyColumn(Modifier.fillMaxSize().background(v383Navy),
        verticalArrangement=Arrangement.spacedBy(13.dp),
        contentPadding=PaddingValues(bottom=24.dp)){
        item{
            Box(Modifier.fillMaxWidth().height(213.dp)){
                Image(painterResource(R.drawable.bagan_safe_1),
                    contentDescription="Illustrative Bagan heritage scenery",
                    contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x55081B2A),Color(0xEB081B2A)))))
                Column(Modifier.align(Alignment.BottomStart).padding(17.dp)){
                    Text("DISCOVER BAGAN",color=v383Gold,fontWeight=FontWeight.Black,
                        fontSize=26.sp)
                    Text("ပုဂံအမွေအနှစ်များကို လေ့လာကြမယ်",color=Color.White,
                        fontWeight=FontWeight.SemiBold,fontSize=15.sp)
                    Spacer(Modifier.height(9.dp))
                    HomeActionV383("Explore Pagodas  ›",{navigate(3)},true)
                }
            }
        }
        item{
            Surface(onClick=onSearch,modifier=Modifier.fillMaxWidth()
                .padding(horizontal=13.dp).heightIn(min=55.dp),
                color=v383Panel,shape=RoundedCornerShape(13.dp),
                border=BorderStroke(1.dp,v383Outline)){
                Row(Modifier.padding(horizontal=15.dp),verticalAlignment=Alignment.CenterVertically){
                    Text("⌕",color=v383Gold,fontSize=28.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("Search pagodas, history and 3D models…",
                        color=v383Muted,fontSize=12.sp)
                }
            }
        }
        item{
            Row(Modifier.fillMaxWidth().padding(horizontal=15.dp),
                horizontalArrangement=Arrangement.SpaceBetween){
                Text("Explore Bagan",color=v383Gold,
                    fontWeight=FontWeight.Bold,fontSize=19.sp)
                Text("${temples.size} heritage entries",color=v383Muted,fontSize=11.sp)
            }
        }
        items(shortcuts.chunked(3)){line->
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),
                horizontalArrangement=Arrangement.spacedBy(8.dp)){
                line.forEach { (glyph,label,route)->
                    Surface(onClick={navigate(route)},modifier=Modifier.weight(1f),
                        color=v383Panel,shape=RoundedCornerShape(14.dp),
                        border=BorderStroke(1.dp,v383Outline)){
                        Column(Modifier.fillMaxWidth().padding(vertical=12.dp,horizontal=3.dp),
                            horizontalAlignment=Alignment.CenterHorizontally){
                            Text(glyph,color=v383Gold,fontSize=26.sp)
                            Spacer(Modifier.height(7.dp))
                            Text(label,color=v383Ivory,fontSize=11.sp,
                                textAlign=TextAlign.Center,maxLines=1)
                        }
                    }
                }
            }
        }
        item{
            Row(Modifier.fillMaxWidth().padding(horizontal=14.dp),
                horizontalArrangement=Arrangement.SpaceBetween,
                verticalAlignment=Alignment.CenterVertically){
                Text("Featured Pagodas",color=v383Gold,
                    fontWeight=FontWeight.Bold,fontSize=19.sp)
                TextButton(onClick={navigate(3)}){Text("View All ›",color=v383Gold)}
            }
        }
        items(temples.take(8).chunked(2)){pair->
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),
                horizontalArrangement=Arrangement.spacedBy(9.dp)){
                pair.forEach {temple->
                    val artIndex=temples.indexOf(temple)%v383Artwork.size
                    Surface(modifier=Modifier.weight(1f),color=v383Panel,
                        shape=RoundedCornerShape(14.dp),
                        border=BorderStroke(1.dp,v383Outline)){
                        Column{
                            Box(Modifier.fillMaxWidth().height(117.dp)){
                                Image(painterResource(v383Artwork[artIndex]),
                                    contentDescription="Illustration for ${temple.nameEn}",
                                    contentScale=ContentScale.Crop,
                                    modifier=Modifier.fillMaxSize())
                                Surface(onClick={onFavorite(temple)},
                                    modifier=Modifier.align(Alignment.TopEnd).padding(6.dp),
                                    color=Color(0xD9081B2A),
                                    shape=RoundedCornerShape(50)){
                                    Text(if(temple.id in favorites)"♥" else "♡",
                                        Modifier.padding(horizontal=9.dp,vertical=4.dp),
                                        color=if(temple.id in favorites)Color(0xFFFF6C6C) else v383Gold)
                                }
                            }
                            Column(Modifier.padding(9.dp),
                                verticalArrangement=Arrangement.spacedBy(4.dp)){
                                Text(temple.nameEn,color=v383Ivory,
                                    fontWeight=FontWeight.Bold,fontSize=12.sp,maxLines=1)
                                Text(temple.nameMm,color=v383Gold,
                                    fontSize=11.sp,maxLines=1)
                                Text("Illustrative image • Bagan",color=v383Muted,
                                    fontSize=9.sp,maxLines=1)
                                HomeActionV383("Details",{onDetails(temple)},false,
                                    Modifier.fillMaxWidth())
                                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                                    HomeActionV383("3D",{on3D(temple)},true,Modifier.weight(1f))
                                    HomeActionV383("AR",{onAR(temple)},false,Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                if(pair.size==1)Spacer(Modifier.weight(1f))
            }
        }
        item{
            Text("3D/AR models are educational visualizations; device AR compatibility varies.",
                Modifier.fillMaxWidth().padding(horizontal=15.dp),
                color=v383Muted,fontSize=11.sp,textAlign=TextAlign.Center)
        }
    }
}
