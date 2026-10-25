package asia.aaii.bagan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExactUiActivity extends Activity {
  private static class Zone {
    float l,t,r,b; int target; String action;
    Zone(float l,float t,float r,float b,int target,String action){this.l=l;this.t=t;this.r=r;this.b=b;this.target=target;this.action=action;}
    boolean hit(float x,float y){return x>=l&&x<=r&&y>=t&&y<=b;}
  }
  private final List<Zone> zones = new ArrayList<>();
  private final ArrayDeque<Integer> backstack = new ArrayDeque<>();
  private int page=1;
  private PosterView view;
  private Bitmap bitmap;
  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
    view=new PosterView();setContentView(view);
    if(state!=null)page=state.getInt("p",1);
    else if(getSharedPreferences("exact",0).getBoolean("intro",false))page=3;
    open(page,false);
  }
  @Override protected void onSaveInstanceState(Bundle b){b.putInt("p",page);super.onSaveInstanceState(b);}
  @Override protected void onDestroy(){if(bitmap!=null){bitmap.recycle();bitmap=null;}super.onDestroy();}
  private void add(float l,float t,float r,float b,int target){zones.add(new Zone(l,t,r,b,target,null));}
  private void action(float l,float t,float r,float b,String name){zones.add(new Zone(l,t,r,b,0,name));}
  private void go(int target){open(target,true);}
  private void open(int num,boolean push){
    if(num<1||num>20)return;
    if(push&&page!=num)backstack.push(page);
    page=num;if(num>=3)getSharedPreferences("exact",0).edit().putBoolean("intro",true).apply();
    if(bitmap!=null){bitmap.recycle();bitmap=null;}
    try(InputStream in=getAssets().open(String.format(Locale.US,"ui/screen_%02d.png",num))){bitmap=BitmapFactory.decodeStream(in);}
    catch(Exception ex){Toast.makeText(this,"Missing Full HD PNG "+num,Toast.LENGTH_LONG).show();}
    zones.clear();buildZones();view.invalidate();
  }
  private void nav(){
    add(0,.918f,.2f,1,3);add(.2f,.918f,.4f,1,4);
    add(.4f,.918f,.6f,1,6);add(.6f,.918f,.8f,1,16);add(.8f,.918f,1,1,20);
  }
  private void back(){if(!backstack.isEmpty())open(backstack.pop(),false);else open(3,false);}
  private void addBack(){action(0,.035f,.14f,.12f,"back");}
  private void buildZones(){
    if(page>=3 && page!=5 && page!=7 && page!=8 && page!=10 && page!=11 && page!=12 && page!=14)nav();
    if(page>=4)addBack();
    switch(page){
     case 1: add(0,0,1,1,2);break;
     case 2: add(.15f,.75f,.96f,1,3);break;
     case 3:
       add(.02f,.32f,.35f,.56f,9);add(.35f,.32f,.68f,.56f,4);add(.68f,.32f,1,.56f,6);
       add(.02f,.54f,.35f,.73f,7);add(.35f,.54f,.68f,.73f,11);add(.68f,.54f,1,.73f,15);
       add(.01f,.69f,.52f,.9f,5);add(.52f,.69f,.99f,.9f,4);
       action(.85f,.01f,1,.12f,"menu");break;
     case 4:
       action(.05f,.08f,.96f,.20f,"guide");add(.01f,.20f,.98f,.71f,5);add(.72f,.71f,1,.9f,18);break;
     case 5:
       action(.0f,.78f,.52f,.96f,"map");add(.52f,.78f,1,.96f,7);add(.0f,.38f,1,.76f,12);break;
     case 6:
       action(.08f,.68f,.95f,.90f,"map");action(.18f,.12f,.88f,.28f,"map");break;
     case 7:
       action(.02f,.16f,.80f,.93f,"3d");add(.8f,.17f,1,.70f,12);action(.80f,.70f,1,.95f,"3d");break;
     case 8: action(.15f,.65f,.92f,1,"camera");break;
     case 9: action(.10f,.24f,.96f,.88f,"history");break;
     case 10: action(.10f,.17f,.95f,.90f,"tour");break;
     case 11: action(.05f,.70f,.95f,.95f,"camera");break;
     case 12: action(.02f,.15f,.98f,.92f,"3d");break;
     case 13: action(.02f,.16f,.95f,.91f,"offline");break;
     case 14: action(.08f,.35f,.96f,.94f,"audio");break;
     case 15: action(.02f,.16f,.95f,.90f,"nearby");break;
     case 16: action(.02f,.16f,.95f,.90f,"favorites");break;
     case 17: action(.04f,.15f,.95f,.9f,"settings");break;
     case 18: action(.05f,.15f,.95f,.93f,"search");break;
     case 19: action(.05f,.18f,.96f,.92f,"safety");break;
     case 20: action(.05f,.2f,.96f,.94f,"menu");break;
    }
  }
  private void act(Zone z){
    if(z.action==null){go(z.target);return;}
    switch(z.action){
     case "back":back();break;
     case "3d":startActivity(new Intent(this,OfflineModelActivity.class));break;
     case "camera":startActivity(new Intent(this,OfflineModelActivity.class).putExtra("camera",true));break;
     case "map":
       try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query=Bupaya%20Pagoda%20Bagan")));}
       catch(Exception ex){Toast.makeText(this,"Google Maps unavailable",Toast.LENGTH_SHORT).show();}
       break;
     case "menu":menu();break;
     default:openCore(z.action);break;
    }
  }
  private void openCore(String feature){
    Intent i=new Intent(this,MainActivity.class);i.putExtra("feature",feature);startActivity(i);
  }
  private void menu(){
    String[] options={"Native Guide (all functions)","Pagoda Guide 30","Google Maps","Offline Bupaya 3D","AR Camera Preview","History Timeline","Travel Planner","Audio Guide","Favorites","Search & Filters","Safety & Help"};
    new AlertDialog.Builder(this).setTitle("Bagan Heritage • More")
    .setItems(options,(d,i)->{
      switch(i){case 0:openCore("home");break;case 1:openCore("guide");break;
      case 2:go(6);break;case 3:startActivity(new Intent(this,OfflineModelActivity.class));break;
      case 4:go(11);break;case 5:go(9);break;case 6:go(10);break;
      case 7:go(14);break;case 8:go(16);break;case 9:go(18);break;case 10:go(19);break;}
    }).show();
  }
  @Override public void onBackPressed(){back();}
  private final class PosterView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final RectF dest=new RectF();
    PosterView(){super(ExactUiActivity.this);}
    @Override protected void onDraw(Canvas c){
      c.drawColor(0xff281b13);
      if(bitmap==null){paint.setColor(Color.WHITE);paint.setTextSize(32);c.drawText("Bagan Heritage — Image loading",20,80,paint);return;}
      float sc=Math.min((float)getWidth()/bitmap.getWidth(),(float)getHeight()/bitmap.getHeight());
      float w=bitmap.getWidth()*sc,h=bitmap.getHeight()*sc;
      float x=(getWidth()-w)/2,y=(getHeight()-h)/2;dest.set(x,y,x+w,y+h);
      c.drawBitmap(bitmap,null,dest,paint);
    }
    @Override public boolean onTouchEvent(MotionEvent ev){
      if(ev.getAction()!=MotionEvent.ACTION_UP)return true;
      float x=(ev.getX()-dest.left)/Math.max(1,dest.width());
      float y=(ev.getY()-dest.top)/Math.max(1,dest.height());
      if(x<0||x>1||y<0||y>1)return true;
      for(int i=zones.size()-1;i>=0;i--){Zone z=zones.get(i);if(z.hit(x,y)){performClick();act(z);return true;}}
      if(page==1)go(2);
      return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
  }
}