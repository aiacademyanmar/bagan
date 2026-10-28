package asia.aaii.bagan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val warmCream = Color(0xFFFFF7E8)
private val darkBrown = Color(0xFF49250F)
private val oldGold = Color(0xFFA76624)
private val goldenCard = Color(0xFFF5CE90)

@Composable
fun V361BottomBar(activeRoute: Int, onNavigate: (Int) -> Unit) {
    val tabs = listOf(
        Triple(2, "⌂", "Home"),
        Triple(13, "⌕", "Explore"),
        Triple(7, "▧", "Map"),
        Triple(12, "♡", "Saved"),
        Triple(16, "☰", "More")
    )
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (route, symbol, label) ->
                val selected = when (route) {
                    2 -> activeRoute == 2
                    13 -> activeRoute in listOf(3, 4, 13, 22)
                    7 -> activeRoute == 7 || activeRoute == 14
                    12 -> activeRoute == 12
                    else -> activeRoute in listOf(16, 17, 18, 21, 23, 24, 26, 27)
                }
                Column(
                    modifier = Modifier.weight(1f).heightIn(min = 58.dp)
                        .clickable { onNavigate(route) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = symbol, fontSize = 23.sp,
                        color = if (selected) oldGold else darkBrown,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = label, fontSize = 11.sp,
                        color = if (selected) oldGold else Color.DarkGray,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun V361PagodaArt(modifier: Modifier = Modifier, seed: Int = 0) {
    val sky = if (seed % 2 == 0) Color(0xFF88B9D6) else Color(0xFFE4B78A)
    val stone = if (seed % 2 == 0) Color(0xFFE3CFB4) else Color(0xFFAE7042)
    Canvas(
        modifier.background(
            Brush.verticalGradient(listOf(sky, warmCream, Color(0xFFB57F4F)))
        )
    ) {
        val w = size.width
        val h = size.height
        val ground = h * .91f
        drawCircle(Color(0xFFFFE8AD), w * .12f, Offset(w * .81f, h * .23f))
        // Stylized original temple drawing, not a historical photograph.
        drawRect(stone, Offset(w * .21f, h * .61f), androidx.compose.ui.geometry.Size(w * .58f, h * .31f))
        drawRect(Color(0xFFF6E5CF), Offset(w * .16f, h * .57f), androidx.compose.ui.geometry.Size(w * .68f, h * .06f))
        drawRect(stone, Offset(w * .34f, h * .43f), androidx.compose.ui.geometry.Size(w * .32f, h * .15f))
        val roof = Path().apply {
            moveTo(w * .38f, h * .45f)
            lineTo(w * .5f, h * .12f)
            lineTo(w * .62f, h * .45f)
            close()
        }
        drawPath(roof, Color(0xFF99571E), style = Fill)
        drawRect(Color(0xFF4F311F), Offset(w * .45f, h * .72f), androidx.compose.ui.geometry.Size(w * .1f, h * .2f))
        drawLine(Color(0xFFE0AE56), Offset(w * .5f, h * .12f), Offset(w * .5f, h * .05f), strokeWidth = w * .013f)
        drawLine(Color(0xFF72532F), Offset(0f, ground), Offset(w, ground), strokeWidth = h * .035f)
    }
}

@Composable
fun V361Home(
    temples: List<Temple>,
    onSearch: () -> Unit,
    onNavigate: (Int) -> Unit,
    onTemple: (Temple) -> Unit
) {
    val shortcuts = listOf(
        Triple("♜", "Temples", 3),
        Triple("▤", "History", 9),
        Triple("⬡", "3D Viewer", 5),
        Triple("◉", "AR Experience", 6),
        Triple("▧", "Map", 7),
        Triple("♥", "Favorites", 12)
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(warmCream),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                color = warmCream,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, Color(0xFFEADDC9))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .clickable(onClick = onSearch).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⌕", color = darkBrown, fontSize = 23.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("Search temples, history...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
        items(shortcuts.chunked(3)) { shortcutRow ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                shortcutRow.forEach { (icon, title, route) ->
                    Column(
                        modifier = Modifier.weight(1f).heightIn(min = 111.dp)
                            .clickable { onNavigate(route) }
                            .padding(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                                .background(goldenCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, color = darkBrown, fontSize = 32.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            title, color = darkBrown, fontSize = 12.sp,
                            textAlign = TextAlign.Center, maxLines = 2,
                            lineHeight = 14.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Featured Temples", fontSize = 22.sp, color = darkBrown, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onNavigate(3) }) { Text("See All", color = oldGold) }
            }
        }
        items(temples.take(6).chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEachIndexed { index, temple ->
                    Card(
                        onClick = { onTemple(temple) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(13.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            V361PagodaArt(Modifier.fillMaxWidth().height(124.dp), seed = index + temple.id.length)
                            Column(Modifier.padding(10.dp)) {
                                Text(
                                    temple.nameMm, color = darkBrown, fontSize = 14.sp,
                                    maxLines = 2, fontWeight = FontWeight.Bold
                                )
                                Text(
                                    temple.nameEn, color = Color.DarkGray,
                                    fontSize = 11.sp, maxLines = 2
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            Text(
                "ပုဂံအမွေအနှစ်များကို လေ့လာကြမယ်။",
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                textAlign = TextAlign.Center,
                color = darkBrown,
                fontSize = 13.sp
            )
        }
    }
}
