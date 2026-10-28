package asia.aaii.baganmuseum;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real geometry from the user's imported Google Drive GLB files.
 * Supports glTF2 scene graph hierarchy, FLOAT POSITION accessor strides,
 * uint8/uint16/uint32 triangle indices and material base-color factors.
 * Draws a bounded triangle sample on GLES2 phones for stability.
 * Embedded textures and full physically based rendering are not implemented. Adjustable approximate material illumination is supported.
 * No illustrative or replacement geometry is silently substituted.
 */
public final class DriveArGlb {
    private static final int MAX_TRIANGLES=145000;
    private float illumination=1f;
    public void setIllumination(float value){illumination=Math.max(.35f,Math.min(1.65f,value));}
    private static final int MAX_FILE_BYTES=65_000_000;
    private static final class FloatList {
        float[] values=new float[2048]; int count;
        void add(float v){if(count==values.length){float[] n=new float[values.length*2];System.arraycopy(values,0,n,0,count);values=n;}values[count++]=v;}
        void vertex(float[] p){add(p[0]);add(p[1]);add(p[2]);}
    }
    private static final class Batch{
        FloatBuffer vertices;int count;float[] rgba;
    }
    private final List<Batch> drawBatches=new ArrayList<>();
    private final int shaderProgram, positionAttr, mvpUniform, colorUniform;
    private final byte[] bytes;
    private final ByteBuffer glb;
    private final JSONObject doc;
    private final JSONArray accessors, bufferViews,meshes,nodes,materials;
    private final int binOffset;
    private final float[] min={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY};
    private final float[] max={Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
    private float unit=1f;
    private int sceneTriangles=0,sampleCursor=0,stride=1;
    public int sampledTriangles(){int n=0;for(Batch p:drawBatches)n+=p.count/3;return n;}

    public DriveArGlb(Context context,File input)throws Exception{
        if(!input.isFile()||input.length()<40||input.length()>MAX_FILE_BYTES)
            throw new IllegalArgumentException("Missing or oversized original GLB");
        bytes=new byte[(int)input.length()];
        try(FileInputStream f=new FileInputStream(input)){
            int o=0,n;while(o<bytes.length&&(n=f.read(bytes,o,bytes.length-o))>0)o+=n;
            if(o!=bytes.length)throw new IllegalArgumentException("Truncated file");
        }
        glb=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if(glb.getInt(0)!=0x46546c67||glb.getInt(4)!=2||glb.getInt(8)!=bytes.length)
            throw new IllegalArgumentException("Bad GLB version/length");
        JSONObject document=null;int binary=-1;
        for(int off=12;off+8<=bytes.length;){
            int length=glb.getInt(off),type=glb.getInt(off+4);off+=8;
            if(length<0||off+length>bytes.length)throw new IllegalArgumentException("Bad chunk");
            if(type==0x4E4F534A)document=new JSONObject(new String(bytes,off,length,StandardCharsets.UTF_8).trim());
            if(type==0x004E4942)binary=off;
            off+=length;
        }
        if(document==null||binary<0)throw new IllegalArgumentException("Incomplete GLB");
        doc=document;binOffset=binary;
        accessors=doc.getJSONArray("accessors");bufferViews=doc.getJSONArray("bufferViews");
        meshes=doc.getJSONArray("meshes");nodes=doc.getJSONArray("nodes");
        materials=doc.optJSONArray("materials");
        shaderProgram=makeProgram();
        positionAttr=GLES20.glGetAttribLocation(shaderProgram,"position");
        mvpUniform=GLES20.glGetUniformLocation(shaderProgram,"mvp");
        colorUniform=GLES20.glGetUniformLocation(shaderProgram,"color");
        if(GLES20.glGetError()!=GLES20.GL_NO_ERROR)throw new IllegalStateException("OpenGL unavailable");
        visitScene(true,null);
        float longest=Math.max(max[0]-min[0],Math.max(max[1]-min[1],max[2]-min[2]));
        if(!Float.isFinite(longest)||longest<=1e-6f)throw new IllegalArgumentException("No usable geometry");
        unit=1f/longest;
        stride=Math.max(1,(int)Math.ceil(sceneTriangles/(double)MAX_TRIANGLES));
        Map<Integer,FloatList> groups=new LinkedHashMap<>();
        visitScene(false,groups);
        for(Map.Entry<Integer,FloatList> entry:groups.entrySet()){
            FloatList f=entry.getValue();if(f.count==0)continue;
            ByteBuffer b=ByteBuffer.allocateDirect(f.count*4).order(ByteOrder.nativeOrder());
            FloatBuffer data=b.asFloatBuffer();data.put(f.values,0,f.count).position(0);
            Batch batch=new Batch();batch.vertices=data;batch.count=f.count/3;
            batch.rgba=materialColor(entry.getKey());
            drawBatches.add(batch);
        }
        if(drawBatches.isEmpty())throw new IllegalArgumentException("No renderable triangles");
    }
    private float[] materialColor(int index){
        float[] fallback={.80f,.62f,.39f,1f};
        try {
            if(materials==null||index<0||index>=materials.length())return fallback;
            JSONObject material=materials.getJSONObject(index);
            JSONObject pbr=material.optJSONObject("pbrMetallicRoughness");
            if(pbr==null)return fallback;
            JSONArray c=pbr.optJSONArray("baseColorFactor");
            if(c==null)return fallback;
            return new float[]{(float)c.getDouble(0),(float)c.getDouble(1),(float)c.getDouble(2),1f};
        }catch(Exception e){return fallback;}
    }
    private void visitScene(boolean bounding,Map<Integer,FloatList> groups)throws Exception{
        JSONArray scenes=doc.optJSONArray("scenes");
        int sceneIndex=doc.optInt("scene",0);
        if(scenes!=null&&scenes.length()>0){
            JSONArray roots=scenes.getJSONObject(Math.min(sceneIndex,scenes.length()-1)).optJSONArray("nodes");
            if(roots!=null)for(int i=0;i<roots.length();i++)traverse(roots.getInt(i),identity(),bounding,groups,0);
        }else{
            for(int i=0;i<nodes.length();i++)traverse(i,identity(),bounding,groups,0);
        }
    }
    private static float[] identity(){float[] f=new float[16];Matrix.setIdentityM(f,0);return f;}
    private float[] transform(JSONObject obj){
        JSONArray arr=obj.optJSONArray("matrix");
        if(arr!=null&&arr.length()==16){
            float[] result=new float[16];
            for(int i=0;i<16;i++)result[i]=(float)arr.optDouble(i,0.0);
            return result;
        }
        float[] f=identity();
        JSONArray trans=obj.optJSONArray("translation");
        if(trans!=null)Matrix.translateM(f,0,(float)trans.optDouble(0),(float)trans.optDouble(1),(float)trans.optDouble(2));
        JSONArray rot=obj.optJSONArray("rotation");
        if(rot!=null){
            float x=(float)rot.optDouble(0),y=(float)rot.optDouble(1),z=(float)rot.optDouble(2),w=(float)rot.optDouble(3,1);
            float xx=x*x,yy=y*y,zz=z*z,xy=x*y,xz=x*z,yz=y*z,wx=w*x,wy=w*y,wz=w*z;
            float[] q={1-2*(yy+zz),2*(xy+wz),2*(xz-wy),0,
                       2*(xy-wz),1-2*(xx+zz),2*(yz+wx),0,
                       2*(xz+wy),2*(yz-wx),1-2*(xx+yy),0,
                       0,0,0,1};
            float[] joined=new float[16];Matrix.multiplyMM(joined,0,f,0,q,0);f=joined;
        }
        JSONArray sc=obj.optJSONArray("scale");
        if(sc!=null)Matrix.scaleM(f,0,(float)sc.optDouble(0,1),(float)sc.optDouble(1,1),(float)sc.optDouble(2,1));
        return f;
    }
    private void traverse(int index,float[] parent,boolean bounding,Map<Integer,FloatList> groups,int depth)throws Exception{
        if(depth>90||index<0||index>=nodes.length())throw new IllegalArgumentException("Invalid node tree");
        JSONObject node=nodes.getJSONObject(index);
        float[] world=new float[16];Matrix.multiplyMM(world,0,parent,0,transform(node),0);
        int meshId=node.optInt("mesh",-1);
        if(meshId>=0&&meshId<meshes.length()){
            JSONArray ps=meshes.getJSONObject(meshId).optJSONArray("primitives");
            if(ps!=null)for(int i=0;i<ps.length();i++){
                JSONObject prim=ps.getJSONObject(i);
                if(prim.optInt("mode",4)!=4)continue;
                JSONObject attrs=prim.optJSONObject("attributes");if(attrs==null||!attrs.has("POSITION"))continue;
                int posId=attrs.getInt("POSITION");
                JSONObject pa=accessors.getJSONObject(posId);
                int vertexCount=pa.getInt("count");
                int indexId=prim.optInt("indices",-1);
                int indexCount=indexId>=0?accessors.getJSONObject(indexId).getInt("count"):vertexCount;
                if(bounding){
                    sceneTriangles+=indexCount/3;
                    JSONArray mn=pa.optJSONArray("min"),mx=pa.optJSONArray("max");
                    if(mn!=null&&mx!=null){
                        for(int k=0;k<8;k++) {
                            float[] xyz={ (float)((k&1)==0?mn.optDouble(0):mx.optDouble(0)),
                                (float)((k&2)==0?mn.optDouble(1):mx.optDouble(1)),
                                (float)((k&4)==0?mn.optDouble(2):mx.optDouble(2))};
                            boundsPoint(transformPoint(world,xyz));
                        }
                    } else {
                        for(int v=0;v<vertexCount;v++)boundsPoint(transformPoint(world,readPosition(posId,v)));
                    }
                }else {
                    int material=prim.optInt("material",-1);
                    FloatList list=groups.get(material);
                    if(list==null){list=new FloatList();groups.put(material,list);}
                    for(int t=0;t+2<indexCount;t+=3){
                        if((sampleCursor++%stride)!=0)continue;
                        for(int k=0;k<3;k++){
                            int n=indexId>=0?readIndex(indexId,t+k):t+k;
                            if(n<0||n>=vertexCount)throw new IllegalArgumentException("Invalid vertex index");
                            float[] p=transformPoint(world,readPosition(posId,n));
                            p[0]=(p[0]-(min[0]+max[0])*.5f)*unit;
                            p[1]=(p[1]-min[1])*unit;
                            p[2]=(p[2]-(min[2]+max[2])*.5f)*unit;
                            list.vertex(p);
                        }
                    }
                }
            }
        }
        JSONArray children=node.optJSONArray("children");
        if(children!=null)for(int i=0;i<children.length();i++)traverse(children.getInt(i),world,bounding,groups,depth+1);
    }
    private void boundsPoint(float[] p){
        for(int i=0;i<3;i++){min[i]=Math.min(min[i],p[i]);max[i]=Math.max(max[i],p[i]);}
    }
    private static float[] transformPoint(float[] m,float[] p){
        return new float[]{
            m[0]*p[0]+m[4]*p[1]+m[8]*p[2]+m[12],
            m[1]*p[0]+m[5]*p[1]+m[9]*p[2]+m[13],
            m[2]*p[0]+m[6]*p[1]+m[10]*p[2]+m[14]
        };
    }
    private int itemOffset(int id,int item,int nominalStride)throws Exception{
        JSONObject acc=accessors.getJSONObject(id);
        JSONObject view=bufferViews.getJSONObject(acc.getInt("bufferView"));
        int offset=binOffset+view.optInt("byteOffset",0)+acc.optInt("byteOffset",0);
        int stride=view.optInt("byteStride",nominalStride);
        long at=(long)offset+(long)item*stride;
        if(at<0||at+nominalStride>bytes.length)throw new IllegalArgumentException("GLB accessor offset out of range");
        return (int)at;
    }
    private float[] readPosition(int id,int vertex)throws Exception{
        JSONObject acc=accessors.getJSONObject(id);
        if(acc.getInt("componentType")!=5126||!"VEC3".equals(acc.getString("type")))
            throw new IllegalArgumentException("Unsupported POSITION accessor");
        int at=itemOffset(id,vertex,12);
        return new float[]{glb.getFloat(at),glb.getFloat(at+4),glb.getFloat(at+8)};
    }
    private int readIndex(int id,int index)throws Exception{
        int type=accessors.getJSONObject(id).getInt("componentType");
        int size=type==5125?4:type==5123?2:type==5121?1:0;
        if(size==0)throw new IllegalArgumentException("Unsupported index type");
        int offset=itemOffset(id,index,size);
        return type==5125?glb.getInt(offset):type==5123?(glb.getShort(offset)&0xffff):(glb.get(offset)&0xff);
    }
    private static int shader(int type,String src){
        int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);
        int[] ok=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);
        if(ok[0]==0)throw new IllegalStateException(GLES20.glGetShaderInfoLog(s));return s;
    }
    private static int makeProgram(){
        int p=GLES20.glCreateProgram();
        GLES20.glAttachShader(p,shader(GLES20.GL_VERTEX_SHADER,
          "attribute vec3 position;uniform mat4 mvp;varying float tint;void main(){gl_Position=mvp*vec4(position,1.0);tint=0.78+0.22*clamp(position.y,0.0,1.0);}"));
        GLES20.glAttachShader(p,shader(GLES20.GL_FRAGMENT_SHADER,
          "precision mediump float;uniform vec4 color;varying float tint;void main(){gl_FragColor=vec4(color.rgb*tint,1.0);}"));
        GLES20.glLinkProgram(p);
        int[] ok=new int[1];GLES20.glGetProgramiv(p,GLES20.GL_LINK_STATUS,ok,0);
        if(ok[0]==0)throw new IllegalStateException("Shader program link failed");
        return p;
    }
    public void draw(float[] mvp){
        GLES20.glUseProgram(shaderProgram);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnableVertexAttribArray(positionAttr);
        GLES20.glUniformMatrix4fv(mvpUniform,1,false,mvp,0);
        for(Batch b:drawBatches){
            b.vertices.position(0);
            GLES20.glVertexAttribPointer(positionAttr,3,GLES20.GL_FLOAT,false,0,b.vertices);
            float[] lit={Math.min(1f,b.rgba[0]*illumination),Math.min(1f,b.rgba[1]*illumination),Math.min(1f,b.rgba[2]*illumination),1f};
            GLES20.glUniform4fv(colorUniform,1,lit,0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,b.count);
        }
        GLES20.glDisableVertexAttribArray(positionAttr);
    }
    public void release(){
        if(shaderProgram!=0)GLES20.glDeleteProgram(shaderProgram);
        drawBatches.clear();
    }
}
