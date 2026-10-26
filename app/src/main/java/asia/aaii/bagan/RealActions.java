package asia.aaii.bagan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.SoundEffectConstants;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public final class RealActions {
  private static final int GOLD=0xffba8734, BROWN=0xff442c1c, IVORY=0xfffff7e7;
  private final Activity context;
  private final SharedPreferences memory;
  private JSONArray places = new JSONArray();
  private JSONArray routes = new JSONArray();
  private JSONObject history = new JSONObject();
  private TextToSpeech tts;
  public RealActions(Activity a){
    context=a;memory=a.getSharedPreferences("bagan_real_v6",0);
    try{places=new JSONArray(read("places.json"));}catch(Exception e){}
    try{routes=new JSONArray(read("routes.json"));}catch(Exception e){}
    try{history=new JSONObject(read("history.json"));}catch(Exception e){}
  }
  private String read(String name)throws IOException{
    try(InputStream in=context.getAssets().open(name);ByteArrayOutputStream b=new ByteArrayOutputStream()){
      byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)b.write(buf,0,n);
      return b.toString("UTF-8");
    }
  }
  private LinearLayout body(){
    LinearLayout l=new LinearLayout(context);
    l.setOrientation(LinearLayout.VERTICAL);l.setPadding(20,8,20,12);
    return l;
  }
  private TextView text(String content,int size){
    TextView t=new TextView(context);t.setText(content);t.setTextSize(size);
    t.setTextColor(BROWN);t.setPadding(10,13,10,13);return t;
  }
  private void label(LinearLayout l,String s){l.addView(text(s,15));}
  private void button(LinearLayout l,String caption,Runnable run){
    Button b=new Button(context);b.setAllCaps(false);b.setText(caption);
    b.setTextColor(BROWN);b.setTextSize(15);l.addView(b);
    b.setOnClickListener(v->{if(memory.getBoolean("sound",true))v.playSoundEffect(SoundEffectConstants.CLICK);run.run();});
  }
  private void dialog(String title,LinearLayout l){
    ScrollView sc=new ScrollView(context);sc.addView(l);
    AlertDialog dlg=new AlertDialog.Builder(context).setTitle(title).setView(sc)
      .setNegativeButton("ပိတ်မည် / Close",(d,w)->{}).create();
    dlg.show();dlg.getWindow().setLayout(-1,(int)(context.getResources().getDisplayMetrics().heightPixels*.82));
  }
  private JSONObject pagoda(int i){return places.optJSONObject(i);}
  private String name(JSONObject o){return o==null?"":o.optString("mm")+" — "+o.optString("en");}
  private String description(JSONObject o){return o.optString("description","အသေးစိတ်သမိုင်းကို စိစစ်နေဆဲဖြစ်သည်။");}
  private boolean saved(JSONObject o){return memory.getBoolean("fav_"+o.optString("id"),false);}
  private void toggle(JSONObject o){
    String key="fav_"+o.optString("id");memory.edit().putBoolean(key,!memory.getBoolean(key,false)).apply();
    Toast.makeText(context,memory.getBoolean(key,false)?"Favorites သိမ်းပြီး":"Favorites မှ ဖယ်ရှားပြီး",Toast.LENGTH_SHORT).show();
  }
  private void maps(String q,boolean directions){
    String type=directions?"dir":"search";
    String u="https://www.google.com/maps/"+type+"/?api=1&"+(directions?"destination=":"query=")+Uri.encode(q);
    try{context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}
    catch(Exception ex){Toast.makeText(context,"Maps/Browser မဖွင့်နိုင်ပါ",Toast.LENGTH_LONG).show();}
  }
  public void info(int i){
    JSONObject p=pagoda(i);if(p==null)return;
    LinearLayout l=body();label(l,name(p));
    label(l,description(p));label(l,"ခေတ် / Period: "+p.optString("period","မသေချာသေး"));
    label(l,"GPS / Place ID: "+(p.optBoolean("gpsVerified")?"Verified":"မစစ်ဆေးရသေး (Map name-search)"));
    button(l,"🗺 Google Maps / Directions",()->maps(p.optString("mapQuery",p.optString("en")+" Bagan"),true));
    button(l,saved(p)?"★ Favorite ဖယ်ရန်":"☆ Favorite သိမ်းရန်",()->toggle(p));
    button(l,"🔊 သမိုင်းကို အသံဖတ်မည်",()->speak(p.optString("mm")+". "+description(p)));
    if(p.optBoolean("has3d")||p.optString("id").equals("bupaya"))
      button(l,"◈ ဗူးဘုရား Offline 3D",()->model());
    dialog(name(p),l);
  }
  public void guide(String initial){
    LinearLayout l=body();EditText query=new EditText(context);query.setHint("ဘုရားအမည် / English search");query.setSingleLine(true);query.setText(initial==null?"":initial);l.addView(query);
    LinearLayout result=body();l.addView(result);
    Runnable fill=()->{
      result.removeAllViews();String q=query.getText().toString().trim().toLowerCase(Locale.ROOT);
      int shown=0;
      for(int i=0;i<places.length();i++){
        JSONObject p=pagoda(i);
        if(p==null)continue;
        if(!(name(p).toLowerCase(Locale.ROOT).contains(q)||p.optString("type").toLowerCase(Locale.ROOT).contains(q)))continue;
        final int idx=i;shown++;
        button(result,(i+1)+". "+name(p),()->info(idx));
      }
      if(shown==0)label(result,"ရလဒ်မတွေ့ပါ");
    };
    query.addTextChangedListener(new android.text.TextWatcher(){
      public void beforeTextChanged(CharSequence s,int st,int count,int after){}
      public void onTextChanged(CharSequence s,int st,int before,int count){fill.run();}
      public void afterTextChanged(android.text.Editable x){}
    });
    fill.run();dialog("Pagoda Guide • 30 Places",l);
  }
  private void speak(String content){
    if(tts==null){
      tts=new TextToSpeech(context,status->{
        if(status!=TextToSpeech.SUCCESS)return;
        int result=tts.setLanguage(new Locale("my","MM"));
        if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED){
          Toast.makeText(context,"Myanmar TTS Voice မရှိပါ။ Device Voice Data တပ်ဆင်ပါ",Toast.LENGTH_LONG).show();
        }else tts.speak(content,TextToSpeech.QUEUE_FLUSH,null,"bagan_voice");
      });
    }else{tts.speak(content,TextToSpeech.QUEUE_FLUSH,null,"bagan_voice");}
  }
  public void audio(){
    LinearLayout l=body();label(l,"Device TTS Voice Engine ကို အသုံးပြုပါတယ်။ မြန်မာအသံ Support ရှိဖို့ လိုပါတယ်။");
    button(l,"▶ ပုဂံသမိုင်း",()->speak(history.optString("intro","ပုဂံသမိုင်း")));
    button(l,"▶ ဗူးဘုရားအကြောင်း",()->{JSONObject p=find("bupaya");if(p!=null)speak(description(p));});
    button(l,"■ Stop",()->{if(tts!=null)tts.stop();});
    dialog("Audio Guide",l);
  }
  private JSONObject find(String id){for(int i=0;i<places.length();i++){JSONObject p=pagoda(i);if(p!=null&&id.equals(p.optString("id")))return p;}return null;}
  public void favorites(){
    LinearLayout l=body();int found=0;
    for(int i=0;i<places.length();i++){JSONObject p=pagoda(i);if(p!=null&&saved(p)){int idx=i;found++;button(l,"★ "+name(p),()->info(idx));}}
    if(found==0)label(l,"Favorites မရှိသေးပါ။ Pagoda Guide ထဲက Favorite ခလုတ်ကို သုံးပါ။");
    button(l,"ဘုရား ၃၀ ရှာဖွေမည်",()->guide(""));dialog("Saved Favorites",l);
  }
  public void history(){
    LinearLayout l=body();label(l,history.optString("intro","ပုဂံသမိုင်း"));
    JSONArray entries=history.optJSONArray("timeline");
    if(entries!=null)for(int i=0;i<entries.length();i++){
      JSONObject e=entries.optJSONObject(i);if(e!=null)label(l,e.toString());
    }
    button(l,"🔊 နားထောင်မည်",()->speak(history.optString("intro")));
    button(l,"UNESCO Bagan Reference",()->openUrl("https://whc.unesco.org/en/list/1588/"));
    dialog("ပုဂံသမိုင်း Timeline",l);
  }
  public void planner(){
    LinearLayout l=body();label(l,"Travel plans are suggested only. Check safety, road access and site rules before travelling.");
    for(int i=0;i<routes.length();i++){
      JSONObject r=routes.optJSONObject(i);if(r==null)continue;
      String title=r.optString("title");
      JSONArray stops=r.optJSONArray("stops");
      StringBuilder text=new StringBuilder();
      if(stops!=null)for(int j=0;j<stops.length();j++){
        JSONObject p=find(stops.optString(j));
        text.append((j+1)+". "+(p==null?stops.optString(j):name(p))+"\n");
      }
      label(l,title+"\n"+text);
      final int idx=i;
      button(l,memory.getInt("tour",-1)==idx?"✓ Selected":"Save this Route",()->{
        memory.edit().putInt("tour",idx).apply();
        Toast.makeText(context,"Travel plan saved offline",Toast.LENGTH_SHORT).show();
      });
    }
    dialog("Travel Planner • 1–3 Days",l);
  }
  public void settings(){
    LinearLayout l=body();
    Switch click=new Switch(context);click.setText("Click sound / နှိပ်သံ");click.setChecked(memory.getBoolean("sound",true));
    click.setOnCheckedChangeListener((v,x)->memory.edit().putBoolean("sound",x).apply());l.addView(click);
    Switch haptic=new Switch(context);haptic.setText("Haptic feedback");haptic.setChecked(memory.getBoolean("haptic",true));
    haptic.setOnCheckedChangeListener((v,x)->memory.edit().putBoolean("haptic",x).apply());l.addView(haptic);
    label(l,"Visual Artwork: မူရင်း ၂၀ PNG ကို မပြင်ထားပါ။ Language/Theme ပြောင်းလဲမှုသည် ရှိပြီးသား PNG စာသားကို မပြောင်းနိုင်ပါ။");
    dialog("Settings",l);
  }
  public void offline(){
    LinearLayout l=body();
    label(l,"✓ Full HD UI/UX Screens 20\n✓ Pagoda guide 30 places\n✓ History & 1–3 day routes\n✓ Bupaya GLB Model\n✓ Offline WebGL 3D Viewer\n✓ Favorites / Settings");
    label(l,"Online needed: Google Maps Search / Directions. ARCore spatial tracking not implemented.");
    button(l,"Bupaya Offline 3D ဖွင့်မည်",()->model());
    dialog("Offline Library",l);
  }
  public void model(){
    context.startActivity(new Intent(context,ModelActivity.class));
  }
  public void camera(){
    LinearLayout l=body();
    label(l,"ကင်မရာကို ဖွင့်နိုင်ပါတယ်။ True ARCore Spatial Tracking, Plane Detection နှင့် Anchoring မပါဝင်သေးပါ။");
    button(l,"Open Camera App",()->{
      try{context.startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}
      catch(Exception ex){Toast.makeText(context,"Camera app unavailable",Toast.LENGTH_LONG).show();}
    });
    button(l,"Offline 3D Model ကို ကြည့်မည်",()->model());
    dialog("AR Camera Preview (not ARCore)",l);
  }
  public void nearby(){maps("temples near Bagan Myanmar",false);}
  public void search(){guide("");}
  public void safety(){
    LinearLayout l=body();label(l,"ဘုရားပုထိုးများပေါ် မတက်ပါနှင့်။\nရှေးဟောင်းအဆောက်အအုံနှင့် နံရံဆေးရေးပန်းချီများကို မထိပါနှင့်။\nပိတ်ထားသော နေရာများသို့ မဝင်ပါနှင့်။\nဝတ်စားဆင်ယင်မှုနှင့် ဒေသခံလမ်းညွှန် စည်းကမ်းများ လိုက်နာပါ။\nမသွားမီ လမ်းအခြေအနေ/လုံခြုံရေးစစ်ဆေးပါ။");
    dialog("Visitor Safety",l);
  }
  public void help(){
    LinearLayout l=body();label(l,"Bagan Heritage Real-Systems Edition\n20 Original Full HD Visual Pages + Functional Dialogs");
    button(l,"Offline Library",()->offline());button(l,"Settings",()->settings());
    button(l,"UNESCO Bagan",()->openUrl("https://whc.unesco.org/en/list/1588/"));
    dialog("Help & About",l);
  }
  public void mapOptions(){
    LinearLayout l=body();button(l,"Bupaya Pagoda",()->maps("Bupaya Pagoda Old Bagan Myanmar",true));
    button(l,"Ananda Temple",()->maps("Ananda Temple Bagan Myanmar",true));
    button(l,"Shwezigon Pagoda",()->maps("Shwezigon Pagoda Nyaung U Myanmar",true));
    button(l,"Choose from 30 places",()->guide(""));dialog("Google Maps • Online Directions",l);
  }
  private void openUrl(String uri){try{context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(uri)));}catch(Exception e){}}
  public void run(int page){
    switch(page){
      case 4:guide("");break;case 5:info(6);break;case 6:mapOptions();break;
      case 7:case 12:model();break;case 8:case 11:camera();break;
      case 9:history();break;case 10:planner();break;case 13:offline();break;
      case 14:audio();break;case 15:nearby();break;case 16:favorites();break;
      case 17:settings();break;case 18:search();break;case 19:safety();break;
      case 20:help();break;
    }
  }
  public void close(){if(tts!=null){tts.stop();tts.shutdown();tts=null;}}
}