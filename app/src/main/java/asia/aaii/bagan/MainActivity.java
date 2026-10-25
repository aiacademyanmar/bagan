package asia.aaii.bagan;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private final int GOLD=Color.rgb(180,128,42);
    private final int BROWN=Color.rgb(79,49,30);
    private final int IVORY=Color.rgb(250,246,234);
    private final int MUTED=Color.rgb(115,99,81);
    private final String[] titles={"ရွှေစည်းခုံစေတီ","အလိုတော်ပြည့်ဂူဘုရား","ထီးလိုမင်းလိုဘုရား","အာနန္ဒာဘုရား","ကန်တော့ပလ္လင်ဘုရား","သဗ္ဗညုဘုရား","ဗူးဘုရား","ရွှေဆံတော်စေတီ","ဓမ္မရံကြီးဘုရား","လောကထိပ်ပန်ဘုရား","မြစေတီ","မနူဟာဘုရား","နန်းဘုရားကျောင်း","လောကနန္ဒာဘုရား","ပြဿဒါးကြီး","သကျမုနိ","ရှင်ပင်သာလျောင်း","ဂူပြောက်ကြီး","ငှက်ပစ်တောင်ဘုရား","စူဠာမဏိဘုရား","ဘုရားသုံးဆူ","သမ္ဘူလဘုရား","တရုတ်ပြေးဘုရားကြီး","အပယ်ရတနာဘုရား","ရွှေဂူကြီးဘုရား","သရပါတံခါး","မြစေတီကျောက်စာ","မြင်းကပါ","ဥပါလိသိမ်","ငှက်ပစ်တောင်"};
    private final String[] english={"Shwezigon Pagoda","Alodawpyi Pagoda","Htilominlo Temple","Ananda Temple","Gawdawpalin Temple","Thatbyinnyu Temple","Bupaya Pagoda","Shwesandaw Pagoda","Dhammayangyi Temple","Lawka Hteik Pan","Myazedi Pagoda","Manuha Temple","Nanpaya Temple","Lawkananda Pagoda","Pyathada Gyi Temple","Thakya Muni Temple","Shinbinthalyaung","Gubyaukgyi Temple","Ngat Pyit Taung Pagoda","Sulamani Temple","Three Pagodas Bagan","Thambula Temple","Tayok Pye Temple","Ape Yadana Temple","Shwegugyi Temple","Tharaba Gate","Myazedi Inscription","Myinkaba","Upali Thein","Ngat Pyit Taung"};
    private String[] pageTitles={"Welcome","Onboarding","Home","Pagoda Guide","Pagoda Detail","Google Maps","Offline 3D","AR Camera Preview","History Timeline","Travel Planner","AR Setup","3D Information","Offline Library","Audio Guide","Nearby Places","Favorites","Settings","Search & Filters","Visitor Safety","Help & About"};
    private LinearLayout main, content;
    private SharedPreferences prefs;
    private TextToSpeech tts;
    private int currentPage=1, selected=6;
    private boolean dark=false;
    private float scale=1f;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
    @Override public void onCreate(Bundle state){super.onCreate(state);prefs=getSharedPreferences("bagan",MODE_PRIVATE);dark=prefs.getBoolean("dark",false);scale=prefs.getFloat("size",1f);tts=new TextToSpeech(this,this);show(1);}
    @Override public void onInit(int result){if(result==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("my","MM"));}}
    private int bg(){return dark?Color.rgb(30,27,24):IVORY;}
    private int fg(){return dark?Color.WHITE:BROWN;}
    private GradientDrawable shape(int color,int rad){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(rad));return g;}
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size*scale);t.setTextColor(fg());t.setPadding(dp(4),dp(6),dp(4),dp(6));if(bold)t.setTypeface(null,1);return t;}
    private void addText(String s,int size,boolean bold){content.addView(text(s,size,bold));}
    private LinearLayout group(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);x.setPadding(dp(14),dp(12),dp(14),dp(12));x.setBackground(shape(dark?Color.rgb(57,49,40):Color.WHITE,16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));content.addView(x,lp);return x;}
    private void tap(LinearLayout parent,String caption,Runnable click){
        TextView b=text(caption,16,true);b.setTextColor(Color.WHITE);b.setGravity(Gravity.CENTER);b.setBackground(shape(GOLD,12));b.setPadding(dp(13),dp(13),dp(13),dp(13));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));parent.addView(b,lp);
        b.setOnClickListener(v->{v.playSoundEffect(SoundEffectConstants.CLICK);v.animate().scaleX(.97f).scaleY(.97f).setDuration(80).withEndAction(()->{v.setScaleX(1);v.setScaleY(1);click.run();}).start();});
    }
    private void link(String s,Runnable action){tap(content,s,action);}
    private void create(){main=new LinearLayout(this);main.setOrientation(1);main.setBackgroundColor(bg());setContentView(main);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(14),dp(9),dp(14),dp(9));head.setBackgroundColor(BROWN);main.addView(head);
        TextView logo=text("✦ BAGAN HERITAGE",19,true);logo.setTextColor(Color.WHITE);head.addView(logo,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView home=text("⌂",30,true);home.setTextColor(Color.WHITE);head.addView(home);home.setOnClickListener(v->show(3));
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);main.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(18),dp(20),dp(18),dp(20));sc.addView(content);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER);bar.setPadding(dp(5),dp(8),dp(5),dp(8));bar.setBackgroundColor(BROWN);main.addView(bar);
        String[] nav={"⌂\nHome","♜\nGuide","⌖\nMap","◈\n3D","☰\nMore"};int[] page={3,4,6,7,20};
        for(int i=0;i<nav.length;i++){final int p=page[i];TextView t=text(nav[i],12,false);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);bar.addView(t,new LinearLayout.LayoutParams(0,dp(48),1));t.setOnClickListener(v->show(p));}
    }
    private void show(int page){currentPage=page;create();switch(page){
      case 1:welcome();break; case 2:onboarding();break;case 3:home();break;case 4:pagodas("");break;case 5:details(selected);break;case 6:mapPage();break;case 7:threeD();break;case 8:cameraPage();break;
      case 9:history();break;case 10:tour();break;case 11:arSetup();break;case 12:modelInfo();break;case 13:offline();break;case 14:audio();break;case 15:nearby();break;case 16:favorites();break;case 17:settings();break;case 18:search();break;case 19:safety();break;case 20:about();break;
    }}
    private void banner(String title,String sub){addText(title,29,true);addText(sub,15,false);}
    private void templeArt(){Temple art=new Temple();content.addView(art,new LinearLayout.LayoutParams(-1,dp(215)));}
    private void welcome(){banner("ပုဂံအမွေအနှစ်","History • Guide • Maps • 3D • AR");templeArt();addText("Discover Bagan's timeless cultural heritage.",19,false);link("စတင်လေ့လာမည် — Get Started",()->show(2));}
    private void onboarding(){banner("Explore Ancient Bagan","Your heritage companion");templeArt();addText("ဘုရား ၃၀ ကို ရှာဖွေပါ။ သမိုင်းကိုဖတ်ပါ။ Google Maps ဖြင့်လမ်းညွှန်ပါ။",17,false);link("Continue to Home",()->show(3));}
    private void home(){banner("မင်္ဂလာပါ — ပုဂံမှ ကြိုဆိုပါတယ်","Golden heritage of Myanmar");templeArt();addText("Explore 30 historic sites and monuments",18,true);
        link("🛕 Pagoda Guide 30",()->show(4));link("📖 History Timeline",()->show(9));link("🗺 Google Maps & Directions",()->show(6));link("◈ Offline 3D — Status",()->show(7));link("📸 AR Camera Preview",()->show(11));link("🧭 Trip Planner",()->show(10));link("🔊 Audio Guide",()->show(14));link("★ Favorites",()->show(16));}
    private void pagodas(String term){banner("ပုဂံဘုရားပုထိုးများ","Heritage Places • 30");
        EditText input=new EditText(this);input.setSingleLine(true);input.setText(term);input.setTextColor(fg());input.setHintTextColor(MUTED);input.setHint("Search Pagoda / ဘုရားအမည်");content.addView(input);
        LinearLayout results=new LinearLayout(this);results.setOrientation(1);content.addView(results);
        Runnable fill=()->{results.removeAllViews();String q=input.getText().toString().trim().toLowerCase(Locale.ROOT);for(int i=0;i<titles.length;i++){if(!titles[i].contains(q)&&!english[i].toLowerCase(Locale.ROOT).contains(q))continue;final int idx=i;TextView t=text((i+1)+". "+titles[i]+"\n"+english[i],17,true);t.setBackground(shape(dark?Color.rgb(52,45,39):Color.WHITE,12));t.setPadding(dp(14),dp(12),dp(14),dp(12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));results.addView(t,lp);t.setOnClickListener(v->{selected=idx;show(5);});}};
        input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){fill.run();}public void afterTextChanged(Editable e){}});fill.run();
    }
    private void details(int i){banner(titles[i],english[i]);templeArt();addText("ပုဂံရှေးဟောင်းအမွေအနှစ် နေရာတစ်ခုဖြစ်သည်။ ဤ Screen တွင် အခြေခံလမ်းညွှန်ပါရှိပြီး စိစစ်ပြီးသမိုင်းအကြောင်းအရာ အပြည့်အစုံကို နောက် Version တွင် ဖြည့်စွက်ရန် ဖြစ်သည်။",16,false);
        link("🗺 View in Google Maps",()->openMap(i));link("🧭 Directions",()->openMap(i));
        link(prefs.getBoolean("f"+i,false)?"★ Remove Favorite":"☆ Save Favorite",()->{boolean value=!prefs.getBoolean("f"+i,false);prefs.edit().putBoolean("f"+i,value).apply();show(5);});
        if(i==6)link("◈ Bupaya 3D Viewer Status",()->show(7));link("🔊 Read Aloud",()->speak(titles[i]+" ပုဂံရှိ သမိုင်းဝင် ဘုရားပုထိုး ဖြစ်ပါသည်။"));link("All Pagodas",()->show(4));
    }
    private void openMap(int i){maps(english[i]+", Bagan, Myanmar");}
    private void maps(String q){try{Uri uri=Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(q));startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception e){Toast.makeText(this,"Maps မဖွင့်နိုင်ပါ",Toast.LENGTH_LONG).show();}}
    private void mapPage(){banner("Google Maps","Heritage Location Guide");addText("နေရာအမည်ဖြင့် Google Maps Search ဖွင့်ပေးသည်။ GPS/Place ID ကို အားလုံး မစိစစ်ရသေးပါ။",16,false);link("🗺 Open Bagan on Google Maps",()->maps("Bagan Myanmar"));link("🛕 Bupaya Pagoda",()->openMap(6));link("🛕 Ananda Temple",()->openMap(3));link("🛕 Shwezigon Pagoda",()->openMap(0));link("Choose from all 30 places",()->show(4));}
    private void threeD(){banner("ဗူးဘုရား — Offline 3D","Bupaya Model");templeArt();addText("ဤ Cloud Build တွင် မူရင်း Bupaya GLB Binary File မပါဝင်သေးပါ။ အပြည့်အစုံ Offline 3D ကို အသုံးပြုရန် Full Asset Version လိုအပ်ပါတယ်။",17,false);link("3D Architecture Information",()->show(12));link("Bupaya Details",()->{selected=6;show(5);});}
    private void cameraPage(){banner("AR Camera Preview","Camera experience • Not ARCore");addText("Camera ကို ဖွင့်ကြည့်နိုင်သော်လည်း 3D Spatial Tracking နှင့် Floor Anchoring မပါဝင်သေးပါ။",17,false);link("Open Camera",()->{try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Camera app unavailable",Toast.LENGTH_LONG).show();}});link("AR Setup",()->show(11));}
    private void history(){banner("ပုဂံသမိုင်း","Bagan History Timeline");String[][] events={{"၉ ရာစု","ပုဂံဒေသတွင် မြို့ပြယဉ်ကျေးမှု ထွန်းကားလာသောကာလ"},{"၁၀၄၄","အနော်ရထာမင်း နန်းတက်သည့်နှစ်အဖြစ် အစဉ်အလာသမိုင်းတွင် သတ်မှတ်"},{"၁၁–၁၃ ရာစု","ဘုရားပုထိုးနှင့် ဘာသာရေးအဆောက်အဦ အများအပြား တည်ဆောက်ခဲ့သောကာလ"},{"၂၀၁၉","UNESCO World Heritage List တွင် ပုဂံ စာရင်းဝင်"}};for(String[] item:events){LinearLayout c=group();c.addView(text(item[0],21,true));c.addView(text(item[1],16,false));}link("UNESCO Reference Website",()->mapsUrl("https://whc.unesco.org/en/list/1588/"));}
    private void mapsUrl(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,"Browser unavailable",Toast.LENGTH_SHORT).show();}}
    private void tour(){banner("Travel Planner","Suggested travel routes — verify local access");
        String[][] plans={{"1 Day","Shwezigon → Htilominlo → Ananda → Bupaya"},{"2 Days","Day 1: Old Bagan temples\nDay 2: Myinkaba and New Bagan"},{"3 Days","Day 1: Nyaung U\nDay 2: Old Bagan\nDay 3: Myinkaba and New Bagan"}};
        for(String[] x:plans){LinearLayout c=group();c.addView(text(x[0],22,true));c.addView(text(x[1],16,false));tap(c,"Google Maps – Bagan",()->maps("Bagan Myanmar"));}addText("သွားရောက်ခွင့်၊ ဖွင့်ချိန်နှင့် လမ်းအခြေအနေများကို သွားခါနီး စစ်ဆေးပါ။",15,false);}
    private void arSetup(){banner("AR Setup","Device compatibility");addText("ARCore Geospatial / World Tracking ပါဝင်ရန် သင့် Device တွင် ARCore Support လိုအပ်သည်။ ဤ Build တွင် Camera Launcher သာပါဝင်သည်။",17,false);link("Open Camera Preview",()->show(8));link("View Bupaya 3D Status",()->show(7));}
    private void modelInfo(){banner("3D Information","Bupaya Pagoda Model");addText("• စေတီကိုယ်ထည် — Golden stupa body\n• ထီးတော် — Hti finial\n• ပစ္စယံ — Base terraces\n• အလင်းအမှောင် — Lighting and surfaces",17,false);addText("မူရင်း Blender model ပါသော GLB ကို Cloud Build ထဲသို့ Upload လုပ်ပြီးမှ Interactive Hotspots ဖွင့်နိုင်ပါမယ်။",15,false);link("Return to 3D Viewer",()->show(7));}
    private void offline(){banner("Offline Library","Saved within application");addText("✓ 30 Pagoda names & English names\n✓ Short Bagan history\n✓ Travel planner text\n✓ Favorites settings\n✕ Original 3D GLB pending\n✕ Full HD Screen Artwork pending",17,false);link("Explore Offline Guide",()->show(4));}
    private void speak(String s){if(tts==null){Toast.makeText(this,"TTS initializing",Toast.LENGTH_SHORT).show();return;}tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"baganAudio");}
    private void audio(){banner("Audio Guide","Text-to-Speech (TTS)");addText("မြန်မာ Unicode TTS ရရှိမှုသည် ဖုန်းတွင် ထည့်ထားသော Voice Engine အပေါ်မူတည်ပါသည်။",16,false);link("▶ Bagan History",()->speak("ပုဂံသည် မြန်မာနိုင်ငံ၏ ရှေးဟောင်း ယဉ်ကျေးမှုအမွေအနှစ်ဒေသ ဖြစ်ပါသည်။"));link("▶ Bupaya Pagoda",()->speak("ဗူးဘုရားသည် ဧရာဝတီမြစ် ကမ်းနားတွင် ရှိသော ပုဂံဘုရားတစ်ဆူ ဖြစ်ပါသည်။"));link("■ Stop Audio",()->{if(tts!=null)tts.stop();});}
    private void nearby(){banner("Nearby Places","GPS-Based Nearby Guide");addText("ဒီ Version မှာ Device GPS အခြေပြု အကွာအဝေးတွက်ချက်မှု မပါဝင်သေးပါ။ Google Maps အတွင်းရှိ Nearby Search ကို ဖွင့်နိုင်ပါတယ်။",17,false);link("Search nearby temples on Google Maps",()->maps("Temples near Bagan Myanmar"));link("Explore all monuments",()->show(4));}
    private void favorites(){banner("My Favorites","Saved offline");int count=0;for(int i=0;i<titles.length;i++){if(prefs.getBoolean("f"+i,false)){final int idx=i;count++;link("★ "+titles[i],()->{selected=idx;show(5);});}}if(count==0)addText("သိမ်းထားသော နေရာ မရှိသေးပါ။ ဘုရား Detail တွင် Save Favorite နှိပ်ပါ။",16,false);link("Explore Pagodas",()->show(4));}
    private void settings(){banner("Settings","Personalization");link("Theme: "+(dark?"Dark":"Light"),()->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();show(17);});link("Font size +",()->{scale=Math.min(1.4f,scale+.1f);prefs.edit().putFloat("size",scale).apply();show(17);});link("Font size -",()->{scale=Math.max(.8f,scale-.1f);prefs.edit().putFloat("size",scale).apply();show(17);});link("Offline Content Status",()->show(13));}
    private void search(){banner("Search & Filters","Pagodas and monuments");link("Search all 30 places",()->show(4));link("View only saved places",()->show(16));link("Go to Maps",()->show(6));}
    private void safety(){banner("Visitor Safety","Preserve the Bagan Heritage");addText("• အမွေအနှစ်အဆောက်အအုံများပေါ် မတက်ပါနှင့်။\n• ပိတ်ထားသောနေရာများကို မဝင်ပါနှင့်။\n• နံရံဆေးရေးပန်းချီများကို မထိပါနှင့်။\n• ဓာတ်ပုံနှင့် ဘုရားဝတ်ပြုမှုဆိုင်ရာ စည်းကမ်းများကို လိုက်နာပါ။\n• ခရီးမသွားမီ လက်ရှိ လုံခြုံရေးနှင့် လမ်းအခြေအနေကို စစ်ဆေးပါ။",17,false);}
    private void about(){banner("Bagan Heritage • Help","Native Android Core APK v3.5");addText("Includes 20 navigable native screen modules, 30 heritage-place index, Google Maps links, saved favorites, text guide, audio via device TTS, travel planner and settings.",16,false);addText("IMPORTANT: This edition does not include the 20 original Full HD design PNG files, the Bupaya_Realistic.glb model, or true ARCore tracking. The design-exact full APK has not been compiled.",16,false);
        link("Offline Library",()->show(13));link("Visitor Safety",()->show(19));link("Settings",()->show(17));link("UNESCO Bagan Website",()->mapsUrl("https://whc.unesco.org/en/list/1588/"));}
    @Override public void onBackPressed(){if(currentPage==3)super.onBackPressed();else show(3);}
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
    private class Temple extends View {
       Paint p=new Paint(3);
       Temple(){super(MainActivity.this);}
       @Override protected void onDraw(Canvas canvas){float w=getWidth(),h=getHeight();p.setShader(new LinearGradient(0,0,0,h,0xffe9cd92,0xfffaf5e8,Shader.TileMode.CLAMP));canvas.drawRect(0,0,w,h,p);p.setShader(null);
          p.setColor(0xffd4a759);canvas.drawCircle(w*.77f,h*.24f,h*.13f,p);p.setColor(0xffac7c45);canvas.drawRect(0,h*.82f,w,h,p);
          drawTower(canvas,w*.28f,h*.82f,h*.59f);drawTower(canvas,w*.73f,h*.82f,h*.40f);
       }
       void drawTower(Canvas c,float x,float base,float height){p.setColor(0xff996c3b);float bw=height*.46f;c.drawRect(x-bw/2,base-height*.48f,x+bw/2,base,p);p.setColor(0xffc79646);Path path=new Path();path.moveTo(x-bw*.64f,base-height*.48f);path.quadTo(x-bw*.24f,base-height*.84f,x,base-height*.90f);path.quadTo(x+bw*.24f,base-height*.84f,x+bw*.64f,base-height*.48f);path.close();c.drawPath(path,p);p.setColor(0xff7e572b);c.drawRect(x-bw*.72f,base-height*.08f,x+bw*.72f,base,p);c.drawRect(x-bw*.10f,base-height*.97f,x+bw*.10f,base-height*.88f,p);}
    }
}