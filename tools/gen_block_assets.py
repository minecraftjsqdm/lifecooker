# -*- coding: utf-8 -*-
"""Generate block textures + models + blockstates + lang for lifecooker (明日料理).

外观参考《明日之后》的厨房电器：
  烧烤炉 (grill)            —— 锈蚀铁皮炉体 + 铸铁烤网 + 前风门 + 四条矮腿，敞口木炭烤炉
  家用灶炉 (stove)          —— 奶油搪瓷外壳 + 铸铁灶台 + 双灶头 + 带观察窗的烤箱门 + 旋钮
  电磁炉 (induction_cooker) —— 深色微晶玻璃面板 + 双红色加热线圈 + 触控面板

每个方块同时导出两份产物：
  1) Minecraft 可用的资源文件 -> ../src/main/resources/assets/lifecooker/
     (textures/block/*.png, models/block/*.json, models/item/*.json, blockstates/*.json, lang/*.json)
  2) Blockbench 工程文件 .bbmodel -> ../../models/*.bbmodel （内嵌 base64 贴图，可直接在
     Blockbench 中打开继续雕琢，改完 File > Export > Java Block/Item 覆盖 models/block/*.json）

用法:  py -3 tools/gen_block_assets.py
"""
import base64
import io
import json
import os
import random
import uuid

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "lifecooker")
TEX_BLOCK = os.path.join(ASSETS, "textures", "block")
MODEL_BLOCK = os.path.join(ASSETS, "models", "block")
MODEL_ITEM = os.path.join(ASSETS, "models", "item")
BLOCKSTATES = os.path.join(ASSETS, "blockstates")
LANG = os.path.join(ASSETS, "lang")
BBMODELS = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models"))
for _d in (TEX_BLOCK, MODEL_BLOCK, MODEL_ITEM, BLOCKSTATES, LANG, BBMODELS):
    os.makedirs(_d, exist_ok=True)

FACES = ("north", "east", "south", "west", "up", "down")

# 贴图缓存: tex_key -> PIL.Image
TEXTURES = {}


# ----------------------------------------------------------------------
# texture helpers
# ----------------------------------------------------------------------
def _clamp(v):
    return max(0, min(255, int(v)))


def _shade(col, d):
    return (_clamp(col[0] + d), _clamp(col[1] + d), _clamp(col[2] + d), 255)


def make(tex_key, draw):
    """draw(px) 直接操作 16x16 的像素；结果登记进 TEXTURES 并落盘。"""
    img = Image.new("RGBA", (16, 16))
    draw(img.load())
    TEXTURES[tex_key] = img
    img.save(os.path.join(TEX_BLOCK, tex_key + ".png"))
    return tex_key


def noise(px, seed, base, amp=12, speck=None, speck_chance=0.0, speck_amp=12):
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            col = _shade(base, rnd.randint(-amp, amp))
            if speck is not None and rnd.random() < speck_chance:
                col = _shade(speck, rnd.randint(-speck_amp, speck_amp))
            px[x, y] = col


def border(px, col, width=1):
    for i in range(16):
        for d in range(width):
            px[i, d] = col
            px[i, 15 - d] = col
            px[d, i] = col
            px[15 - d, i] = col


def make_iron():
    """拉丝钢材：炉脚、把手、旋钮、边框等小件。"""
    def draw(px):
        noise(px, 11, (114, 116, 122), amp=8)
        rnd = random.Random(12)
        for x in range(16):
            d = rnd.randint(-9, 9)
            for y in range(16):
                px[x, y] = _shade(px[x, y], d)
    return make("iron", draw)


def make_grill_body():
    """烧烤炉炉体：深色铁皮 + 少量锈斑 + 两道压筋。"""
    def draw(px):
        noise(px, 21, (60, 58, 56), amp=11, speck=(104, 64, 40), speck_chance=0.10)
        for x in range(16):
            for y in (4, 11):
                px[x, y] = (40, 38, 37, 255)
            px[x, 5] = (84, 81, 78, 255)
    return make("grill_body", draw)


