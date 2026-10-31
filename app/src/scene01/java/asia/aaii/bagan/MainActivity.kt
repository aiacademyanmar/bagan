package asia.aaii.bagan
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  @Suppress("DEPRECATION")
  window.decorView.systemUiVisibility =
   View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
   View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
   View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
  val root = FrameLayout(this)
  root.setBackgroundColor(Color.rgb(2,17,34))
  val splash = ImageView(this)
  splash.setImageResource(R.drawable.bagan_scene01)
  splash.scaleType = ImageView.ScaleType.CENTER_CROP
  root.addView(splash, FrameLayout.LayoutParams(-1, -1))
  setContentView(root)
 }
}