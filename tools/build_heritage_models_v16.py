#!/usr/bin/env python3
"""Build 10 ORIGINAL illustrative offline GLB monument models for V1.6.

No source .blend file is read here. This is explicitly *not* an export of the
user's Blender files nor a measured reconstruction. Each model is distinct,
valid GLB 2.0, self-contained, and built without external Python packages.
Later, a Blender-exported model can replace each models/<slug>.glb.
"""
import base64, json, math, struct
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app"/"src"/"main"/"assets"
MODELS=ASSETS/"models"
MODELS.mkdir(parents=True,exist_ok=True)
CATALOG=json.loads((ASSETS/"bagan_drive_catalog_v15.json").read_text(encoding="utf8"))["items"]

BRICK=(.56,.29,.18,1);BRICK_LIGHT=(.72,.42,.27,1);PLASTER=(.84,.76,.61,1)
GOLD=(.97,.72,.25,1);STONE=(.47,.46,.43,1);DARK=(.22,.15,.12,1)
EARTH=(.49,.35,.24,1);ROOF=(.43,.24,.16,1);GREEN=(.30,.39,.27,1)

class Scene:
    def __init__(self):
        self.faces=[]
    def tri(self,a,b,c,color):
        u=[b[k]-a[k] for k in range(3)];v=[c[k]-a[k] for k in range(3)]
        n=(u[1]*v[2]-u[2]*v[1],u[2]*v[0]-u[0]*v[2],u[0]*v[1]-u[1]*v[0])
        nn=math.sqrt(sum(x*x for x in n)) or 1
        self.faces.append((a,b,c,tuple(x/nn for x in n),color))
    def box(self,x,y,z,w,h,d,col):
        l,r=x-w/2,x+w/2;b,t=y-h/2,y+h/2;f,k=z-d/2,z+d/2
        p=[(l,b,f),(r,b,f),(r,t,f),(l,t,f),(l,b,k),(r,b,k),(r,t,k),(l,t,k)]
        for A,B,C,D in ((0,1,2,3),(5,4,7,6),(4,0,3,7),(1,5,6,2),(3,2,6,7),(4,5,1,0)):
            self.tri(p[A],p[B],p[C],col);self.tri(p[A],p[C],p[D],col)
    def cylinder(self,x,y,z,rad,h,col,rTop=None,n=16):
        end=rad if rTop is None else rTop
        for i in range(n):
            a,b=2*math.pi*i/n,2*math.pi*(i+1)/n
            A=(x+rad*math.cos(a),y,z+rad*math.sin(a))
            B=(x+rad*math.cos(b),y,z+rad*math.sin(b))
            C=(x+end*math.cos(b),y+h,z+end*math.sin(b))
            D=(x+end*math.cos(a),y+h,z+end*math.sin(a))
            self.tri(A,B,C,col);self.tri(A,C,D,col);self.tri((x,y+h,z),D,C,col)
    def spire(self,x,y,z,r,h,col):
        self.cylinder(x,y,z,r,h,col,rTop=.01,n=16)
    def stairs(self,size):
        for i in range(3):
            self.box(0,.03+i*.06,size*.56+i*.32,size*.56,.06+i*.04,.48,PLASTER)
    def pagoda(self,style):
        self.box(0,-.18,0,12,.35,12,EARTH)
        if style=="shwezigon":
            for j in range(4):
                size=8.6-j*.6
                self.box(0,.18+j*.35,0,size,.40,size,GOLD if j%2==0 else PLASTER)
            self.cylinder(0,1.55,0,3.45,1.1,GOLD,2.65,24)
            self.cylinder(0,2.65,0,2.65,3.4,GOLD,.6,24)
            self.spire(0,6.05,0,.65,2.2,GOLD)
        elif style=="bupaya":
            self.box(0,.2,0,8,.45,8,BRICK)
            for radius,y in ((2.9,.55),(2.55,1.15),(2.9,2.15),(2.5,3.0),(1.8,3.75)):
                self.cylinder(0,y,0,radius,.65,GOLD,radius*(1.04 if y<2.5 else .72),24)
            self.spire(0,4.4,0,1.25,2.75,GOLD)
        elif style=="alodawpyae":
            self.box(0,.2,0,7,.45,7,PLASTER)
            for j in range(3):
                self.cylinder(0,.48+j*.55,0,2.6-j*.35,.55,GOLD,2.30-j*.37,16)
            self.spire(0,2.1,0,1.55,3.25,GOLD)
        else:
            self.box(0,.2,0,8.2,.45,8.2,PLASTER)
            self.cylinder(0,.5,0,2.8,.7,GOLD,2.55,20)
            self.spire(0,1.2,0,2.3,4.8,GOLD)
    def temple(self,style):
        self.box(0,-.20,0,12,.4,12,EARTH)
        shape={"thatbyinnyu":(6.4,7.6,8.8),"gawdawpalin":(7.2,6.8,7.8),
               "htilominlo":(7.6,7.3,6.4),"ananda":(6.5,5.8,7.8)}.get(style,(7,7,8))
        size,tower,baseH=shape
        self.box(0,baseH/2,0,size,baseH,size,PLASTER if style=="ananda" else BRICK)
        for x in (-size*.39,size*.39):
            for z in (-size*.39,size*.39):
                self.box(x,baseH+0.2,z,.82,1.7,.82,BRICK_LIGHT)
                self.spire(x,baseH+1.05,z,.68,1.5,GOLD)
        if style=="ananda":
            self.box(0,1.5,size*.58,3.0,3.0,5.0,PLASTER)
            self.box(0,1.5,-size*.58,3.0,3.0,5.0,PLASTER)
            self.box(size*.58,1.5,0,5.0,3.0,3.0,PLASTER)
            self.box(-size*.58,1.5,0,5.0,3.0,3.0,PLASTER)
        for j in range(3):
            w=size+1-j*.7
            self.box(0,baseH+.65+j*.68,0,w,.55,w,BRICK_LIGHT if j%2 else PLASTER)
        self.box(0,baseH+2.65,0,size*.65,tower*.35,size*.65,BRICK)
        self.cylinder(0,baseH+2.85+tower*.35,0,size*.27,.75,PLASTER,size*.20)
        self.spire(0,baseH+3.6+tower*.35,0,size*.20,tower*.6,GOLD)
        for side in (-1,1):
            self.box(side*size*.52,2.1,0,1.0,4.2,size*.65,BRICK_LIGHT)
        self.stairs(size)
    def gateway(self,style):
        self.box(0,-.18,0,12,.35,9,EARTH)
        w=8.5 if style=="tharabar" else 9.2
        self.box(-w*.37,2.3,0,2.7,4.6,2.9,BRICK)
        self.box(w*.37,2.3,0,2.7,4.6,2.9,BRICK)
        self.box(0,5.25,0,w,1.3,2.9,BRICK_LIGHT)
        for x in (-w*.37,w*.37):
            self.box(x,6.3,0,2.8,.9,3.1,BRICK_LIGHT)
            self.spire(x,6.8,0,1.5,2.3,ROOF)
        self.box(0,6.3,0,2.1,.85,2.9,ROOF)
        self.spire(0,6.7,0,1.2,2.4,GOLD)
        self.stairs(w)
    def inscription(self):
        self.box(0,-.23,0,9,.46,8,EARTH)
        self.box(0,.4,0,3.2,.8,2.5,PLASTER)
        self.box(0,3.2,0,1.8,5.2,1.1,STONE)
        self.box(0,5.85,0,2.05,.65,1.35,STONE)
        for side in (-.58,.58):
            for row in range(11):
                for column in range(3):
                    self.box(side+.02*column,.75+row*.40, .568, .035,.25,.028,DARK)
        for row in range(9):
            self.box(-.78+row*.18,2.0,.573,.08,.12,.028,DARK)
    def export_glb(self):
        mats=[];mapmat={}
        positions=[];normals=[];indices=[]
        grouped={}
        for a,b,c,n,clr in self.faces:
            grouped.setdefault(clr,[]).append((a,b,c,n))
        raw=bytearray();views=[];accessors=[];primitives=[]
        def view(blob,target):
            while len(raw)%4:raw.append(0)
            start=len(raw);raw.extend(blob)
            i=len(views);views.append({"buffer":0,"byteOffset":start,"byteLength":len(blob),"target":target})
            return i
        def accessor(bv,ctype,count,kind,mi=None,ma=None):
            d={"bufferView":bv,"componentType":ctype,"count":count,"type":kind}
            if mi is not None:d["min"]=mi;d["max"]=ma
            i=len(accessors);accessors.append(d);return i
        for clr,faces in grouped.items():
            ps=[];ns=[];ind=[]
            for a,b,c,n in faces:
                for pt in (a,b,c):
                    ps.extend(pt);ns.extend(n);ind.append(len(ind))
            if len(ind)>65535:raise ValueError("GLB too many vertices")
            position=accessor(view(struct.pack("<%sf"%len(ps),*ps),34962),5126,len(ind),"VEC3",
                              [min(ps[k::3]) for k in range(3)],[max(ps[k::3]) for k in range(3)])
            normal=accessor(view(struct.pack("<%sf"%len(ns),*ns),34962),5126,len(ind),"VEC3")
            ix=accessor(view(struct.pack("<%sH"%len(ind),*ind),34963),5123,len(ind),"SCALAR")
            mid=len(mats)
            mats.append({"pbrMetallicRoughness":{"baseColorFactor":list(clr),"metallicFactor":0,"roughnessFactor":.87},
                         "doubleSided":True})
            primitives.append({"attributes":{"POSITION":position,"NORMAL":normal},"indices":ix,"material":mid,"mode":4})
        data={"asset":{"version":"2.0","generator":"V1.6-original-concept-geometry-NOT-Blender-export"},
              "scenes":[{"nodes":[0]}],"scene":0,"nodes":[{"mesh":0}],
              "meshes":[{"primitives":primitives}],"buffers":[{"byteLength":len(raw)}],
              "bufferViews":views,"accessors":accessors,"materials":mats}
        js=json.dumps(data,separators=(",",":")).encode("utf8")
        js+=b" "*((-len(js))%4)
        bb=bytes(raw)+bytes((-len(raw))%4)
        return (struct.pack("<III",0x46546c67,2,12+8+len(js)+8+len(bb))+
                struct.pack("<II",len(js),0x4E4F534A)+js+
                struct.pack("<II",len(bb),0x004E4942)+bb)

