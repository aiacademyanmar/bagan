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
import android.os.Bundle;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Bagan Heritage V8 — 20 exact-design scenes drawn as full HD native bitmaps.
 * Every touch is handled by Android's native View; actual data features are
 * available from the native feature menu. No permission is needed offline.
 * PNG text remains non-editable artwork. Admin frames are reserved in JSON.
 */
public final class SceneActivity extends Activity {
    private static final String[] TITLE={
        "","Splash Screen","Offline / Online","Home","Pagoda Guide","Pagoda Detail",
        "Museum","Museum Exhibition","Local Products","Product Detail","Map",
        "Offline 3D","Audio Guide","Travel Planner","History Timeline","Gallery",
        "Events & Festivals","Local Services","Visitor Safety","Favorites & Downloads","Settings & About"
    };
    private static final int GOLD=0xffd8a747;
    private final ArrayDeque<Integer> backStack=new ArrayDeque<>();
    private final LinkedHashMap<Integer, Bitmap> cache=new LinkedHashMap<Integer, Bitmap>(8,0.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Integer, Bitmap> eldest){
            if(size()<=3)return false;
            if(eldest.getValue()!=image&&!eldest.getValue().isRecycled())
                eldest.getValue().recycle();
            return true;
        }
    };
    private final RectF rect=new RectF();
    private Bitmap image;
    private int screen=1;
    private DrawView view;
    private SharedPreferences preferences;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        preferences=getSharedPreferences("bagan_scene_v8",MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        view=new DrawView();
        setContentView(view);
        int start=b!=null?b.getInt("screen",1):1;
        showScene(start,false);
    }
    @Override protected void onSaveInstanceState(Bundle b){
        b.putInt("screen",screen);super.onSaveInstanceState(b);
    }
    private Bitmap load(int id){
        Bitmap saved=cache.get(id);
        if(saved!=null&&!saved.isRecycled())return saved;
        String path=String.format(Locale.US,"scenes/scene_%02d.png",id);
        try(InputStream stream=getAssets().open(path)){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;
            Bitmap b=BitmapFactory.decodeStream(stream,null,options);
            if(b!=null)cache.put(id,b);
            return b;
        }catch(Exception e){
            Toast.makeText(this,"Missing UI scene "+id,Toast.LENGTH_LONG).show();
            return null;
        }
    }
    private void showScene(int id,boolean remember){
        if(id<1||id>20)return;
        if(remember&&id!=screen)backStack.push(screen);
        screen=id;image=load(id);view.invalidate();
    }
    private void feedback(){
        if(preferences.getBoolean("sound",true))
            view.playSoundEffect(SoundEffectConstants.CLICK);
        if(preferences.getBoolean("vibration",true))
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    private void chooseMode(boolean online){
        preferences.edit().putString("mode",online?"online":"offline").apply();
        Toast.makeText(this,online?"Online Mode — demo content, sync not connected":"Offline Mode — saved content",Toast.LENGTH_SHORT).show();
        showScene(3,true);
    }
    private void openModel(){
        try{startActivity(new Intent(this,ModelActivity.class));}
        catch(Exception e){Toast.makeText(this,"3D Viewer unavailable",Toast.LENGTH_LONG).show();}
    }
    private void nativePage(int page){
        Intent i=new Intent(this,NativeUiActivity.class);
        i.putExtra("scene_page",page);
        if(page==5)i.putExtra("scene_place",3);
        startActivity(i);
    }
    private void liveFeature(int id){
        if(id==11){openModel();return;}
        if(id==10){
            String url="https://www.google.com/maps/search/?api=1&query=Bagan%20Myanmar";
            try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));}
            catch(Exception e){Toast.makeText(this,"No Maps/browser found",Toast.LENGTH_LONG).show();}
            return;
        }
        int p;
        switch(id){
            case 4:p=4;break;
            case 5:p=5;break;
            case 6:p=17;break; // Museum core is future admin-managed; open available native About
            case 7:p=6;break;  // curated exhibit/gallery placeholder
            case 8:p=13;break; // native search catalog placeholder
            case 9:p=13;break;
            case 12:p=12;break;
            case 13:p=11;break;
            case 14:p=10;break;
            case 15:p=6;break;
            case 16:p=18;break;
            case 17:p=15;break;
            case 18:p=18;break;
            case 19:p=14;break;
            case 20:p=16;break;
            default:p=3;break;
        }
        nativePage(p);
    }
    private void openMenu(){
        final String[] list=new String[22];
        list[0]="⚙ Open this screen's live/native function";
        list[1]="◈ Open Bupaya Offline 3D Model";
        for(int i=1;i<=20;i++)
            list[i+1]=String.format(Locale.US,"%02d  %s",i,TITLE[i]);
        new AlertDialog.Builder(this)
            .setTitle("BAGAN HERITAGE · 20 Screens")
            .setItems(list,(dialog,which)->{
                feedback();
                if(which==0)liveFeature(screen);
                else if(which==1)openModel();
                else showScene(which-1,true);
            }).setNegativeButton("Close",(d,w)->{}).show();
    }
    private void settingsDialog(){
        String[] labels={"Click Sound","Haptic Feedback","Open Native Settings","All 20 Pages"};
        new AlertDialog.Builder(this).setTitle("App Interaction")
        .setItems(labels,(d,index)->{
            if(index==0){boolean on=!preferences.getBoolean("sound",true);preferences.edit().putBoolean("sound",on).apply();Toast.makeText(this,"Sound "+(on?"ON":"OFF"),Toast.LENGTH_SHORT).show();}
            if(index==1){boolean on=!preferences.getBoolean("vibration",true);preferences.edit().putBoolean("vibration",on).apply();Toast.makeText(this,"Haptic "+(on?"ON":"OFF"),Toast.LENGTH_SHORT).show();}
            if(index==2)nativePage(16);
            if(index==3)openMenu();
        }).show();
    }
    private void hit(float x,float y){
        if(screen==1){showScene(2,true);return;}
        if(screen==2){chooseMode(y<.58f);return;}
        if(y<.085f && x<.16f && screen!=3){onBackPressed();return;}
        if(screen>=3 && y>.91f){
            if(x<.2f)showScene(3,true);
            else if(x<.4f)showScene(4,true);
            else if(x<.6f)showScene(10,true);
            else if(x<.8f)showScene(19,true);
            else showScene(20,true);
            return;
        }
        switch(screen){
            case 3: // Main home has three rows of category cards.
                if(y>.32f && y<.72f){
                    int col=Math.min(2,(int)(x*3));
                    int row=Math.max(0,Math.min(2,(int)((y-.32f)/.135f)));
                    int[][] dest={{4,6,8},{10,11,14},{12,13,16}};
                    showScene(dest[row][col],true);
                } else if(y>.74f && y<.9f)showScene(4,true);
                else if(y>.18f && y<.3f)nativePage(13);
                else openMenu();
                return;
            case 4: if(y>.16f&&y<.88f)showScene(5,true);else liveFeature(4);return;
            case 5: if(y>.64f&&x>.55f)openModel();else liveFeature(5);return;
            case 6: if(y>.42f)showScene(7,true);else liveFeature(6);return;
            case 7: liveFeature(7);return;
            case 8: if(y>.16f)showScene(9,true);else liveFeature(8);return;
            case 9: liveFeature(9);return;
            case 10: liveFeature(10);return;
            case 11: openModel();return;
            case 12:case 13:case 14:case 15:case 16:case 17:case 18:
                liveFeature(screen);return;
            case 19: liveFeature(19);return;
            case 20: if(y>.25f && y<.67f)settingsDialog();else openMenu();return;
            default:openMenu();
        }
    }
    @Override public void onBackPressed(){
        if(!backStack.isEmpty()){showScene(backStack.pop(),false);return;}
        if(screen!=3){showScene(3,false);return;}
        new AlertDialog.Builder(this).setTitle("Exit Bagan Heritage?")
            .setNegativeButton("Cancel",(d,w)->{})
            .setPositiveButton("Exit",(d,w)->finish()).show();
    }
    @Override protected void onDestroy(){
        for(Bitmap b:cache.values()) if(!b.isRecycled()) b.recycle();
        cache.clear();image=null;
        super.onDestroy();
    }
    private final class DrawView extends View {
        private final Paint brush=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);
        private float xDown,yDown;
        private long timeDown;
        DrawView(){super(SceneActivity.this);setClickable(true);}
        @Override protected void onDraw(Canvas canvas){
            canvas.drawColor(0xff1a120d);
            if(image==null){
                Paint p=new Paint(3);p.setTextSize(34);p.setColor(GOLD);
                canvas.drawText("BAGAN HERITAGE",24,72,p);return;
            }
            float scale=Math.min((float)getWidth()/image.getWidth(),(float)getHeight()/image.getHeight());
            float w=image.getWidth()*scale,h=image.getHeight()*scale;
            float left=(getWidth()-w)/2,top=(getHeight()-h)/2;
            rect.set(left,top,left+w,top+h);
            canvas.drawBitmap(image,null,rect,brush);
        }
        @Override public boolean onTouchEvent(MotionEvent event){
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
                xDown=event.getX();yDown=event.getY();
                timeDown=SystemClock.uptimeMillis();return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
                float dx=event.getX()-xDown,dy=event.getY()-yDown;
                if(SystemClock.uptimeMillis()-timeDown>650 &&
                    Math.abs(dx)<24 && Math.abs(dy)<24){
                    feedback();openMenu();performClick();return true;
                }
                if(Math.abs(dx)>getResources().getDisplayMetrics().density*55 &&
                     Math.abs(dx)>Math.abs(dy)*1.3f){
                    feedback();showScene(screen+(dx<0?1:-1),true);
                    performClick();return true;
                }
                float x=(event.getX()-rect.left)/Math.max(1,rect.width());
                float y=(event.getY()-rect.top)/Math.max(1,rect.height());
                if(x>=0 && x<=1 && y>=0 && y<=1){feedback();hit(x,y);}
                performClick();return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
