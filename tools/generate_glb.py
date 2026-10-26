#!/usr/bin/env python3
"""Generate a small offline GLB museum concept model and embed it into local viewer HTML.
No external packages needed. Not a reconstruction of an existing Bagan monument.
"""
import base64
import json
import math
from pathlib import Path
import struct

HERE = Path(__file__).resolve().parents[1]
OUT = HERE / "app" / "src" / "main" / "assets"
OUT.mkdir(parents=True, exist_ok=True)
PALETTE = [
    ("Stone base", [0.54, 0.32, 0.21, 1.0]),
    ("Brick shell", [0.63, 0.31, 0.17, 1.0]),
    ("Sandstone", [0.83, 0.60, 0.38, 1.0]),
    ("Roof", [0.38, 0.20, 0.14, 1.0]),
    ("Gold finial", [0.93, 0.69, 0.27, 1.0]),
    ("Interior", [0.72, 0.49, 0.30, 1.0]),
    ("Garden", [0.23, 0.38, 0.25, 1.0]),
    ("Walkway", [0.70, 0.46, 0.31, 1.0]),
]
faces = [[] for _ in PALETTE]

def triangle(a,b,c,mat):
    ux,uy,uz=(b[i]-a[i] for i in range(3))
    vx,vy,vz=(c[i]-a[i] for i in range(3))
    n=(uy*vz-uz*vy, uz*vx-ux*vz, ux*vy-uy*vx)
    length=math.sqrt(sum(t*t for t in n)) or 1
    n=tuple(t/length for t in n)
    faces[mat].append((a,b,c,n))

def box(x,y,z,w,h,d,mat):
    a,b=x-w/2,x+w/2
    c,e=y-h/2,y+h/2
    f,g=z-d/2,z+d/2
    corners=[(a,c,f),(b,c,f),(b,e,f),(a,e,f),(a,c,g),(b,c,g),(b,e,g),(a,e,g)]
    for face in [(0,1,2,3),(5,4,7,6),(4,0,3,7),(1,5,6,2),(3,2,6,7),(4,5,1,0)]:
        u,v,w1,t=face
        triangle(corners[u],corners[v],corners[w1],mat)
        triangle(corners[u],corners[w1],corners[t],mat)

def cone(x,y,z,rad,height,mat,sides=14):
    top=(x,y+height,z)
    for i in range(sides):
        a=2*math.pi*i/sides
        b=2*math.pi*(i+1)/sides
        p=(x+rad*math.cos(a),y,z+rad*math.sin(a))
        q=(x+rad*math.cos(b),y,z+rad*math.sin(b))
        triangle(p,q,top,mat)
        triangle((x,y,z),q,p,mat)

def ring(x,y,z,r0,r1,h,mat,sides=14):
    for i in range(sides):
        a=2*math.pi*i/sides
        b=2*math.pi*(i+1)/sides
        p=(x+r0*math.cos(a),y,z+r0*math.sin(a))
        q=(x+r0*math.cos(b),y,z+r0*math.sin(b))
        r=(x+r1*math.cos(a),y+h,z+r1*math.sin(a))
        s=(x+r1*math.cos(b),y+h,z+r1*math.sin(b))
        triangle(p,q,s,mat);triangle(p,s,r,mat)

# Courtyard and museum shell
box(0,-.45,0,28,.9,23,0)
box(0,-.21,13,9,.24,10,7)
for z in (10,13,16): box(0,-.08,z,5,.16,.12,2)
box(-8,2.7,0,.8,5.4,14,1)
box(8,2.7,0,.8,5.4,14,1)
box(0,2.7,-7,16,5.4,.8,1)
box(-5.35,2.7,7,5.3,5.4,.8,1)
box(5.35,2.7,7,5.3,5.4,.8,1)
box(0,5.65,0,17,.9,15,3)
# entrance arch and ornaments
box(-2.05,3,7.55,.8,6,1.2,2)
box(2.05,3,7.55,.8,6,1.2,2)
box(0,6.2,7.55,5,1,1.2,2)
cone(0,6.7,7.55,2.8,2,2,4)
for x in (-7.8,7.8):
    for z in (-6.8,6.8):
        box(x,6.3,z,2,1.3,2,2)
        cone(x,6.95,z,1.7,3,3,12)