def make_grill_ember():
    """炉膛炭床：深色焦炭 + 透出的橙红余烬（从烤网缝隙里能看到）。"""
    def draw(px):
        rnd = random.Random(131)
        for y in range(16):
            for x in range(16):
                px[x, y] = _shade((26, 24, 24), rnd.randint(-6, 6))
        for _ in range(44):
            x, y = rnd.randint(0, 15), rnd.randint(0, 15)
            col = rnd.choice([(92, 34, 20), (146, 54, 24), (204, 94, 32), (228, 150, 48)])
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                if x + dx < 16 and y + dy < 16:
                    px[x + dx, y + dy] = _shade(col, rnd.randint(-14, 14))
    return make("grill_ember", draw)


def make_grill_bar():
    """烤网/把手钢条：暗钢 + 顶部高光。"""
    def draw(px):
        noise(px, 31, (60, 60, 65), amp=6)
        for x in range(16):
            px[x, 0] = (150, 150, 156, 255)
            px[x, 1] = (110, 110, 116, 255)
    return make("grill_bar", draw)


def make_grill_grate_top():
    """烤网易视图：横向钢条，条间为炉膛暗色。"""
    def draw(px):
        rnd = random.Random(41)
        for y in range(16):
            bar = (y % 3 != 2)
            for x in range(16):
                col = (116, 116, 122) if bar else (28, 26, 25)
                px[x, y] = _shade(col, rnd.randint(-8, 8))
                if bar and y % 3 == 0:
                    px[x, y] = _shade(col, 24)
    return make("grill_grate_top", draw)


def make_grill_door():
    """前风门：铁皮门 + 竖条通风孔 + 铰链。"""
    def draw(px):
        noise(px, 45, (66, 64, 61), amp=10)
        border(px, (40, 38, 36, 255))
        for x in (4, 6, 8, 10, 12):
            for y in range(4, 12):
                px[x, y] = (26, 24, 23, 255)
        for y in (4, 5):
            for x in range(2, 14):
                px[x, y] = (118, 114, 110, 255)
    return make("grill_door", draw)


def make_stove_body():
    """家用灶炉外壳：奶油色搪瓷 + 压边。"""
    def draw(px):
        noise(px, 51, (218, 212, 198), amp=4)
        border(px, (178, 172, 158, 255))
        for x in range(16):
            px[x, 1] = (238, 234, 224, 255)
    return make("stove_body", draw)


def make_stove_top():
    """灶台台面：低变化的深色铸铁，避免顶面出现明显色块。"""
    def draw(px):
        for y in range(16):
            for x in range(16):
                px[x, y] = (52, 52, 56, 255)
    return make("stove_top", draw)


def make_stove_burner():
    """灶头：黑色铸铁炉架 —— 外圈承火环 + 四臂十字炉架 + 中心火盖 + 火孔。"""
    def draw(px):
        noise(px, 141, (28, 27, 27), amp=4)
        cx = cy = 7.5
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
                if 5.8 <= d <= 7.2:
                    px[x, y] = (72, 70, 68, 255)
        for i in range(2, 14):
            for d in (7, 8):
                px[d, i] = (98, 96, 94, 255)
                px[i, d] = (98, 96, 94, 255)
        for y in range(6, 10):
            for x in range(6, 10):
                px[x, y] = (126, 124, 120, 255)
        for (x, y) in ((4, 8), (8, 4), (11, 7), (7, 11)):
            px[x, y] = (48, 46, 45, 255)
    return make("stove_burner", draw)


def make_stove_door():
    """烤箱门框：暗色面板 + 金属包边。"""
    def draw(px):
        noise(px, 71, (46, 46, 50), amp=5)
        border(px, (104, 104, 110, 255))
    return make("stove_door", draw)


def make_stove_glass():
    """烤箱观察窗：深色玻璃 + 内部暖光。"""
    def draw(px):
        noise(px, 75, (30, 28, 30), amp=4)
        for y in range(10, 14):
            for x in range(2, 14):
                px[x, y] = _shade((86, 58, 34), random.Random(x * 16 + y).randint(-8, 8))
        for x in range(2, 14):
            px[x, 3] = (72, 78, 88, 255)
    return make("stove_glass", draw)


def make_induction_base():
    """电磁炉底壳：深灰钢壳 + 接缝。"""
    def draw(px):
        noise(px, 81, (82, 84, 90), amp=6)
        for x in range(16):
            for y in (5, 11):
                px[x, y] = (52, 54, 60, 255)
    return make("induction_base", draw)


