package asia.aaii.baganmuseum;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

/**
 * Optional, native ARCore experience.
 * Places a lightweight procedural temple marker onto a tracked horizontal surface.
 * The museum GLB viewer is separate; this activity does NOT place that exact GLB.
 */
public final class ArMuseumActivity extends Activity implements GLSurfaceView.Renderer {
    private GLSurfaceView surface;
    private TextView statusView;
    private Session session;
    private boolean installRequested;
    private boolean resumed;
    private volatile boolean glReady;
    private int cameraTexture;
    private int backgroundProgram;
    private int templeProgram;
    private int width = 1, height = 1;
    private Anchor anchor;
    private final AtomicReference<PointF> tap = new AtomicReference<>();
    private FloatBuffer quad, uv, templeBase, templeRoof;
    private final float[] view = new float[16], projection = new float[16];
    private final float[] model = new float[16], modelView = new float[16], mvp = new float[16];

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        surface = new GLSurfaceView(this) {
            @Override public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    tap.set(new PointF(event.getX(), event.getY()));
                    performClick();
                    return true;
                }
                return true;
            }
        };
        surface.setEGLContextClientVersion(2);
        surface.setPreserveEGLContextOnPause(true);
        surface.setRenderer(this);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        FrameLayout root = new FrameLayout(this);
        root.addView(surface, new FrameLayout.LayoutParams(-1,-1));
        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.VERTICAL);
        hud.setPadding(18,22,18,18);
        hud.setBackgroundColor(0xAA1F1611);
        TextView title = new TextView(this);
        title.setText("BAGAN MUSEUM — NATIVE AR 1.3");
        title.setTextColor(0xFFF3BD69);
        title.setTextSize(17);
        hud.addView(title);
        statusView = new TextView(this);
        statusView.setTextColor(0xFFFFEAD3);
        statusView.setTextSize(14);
        statusView.setText("Move the phone slowly to find a flat surface. Tap the plane to place a miniature Bagan-inspired monument.");
        hud.addView(statusView);
        LinearLayout controls = new LinearLayout(this);
        Button clear = new Button(this);
        clear.setText("Clear object");
        clear.setOnClickListener(v -> surface.queueEvent(() -> {
            if (anchor != null) { anchor.detach(); anchor = null; }
            report("Object cleared. Tap a detected floor to place another.");
        }));
        Button close = new Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> finish());
        controls.addView(clear);
        controls.addView(close);
        hud.addView(controls);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);
        root.addView(hud,hp);
        setContentView(root);
    }

    private void report(String msg) {
        runOnUiThread(() -> { if (statusView != null) statusView.setText(msg); });
    }

    @Override protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},44);
            return;
        }
        resumeAr();
    }

    private void resumeAr() {
        try {
            if (session == null) {
                ArCoreApk.InstallStatus installed = ArCoreApk.getInstance().requestInstall(this,!installRequested);
                if (installed == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    installRequested=true;
                    report("Google Play Services for AR installation requested.");
                    return;
                }
                session = new Session(this);
                Config config = new Config(session);
                config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
                session.configure(config);
            }
            session.resume();
            resumed=true;
            surface.onResume();
            report("Move your phone slowly to detect a floor, then tap to place the miniature museum monument.");
        } catch(Exception e) {
            report("ARCore is unavailable or needs an update on this device: " + e.getClass().getSimpleName());
        }
    }

    @Override public void onRequestPermissionsResult(int req, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(req,perms,grants);
        if (req == 44) {
            if(grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) resumeAr();
            else report("Camera permission is required for native AR. It can be enabled in Android Settings.");
        }
    }

    @Override protected void onPause() {
        super.onPause();
        if (resumed) {
            surface.onPause();
            if (session != null) session.pause();
            resumed=false;
        }
    }

    @Override protected void onDestroy() {
        if (anchor != null) { anchor.detach(); anchor=null; }
        if (session != null) { session.close(); session=null; }
        super.onDestroy();
    }

    private static FloatBuffer buffer(float[] values) {
        FloatBuffer f = ByteBuffer.allocateDirect(values.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        f.put(values).position(0);
        return f;
    }

    private static int shader(int type,String source) {
        int id=GLES20.glCreateShader(type);
        GLES20.glShaderSource(id,source);
        GLES20.glCompileShader(id);
        int[] check=new int[1];
        GLES20.glGetShaderiv(id,GLES20.GL_COMPILE_STATUS,check,0);
        if(check[0]==0)throw new IllegalStateException(GLES20.glGetShaderInfoLog(id));
        return id;
    }
    private static int program(String vertex,String fragment) {
        int p=GLES20.glCreateProgram();
        GLES20.glAttachShader(p,shader(GLES20.GL_VERTEX_SHADER,vertex));
        GLES20.glAttachShader(p,shader(GLES20.GL_FRAGMENT_SHADER,fragment));
        GLES20.glLinkProgram(p);
        int[] check=new int[1];
        GLES20.glGetProgramiv(p,GLES20.GL_LINK_STATUS,check,0);
        if(check[0]==0)throw new IllegalStateException(GLES20.glGetProgramInfoLog(p));
        return p;
    }

    @Override public void onSurfaceCreated(GL10 unused,EGLConfig config) {
        int[] textures=new int[1];
        GLES20.glGenTextures(1,textures,0);
        cameraTexture=textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,cameraTexture);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        backgroundProgram=program(
            "attribute vec2 position; attribute vec2 tex; varying vec2 uv; void main(){uv=tex;gl_Position=vec4(position,0.0,1.0);}",
            "#extension GL_OES_EGL_image_external : require\nprecision mediump float; varying vec2 uv; uniform samplerExternalOES cameraTex; void main(){gl_FragColor=texture2D(cameraTex,uv);}"
        );
        templeProgram=program(
            "attribute vec3 position; uniform mat4 mvp; void main(){gl_Position=mvp*vec4(position,1.0);}",
            "precision mediump float; uniform vec4 color; void main(){gl_FragColor=color;}"
        );
        quad=buffer(new float[]{-1,-1,1,-1,-1,1,1,1});
        uv=buffer(new float[8]);
        // A small square brick pedestal, plus a gold Bagan-inspired pagoda/pyramid.
        templeBase=buffer(new float[]{
            -0.5f,0,-0.5f, 0.5f,0,-0.5f, 0.5f,.35f,-0.5f,
            -0.5f,0,-0.5f, 0.5f,.35f,-0.5f,-0.5f,.35f,-0.5f,
            -.5f,0,.5f, .5f,.35f,.5f, .5f,0,.5f,
            -.5f,0,.5f,-.5f,.35f,.5f,.5f,.35f,.5f,
            -.5f,.35f,-.5f,.5f,.35f,-.5f,.5f,.35f,.5f,
            -.5f,.35f,-.5f,.5f,.35f,.5f,-.5f,.35f,.5f
        });
        templeRoof=buffer(new float[]{
            -.55f,.35f,-.55f, .55f,.35f,-.55f,0,1.6f,0,
            .55f,.35f,-.55f, .55f,.35f,.55f,0,1.6f,0,
            .55f,.35f,.55f,-.55f,.35f,.55f,0,1.6f,0,
            -.55f,.35f,.55f,-.55f,.35f,-.55f,0,1.6f,0
        });
        glReady=true;
        GLES20.glClearColor(.07f,.04f,.03f,1f);
    }

    @Override public void onSurfaceChanged(GL10 unused,int w,int h) {
        width=w; height=h;
        GLES20.glViewport(0,0,w,h);
    }

    @Override public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        if (!resumed || session == null || !glReady) return;
        try {
            session.setCameraTextureName(cameraTexture);
            session.setDisplayGeometry(getWindowManager().getDefaultDisplay().getRotation(),width,height);
            Frame frame=session.update();
            Camera camera=frame.getCamera();
            float[] coords=new float[]{-1,-1,1,-1,-1,1,1,1};
            FloatBuffer in=buffer(coords),out=buffer(new float[8]);
            frame.transformCoordinates2d(Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,in,Coordinates2d.TEXTURE_NORMALIZED,out);
            if(frame.getTimestamp()!=0) drawBackground(out);
            if(camera.getTrackingState()!=TrackingState.TRACKING)return;
            PointF point=tap.getAndSet(null);
            if(point!=null) {
                for(HitResult hit:frame.hitTest(point.x,point.y)) {
                    if(hit.getTrackable() instanceof Plane) {
                        Plane plane=(Plane)hit.getTrackable();
                        if(plane.getType()==Plane.Type.HORIZONTAL_UPWARD_FACING && plane.isPoseInPolygon(hit.getHitPose())) {
                            if(anchor!=null)anchor.detach();
                            anchor=hit.createAnchor();
                            report("Miniature temple placed on the detected surface. Tap another position to move it.");
                            break;
                        }
                    }
                }
            }
            if(anchor != null && anchor.getTrackingState()==TrackingState.TRACKING) {
                anchor.getPose().toMatrix(model,0);
                Matrix.scaleM(model,0,.28f,.28f,.28f);
                camera.getViewMatrix(view,0);
                camera.getProjectionMatrix(projection,0,.1f,80f);
                Matrix.multiplyMM(modelView,0,view,0,model,0);
                Matrix.multiplyMM(mvp,0,projection,0,modelView,0);
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                drawSolid(templeBase,18,new float[]{.64f,.33f,.20f,1});
                drawSolid(templeRoof,12,new float[]{.93f,.68f,.25f,1});
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            }
        } catch(Exception ex) {
            report("AR camera error: "+ex.getClass().getSimpleName());
        }
    }

    private void drawBackground(FloatBuffer transformed) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(backgroundProgram);
        int position=GLES20.glGetAttribLocation(backgroundProgram,"position");
        int tex=GLES20.glGetAttribLocation(backgroundProgram,"tex");
        quad.position(0);transformed.position(0);
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glEnableVertexAttribArray(tex);
        GLES20.glVertexAttribPointer(position,2,GLES20.GL_FLOAT,false,0,quad);
        GLES20.glVertexAttribPointer(tex,2,GLES20.GL_FLOAT,false,0,transformed);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,cameraTexture);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(backgroundProgram,"cameraTex"),0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        GLES20.glDisableVertexAttribArray(position);
        GLES20.glDisableVertexAttribArray(tex);
    }
    private void drawSolid(FloatBuffer vertices,int count,float[] color) {
        GLES20.glUseProgram(templeProgram);
        int position=GLES20.glGetAttribLocation(templeProgram,"position");
        vertices.position(0);
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,0,vertices);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(templeProgram,"mvp"),1,false,mvp,0);
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(templeProgram,"color"),1,color,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,count);
        GLES20.glDisableVertexAttribArray(position);
    }
}
