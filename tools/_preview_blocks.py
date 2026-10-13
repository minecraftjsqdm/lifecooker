# -*- coding: utf-8 -*-
"""临时工具：把 models/block/*.json 投影成 PNG，用于人眼检查外形与贴图。

带 z-buffer 的软件光栅化：每个面按 MC 的 uv 采样贴图，逐像素比较深度，
因此不会像画家算法那样出现大面片盖住整体的问题。
左列从东南上方看（up/east/south），右列正对正面（up/east/north）。
"""
import json
import math
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
MB = os.path.join(ROOT, "src", "main", "resources", "assets", "lifecooker", "models", "block")
TX = os.path.join(ROOT, "src", "main", "resources", "assets", "lifecooker", "textures", "block")
OUT = os.path.join(os.path.dirname(__file__), "_preview.png")

SCALE = 14          # 每模型单位像素
SS = 2              # 超采样倍数
COS, SIN = math.cos(math.radians(30)), 0.5
BG = (232, 236, 240)

TEXCACHE = {}


def load_tex(key):
    if key not in TEXCACHE:
        TEXCACHE[key] = Image.open(os.path.join(TX, key + ".png")).convert("RGBA")
    return TEXCACHE[key]


def face_geom(fname, b):
    """返回 (4 个 3d 角点, 4 个 uv 归一化角点, 亮度)。

    角点顺序与 MC 的 uv 展开一致：(u1,v1) (u2,v1) (u2,v2) (u1,v2)。
    """
    x0, x1, y0, y1, z0, z1 = b
    uvv = [(0, 0), (1, 0), (1, 1), (0, 1)]
    if fname == "up":
        return [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)], uvv, 1.00
    if fname == "down":
        return [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], uvv, 0.45
    if fname == "north":
        return [(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], uvv, 0.62
    if fname == "south":
        return [(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)], uvv, 0.62
    if fname == "west":
        return [(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)], uvv, 0.78
    return [(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)], uvv, 0.78  # east


def collect_tris(model, view):
    """把模型拆成三角面片，附带屏幕坐标 / 深度 / uv / 贴图 / 亮度。"""
    sign = 1 if view == "back" else -1

    def proj(p):
        x, y, z = p
        if view == "back":
            return ((x - z) * COS, (x + z) * SIN - y)
        return ((x + z) * COS, (x - z) * SIN - y)

    def close(p):  # 越大越靠近相机
        return p[0] + p[1] + sign * p[2]

    tris = []
    for e in model["elements"]:
        b = (e["from"][0], e["to"][0], e["from"][1], e["to"][1], e["from"][2], e["to"][2])
        for fname in ("up", "down", "north", "south", "west", "east"):
            spec = e["faces"].get(fname)
            if not spec:
                continue
            pts, uvv, bright = face_geom(fname, b)
            tex = load_tex(spec["texture"].lstrip("#").split("/")[-1])
            u1, v1, u2, v2 = spec["uv"]
            scr = [proj(p) for p in pts]
            dep = [close(p) for p in pts]
            tuv = [(u1 + fu * (u2 - u1), v1 + fv * (v2 - v1)) for (fu, fv) in uvv]
            for i, j, k in ((0, 1, 2), (0, 2, 3)):
                tris.append(((scr[i], scr[j], scr[k]), (dep[i], dep[j], dep[k]),
                             (tuv[i], tuv[j], tuv[k]), tex, bright))
    return tris


def render(model, view, scale=SCALE, ss=SS):
    tris = collect_tris(model, view)
    xs = [p[0] for t in tris for p in t[0]]
    ys = [p[1] for t in tris for p in t[0]]
    minx, maxx, miny, maxy = min(xs), max(xs), min(ys), max(ys)
    pad = 0.6
    w = max(2, int((maxx - minx + 2 * pad) * scale * ss))
    h = max(2, int((maxy - miny + 2 * pad) * scale * ss))

    canvas = Image.new("RGB", (w, h), BG)
    px = canvas.load()
    zbuf = [[-1e18] * w for _ in range(h)]

    def to_screen(p):
        return ((p[0] - minx + pad) * scale * ss, (p[1] - miny + pad) * scale * ss)

    for scr, dep, tuv, tex, bright in tris:
        s0, s1, s2 = (to_screen(p) for p in scr)
        d0, d1, d2 = dep
        area = (s1[0] - s0[0]) * (s2[1] - s0[1]) - (s2[0] - s0[0]) * (s1[1] - s0[1])
        if abs(area) < 1e-9:
            continue
        xlo = max(0, int(math.floor(min(s0[0], s1[0], s2[0]))))
        xhi = min(w - 1, int(math.ceil(max(s0[0], s1[0], s2[0]))))
        ylo = max(0, int(math.floor(min(s0[1], s1[1], s2[1]))))
        yhi = min(h - 1, int(math.ceil(max(s0[1], s1[1], s2[1]))))
        texpx = tex.load()
        for y in range(ylo, yhi + 1):
            zrow = zbuf[y]
            for x in range(xlo, xhi + 1):
                cx, cy = x + 0.5, y + 0.5
                w0 = ((s1[0] - cx) * (s2[1] - cy) - (s2[0] - cx) * (s1[1] - cy)) / area
                w1 = ((s2[0] - cx) * (s0[1] - cy) - (s0[0] - cx) * (s2[1] - cy)) / area
                w2 = 1.0 - w0 - w1
                if w0 < -1e-6 or w1 < -1e-6 or w2 < -1e-6:
                    continue
                d = w0 * d0 + w1 * d1 + w2 * d2
                if d <= zrow[x]:
                    continue
                tx = int(w0 * tuv[0][0] + w1 * tuv[1][0] + w2 * tuv[2][0])
                ty = int(w0 * tuv[0][1] + w1 * tuv[1][1] + w2 * tuv[2][1])
                tx = 0 if tx < 0 else (15 if tx > 15 else tx)
                ty = 0 if ty < 0 else (15 if ty > 15 else ty)
                r, g, b, a = texpx[tx, ty]
                if a < 128:
                    continue
                zrow[x] = d
                px[x, y] = (min(255, int(r * bright)), min(255, int(g * bright)),
                            min(255, int(b * bright)))
    return canvas


names = ["grill", "stove", "induction_cooker"]
models = {}
for n in names:
    with open(os.path.join(MB, n + ".json"), encoding="utf-8") as f:
        models[n] = json.load(f)

renders = {}
for n in names:
    for view in ("back", "front"):
        renders[(n, view)] = render(models[n], view)

TILE_W = max(im.width for im in renders.values()) + 24
TILE_H = max(im.height for im in renders.values()) + 24
sheet = Image.new("RGB", (TILE_W * 2, TILE_H * len(names)), (250, 250, 252))
for row, n in enumerate(names):
    for col, view in enumerate(("back", "front")):
        im = renders[(n, view)]
        sheet.paste(im, (TILE_W * col + (TILE_W - im.width) // 2,
                         TILE_H * row + (TILE_H - im.height) // 2))

sheet.save(OUT)
print("wrote", OUT)