def make_induction_glass():
    """微晶玻璃面板：近黑 + 斜向反光。"""
    def draw(px):
        noise(px, 91, (26, 26, 32), amp=3)
        for i in range(-16, 16):
            for k in range(2):
                x, y = i + k, 15 - i
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = _shade((66, 68, 82), 0)
    return make("induction_glass", draw)


def make_induction_hob():
    """加热区：黑面板 + 红色线圈环 + 中心点。"""
    def draw(px):
        noise(px, 101, (18, 18, 24), amp=3)
        cx = cy = 7.5
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
                if 4.3 <= d <= 5.6:
                    px[x, y] = (198, 62, 44, 255)
                elif 2.5 <= d <= 3.0:
                    px[x, y] = (146, 48, 36, 255)
        for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
            px[x, y] = (206, 80, 58, 255)
    return make("induction_hob", draw)


def make_induction_panel():
    """触控面板：指示灯 + 滑条。"""
    def draw(px):
        noise(px, 111, (44, 46, 52), amp=4)
        for x in range(3, 13):
            for y in (11, 12):
                px[x, y] = (152, 154, 160, 255)
        for (x, y, col) in ((3, 6, (92, 216, 128)), (3, 8, (226, 92, 62)), (3, 10, (230, 200, 96))):
            for dy in range(2):
                for dx in range(2):
                    px[x + dx, y + dy] = (col[0], col[1], col[2], 255)
    return make("induction_panel", draw)


make_iron()
make_grill_body()
make_grill_ember()
make_grill_bar()
make_grill_grate_top()
make_grill_door()
make_stove_body()
make_stove_top()
make_stove_burner()
make_stove_door()
make_stove_glass()
make_induction_base()
make_induction_glass()
make_induction_hob()
make_induction_panel()


# ----------------------------------------------------------------------
# element helpers  (texture 用 key，导出时再决定 #ref 还是 index)
# ----------------------------------------------------------------------
def face(tex, uv=(0, 0, 16, 16)):
    return {"tex": tex, "uv": list(uv)}


def all_faces(tex, uv=(0, 0, 16, 16)):
    return {d: face(tex, uv) for d in FACES}


def cube(name, frm, to, tex, uv=(0, 0, 16, 16), **face_over):
    """默认六面同贴图；face_over 可覆盖单面，值为 (tex, uv)。"""
    faces = all_faces(tex, uv)
    for fname, spec in face_over.items():
        faces[fname] = face(spec[0], spec[1])
    return {"name": name, "from": [float(v) for v in frm], "to": [float(v) for v in to],
            "faces": faces}


# ----------------------------------------------------------------------
# 模型定义
# ----------------------------------------------------------------------
def build_grill():
    """烧烤炉：四条矮腿 + 底部置物架 + 锈铁炉体 + 顶框 + 铸铁烤网 + 前风门 + 两侧把手。"""
    e = []
    for i, (x, z) in enumerate(((1, 1), (12, 1), (1, 12), (12, 12))):
        e.append(cube("Leg%d" % i, (x, 0, z), (x + 3, 3, z + 3), "iron"))
    e.append(cube("Shelf", (2, 2, 2), (14, 2.5, 14), "iron"))
    e.append(cube("Firebox", (1, 3, 1), (15, 10, 15), "grill_body",
                  up=("grill_ember", (0, 0, 16, 16))))
    # 顶部边框（中间镂空，露出炉膛）
    e.append(cube("FrameN", (0, 10, 0), (16, 11, 1.5), "iron"))
    e.append(cube("FrameS", (0, 10, 14.5), (16, 11, 16), "iron"))
    e.append(cube("FrameW", (0, 10, 1.5), (1.5, 11, 14.5), "iron"))
    e.append(cube("FrameE", (14.5, 10, 1.5), (16, 11, 14.5), "iron"))
    e.append(cube("Firebed", (1.5, 4, 1.5), (14.5, 4.5, 14.5), "grill_bar"))
    # 铸铁烤网：7 根横条，俯视用烤网贴图
    for i in range(7):
        z = 1.5 + i * 2
        e.append(cube("Grate%d" % i, (1, 11, z), (15, 11.4, z + 1), "grill_bar",
                      up=("grill_grate_top", (0, 0, 16, 16)),
                      down=("grill_grate_top", (0, 0, 16, 16))))
    # 前风门 + 门把手
    e.append(cube("AshDoor", (4, 4, 0), (12, 8, 0.8), "grill_door"))
    e.append(cube("AshDoorKnob", (7, 8, 0), (9, 9, 1), "iron"))
    # 两侧把手
    e.append(cube("HandleW", (-1, 8, 6), (1, 9, 10), "iron"))
    e.append(cube("HandleE", (15, 8, 6), (17, 9, 10), "iron"))
    return e