allowed={"thatbyinnyu","myazedi","entrance","tharabar","gawdawpalin",
         "ananda","htilominlo","alodawpyae","shwezigon","bupaya"}
assert {i["slug"] for i in CATALOG}==allowed
embedded=[]
for item in CATALOG:
    key=item["slug"];s=Scene()
    if key=="myazedi":s.inscription()
    elif key in ("entrance","tharabar"):s.gateway(key)
    elif key in ("shwezigon","bupaya","alodawpyae"):s.pagoda(key)
    else:s.temple(key)
    glb=s.export_glb()
    (MODELS/(key+".glb")).write_bytes(glb)
    # Captions come from the user's authenticated Drive metadata snapshot.
    embedded.append({"slug":key,"name":item["name"],"myanmar":item["myanmar"],
                     "category":item["category"],"description":item["description_mm"],
                     "folder_id":item["folder_id"],"model_type":"illustrative_concept_not_blend_export",
                     "glb":base64.b64encode(glb).decode("ascii")})
    print("Generated",key,len(glb),"bytes")
page=(ROOT/"tools"/"heritage_viewer.template.html").read_text(encoding="utf8")
page=page.replace("__HERITAGE_MODEL_DATA__",json.dumps(embedded,ensure_ascii=False,separators=(",",":")))
(ASSETS/"heritage_viewer_v16.html").write_text(page,encoding="utf8")
(ASSETS/"heritage_model_manifest_v16.json").write_text(
    json.dumps([{k:v for k,v in item.items() if k!="glb"} for item in embedded],ensure_ascii=False,indent=2),
    encoding="utf8")
print("V1.6 concept GLBs:",len(embedded),"viewer bytes:",(ASSETS/"heritage_viewer_v16.html").stat().st_size)
