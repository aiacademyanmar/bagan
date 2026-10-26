package asia.aaii.bagan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.*;
import java.io.*;
import java.util.*;

/**
 * Bagan Heritage — V7 real native UI/UX.
 * All screen text, buttons, cards and navigation are true editable Android Views.
 * No pre-rendered screenshot overlays and no fake touch hotspots.
 * RealActions supplies existing guide, maps, favorites, TTS and 3D bridges.
 */
public final class NativeUiActivity extends Activity {
  final int BROWN=0xff3a251d,GOLD=0xffc39442,PALE=0xfffff5df,IVORY=0xfffff9ef,
      MUTED=0xff82746a,NAVY=0xff172a36,WHITE=0xffffffff,STROKE=0xffeadbc0,AMBER=0xfff2d499;
  final String[] names={"","Splash","Welcome","Home","Pagoda Guide","Pagoda Detail","Gallery",
    "Map & Directions","Offline 3D","AR Preview","History Timeline","Travel Planner",
    "Audio Guide","Search","Favorites","Nearby Places","Settings","About Bagan",
    "Travel Tips","Help & Support","Thank You"};
  private final ArrayDeque<Integer> stack=new ArrayDeque<>();
  private JSONArray places=new JSONArray(),routes=new JSONArray();
  private JSONObject history=new JSONObject();
  private SharedPreferences prefs;
  private RealActions actions;
  private int page=1,chosen=6;
  private LinearLayout panel;
  private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  @Override public void onCreate(Bundle b){super.onCreate(b);
    getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(NAVY);
    prefs=getSharedPreferences("bagan_real_v6",MODE_PRIVATE);
    actions=new RealActions(this);
    try{places=new JSONArray(read("places.json"));}catch(Exception ignored){}
    try{routes=new JSONArray(read("routes.json"));}catch(Exception ignored){}
    try{history=new JSONObject(read("history.json"));}catch(Exception ignored){}
    if(b!=null){page=b.getInt("page",1);chosen=b.getInt("place",6);}
    render();
  }
  @Override protected void onSaveInstanceState(Bundle b){b.putInt("page",page);b.putInt("place",chosen);super.onSaveInstanceState(b);}
  @Override protected void onPause(){actions.pause();super.onPause();}
  @Override protected void onDestroy(){actions.close();super.onDestroy();}
  @Override public void onBackPressed(){if(!stack.isEmpty()){page=stack.pop();render();}else if(page>3)go(3);else super.onBackPressed();}
  private String read(String name)throws IOException{
    try(InputStream in=getAssets().open(name);ByteArrayOutputStream o=new ByteArrayOutputStream()){
      byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)o.write(buf,0,n);
      return o.toString("UTF-8");
    }
  }
  private JSONObject place(int i){return places.optJSONObject(i);}
  private String title(JSONObject o){return o==null?"Bagan":o.optString("mm",o.optString("en"));}
  private String english(JSONObject o){return o==null?"":o.optString("en");}
  private boolean saved(JSONObject o){return o!=null&&prefs.getBoolean("fav_"+o.optString("id"),false);}
  private GradientDrawable solid(int color,int radius,int stroke) {
    GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));
    if(stroke!=0)d.setStroke(dp(1),stroke);return d;
  }
  private GradientDrawable gradient(int color1,int color2,int radius){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color1,color2});
    d.setCornerRadius(dp(radius));return d;
  }
  private TextView text(String value,int sp,boolean bold,int color){
    TextView t=new TextView(this);t.setText(value);t.setTextColor(color);t.setTextSize(sp);
    if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
    t.setLineSpacing(dp(3),1.06f);return t;
  }
  private void add(LinearLayout dest,View view,int top){
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(top);dest.addView(view,lp);
  }
  private void space(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
  private TextView copy(LinearLayout l,String s,int size,boolean bold){
    TextView t=text(s,size,bold,bold?BROWN:MUTED);add(l,t,8);return t;
  }
  private TextView button(String label,int bg,int color,Runnable cb){
    TextView t=text(label,15,true,color);
    t.setGravity(Gravity.CENTER);t.setMinHeight(dp(49));t.setPadding(dp(14),dp(12),dp(14),dp(12));
    t.setBackground(solid(bg,14,0));t.setContentDescription(label);t.setFocusable(true);
    t.setOnClickListener(v->{feedback(v);cb.run();});return t;
  }
  private void action(LinearLayout l,String label,boolean primary,Runnable cb){
    add(l,button(label,primary?GOLD:0xfff0e4cc,primary?BROWN:BROWN,cb),10);
  }
  private void feedback(View v){
    if(prefs.getBoolean("haptic",true))v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    if(prefs.getBoolean("sound",true))v.playSoundEffect(SoundEffectConstants.CLICK);
    v.animate().scaleX(.97f).scaleY(.97f).setDuration(75).withEndAction(()->{
       v.animate().scaleX(1).scaleY(1).setDuration(110).start();
    }).start();
  }
  private LinearLayout card(LinearLayout into){
    LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(15),dp(15),dp(15),dp(16));
    c.setBackground(solid(WHITE,18,STROKE));c.setElevation(dp(1));
    add(into,c,12);return c;
  }
  private LinearLayout hrow(){LinearLayout r=new LinearLayout(this);r.setOrientation(0);r.setGravity(Gravity.CENTER_VERTICAL);return r;}
  private void go(int p){if(p<1||p>20)return;if(page!=p)stack.push(page);page=p;render();}
  private void choose(int index){chosen=index;JSONObject p=place(index);if(p!=null)prefs.edit().putString("last_place",p.optString("id")).apply();go(5);}
  private void externalMap(JSONObject p,boolean route){
    if(p==null)return;String q=p.optString("mapQuery",english(p)+" Bagan Myanmar");
    String u="https://www.google.com/maps/"+(route?"dir":"search")+"/?api=1&"
      +(route?"destination=":"query=")+Uri.encode(q);
    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}
    catch(Exception ex){Toast.makeText(this,"Maps/Browser unavailable",Toast.LENGTH_LONG).show();}
  }
  private void favorite(JSONObject p,Runnable rerender){
    if(p==null)return;String k="fav_"+p.optString("id");prefs.edit().putBoolean(k,!prefs.getBoolean(k,false)).apply();rerender.run();
  }
  private void hero(LinearLayout l,String title,String desc,int height,boolean dark){
    FrameLayout frame=new FrameLayout(this);
    frame.setBackground(gradient(dark?NAVY:0xffd4a35d,dark?0xff4c2e24:0xfff4dba5,21));
    frame.setClipToOutline(true);
    frame.addView(new HeritageArtwork(this),new FrameLayout.LayoutParams(-1,-1));
    LinearLayout overlay=new LinearLayout(this);overlay.setOrientation(1);overlay.setPadding(dp(18),dp(12),dp(18),dp(18));
    FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);frame.addView(overlay,op);
    TextView h=text(title,25,true,WHITE);h.setShadowLayer(4,1,2,0xff38251c);overlay.addView(h);
    TextView d=text(desc,13,false,0xfff9eedc);add(overlay,d,4);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(height));lp.topMargin=dp(8);l.addView(frame,lp);
  }
  private void navBar(LinearLayout root){
    LinearLayout bar=hrow();bar.setPadding(dp(6),dp(4),dp(6),dp(4));bar.setBackgroundColor(0xfffff3da);
    String[] labels={"⌂\nHome","▣\nGuide","⌖\nMap","♡\nSaved","☰\nMore"};
    int[] pages={3,4,7,14,19};
    for(int i=0;i<labels.length;i++){final int p=pages[i];TextView n=text(labels[i],12,p==page,BROWN);
      n.setGravity(Gravity.CENTER);n.setMinHeight(dp(51));
      bar.addView(n,new LinearLayout.LayoutParams(0,-2,1));
      n.setOnClickListener(v->{feedback(v);go(p);});
    }root.addView(bar);
  }
  private void topBar(LinearLayout root){
    LinearLayout bar=hrow();bar.setBackgroundColor(NAVY);bar.setPadding(dp(14),dp(8),dp(15),dp(8));
    TextView b=text("‹",32,true,WHITE);b.setGravity(Gravity.CENTER);b.setMinWidth(dp(38));bar.addView(b);b.setOnClickListener(v->{feedback(v);onBackPressed();});
    LinearLayout name=new LinearLayout(this);name.setOrientation(1);
    TextView brand=text("BAGAN HERITAGE",17,true,0xfff5cb72);name.addView(brand);
    TextView subtitle=text(page==3?"Explore Myanmar's heritage":names[page],12,false,WHITE);name.addView(subtitle);
    LinearLayout.LayoutParams w=new LinearLayout.LayoutParams(0,-2,1);w.leftMargin=dp(8);bar.addView(name,w);
    TextView menu=text("⋮",29,true,WHITE);menu.setGravity(Gravity.CENTER);menu.setMinWidth(dp(35));bar.addView(menu);
    menu.setOnClickListener(v->pageMenu());root.addView(bar);
  }
  private void pageMenu(){
    String[] list=new String[20];for(int i=1;i<=20;i++)list[i-1]=String.format(Locale.US,"%02d — %s",i,names[i]);
    new AlertDialog.Builder(this).setTitle("All 20 screens").setItems(list,(d,n)->go(n+1)).show();
  }
  private void render(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(IVORY);setContentView(root);
    if(page!=1&&page!=2&&page!=20)topBar(root);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(dp(16),dp(17),dp(16),dp(28));scroll.addView(panel);
    switch(page){
      case 1:splash();break;case 2:welcome();break;case 3:home();break;case 4:guide();break;
      case 5:detail();break;case 6:gallery();break;case 7:map();break;case 8:model();break;
      case 9:ar();break;case 10:history();break;case 11:planner();break;case 12:audio();break;
      case 13:search();break;case 14:favorites();break;case 15:nearby();break;
      case 16:settings();break;case 17:about();break;case 18:tips();break;
      case 19:help();break;case 20:exit();break;
    }
    if(page!=1&&page!=2&&page!=20)navBar(root);
  }
  private void section(String title,String hint){
    copy(panel,title,23,true);if(hint!=null)copy(panel,hint,13,false);
  }
  private void splash(){
    hero(panel,"BAGAN HERITAGE","Explore Ancient Myanmar",425,true);
    TextView h=copy(panel,"မြန်မာ့ပုဂံအမွေအနှစ်",24,true);h.setGravity(Gravity.CENTER);
    TextView d=copy(panel,"Ancient temples • History • Culture",15,false);d.setGravity(Gravity.CENTER);
    action(panel,"ပုဂံကို စတင်လေ့လာမည်  →",true,()->go(2));
  }
  private void welcome(){
    hero(panel,"Welcome to Bagan","Discover • Learn • Explore",360,false);
    section("ပုဂံရှေးဟောင်းအမွေအနှစ်","ဘုရားပုထိုး ၃၀၊ သမိုင်း Timeline၊ Offline 3D နှင့် Travel Planner");
    action(panel,"GET STARTED  →",true,()->go(3));action(panel,"Skip introduction",false,()->go(3));
  }
  private void home(){
    hero(panel,"Explore Bagan","A living heritage • Myanmar",195,true);
    section("Explore & Discover","Choose a destination or experience");
    quickGrid(new String[]{"♜  Pagodas","⌖  Maps","◈  3D Model","♪  Audio Guide","☼  Travel Plan","♡  Favorites"},new int[]{4,7,8,12,11,14});
    section("Featured Temple","Must-see pagodas of Bagan");
    int[] indexes={6,3,0};for(int i:indexes)siteRow(panel,i);
    action(panel,"See all 30 pagodas",false,()->go(4));
  }
  private void quickGrid(String[] names,int[] pages){
    for(int i=0;i<names.length;i+=2){
      LinearLayout r=hrow();add(panel,r,8);
      for(int j=i;j<Math.min(i+2,names.length);j++){
        final int next=pages[j];TextView t=button(names[j],(j%4<2)?0xfff2dba9:0xffe8d2a7,BROWN,()->go(next));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(83),1f);lp.setMargins(dp(4),0,dp(4),0);
        r.addView(t,lp);
      }
    }
  }
  private void siteRow(LinearLayout into,int idx){
    JSONObject p=place(idx);if(p==null)return;
    LinearLayout outer=new LinearLayout(this);outer.setOrientation(0);outer.setGravity(Gravity.CENTER_VERTICAL);
    outer.setPadding(dp(10),dp(9),dp(10),dp(9));outer.setBackground(solid(WHITE,15,STROKE));
    LinearLayout.LayoutParams ml=new LinearLayout.LayoutParams(-1,-2);ml.topMargin=dp(8);into.addView(outer,ml);
    HeritageArtwork art=new HeritageArtwork(this);
    LinearLayout.LayoutParams artP=new LinearLayout.LayoutParams(dp(72),dp(72));outer.addView(art,artP);
    LinearLayout details=new LinearLayout(this);details.setOrientation(1);details.setPadding(dp(11),0,dp(5),0);
    TextView name=text(title(p),16,true,BROWN);details.addView(name);
    TextView subtitle=text(english(p),12,false,MUTED);add(details,subtitle,5);
    outer.addView(details,new LinearLayout.LayoutParams(0,-2,1));
    TextView chevron=text("›",30,true,GOLD);outer.addView(chevron);
    outer.setOnClickListener(v->{feedback(v);choose(idx);});
  }
  private void guide(){
    section("Pagoda Guide","30 monuments • Tap any heritage place");
    searchBox(panel,false,"");
  }
  private void searchBox(LinearLayout root,boolean compact,String initial){
    EditText input=new EditText(this);input.setSingleLine(true);
    input.setTextSize(15);input.setHint("⌕  Search pagodas / ဘုရားရှာရန်");
    input.setPadding(dp(16),dp(11),dp(16),dp(11));input.setTextColor(BROWN);
    input.setHintTextColor(MUTED);input.setBackground(solid(WHITE,14,STROKE));input.setText(initial);
    add(root,input,12);
    LinearLayout results=new LinearLayout(this);results.setOrientation(1);add(root,results,8);
    Runnable update=()->{
      results.removeAllViews();String q=input.getText().toString().trim().toLowerCase(Locale.ROOT);
      int seen=0;
      for(int i=0;i<places.length();i++){
        JSONObject p=place(i);if(p==null)continue;
        String words=title(p)+" "+english(p)+" "+p.optString("type");
        if(!words.toLowerCase(Locale.ROOT).contains(q))continue;
        final int id=i;
        siteRow(results,id);seen++;
      }
      if(seen==0){TextView empty=text("No places found — စာရင်းမတွေ့ပါ",15,false,MUTED);add(results,empty,12);}
    };
    input.addTextChangedListener(new TextWatcher(){
      public void beforeTextChanged(CharSequence x,int start,int count,int after){}
      public void onTextChanged(CharSequence x,int start,int before,int count){update.run();}
      public void afterTextChanged(Editable e){}
    });update.run();
  }
  private void detail(){
    JSONObject p=place(chosen);if(p==null){section("Bupaya Pagoda","Data unavailable");return;}
    hero(panel,title(p),english(p),255,false);
    section(title(p),english(p));
    copy(panel,p.optString("description","Historical information pending review."),16,false);
    LinearLayout facts=card(panel);
    copy(facts,"Period  •  "+p.optString("period","Pending verification"),15,true);
    copy(facts,"GPS / Place ID  •  "+(p.optBoolean("gpsVerified")?"Verified":"Name-search only; not verified"),13,false);
    action(panel,"⌖  View in Google Maps",true,()->externalMap(p,true));
    action(panel,saved(p)?"♥  Remove from Favorites":"♡  Save to Favorites",false,()->favorite(p,()->render()));
    action(panel,"◈  Explore Bupaya 3D",false,()->go(8));
    action(panel,"▣  Gallery",false,()->go(6));
    action(panel,"♪  Audio Guide",false,()->go(12));
  }
  private void gallery(){
    section("Heritage Gallery","Original pictures and detailed verified imagery to be added");
    hero(panel,"Bagan Temples","Historical monuments of Myanmar",235,true);
    for(int i:new int[]{6,3,0,7}){siteRow(panel,i);}
    copy(panel,"Image placeholder cards are real native UI, not gallery photos. Please add licensed photos for a production release.",13,false);
  }
  private void map(){
    section("Location Map","Open Google Maps for online directions");
    hero(panel,"Explore Bagan","Navigation & directions",170,false);
    copy(panel,"Verified exact GPS coordinates are not available for every site. Links search by place name.",14,false);
    for(int id:new int[]{6,3,0,2,7}){
      JSONObject p=place(id);if(p==null)continue;
      LinearLayout c=card(panel);copy(c,title(p),16,true);
      action(c,"Open Directions  ↗",true,()->externalMap(p,true));
    }
    action(panel,"Browse all 30 places",false,()->go(4));
  }
  private void model(){
    section("Bupaya 3D Model","Offline GLB • Rotate / zoom");
    hero(panel,"Bupaya Pagoda","Explore a digital reconstruction",245,true);
    copy(panel,"Original GLB model loads in the offline WebGL viewer. This is a reconstruction, not an archaeological scan.",15,false);
    action(panel,"◈  OPEN INTERACTIVE 3D",true,()->actions.model());
    action(panel,"Bupaya Pagoda Details",false,()->choose(6));
  }
  private void ar(){
    section("AR Camera Preview","Experimental camera feature");
    hero(panel,"Bupaya AR","Camera preview concept",245,false);
    copy(panel,"This edition supports the device Camera application. It does NOT include tracked ARCore anchors, plane detection or spatial placement.",15,false);
    action(panel,"Open Camera / Preview",true,()->actions.camera());
    action(panel,"View Offline 3D instead",false,()->go(8));
  }
  private void history(){
    section("History Timeline","Bagan — World Heritage Site");
    copy(panel,history.optString("intro","Bagan's history and cultural heritage."),15,false);
    JSONArray es=history.optJSONArray("timeline");
    if(es!=null)for(int i=0;i<es.length();i++){
      JSONObject e=es.optJSONObject(i);if(e==null)continue;
      LinearLayout c=card(panel);
      copy(c,"● "+e.optString("era","Historic Period"),17,true);
      copy(c,e.optString("text",""),15,false);
    }
    action(panel,"UNESCO Bagan Reference  ↗",true,()->externalUrl("https://whc.unesco.org/en/list/1588/"));
  }
  private void planner(){
    section("Travel Planner","Suggested itineraries • check local access");
    for(int i=0;i<routes.length();i++){
      JSONObject route=routes.optJSONObject(i);if(route==null)continue;
      final int ix=i;LinearLayout c=card(panel);
      copy(c,route.optString("title","Heritage route"),19,true);
      copy(c,route.optString("notice",""),13,false);
      JSONArray stops=route.optJSONArray("stops");
      if(stops!=null)for(int j=0;j<stops.length();j++){
        JSONObject p=find(stops.optString(j));if(p!=null)copy(c,(j+1)+".  "+title(p),14,false);
      }
      action(c,prefs.getInt("tour",-1)==ix?"✓ Route Saved":"Save this Route",true,()->{
        prefs.edit().putInt("tour",ix).apply();Toast.makeText(this,"Travel route saved",Toast.LENGTH_SHORT).show();render();
      });
    }
  }
  private JSONObject find(String id){
    for(int i=0;i<places.length();i++){JSONObject p=place(i);if(p!=null&&id.equals(p.optString("id")))return p;}
    return null;
  }
  private void audio(){
    section("Audio Guide","Listen to Bagan's history");
    hero(panel,"Stories of Bagan","Myanmar Text-to-Speech",200,false);
    copy(panel,"Audio is generated by your phone's TTS engine. Myanmar voice availability differs by device.",14,false);
    action(panel,"▶  Play Bagan History",true,()->actions.audio());
    action(panel,"▶  Pagoda Details",false,()->actions.info(chosen));
    action(panel,"■  Stop Audio",false,()->actions.pause());
  }
  private void search(){section("Search & Discover","Search by Burmese or English name");searchBox(panel,false,"");}
  private void favorites(){
    section("My Favorites","Saved heritage places on this phone");
    int count=0;for(int i=0;i<places.length();i++){
      JSONObject p=place(i);if(saved(p)){siteRow(panel,i);count++;}
    }
    if(count==0)copy(panel,"No favorites yet. Save a pagoda in the Detail screen.",15,false);
    action(panel,"Explore Pagodas",true,()->go(4));
  }
  private void nearby(){
    section("Nearby Places","Find nearby historical monuments");
    copy(panel,"No unverified GPS distance values are shown. Open Google Maps for nearby results based on your device location and permissions.",15,false);
    for(int i:new int[]{6,3,0,2})siteRow(panel,i);
    action(panel,"⌖  Open Nearby Places in Maps",true,()->externalUrl("https://www.google.com/maps/search/?api=1&query=Temples%20near%20Bagan%20Myanmar"));
  }
  private void settings(){
    section("App Settings","Personalize sound and feedback");
    LinearLayout c=card(panel);
    sw(c,"Click Sound", "sound",true);
    sw(c,"Haptic Feedback","haptic",true);
    copy(c,"All typography and controls are native Android views. Theme follows the Bagan gold/ivory design language.",13,false);
    action(panel,"Show recently viewed pagoda",false,()->actions.recent());
    action(panel,"Favorites Manager",false,()->actions.settings());
  }
  private void sw(LinearLayout into,String label,String key,boolean def){
    Switch s=new Switch(this);s.setText(label);s.setTextColor(BROWN);s.setTextSize(16);
    s.setPadding(dp(5),dp(10),dp(5),dp(10));s.setChecked(prefs.getBoolean(key,def));
    s.setOnCheckedChangeListener((b,on)->prefs.edit().putBoolean(key,on).apply());add(into,s,8);
  }
  private void about(){
    section("About Bagan","UNESCO World Heritage");
    hero(panel,"Bagan Heritage","Preserve our cultural heritage",225,false);
    copy(panel,"Bagan is a remarkable archaeological landscape along the Ayeyarwady River in Myanmar. It was inscribed on UNESCO's World Heritage List in 2019.",16,false);
    action(panel,"Learn More • UNESCO",true,()->externalUrl("https://whc.unesco.org/en/list/1588/"));
    action(panel,"Heritage History",false,()->go(10));
  }
  private void tips(){
    section("Travel Tips","Respect temples, local communities and safety");
    String[][] tips={
      {"Best time to visit","Seasonal weather can be hot. Check forecasts, site hours and local access."},
      {"What to bring","Water, sun protection, comfortable footwear and appropriate temple clothing."},
      {"Preserve heritage","Do not climb monuments or touch historic artwork. Observe signs."},
      {"Respect local culture","Dress modestly at religious sites and follow local customs."},
      {"Travel safety","Check current security and road conditions before any trip."}
    };
    for(String[] item:tips){LinearLayout c=card(panel);copy(c,item[0],17,true);copy(c,item[1],14,false);}
  }
  private void help(){
    section("Help & Support","Bagan Heritage Android App");
    action(panel,"Pagoda Guide",false,()->go(4));
    action(panel,"Offline 3D Troubleshooting",false,()->{
      new AlertDialog.Builder(this).setTitle("3D Viewer Help")
      .setMessage("Offline GLB requires a WebGL-compatible Android System WebView. The downloaded model is a digital reconstruction. Try updating Android System WebView if rendering fails.")
      .setPositiveButton("OK",(d,w)->{}).show();
    });
    action(panel,"About Bagan",false,()->go(17));
    action(panel,"Settings",false,()->go(16));
    action(panel,"Exit Screen",true,()->go(20));
  }
  private void exit(){
    hero(panel,"Thank You","for Exploring Bagan Heritage",410,true);
    TextView h=copy(panel,"Preserve our heritage for future generations.",18,true);h.setGravity(Gravity.CENTER);
    action(panel,"Return to Home",true,()->go(3));
    action(panel,"Exit App",false,()->new AlertDialog.Builder(this)
      .setTitle("Exit Bagan Heritage?")
      .setNegativeButton("Cancel",(d,w)->{})
      .setPositiveButton("Exit",(d,w)->finishAffinity()).show());
  }
  private void externalUrl(String url){
    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
    catch(Exception e){Toast.makeText(this,"No browser or maps app available",Toast.LENGTH_SHORT).show();}
  }
  private static class HeritageArtwork extends View {
    private final Paint p=new Paint(3);
    HeritageArtwork(Activity a){super(a);}
    @Override protected void onDraw(Canvas c){
      float w=getWidth(),h=getHeight();
      p.setShader(new LinearGradient(0,0,w,h,0xff213946,0xffad7040,Shader.TileMode.CLAMP));
      c.drawRect(0,0,w,h,p);p.setShader(null);
      p.setColor(0xffedb761);c.drawCircle(w*.76f,h*.27f,h*.14f,p);
      p.setColor(0x99703727);c.drawOval(-w*.2f,h*.73f,w*1.2f,h*1.12f,p);
      drawTemple(c,w*.32f,h*.87f,h*.61f);
      drawTemple(c,w*.79f,h*.90f,h*.37f);
      p.setColor(0xff765038);
      c.drawRect(0,h*.88f,w,h,p);
    }
    private void drawTemple(Canvas c,float x,float y,float height){
      float bw=height*.49f;
      p.setColor(0xff633e2c);c.drawRect(x-bw*.65f,y-height*.40f,x+bw*.65f,y,p);
      p.setColor(0xffdfa75b);
      Path roof=new Path();roof.moveTo(x-bw*.7f,y-height*.4f);
      roof.quadTo(x-bw*.26f,y-height*.87f,x,y-height*.97f);
      roof.quadTo(x+bw*.26f,y-height*.87f,x+bw*.7f,y-height*.4f);
      roof.close();c.drawPath(roof,p);
      p.setColor(0xff412c26);c.drawRect(x-bw*.82f,y-height*.08f,x+bw*.82f,y,p);
      c.drawRect(x-bw*.13f,y-height*1.04f,x+bw*.13f,y-height*.92f,p);
      p.setColor(0xfff3ce86);c.drawRect(x-bw*.09f,y-height*.17f,x+bw*.09f,y-height*.07f,p);
    }
  }
}
