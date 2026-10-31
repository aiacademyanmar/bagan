package asia.aaii.bagan
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
class HomeActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(28,28,28,28);setBackgroundColor(Color.rgb(3,19,34))}
 fun add(text:String,size:Float){root.addView(TextView(this).apply{this.text=text;textSize=size;gravity=Gravity.CENTER;setTextColor(if(size>25f)Color.rgb(255,208,88) else Color.WHITE);setPadding(4,14,4,14)})}
 add("BAGAN",42f);add("HERITAGE MUSEUM",23f);add("ပုဂံ ယဉ်ကျေးမှုပြတိုက်",21f);add("Splash Screen Completed ✓",18f);add("Home Screen is ready for next UI development.",16f)
 setContentView(root)
 }
}