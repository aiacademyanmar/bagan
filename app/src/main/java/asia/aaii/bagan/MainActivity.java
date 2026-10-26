package asia.aaii.bagan;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.app.AlertDialog;
import android.util.LruCache;
import android.view.View;
import android.view.WindowManager;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BAGAN HERITAGE — 20 PAGE VISUAL ONLY EDITION.
 *
 * Uses the original full-HD screenshot bitmaps without redrawing them.
 * Swipe left/right, hardware Back and on-image tap navigation are the
 * only interactions. This build intentionally has NO map, camera,
 * AR, WebView, GLB model, TTS, data sync or external URL functionality.
 *
 * Full-HD PNG assets are packaged as assets/ui/screen_01.png .. screen_20.png.
 */
public class MainActivity extends Activity {
    private final ArrayDeque<Integer> visited = new ArrayDeque<>();
    private final ArrayList<Hit> hits = new ArrayList<>();
    private int current = 1;
    private Bitmap image;
    private final LruCache<Integer, Bitmap> bitmapCache = new LruCache<Integer, Bitmap>(32 * 1024 * 1024) {
        @Override protected int sizeOf(Integer k, Bitmap v) { return v.getByteCount(); }
        @Override protected void entryRemoved(boolean evicted, Integer k, Bitmap old, Bitmap replacement) {
            if (old != image && old != replacement && !old.isRecycled()) old.recycle();
        }
    };
    private ScreenView screen;
    private RealActions real;

    private static class Hit {
        final float x1,y1,x2,y2; final int target;
        Hit(float x1,float y1,float x2,float y2,int target) {
            this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;this.target=target;
        }
        boolean matches(float x,float y){return x>=x1&&x<=x2&&y>=y1&&y<=y2;}
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        // Preserve battery life; no forced keep-awake during heritage browsing.
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        real=new RealActions(this);
        screen = new ScreenView();
        setContentView(screen);
        int start=saved==null?getSharedPreferences("bagan_ui_v62",MODE_PRIVATE).getInt("last_page",1):saved.getInt("page",1);
        open(start,false);
    }
    @Override protected void onSaveInstanceState(Bundle b) {
        b.putInt("page",current);super.onSaveInstanceState(b);
    }
    @Override protected void onDestroy() {
        image=null;
        bitmapCache.evictAll();
        if(real!=null)real.close();
        super.onDestroy();
    }
    @Override protected void onPause() {
        if(real!=null)real.pause();
        super.onPause();
    }
    @Override public void onBackPressed() {
        if(!visited.isEmpty())open(visited.pop(),false);
        else if(current!=3)open(3,false);
        else super.onBackPressed();
    }
    private void open(int page,boolean remember) {
        if(page<1||page>20||page==current&&image!=null)return;
        if(remember&&current!=page)visited.push(current);
        current=page;
        getSharedPreferences("bagan_ui_v62",MODE_PRIVATE).edit().putInt("last_page",page).apply();
        image=bitmapCache.get(page);
        if(image==null || image.isRecycled()){
            BitmapFactory.Options opt=new BitmapFactory.Options();
            // ARGB_8888 retains the original Full-HD PNG gradient/color fidelity.
            opt.inPreferredConfig=Bitmap.Config.ARGB_8888;
            try(InputStream stream=getAssets().open(
                String.format(Locale.US,"ui/screen_%02d.png",page))) {
                image=BitmapFactory.decodeStream(stream,null,opt);
                if(image!=null) bitmapCache.put(page,image);
            }catch(OutOfMemoryError oom) {
                bitmapCache.evictAll();
                opt.inPreferredConfig=Bitmap.Config.RGB_565;
                try(InputStream stream=getAssets().open(
                    String.format(Locale.US,"ui/screen_%02d.png",page))) {
                    image=BitmapFactory.decodeStream(stream,null,opt);
                    if(image!=null) bitmapCache.put(page,image);
                }catch(Exception ignored){image=null;}
            }catch(Exception ignored){image=null;}
        }
        installZones();
        screen.invalidate();
    }
    private void link(float l,float t,float r,float b,int p){
        hits.add(new Hit(l,t,r,b,p));
    }
    private void backButton(){link(0f,.02f,.17f,.12f,Math.max(3,current-1));}
    private void bottom(){
        link(0,.914f,.2f,1f,3);
        link(.2f,.914f,.4f,1f,4);
        link(.4f,.914f,.6f,1f,6);
        link(.6f,.914f,.8f,1f,16);
        link(.8f,.914f,1f,1f,20);
    }
    private void installZones() {
        hits.clear();
        // Swipe navigation works on every one of the 20 screens.
        if(current>=3)backButton();
        switch(current){
            case 1:
                link(0,.45f,1f,1f,2);
                break;
            case 2:
                link(0,.70f,1f,1f,3);
                break;
            case 3:
                bottom();
                link(.0f,.25f,.35f,.51f,9);   // History
                link(.35f,.25f,.67f,.51f,4);  // Pagodas
                link(.67f,.25f,1f,.51f,6);   // Map
                link(.0f,.51f,.35f,.71f,7);   // 3D presentation page
                link(.35f,.51f,.67f,.71f,8);  // AR presentation page
                link(.67f,.51f,1f,.71f,15);  // Nearby
                link(.0f,.72f,.51f,.9f,5);    // Feature card
                link(.51f,.72f,1f,.9f,4);
                break;
            case 4:
                bottom();
                link(.04f,.18f,.96f,.84f,4);  // open actual searchable guide, not a hardcoded temple
                link(.1f,.1f,.98f,.18f,18);
                break;
            case 5:
                link(.02f,.40f,.32f,.49f,9); // History tab
                link(.35f,.40f,.65f,.49f,13); // Gallery page mockup
                link(.66f,.40f,.84f,.49f,7); // 3D page mockup
                link(.84f,.40f,1f,.49f,6);   // Map page mockup
                link(.01f,.78f,.50f,.99f,6);
                link(.50f,.78f,1f,.99f,7);
                break;
            case 6:
                link(.07f,.68f,.98f,.94f,5);
                break;
            case 7:
                link(.72f,.16f,1f,.66f,12);
                link(.0f,.84f,.50f,1f,12);
                link(.50f,.84f,1f,1f,8);
                break;
            case 8:
                link(.0f,.82f,.50f,1f,7);
                link(.50f,.82f,1f,1f,11);
                break;
            case 9:
                bottom();break;
            case 10:
                link(.05f,.83f,.96f,1f,15);
                break;
            case 11:
                link(.05f,.78f,.96f,1f,8);
                break;
            case 12:
                link(0,.20f,.99f,.93f,7);
                break;
            case 13:
                link(.1f,.20f,.96f,.49f,5);
                link(.1f,.49f,.96f,.76f,14);
                bottom();
                break;
            case 14:
                link(0,.82f,1f,1f,13);
                break;
            case 15:
                link(.05f,.19f,.96f,.9f,5);
                bottom();
                break;
            case 16:
                link(.05f,.19f,.96f,.85f,5);
                bottom();
                break;
            case 17:
                bottom();break;
            case 18:
                link(.1f,.80f,.97f,.93f,4);
                bottom();
                break;
            case 19:
                bottom();break;
            case 20:
                link(.02f,.22f,.96f,.38f,19);
                link(.02f,.38f,.96f,.52f,9);
                link(.02f,.52f,.96f,.70f,17);
                bottom();
                break;
        }
    }