def build_stove():
    """家用灶炉：矮腿 + 搪瓷炉体 + 铸铁灶台 + 双灶头 + 后挡板 + 烤箱门(带观察窗) + 旋钮。"""
    e = []
    for i, (x, z) in enumerate(((1, 1), (13, 1), (1, 13), (13, 13))):
        e.append(cube("Leg%d" % i, (x, 0, z), (x + 2, 1.5, z + 2), "iron"))
    e.append(cube("Body", (1, 1.5, 1), (15, 13, 15), "stove_body"))
    e.append(cube("Cooktop", (0, 13, 0), (16, 14.5, 16), "stove_top"))
    for i, (x0, x1, z0, z1) in enumerate(((3, 7.5, 3, 7.5), (8.5, 13, 8.5, 13))):
        e.append(cube("Burner%d" % i, (x0, 14.5, z0), (x1, 15, z1), "iron",
                      up=("stove_burner", (0, 0, 16, 16))))
    e.append(cube("BackSplash", (1, 14.5, 14), (15, 16, 15.5), "stove_body"))
    # 烤箱门边框 + 观察窗
    e.append(cube("DoorTop", (2, 7.5, 0), (14, 8.5, 0.7), "stove_door"))
    e.append(cube("DoorBottom", (2, 1.5, 0), (14, 2.5, 0.7), "stove_door"))
    e.append(cube("DoorLeft", (2, 2.5, 0), (4, 7.5, 0.7), "stove_door"))
    e.append(cube("DoorRight", (12, 2.5, 0), (14, 7.5, 0.7), "stove_door"))
    e.append(cube("OvenWindow", (4, 2.5, 0.5), (12, 7.5, 0.8), "stove_glass"))
    e.append(cube("DoorHandle", (2, 8.5, 0), (14, 9.5, 1.2), "iron"))
    # 旋钮
    e.append(cube("KnobA", (3.2, 10.5, 0), (4.9, 12, 0.9), "iron"))
    e.append(cube("KnobB", (11.1, 10.5, 0), (12.8, 12, 0.9), "iron"))
    return e


def build_induction():
    """电磁炉：矮脚 + 钢底壳 + 微晶玻璃台面 + 双红色线圈 + 触控面板。"""
    e = []
    for i, (x, z) in enumerate(((1, 1), (14, 1), (1, 14), (14, 14))):
        e.append(cube("Foot%d" % i, (x, 0, z), (x + 1, 0.6, z + 1), "iron"))
    # 总高度控制为 9.6 像素（0.6 方块），顶面布置四个红色加热线圈。
    e.append(cube("Base", (1, 0.6, 1), (15, 8.9, 15), "induction_base"))
    e.append(cube("GlassTop", (0, 8.9, 0), (16, 9.5, 16), "induction_glass"))
    for i, (x0, z0) in enumerate(((2.5, 2.5), (9.5, 2.5), (2.5, 9.5), (9.5, 9.5))):
        e.append(cube("RedRing%d" % i, (x0, 9.5, z0), (x0 + 4, 9.6, z0 + 4), "induction_hob"))
    e.append(cube("ControlPanel", (10, 9.5, 1.5), (15, 9.6, 4), "induction_panel"))
    return e


# ----------------------------------------------------------------------
# 导出: Minecraft 模型 JSON
# ----------------------------------------------------------------------
DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0],
                              "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0],
                              "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0],
                             "scale": [0.4, 0.4, 0.4]},
}


def used_textures(elements):
    seen = []
    for el in elements:
        for f in FACES:
            k = el["faces"][f]["tex"]
            if k not in seen:
                seen.append(k)
    return seen


