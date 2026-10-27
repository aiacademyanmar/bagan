package asia.aaii.baganmuseum;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Offline renderer for the app's own GLB mesh. glTF subset: FLOAT VEC3 + UNSIGNED_SHORT indices. */
public final class OfflineMuseumGlb {
    private static final class Part {
        FloatBuffer vertices;
        ShortBuffer triangles;
        int count;
        float[] color;
    }
    private final List<Part> parts = new ArrayList<>();
    private final int program;
    private final int position;
    private final int mvp;
    private final int color;

    private static int shader(int type,String source) {
        int shader=GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,source);
        GLES20.glCompileShader(shader);
        int[] status=new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,status,0);
        if(status[0]==0)throw new IllegalStateException(GLES20.glGetShaderInfoLog(shader));
        return shader;
    }

    public OfflineMuseumGlb(Context context) throws Exception {
        this(context, "bagan_museum_concept.glb");
    }
    public OfflineMuseumGlb(Context context,String assetPath) throws Exception {
        int p=GLES20.glCreateProgram();
        GLES20.glAttachShader(p,shader(GLES20.GL_VERTEX_SHADER,
            "attribute vec3 position; uniform mat4 mvp; void main(){gl_Position=mvp*vec4(position,1.0);}" ));
        GLES20.glAttachShader(p,shader(GLES20.GL_FRAGMENT_SHADER,
            "precision mediump float; uniform vec4 color; void main(){gl_FragColor=color;}" ));
        GLES20.glLinkProgram(p);
        int[] status=new int[1];
        GLES20.glGetProgramiv(p,GLES20.GL_LINK_STATUS,status,0);
        if(status[0]==0)throw new IllegalStateException("GL program link failed");
        program=p;
        position=GLES20.glGetAttribLocation(p,"position");
        mvp=GLES20.glGetUniformLocation(p,"mvp");
        color=GLES20.glGetUniformLocation(p,"color");

        byte[] file;
        try(InputStream stream=context.getAssets().open(assetPath);
            ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buf=new byte[8192];int size;
            while((size=stream.read(buf))!=-1)out.write(buf,0,size);
            file=out.toByteArray();
        }
        ByteBuffer source=ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
        if(source.getInt(0)!=0x46546C67 || source.getInt(4)!=2)
            throw new IllegalArgumentException("Invalid GLB 2.0 header");
        byte[] geometry=null;
        JSONObject json=null;
        int offset=12;
        while(offset+8<=file.length){
            int length=source.getInt(offset), type=source.getInt(offset+4);
            offset+=8;
            if(length<0 || offset+length>file.length)throw new IllegalArgumentException("Invalid GLB chunk");
            if(type==0x4E4F534A)json=new JSONObject(new String(file,offset,length,StandardCharsets.UTF_8).trim());
            if(type==0x004E4942){
                geometry=new byte[length];
                System.arraycopy(file,offset,geometry,0,length);
            }
            offset+=length;
        }
        if(json==null||geometry==null)throw new IllegalArgumentException("Incomplete GLB");
        JSONArray accessors=json.getJSONArray("accessors");
        JSONArray views=json.getJSONArray("bufferViews");
        JSONArray materials=json.getJSONArray("materials");
        JSONArray primitives=json.getJSONArray("meshes").getJSONObject(0).getJSONArray("primitives");
        for(int i=0;i<primitives.length();i++){
            JSONObject primitive=primitives.getJSONObject(i);
            int positionId=primitive.getJSONObject("attributes").getInt("POSITION");
            int indicesId=primitive.getInt("indices");
            JSONObject pa=accessors.getJSONObject(positionId);
            JSONObject ia=accessors.getJSONObject(indicesId);
            if(pa.getInt("componentType")!=5126 || ia.getInt("componentType")!=5123)
                throw new IllegalArgumentException("Unsupported glTF accessor types");
            ByteBuffer pv=viewBuffer(geometry,views,pa,pa.getInt("count")*12);
            ByteBuffer iv=viewBuffer(geometry,views,ia,ia.getInt("count")*2);
            Part part=new Part();
            ByteBuffer vdata=ByteBuffer.allocateDirect(pv.remaining()).order(ByteOrder.nativeOrder());
            vdata.put(pv).flip();
            part.vertices=vdata.asFloatBuffer();
            ByteBuffer idata=ByteBuffer.allocateDirect(iv.remaining()).order(ByteOrder.nativeOrder());
            idata.put(iv).flip();
            part.triangles=idata.asShortBuffer();
            part.count=ia.getInt("count");
            JSONArray colors=materials.getJSONObject(primitive.getInt("material"))
                .getJSONObject("pbrMetallicRoughness").getJSONArray("baseColorFactor");
            part.color=new float[]{(float)colors.getDouble(0),(float)colors.getDouble(1),(float)colors.getDouble(2),(float)colors.getDouble(3)};
            parts.add(part);
        }
        if(parts.isEmpty())throw new IllegalArgumentException("No GLB triangles found");
    }
    private static ByteBuffer viewBuffer(byte[] geometry,JSONArray views,JSONObject accessor,int byteLength) throws Exception {
        JSONObject view=views.getJSONObject(accessor.getInt("bufferView"));
        int start=view.optInt("byteOffset",0)+accessor.optInt("byteOffset",0);
        if(start<0||start+byteLength>geometry.length)throw new IllegalArgumentException("GLB accessor out of range");
        return ByteBuffer.wrap(geometry,start,byteLength).slice().order(ByteOrder.LITTLE_ENDIAN);
    }
    public void draw(float[] matrix) {
        GLES20.glUseProgram(program);
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glUniformMatrix4fv(mvp,1,false,matrix,0);
        for(Part part:parts){
            part.vertices.position(0);part.triangles.position(0);
            GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,0,part.vertices);
            GLES20.glUniform4fv(color,1,part.color,0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,part.count,GLES20.GL_UNSIGNED_SHORT,part.triangles);
        }
        GLES20.glDisableVertexAttribArray(position);
    }
}
