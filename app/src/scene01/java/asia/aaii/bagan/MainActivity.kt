package asia.aaii.bagan
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.widget.*
class MainActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setBackgroundColor(Color.rgb(3,19,34));setPadding(32,50,32,50)}
  val icon=TextView(this).apply{text="♜";textSize=78f;setTextColor(Color.rgb(255,208,88));gravity=Gravity.CENTER}
  val title=TextView(this).apply{text="BAGAN";textSize=48f;gravity=Gravity.CENTER;setTextColor(Color.rgb(255,208,88))}
  val sub=TextView(this).apply{text="HERITAGE MUSEUM\nExplore · Learn · Experience";textSize=18f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)}
  val loading=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{isIndeterminate=true}
  root.addView(icon);root.addView(title);root.addView(sub);root.addView(loading,LinearLayout.LayoutParams(-1,12).apply{topMargin=70})
  setContentView(root)
  Handler(Looper.getMainLooper()).postDelayed({if(!isFinishing){startActivity(Intent(this,HomeActivity::class.java));finish()}},2500)
 }
}