# Central pagoda
box(0,6.6,-1.5,6.4,1.5,6.4,2)
ring(0,7.25,-1.5,2.7,2.4,1.0,1)
ring(0,8.25,-1.5,2.35,1.7,1.65,1)
cone(0,9.85,-1.5,1.8,6,1,20)
cone(0,15.85,-1.5,.48,2.0,4,12)
# Inside: visible if shell hidden
for x in (-5,-2,2,5):
    for z in (-4,2,5):
        box(x,2.25,z,.55,4.5,.55,5)
        box(x,4.65,z,.95,.35,.95,2)
box(0,.65,-1,3.2,1.3,2.8,2)
cone(0,1.3,-1,1.1,2.5,4,14)
for x,z in [(-11,-8),(11,-8),(-11,8),(11,8)]:
    box(x,1.2,z,2.8,2.4,2.8,6)
    cone(x,2.4,z,1.8,2.7,6,9)

raw=bytearray()
views=[]
accessors=[]
primitives=[]

def store(data_bytes,component_type,count,kind,target,mins=None,maxs=None):
    while len(raw)%4: raw.append(0)
    pos=len(raw)
    raw.extend(data_bytes)
    vid=len(views)
    views.append({"buffer":0,"byteOffset":pos,"byteLength":len(data_bytes),"target":target})
    aid=len(accessors)
    acc={"bufferView":vid,"byteOffset":0,"componentType":component_type,"count":count,"type":kind}
    if mins is not None: acc["min"]=mins;acc["max"]=maxs
    accessors.append(acc)
    return aid

for mat,triangles in enumerate(faces):
    if not triangles: continue
    coords=[]; norms=[]; indices=[]
    for t in triangles:
        for point in t[:3]:
            coords.extend(point)
            norms.extend(t[3])
            indices.append(len(indices))
    pos=store(struct.pack("<%sf"%len(coords),*coords),5126,len(indices),"VEC3",34962,
        [min(coords[k::3]) for k in range(3)],[max(coords[k::3]) for k in range(3)])
    normal=store(struct.pack("<%sf"%len(norms),*norms),5126,len(indices),"VEC3",34962)
    idx=store(struct.pack("<%sH"%len(indices),*indices),5123,len(indices),"SCALAR",34963)
    primitives.append({"attributes":{"POSITION":pos,"NORMAL":normal},"indices":idx,"material":mat,"mode":4})

data={
    "asset":{"version":"2.0","generator":"Bagan Museum V1.2 Procedural Model"},
    "scene":0,
    "scenes":[{"nodes":[0]}],
    "nodes":[{"mesh":0,"name":"Bagan Museum Concept"}],
    "meshes":[{"name":"Museum Exterior and Interior","primitives":primitives}],
    "buffers":[{"byteLength":len(raw)}],
    "bufferViews":views,"accessors":accessors,
    "materials":[{"name":name,"pbrMetallicRoughness":{"baseColorFactor":rgb,"metallicFactor":0.0,"roughnessFactor":1.0},"doubleSided":True} for name,rgb in PALETTE]
}
json_chunk=json.dumps(data,separators=(",",":")).encode("utf-8")
json_chunk+=b" "*((-len(json_chunk))%4)
bin_chunk=bytes(raw)+bytes((-len(raw))%4)
length=12+8+len(json_chunk)+8+len(bin_chunk)
glb=struct.pack("<III",0x46546C67,2,length)+struct.pack("<II",len(json_chunk),0x4E4F534A)+json_chunk+struct.pack("<II",len(bin_chunk),0x004E4942)+bin_chunk
(OUT/"bagan_museum_concept.glb").write_bytes(glb)
html=(HERE/"tools"/"temple_viewer.template.html").read_text(encoding="utf-8")
html=html.replace("__EMBEDDED_GLB__",base64.b64encode(glb).decode("ascii"))
(OUT/"temple_viewer.html").write_text(html,encoding="utf-8")
print("Generated GLB bytes:",len(glb),"primitives:",len(primitives),"Viewer HTML bytes:",len(html))
