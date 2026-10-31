package asia.aaii.bagan
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
class HomeActivity : Activity() {
 private val navy=Color.rgb(3,19,34)
 private val gold=Color.rgb(255,208,88)
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val scroll=ScrollView(this).apply{setBackgroundColor(navy)}
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,30,22,28)}
  scroll.addView(root)
  fun label(txt:String,size:Float,color:Int=Color.WHITE):TextView = TextView(this).apply{text=txt;textSize=size;setTextColor(color);gravity=Gravity.CENTER;textAlignment=android.view.View.TEXT_ALIGNMENT_CENTER;setPadding(6,12,6,12)}
  fun card(title:String,subtitle:String):LinearLayout {
   val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(12,20,12,20)
    background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(16,48,68),Color.rgb(3,23,36))).apply{cornerRadius=22f;setStroke(2,gold)} }
   layout.addView(label(title,20f,gold));layout.addView(label(subtitle,13f));return layout
  }
  root.addView(label("♜",72f,gold))
  root.addView(label("BAGAN",52f,gold).apply{setTypeface(null,Typeface.BOLD)})
  root.addView(label("HERITAGE MUSEUM",20f,gold))
  root.addView(label("EXPLORE  ·  DISCOVER  ·  PRESERVE",14f))
  val search=EditText(this).apply{hint="Search pagodas, history, places...";setSingleLine(true);setTextColor(Color.WHITE);setHintTextColor(Color.LTGRAY);setPadding(26,16,20,16);background=GradientDrawable().apply{setColor(Color.rgb(20,43,60));cornerRadius=38f;setStroke(2,gold)}}
  root.addView(search,LinearLayout.LayoutParams(-1,-2).apply{topMargin=26;bottomMargin=22})
  val features=listOf("Explore Bagan" to "ပုဂံကိုလေ့လာကြမယ်","History & Culture" to "သမိုင်းနှင့် ယဉ်ကျေးမှု","Gallery" to "ဓာတ်ပုံပြခန်း","3D Tours" to "3D လေ့လာမှု","Map & Guide" to "မြေပုံနှင့် လမ်းညွှန်","Highlights" to "အထူးအချက်များ")
  for(row in 0..2){
   val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
   for(col in 0..1){val (name,mm)=features[row*2+col];val v=card(name,mm)
    bar.addView(v,LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(3,5,3,5)})
    v.setOnClickListener { Toast.makeText(this,"$name – Next development",Toast.LENGTH_SHORT).show() }
   }
   root.addView(bar)
  }
  root.addView(label("BAGAN · Myanmar's Timeless Heritage",14f,gold))
  val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setBackgroundColor(Color.rgb(8,30,44))}
  listOf("Home","Explore","History","Gallery","3D","Map","More").forEach { name->
   val t=label(name,11f,if(name=="Home")gold else Color.WHITE)
   nav.addView(t,LinearLayout.LayoutParams(0,-2,1f))
   t.setOnClickListener{Toast.makeText(this,name,Toast.LENGTH_SHORT).show()}
  }
  val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  page.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  page.addView(nav)
  setContentView(page)
 }
}