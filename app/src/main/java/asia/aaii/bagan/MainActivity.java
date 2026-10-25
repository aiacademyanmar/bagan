package asia.aaii.bagan;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;
public class MainActivity extends Activity {
 final String[] names={"ရွှေစည်းခုံစေတီ","အလိုတော်ပြည့်ဂူဘုရား","ထီးလိုမင်းလိုဘုရား","အာနန္ဒာဘုရား","ကန်တော့ပလ္လင်ဘုရား","သဗ္ဗညုဘုရား","ဗူးဘုရား","ရွှေဆံတော်စေတီ","ဓမ္မရံကြီးဘုရား","လောကထိပ်ပန်ဘုရား","မြစေတီ","မနူဟာဘုရား","နန်းဘုရားကျောင်း","လောကနန္ဒာဘုရား","ပြဿဒါးကြီး","သကျမုနိ","ရှင်ပင်သာလျောင်း","ဂူပြောက်ကြီး","ငှက်ပစ်တောင်ဘုရား","စူဠာမဏိဘုရား","ဘုရားသုံးဆူ","သမ္ဘူလဘုရား","တရုတ်ပြေးဘုရားကြီး","အပယ်ရတနာဘုရား","ရွှေဂူကြီးဘုရား","သရပါတံခါး","မြစေတီကျောက်စာ","မြင်းကပါ","ဥပါလိသိမ်","ငှက်ပစ်တောင်"};
 final String[] eng={"Shwezigon Pagoda","Alodawpyi Pagoda","Htilominlo Temple","Ananda Temple","Gawdawpalin Temple","Thatbyinnyu Temple","Bupaya Pagoda","Shwesandaw Pagoda","Dhammayangyi Temple","Lawka Hteik Pan","Myazedi Pagoda","Manuha Temple","Nanpaya Temple","Lawkananda Pagoda","Pyathada Gyi Temple","Thakya Muni Temple","Shinbinthalyaung","Gubyaukgyi Temple","Ngat Pyit Taung Pagoda","Sulamani Temple","Three Pagodas Bagan","Thambula Temple","Tayok Pye Temple","Ape Yadana Temple","Shwegugyi Temple","Tharaba Gate","Myazedi inscription","Myinkaba","Upali Thein","Ngat Pyit Taung"};
 LinearLayout root; int gold=Color.rgb(191,141,48), dark=Color.rgb(35,30,24);
 @Override public void onCreate(Bundle b){super.onCreate(b); home();}
 LinearLayout page(String title){ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(25,28,25,25);root.setBackgroundColor(Color.rgb(250,247,238));scroll.addView(root);setContentView(scroll);TextView heading=new TextView(this);heading.setText(title);heading.setTextSize(24);heading.setTextColor(dark);heading.setGravity(Gravity.CENTER);heading.setPadding(0,10,0,28);root.addView(heading);return root;}
 void btn(String t,Runnable act){Button b=new Button(this);b.setText(t);b.setTextSize(15);b.setAllCaps(false);root.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->act.run());}
 void txt(String t){TextView x=new TextView(this);x.setText(t);x.setTextSize(17);x.setTextColor(dark);x.setPadding(5,12,5,15);root.addView(x);}
 void home(){page("BAGAN HERITAGE | ပုဂံအမွေအနှစ်");txt("History • Guide • Google Maps\nNative Android Build Test");btn("ဘုရားပုထိုး ၃၀ နှင့် အထင်ကရနေရာများ",()->list(""));btn("ပုဂံသမိုင်း",()->history());btn("Google Maps — Bagan",()->maps("Bagan Myanmar"));btn("About This Build",()->about());}
 void history(){page("ပုဂံသမိုင်း");txt("ပုဂံသည် မြန်မာနိုင်ငံ အလယ်ပိုင်း ဧရာဝတီမြစ်ကမ်းအနီးရှိ ရှေးဟောင်းယဉ်ကျေးမှု အမွေအနှစ်ဒေသဖြစ်သည်။ ၁၁ ရာစုမှ ၁၃ ရာစုအတွင်း ဗုဒ္ဓဘာသာ အဆောက်အဦများစွာ တည်ဆောက်ခဲ့သည်။ ၂၀၁၉ တွင် UNESCO ကမ္ဘာ့အမွေအနှစ် စာရင်းဝင်ခဲ့သည်။");btn("Home",()->home());}
 void maps(String q){try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(q)));startActivity(i);}catch(Exception e){Toast.makeText(this,"Google Maps မဖွင့်နိုင်ပါ",Toast.LENGTH_LONG).show();}}
 void list(String query){page("ပုဂံဘုရားပုထိုးများ"); EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Search / ဘုရားအမည်ရှာရန်"); search.setText(query);root.addView(search);btn("Search",()->list(search.getText().toString()));for(int k=0;k<names.length;k++){final int i=k;if(!query.isEmpty()&&!names[i].contains(query)&&!eng[i].toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;btn(names[i]+" · "+eng[i],()->details(i));}btn("Home",()->home());}
 void details(int index){page(names[index]);txt(eng[index]);txt("ပုဂံရှိ အထင်ကရ သမိုင်းဝင် အဆောက်အအုံတစ်ခုဖြစ်သည်။ အသေးစိတ် သမိုင်းအချက်အလက်များကို စိစစ်ပြီး နောက် Version တွင် ဖြည့်စွက်ရန်ဖြစ်သည်။");btn("Google Maps",()->maps(eng[index]+", Bagan Myanmar"));btn("ဘုရားစာရင်း",()->list(""));btn("Home",()->home());}
 void about(){page("Build Test | Bagan Heritage");txt("Android Debug Build: Source-only verification edition.\n\nThe 20 exact UI PNG files and offline Bupaya GLB are NOT included in this test edition.\n\nThis is not the promised exact-design APK. Final APK requires uploading full assets and recompilation.");btn("Home",()->home());}
 @Override public void onBackPressed(){home();}
}