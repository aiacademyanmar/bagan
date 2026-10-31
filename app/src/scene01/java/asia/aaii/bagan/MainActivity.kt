package asia.aaii.bagan
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
class MainActivity:Activity(){
 private val handler=Handler(Looper.getMainLooper())
 private var navigated=false
 private val enter=Runnable{openHome()}
 private fun openHome(){if(navigated||isFinishing||isDestroyed)return;navigated=true;startActivity(Intent(this,HomeActivity::class.java));finish()}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(26,35,26,35);setBackgroundColor(Color.rgb(3,19,34))}
  val logo=ImageView(this).apply{setImageResource(R.drawable.bagan_logo);scaleType=ImageView.ScaleType.FIT_CENTER}
  root.addView(logo,LinearLayout.LayoutParams(220,220))
  fun label(v:String,s:Float,c:Int):TextView=TextView(this).apply{text=v;textSize=s;setTextColor(c);gravity=Gravity.CENTER;setPadding(4,10,4,10)}
  val gold=Color.rgb(255,207,99)
  root.addView(label("BAGAN",48f,gold).apply{setTypeface(null,Typeface.BOLD)})
  root.addView(label("HERITAGE MUSEUM",21f,gold))
  root.addView(label("EXPLORE · LEARN · EXPERIENCE",14f,Color.WHITE))
  root.addView(label("MYANMAR'S TIMELESS HERITAGE",12f,Color.WHITE))
  val bar=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{isIndeterminate=true;progressTintList=android.content.res.ColorStateList.valueOf(gold)}
  root.addView(bar,LinearLayout.LayoutParams(-1,10).apply{topMargin=60})
  root.addView(label("Opening museum…",15f,Color.WHITE))
  val skip=Button(this).apply{text="ENTER BAGAN  ›";setTextColor(Color.rgb(2,17,34));backgroundTintList=android.content.res.ColorStateList.valueOf(gold);setOnClickListener{openHome()}}
  root.addView(skip,LinearLayout.LayoutParams(-1,-2).apply{topMargin=20})
  setContentView(root)
  handler.postDelayed(enter,2200L)
 }
 override fun onDestroy(){handler.removeCallbacks(enter);super.onDestroy()}
}