    private void quickMenu() {
        final String[] pageLabels = {
            "01 — Welcome", "02 — Onboarding", "03 — Home", "04 — Pagoda Guide",
            "05 — Bupaya Detail", "06 — Google Maps", "07 — Offline 3D",
            "08 — AR Preview", "09 — History Timeline", "10 — Travel Planner",
            "11 — AR Setup", "12 — 3D Information", "13 — Offline Library",
            "14 — Audio Guide", "15 — Nearby Places", "16 — Favorites",
            "17 — Settings", "18 — Search", "19 — Visitor Safety", "20 — About"
        };
        final String[] entries = new String[21];
        entries[0] = "▶ ဒီစာမျက်နှာ၏ Function ဖွင့်ရန်";
        System.arraycopy(pageLabels,0,entries,1,20);
        new AlertDialog.Builder(this).setTitle("BAGAN • Pages & Features")
          .setItems(entries,(d,idx)->{
              if(idx==0){activate(current);return;}
              int next=idx;
              open(next,true);
          })
          .setNegativeButton("Cancel",(d,w)->{})
          .show();
    }
    private void pressFeedback() {
        if(real==null) return;
        android.content.SharedPreferences p = getSharedPreferences("bagan_real_v6",MODE_PRIVATE);
        if(p.getBoolean("sound",true)) screen.playSoundEffect(SoundEffectConstants.CLICK);
        if(p.getBoolean("haptic",true)) screen.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }
    private void activate(int page){
       if(real!=null&&page>=4) real.run(page);
    }
    private class ScreenView extends View {
        private final Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        private final RectF bounds=new RectF();
        private float touchX,touchY;
        private long touchAt;
        ScreenView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xff21150e);
            if(image==null)return;
            // Exact aspect ratio; never stretch, recolor, resample UI elements independently.
            float scale=Math.min((float)getWidth()/image.getWidth(),
                                 (float)getHeight()/image.getHeight());
            float w=image.getWidth()*scale,h=image.getHeight()*scale;
            float left=(getWidth()-w)/2f,top=(getHeight()-h)/2f;
            bounds.set(left,top,left+w,top+h);
            canvas.drawBitmap(image,null,bounds,brush);
        }
        @Override public boolean onTouchEvent(MotionEvent ev) {
            switch(ev.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    touchX=ev.getX();touchY=ev.getY();touchAt=android.os.SystemClock.uptimeMillis();return true;
                case MotionEvent.ACTION_UP:
                    float dx=ev.getX()-touchX;
                    float dy=ev.getY()-touchY;
                    long duration=android.os.SystemClock.uptimeMillis()-touchAt;
                    if(duration>620 && Math.abs(dx)<getResources().getDisplayMetrics().density*18 && Math.abs(dy)<getResources().getDisplayMetrics().density*18){
                        pressFeedback();quickMenu();performClick();return true;
                    }
                    if(Math.abs(dx)>getResources().getDisplayMetrics().density*45
                       &&Math.abs(dx)>Math.abs(dy)*1.25f) {
                        int next=current+(dx<0?1:-1);
                        if(next>=1&&next<=20){pressFeedback();open(next,true);}
                        performClick();return true;
                    }
                    float x=(ev.getX()-bounds.left)/Math.max(1,bounds.width());
                    float y=(ev.getY()-bounds.top)/Math.max(1,bounds.height());
                    if(x>=0&&x<=1&&y>=0&&y<=1){
                        for(int i=hits.size()-1;i>=0;i--){
                            Hit z=hits.get(i);
                            if(z.matches(x,y)){
                                pressFeedback();
                                if(z.target==current){activate(current);}
                                else {open(z.target,true);if(z.target>=4)activate(z.target);}
                                performClick();return true;
                            }
                        }
                    }
                    // On content-centric pages, tapping the image opens its real feature.
                    if(current>=4 && y>.14f && y<.91f){pressFeedback();activate(current);}
                    performClick();return true;
                default:return true;
            }
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