def to_mc_model(elements, particle):
    out = []
    for el in elements:
        faces = {}
        for f in FACES:
            spec = el["faces"][f]
            faces[f] = {"uv": spec["uv"], "texture": "#" + spec["tex"]}
        out.append({"name": el["name"], "from": el["from"], "to": el["to"], "faces": faces})
    textures = {k: "lifecooker:block/" + k for k in used_textures(elements)}
    textures["particle"] = "lifecooker:block/" + particle
    return {
        "credit": "Life Cooker",
        "textures": textures,
        "elements": out,
        "display": DISPLAY,
    }


# ----------------------------------------------------------------------
# 导出: Blockbench 工程文件 .bbmodel
# ----------------------------------------------------------------------
def _data_uri(tex_key):
    buf = io.BytesIO()
    TEXTURES[tex_key].save(buf, format="PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode("ascii")


def to_bbmodel(name, elements, icon_tex=None):
    keys = used_textures(elements)
    textures = []
    for i, k in enumerate(keys):
        textures.append({
            "path": "", "name": k + ".png", "folder": "block", "namespace": "lifecooker",
            "id": str(i), "group": "", "width": 16, "height": 16, "uv_width": 16, "uv_height": 16,
            "particle": k == icon_tex, "use_as_default": False, "layers_enabled": False,
            "sync_to_project": "", "render_mode": "default", "render_sides": "auto",
            "z_fighting_offset": 0, "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
            "frame_interpolate": False, "visible": True, "mode": "bitmap", "saved": True,
            "uuid": str(uuid.uuid4()), "relative_path": "", "source": _data_uri(k),
        })
    bb_els = []
    for el in elements:
        faces = {}
        for f in FACES:
            spec = el["faces"][f]
            faces[f] = {"uv": spec["uv"], "texture": keys.index(spec["tex"])}
        bb_els.append({
            "name": el["name"], "box_uv": False, "rescale": False, "locked": False,
            "light_emission": 0, "render_order": "default", "allow_mirror_modeling": True,
            "from": el["from"], "to": el["to"], "autouv": 0, "color": 0, "origin": [0, 0, 0],
            "faces": faces, "type": "cube", "uuid": str(uuid.uuid4()),
        })
    return {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": name,
        "model_identifier": "",
        "parent": "",
        "visible_box": [1, 1, 0],
        "variable_placeholders": "",
        "variable_placeholders_separator": "_",
        "nbt_scope": "auto",
        "resolution": {"width": 16, "height": 16},
        "elements": bb_els,
        "outliner": [{
            "name": name, "origin": [0, 0, 0], "uuid": str(uuid.uuid4()), "export": True,
            "mirror_uv": False, "isOpen": True, "locked": False, "visibility": True,
            "autouv": 0, "children": [e["uuid"] for e in bb_els],
        }],
        "textures": textures,
    }


def dump(path, obj):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


BLOCKS = {
    "grill": (build_grill, "grill_body"),
    "stove": (build_stove, "stove_body"),
    "induction_cooker": (build_induction, "induction_base"),
}

for name, (builder, particle) in BLOCKS.items():
    els = builder()
    dump(os.path.join(BLOCKSTATES, name + ".json"),
         {"variants": {"": {"model": "lifecooker:block/%s" % name}}})
    dump(os.path.join(MODEL_BLOCK, name + ".json"), to_mc_model(els, particle))
    dump(os.path.join(MODEL_ITEM, name + ".json"),
         {"parent": "lifecooker:block/%s" % name})
    dump(os.path.join(BBMODELS, name + ".bbmodel"),
         to_bbmodel(name, els, icon_tex=particle))

ZH = {
    "block.lifecooker.grill": "烧烤炉",
    "block.lifecooker.stove": "家用灶炉",
    "block.lifecooker.induction_cooker": "电磁炉",
}
EN = {
    "block.lifecooker.grill": "Grill",
    "block.lifecooker.stove": "Household Stove",
    "block.lifecooker.induction_cooker": "Induction Cooker",
}
dump(os.path.join(LANG, "zh_cn.json"), dict(sorted(ZH.items())))
dump(os.path.join(LANG, "en_us.json"), dict(sorted(EN.items())))

print("generated %d block models, %d textures, %d bbmodel projects"
      % (len(BLOCKS), len(TEXTURES), len(BLOCKS)))
