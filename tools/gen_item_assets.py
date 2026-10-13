# -*- coding: utf-8 -*-
"""Generate item textures + item models + lang entries for lifecooker (明日料理).

当前覆盖：
  不可名状的食物 (unspeakable_food) —— 无效组合的产出，一碗色泽可疑的糊状物
  果酱 (jam)                       —— 4 份水果熬制，深红果酱罐
  24 道《明日之后》风格料理         —— 8 道基础（同种食材 ×4）+ 16 道组合料理

后续 P5 的食材 / 料理在此文件按同样方式追加即可（新增 _draw_xxx + ITEMS 条目）。

产物 -> ../src/main/resources/assets/lifecooker/
  textures/item/*.png, models/item/*.json, lang/{en_us,zh_cn}.json

用法:  py -3 tools/gen_item_assets.py
"""
import json
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "lifecooker")
TEX_ITEM = os.path.join(ASSETS, "textures", "item")
MODEL_ITEM = os.path.join(ASSETS, "models", "item")
LANG = os.path.join(ASSETS, "lang")
for _d in (TEX_ITEM, MODEL_ITEM, LANG):
    os.makedirs(_d, exist_ok=True)

# 调色板
OUT = (34, 32, 28, 255)
BOWL_L = (152, 150, 144, 255)
BOWL_M = (116, 114, 108, 255)
BOWL_D = (78, 76, 70, 255)
BLOB_D = (64, 76, 46, 255)
BLOB_M = (98, 112, 66, 255)
BLOB_L = (130, 146, 90, 255)
SPOT_A = (168, 134, 84, 255)
SPOT_B = (92, 68, 56, 255)

# 果酱罐
JAM_D = (124, 30, 36, 255)
JAM_M = (176, 46, 50, 255)
JAM_L = (214, 78, 72, 255)
GLASS_HL = (252, 226, 206, 255)
LID_L = (178, 176, 170, 255)
LID_M = (142, 140, 134, 255)
LID_D = (98, 96, 92, 255)
LABEL = (238, 228, 200, 255)
LABEL_D = (196, 182, 152, 255)


def _draw_unspeakable(px):
    """16x16 俯视餐具图标：石灰色浅碗 + 一坨分不清成分的墨绿糊状物。"""

    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = c

    def band(y, x0, x1, fill, left=None, right=None):
        for x in range(x0, x1 + 1):
            put(x, y, fill)
        put(x0, y, left or OUT)
        put(x1, y, right or OUT)

    # --- 碗：碗沿 y=8，逐行收窄到 y=13 ---
    band(8, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    band(9, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    band(10, 3, 12, BOWL_D, BOWL_M, BOWL_D)
    band(11, 4, 11, BOWL_D, BOWL_M, BOWL_D)
    band(12, 5, 10, BOWL_D, BOWL_M, BOWL_D)
    band(13, 6, 9, BOWL_D, BOWL_D, BOWL_D)
    for x in range(6, 10):
        put(x, 14, OUT)

    # --- 糊状物：叠在碗上的不规则团块 ---
    band(3, 6, 9, BLOB_M)
    band(4, 4, 11, BLOB_M)
    band(5, 3, 12, BLOB_M, BLOB_L, BLOB_D)
    band(6, 3, 12, BLOB_M, BLOB_L, BLOB_D)
    band(7, 4, 11, BLOB_M, BLOB_L, BLOB_D)
    band(8, 5, 10, BLOB_D, BLOB_M, BLOB_D)

    # 高光：左上受光面
    for x, y in ((5, 4), (6, 4), (4, 5), (5, 5), (4, 6), (5, 6)):
        put(x, y, BLOB_L)

    # 可疑颗粒：让「不可名状」有具体的可疑感
    for x, y, c in ((8, 5, SPOT_A), (10, 6, SPOT_B), (6, 7, SPOT_A),
                    (9, 7, SPOT_B), (7, 8, SPOT_A), (11, 5, SPOT_A)):
        put(x, y, c)


def _draw_jam(px):
    """16x16 果酱罐图标：深红果酱 + 玻璃高光 + 金属旋盖 + 米色标签。"""

    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = c

    def band(y, x0, x1, fill, left=None, right=None):
        for x in range(x0, x1 + 1):
            put(x, y, fill)
        put(x0, y, left or OUT)
        put(x1, y, right or OUT)

    # 金属旋盖
    band(3, 4, 11, LID_M, LID_L, LID_D)
    band(4, 4, 11, LID_M, LID_L, LID_D)

    # 罐身：颈肩一行，随后为直筒罐体
    band(5, 4, 11, JAM_M, JAM_L, JAM_D)
    for y in (6, 7, 8, 9, 10, 11):
        band(y, 3, 12, JAM_M, JAM_L, JAM_D)

    # 收底
    band(12, 4, 11, JAM_D, JAM_M, JAM_D)
    band(13, 5, 10, JAM_D, JAM_D, JAM_D)

    # 玻璃高光：左侧一条竖向反光
    put(4, 6, GLASS_HL)
    put(4, 7, GLASS_HL)

    # 米色标签（内缩，保留罐体轮廓）
    band(8, 5, 10, LABEL, LABEL_D, LABEL_D)
    band(9, 5, 10, LABEL_D, LABEL_D, LABEL_D)


LABEL = (238, 228, 200, 255)
LABEL_D = (196, 182, 152, 255)

# --- 24 道料理共用色板 ---
STEAM = (228, 232, 234, 205)          # 热气
DISH_L = (216, 214, 208, 255)         # 瓷盘 / 陶碗
DISH_M = (176, 174, 168, 255)
DISH_D = (120, 118, 112, 255)
MUG_L = (212, 210, 204, 255)          # 陶杯
MUG_M = (168, 166, 160, 255)
MUG_D = (110, 108, 104, 255)
WATER_M = (126, 180, 212, 255)        # 清水
WATER_L = (190, 224, 242, 255)
WOOD_L = (180, 130, 82, 255)          # 木签 / 木勺
WOOD_M = (138, 96, 56, 255)
WOOD_D = (94, 62, 36, 255)
MEAT_L = (212, 118, 94, 255)          # 生肉色
MEAT_M = (172, 78, 64, 255)
MEAT_D = (124, 52, 46, 255)
SEAR_M = (116, 60, 42, 255)           # 炙烤焦面
SEAR_D = (78, 40, 30, 255)
VEG_L = (130, 176, 88, 255)           # 叶菜
VEG_M = (84, 128, 60, 255)
VEG_D = (52, 86, 42, 255)
CARROT_L = (248, 176, 88, 255)        # 胡萝卜
CARROT_M = (222, 130, 42, 255)
CARROT_D = (176, 88, 22, 255)
BERRY_L = (240, 112, 92, 255)         # 红果
BERRY_M = (204, 62, 66, 255)
BERRY_D = (156, 32, 48, 255)
GRAPE_L = (172, 120, 210, 255)        # 紫果
GRAPE_M = (124, 74, 168, 255)
GRAPE_D = (78, 42, 116, 255)
STEM_L = (232, 222, 196, 255)         # 菌柄
STEM_M = (204, 188, 156, 255)
STEM_D = (156, 138, 112, 255)
CAP_L = (230, 116, 96, 255)           # 红菇伞盖
CAP_M = (194, 70, 60, 255)
CAP_D = (144, 40, 38, 255)
MUSH_L = (200, 156, 108, 255)         # 棕菇伞盖
MUSH_M = (158, 112, 70, 255)
MUSH_D = (112, 76, 48, 255)
DOUGH_L = (242, 216, 166, 255)        # 面团 / 面包瓤
DOUGH_M = (212, 178, 120, 255)
DOUGH_D = (168, 132, 80, 255)
CRUST_L = (222, 170, 98, 255)         # 酥皮 / 面包壳
CRUST_M = (182, 124, 62, 255)
CRUST_D = (136, 86, 40, 255)
CHEESE_L = (250, 226, 150, 255)       # 奶酪
CHEESE_M = (236, 194, 90, 255)
HONEY_L = (252, 216, 106, 255)        # 蜂蜜
HONEY_M = (230, 176, 40, 255)
HONEY_D = (186, 132, 20, 255)
MILK_L = (250, 248, 244, 255)         # 牛奶 / 奶油
MILK_M = (230, 228, 222, 255)
MILK_D = (188, 186, 180, 255)
FISH_L = (196, 220, 236, 255)         # 鱼身
FISH_M = (140, 170, 196, 255)
FISH_D = (86, 116, 142, 255)


# --- 新增食材色板 ---
RICE_L = (250, 248, 240, 255)         # 米粒
RICE_M = (226, 222, 208, 255)
RICE_D = (182, 176, 158, 255)
COCO_L = (128, 88, 54, 255)           # 椰壳
COCO_M = (98, 64, 38, 255)
COCO_D = (64, 40, 24, 255)
COCO_F = (246, 242, 232, 255)         # 椰肉 / 椰丝
BLUE_L = (128, 150, 226, 255)         # 蓝莓
BLUE_M = (78, 96, 186, 255)
BLUE_D = (44, 56, 128, 255)
CHILI_L = (226, 66, 52, 255)          # 辣椒
CHILI_M = (186, 36, 34, 255)
CHILI_D = (128, 20, 24, 255)
TOMATO_L = (238, 86, 66, 255)         # 番茄
TOMATO_M = (198, 46, 40, 255)
TOMATO_D = (140, 24, 26, 255)
LEAF_L = (120, 176, 74, 255)          # 蒂 / 葱花
LEAF_M = (78, 132, 52, 255)
LEAF_D = (46, 88, 36, 255)
EGG_Y_L = (252, 226, 130, 255)        # 蛋液
EGG_Y = (244, 200, 78, 255)
EGG_Y_D = (206, 156, 44, 255)
SHRIMP_L = (250, 168, 128, 255)       # 虾
SHRIMP_M = (226, 118, 86, 255)
SHRIMP_D = (168, 66, 52, 255)
OIL_L = (250, 216, 118, 255)          # 食用油
OIL_M = (232, 178, 52, 255)
OIL_D = (176, 122, 24, 255)


def _put(px, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        px[x, y] = c


def _hline(px, y, x0, x1, c):
    for x in range(x0, x1 + 1):
        _put(px, x, y, c)


def _vline(px, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        _put(px, x, y, c)


def _rect(px, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        _hline(px, y, x0, x1, c)


def _row(px, y, x0, x1, fill, left=None, right=None):
    """画一行并给两端换色（默认描边色 OUT）。"""
    _hline(px, y, x0, x1, fill)
    _put(px, x0, y, left or OUT)
    _put(px, x1, y, right or OUT)


def _ellipse(px, cx, cy, rx, ry, c):
    for y in range(int(cy - ry), int(cy + ry) + 1):
        for x in range(int(cx - rx), int(cx + rx) + 1):
            if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0:
                _put(px, x, y, c)


# ----------------------------------------------------------------------
# 基础料理（同种食材 ×4）
# ----------------------------------------------------------------------

def _draw_grilled_meat(px):
    """烤肉：一块带炙烤纹与焦边的厚切肉排。"""
    _rect(px, 4, 4, 11, 11, MEAT_M)
    _row(px, 4, 4, 11, MEAT_L, MEAT_D, MEAT_D)
    _row(px, 5, 4, 11, MEAT_M, MEAT_D, MEAT_D)
    for y in (7, 9):
        _hline(px, y, 5, 10, SEAR_D)
    _row(px, 10, 4, 11, SEAR_M, SEAR_D, SEAR_D)
    _row(px, 11, 4, 11, SEAR_D, OUT, OUT)
    _vline(px, 3, 5, 11, MEAT_D)
    _vline(px, 12, 5, 11, MEAT_D)
    _put(px, 3, 4, OUT)
    _put(px, 12, 4, OUT)


def _draw_roast_vegetables(px):
    """蒸烤蔬菜：浅盘里码着胡萝卜、土豆与绿叶菜。"""
    _row(px, 11, 2, 13, DISH_M, DISH_L, DISH_D)
    _row(px, 12, 3, 12, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 5, 10, DISH_D, DISH_M, DISH_D)
    _rect(px, 3, 6, 6, 10, CARROT_M)
    _row(px, 6, 3, 6, CARROT_L, CARROT_D, CARROT_D)
    _row(px, 10, 3, 6, CARROT_D, CARROT_D, CARROT_D)
    _rect(px, 7, 7, 10, 10, DOUGH_M)
    _row(px, 7, 7, 10, DOUGH_L, DOUGH_D, DOUGH_D)
    _row(px, 10, 7, 10, DOUGH_D, DOUGH_D, DOUGH_D)
    _vline(px, 11, 7, 8, VEG_D)
    _rect(px, 10, 4, 13, 6, VEG_M)
    _row(px, 4, 10, 13, VEG_L, VEG_D, VEG_D)
    _row(px, 6, 10, 13, VEG_D, VEG_D, VEG_D)


def _draw_toast_bread(px):
    """烤面包：一片方形吐司，外皮焦脆、内里米白。"""
    _rect(px, 3, 4, 12, 12, CRUST_M)
    _row(px, 4, 3, 12, CRUST_L, CRUST_D, CRUST_D)
    _row(px, 12, 3, 12, CRUST_D, OUT, OUT)
    _vline(px, 3, 4, 12, CRUST_D)
    _vline(px, 12, 4, 12, CRUST_D)
    _put(px, 3, 4, OUT)
    _put(px, 12, 4, OUT)
    _rect(px, 5, 6, 10, 10, DOUGH_M)
    _hline(px, 6, 5, 10, DOUGH_L)
    _hline(px, 10, 5, 10, DOUGH_D)
    for x, y in ((6, 7), (9, 8), (7, 9)):
        _put(px, x, y, CRUST_M)


def _draw_roast_mushroom(px):
    """烤蘑菇：两朵带焦边的蘑菇，伞盖饱满。"""

    def shroom(cx, top, cap_d, cap_m, cap_l):
        _ellipse(px, cx, top, 3.0, 2.0, cap_m)
        _hline(px, top - 1, cx - 2, cx + 2, cap_l)
        _hline(px, top + 2, cx - 2, cx + 2, cap_d)
        _put(px, cx - 1, top, cap_d)
        _put(px, cx + 1, top + 1, cap_d)
        _rect(px, cx - 1, top + 3, cx + 1, top + 5, STEM_M)
        _vline(px, cx + 1, top + 3, top + 5, STEM_D)
        _hline(px, top + 5, cx - 1, cx + 1, STEM_D)

    shroom(5, 6, MUSH_D, MUSH_M, MUSH_L)
    shroom(10, 7, CAP_D, CAP_M, CAP_L)


def _draw_grilled_fish(px):
    """烤鱼：横卧的整条鱼，鱼鳍与尾部完整，表面微焦。"""
    _ellipse(px, 7, 8, 5.0, 3.0, FISH_M)
    _hline(px, 5, 4, 9, FISH_L)
    _hline(px, 11, 3, 9, FISH_D)
    for y in range(6, 11):
        _hline(px, y, 11, 13 - abs(y - 8), FISH_M)
    _hline(px, 4, 6, 8, FISH_D)
    _put(px, 4, 7, OUT)
    _put(px, 4, 8, FISH_L)
    for x in (6, 8):
        _vline(px, x, 6, 9, CRUST_M)


def _draw_hot_water(px):
    """热水：灰陶杯里盛着清水，杯口升起两道热气。"""
    _rect(px, 4, 7, 11, 13, MUG_M)
    _row(px, 7, 4, 11, MUG_L, MUG_D, MUG_D)
    _row(px, 13, 4, 11, MUG_D, OUT, OUT)
    _vline(px, 4, 7, 13, MUG_D)
    _vline(px, 11, 7, 13, MUG_D)
    _put(px, 12, 9, MUG_M)
    _put(px, 13, 9, MUG_M)
    _put(px, 13, 10, MUG_M)
    _put(px, 13, 11, MUG_M)
    _put(px, 12, 8, MUG_D)
    _put(px, 12, 12, MUG_D)
    _row(px, 8, 4, 11, WATER_M, MUG_D, MUG_D)
    _hline(px, 8, 6, 9, WATER_L)
    for x, y0 in ((6, 3), (9, 4)):
        for y in range(y0, 7):
            _put(px, x, y, STEAM)


def _draw_hot_milk(px):
    """热牛奶：白瓷杯里的温热牛奶，杯口冒热气。"""
    _rect(px, 4, 5, 11, 13, MILK_M)
    _row(px, 5, 4, 11, MILK_L, MILK_D, MILK_D)
    _row(px, 13, 4, 11, MILK_D, OUT, OUT)
    _vline(px, 4, 5, 13, MILK_D)
    _vline(px, 11, 5, 13, MILK_D)
    _hline(px, 6, 6, 9, MILK_L)
    _put(px, 12, 9, MILK_M)
    _put(px, 13, 9, MILK_M)
    _put(px, 13, 10, MILK_M)
    _put(px, 13, 11, MILK_M)
    _put(px, 12, 8, MILK_D)
    _put(px, 12, 12, MILK_D)
    for x, y0 in ((7, 2), (9, 3)):
        for y in range(y0, 5):
            _put(px, x, y, STEAM)


def _draw_honey_syrup(px):
    """蜜糖：矮胖玻璃罐里的琥珀色蜂蜜，插着一支木勺。"""
    _vline(px, 10, 2, 6, WOOD_M)
    _put(px, 10, 2, WOOD_L)
    _row(px, 4, 4, 11, LID_M, LID_L, LID_D)
    _row(px, 5, 4, 11, LID_D, LID_M, LID_D)
    for y in (6, 7, 8, 9, 10, 11):
        _row(px, y, 3, 12, HONEY_M, HONEY_L, HONEY_D)
    _row(px, 12, 4, 11, HONEY_D, HONEY_M, HONEY_D)
    _row(px, 13, 5, 10, HONEY_D, HONEY_D, HONEY_D)
    _hline(px, 6, 5, 9, HONEY_L)
    _put(px, 4, 8, HONEY_L)
    _put(px, 4, 9, HONEY_L)


# ----------------------------------------------------------------------
# 双食材组合料理
# ----------------------------------------------------------------------

def _draw_kebab(px):
    """肉串：木签斜穿烤肉与果肉各两块。"""
    for i in range(11):
        _put(px, 3 + i, 12 - i, WOOD_M)
    _put(px, 3, 12, WOOD_L)
    for cx, cy, m, l, d in ((5, 10, MEAT_M, MEAT_L, MEAT_D),
                            (7, 8, BERRY_M, BERRY_L, BERRY_D),
                            (9, 6, MEAT_M, MEAT_L, MEAT_D),
                            (11, 4, BERRY_M, BERRY_L, BERRY_D)):
        _rect(px, cx - 1, cy - 1, cx + 1, cy + 1, m)
        _put(px, cx - 1, cy - 1, l)
        _put(px, cx + 1, cy + 1, d)


def _draw_meatball(px):
    """肉丸子：白盘里盛着三颗酱色肉丸。"""
    _row(px, 12, 2, 13, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 4, 11, DISH_D, DISH_M, DISH_D)
    for cx, cy in ((5, 8), (9, 7), (7, 10)):
        _ellipse(px, cx, cy, 2.2, 2.2, MEAT_M)
        _put(px, cx - 1, cy - 1, MEAT_L)
        _put(px, cx + 1, cy + 1, MEAT_D)


def _draw_meat_pie(px):
    """肉馅千层饼：金黄酥皮层层堆叠，缝隙露出肉馅。"""
    for y, m, l, d in ((4, DOUGH_M, DOUGH_L, DOUGH_D),
                       (6, CRUST_M, CRUST_L, CRUST_D),
                       (8, DOUGH_M, DOUGH_L, DOUGH_D),
                       (10, CRUST_M, CRUST_L, CRUST_D)):
        _row(px, y, 3, 12, m, l, d)
        _row(px, y + 1, 3, 12, d, m, d)
    _hline(px, 7, 5, 10, MEAT_M)
    _row(px, 12, 4, 11, CRUST_D, CRUST_D, CRUST_D)


def _draw_mushroom_pie(px):
    """蘑菇千层饼：酥皮盖上嵌着蘑菇片。"""
    _ellipse(px, 8, 9, 5.5, 4.0, CRUST_M)
    _hline(px, 5, 5, 10, CRUST_L)
    _hline(px, 12, 4, 11, CRUST_D)
    _hline(px, 13, 6, 10, OUT)
    for cx, cy in ((5, 7), (8, 6), (11, 8)):
        _ellipse(px, cx, cy, 2.0, 1.6, MUSH_M)
        _hline(px, cy - 1, cx - 1, cx + 1, MUSH_L)
        _hline(px, cy + 1, cx - 1, cx + 1, MUSH_D)


def _draw_fruit_bread(px):
    """果味面包：椭圆面包上嵌着红紫果粒。"""
    _ellipse(px, 8, 8, 5.5, 4.5, DOUGH_M)
    _hline(px, 4, 6, 10, DOUGH_L)
    _hline(px, 12, 5, 11, DOUGH_D)
    _hline(px, 13, 6, 10, CRUST_D)
    for x, y, c in ((6, 7, BERRY_M), (9, 6, GRAPE_M), (10, 9, BERRY_M),
                    (5, 9, GRAPE_M), (8, 10, BERRY_D), (7, 5, GRAPE_L)):
        _put(px, x, y, c)


def _draw_fruit_popsicle(px):
    """果味冰棒：红色冰体裹着糖霜，下端露出木棍。"""
    _rect(px, 4, 2, 11, 11, BERRY_M)
    _ellipse(px, 8, 4, 4.0, 2.5, BERRY_M)
    _hline(px, 2, 6, 9, BERRY_L)
    _vline(px, 4, 3, 11, BERRY_L)
    _vline(px, 11, 3, 11, BERRY_D)
    _hline(px, 11, 4, 11, BERRY_D)
    for i in range(3):
        _hline(px, 6 + i * 2, 5 + i, 10, (255, 255, 255, 120))
    _rect(px, 7, 12, 8, 14, WOOD_M)
    _vline(px, 6, 12, 14, WOOD_D)
    _vline(px, 9, 12, 14, WOOD_D)


def _draw_honeyed_meat(px):
    """蜜汁烤肉：刷了蜜釉的肉排闪着琥珀色光泽。"""
    _rect(px, 3, 4, 12, 11, MEAT_M)
    _row(px, 4, 3, 12, MEAT_L, MEAT_D, MEAT_D)
    _row(px, 11, 3, 12, SEAR_D, OUT, OUT)
    _vline(px, 3, 4, 11, MEAT_D)
    _vline(px, 12, 4, 11, MEAT_D)
    for x in (5, 7, 9):
        _vline(px, x, 5, 9, HONEY_M)
        _put(px, x, 4, HONEY_L)
    _hline(px, 10, 4, 11, HONEY_D)


def _draw_candied_fruit(px):
    """水果蜜饯：三颗裹着糖衣的果肉，表面糖霜点点。"""
    _row(px, 12, 3, 12, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 5, 10, DISH_D, DISH_M, DISH_D)
    for cx, cy, m, l, d in ((5, 9, BERRY_M, BERRY_L, BERRY_D),
                            (8, 7, GRAPE_M, GRAPE_L, GRAPE_D),
                            (11, 9, BERRY_M, BERRY_L, BERRY_D)):
        _ellipse(px, cx, cy, 2.4, 2.4, m)
        _put(px, cx - 1, cy - 1, l)
        _put(px, cx, cy - 2, (250, 246, 226, 255))
        _put(px, cx + 1, cy + 1, d)


def _draw_fruit_milk(px):
    """水果牛奶：玻璃杯中的奶白色饮品，表面浮着果粒与吸管。"""
    _rect(px, 4, 4, 11, 13, MILK_M)
    _row(px, 4, 4, 11, MILK_L, MILK_D, MILK_D)
    _row(px, 13, 4, 11, MILK_D, OUT, OUT)
    _vline(px, 4, 4, 13, MILK_L)
    _vline(px, 11, 4, 13, MILK_L)
    _vline(px, 5, 6, 11, MILK_L)
    for x, y, c in ((7, 6, BERRY_M), (9, 7, GRAPE_M), (6, 8, BERRY_M)):
        _put(px, x, y, c)
    _hline(px, 3, 9, 10, BERRY_L)
    _put(px, 10, 4, BERRY_L)


def _draw_cream_soup(px):
    """奶油浓汤：陶碗里的浓稠奶汤，撒着香料碎。"""
    _row(px, 8, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    _row(px, 9, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    _row(px, 10, 3, 12, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 11, 4, 11, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 12, 5, 10, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 13, 6, 9, BOWL_D, BOWL_D, BOWL_D)
    _ellipse(px, 8, 8, 4.5, 2.0, MILK_M)
    _hline(px, 7, 5, 10, MILK_L)
    _hline(px, 9, 5, 10, MILK_D)
    for x, y, c in ((6, 8, VEG_M), (9, 7, CARROT_M), (10, 9, VEG_D)):
        _put(px, x, y, c)


# ----------------------------------------------------------------------
# 三 / 四食材组合料理
# ----------------------------------------------------------------------

def _draw_dumpling(px):
    """水饺：三只褶边饺子，薄皮透出肉馅。"""
    _row(px, 12, 3, 12, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 5, 10, DISH_D, DISH_M, DISH_D)
    for cx, cy in ((5, 9), (8, 7), (11, 9)):
        _ellipse(px, cx, cy, 2.6, 1.9, DOUGH_M)
        _hline(px, cy - 1, cx - 2, cx + 2, DOUGH_L)
        _hline(px, cy + 1, cx - 2, cx + 2, DOUGH_D)
        _put(px, cx - 1, cy, MEAT_M)
        _put(px, cx + 1, cy, MEAT_D)


def _draw_sandwich(px):
    """三明治：两片面包夹着肉与生菜。"""
    _rect(px, 3, 9, 12, 12, DOUGH_M)
    _row(px, 9, 3, 12, DOUGH_L, DOUGH_D, DOUGH_D)
    _row(px, 12, 3, 12, CRUST_D, OUT, OUT)
    _row(px, 8, 3, 12, VEG_M, VEG_D, VEG_D)
    _row(px, 7, 3, 12, MEAT_M, MEAT_D, MEAT_D)
    _rect(px, 3, 4, 12, 6, DOUGH_M)
    _row(px, 4, 3, 12, DOUGH_L, DOUGH_D, DOUGH_D)
    _put(px, 5, 5, DOUGH_D)
    _put(px, 10, 5, DOUGH_D)


def _draw_fruit_pie(px):
    """水果派：圆派上编着格纹酥皮，缝隙透出果馅。"""
    _ellipse(px, 8, 8, 5.5, 4.5, CRUST_M)
    _hline(px, 4, 6, 10, CRUST_L)
    _hline(px, 12, 5, 11, CRUST_D)
    _hline(px, 13, 6, 10, OUT)
    _ellipse(px, 8, 8, 4.0, 3.0, BERRY_M)
    for y in (6, 9):
        _hline(px, y, 5, 11, CRUST_M)
    for x in (6, 9, 11):
        _vline(px, x, 5, 11, CRUST_M)


def _draw_milk_pudding(px):
    """牛奶布丁：倒扣的奶白布丁顶着焦糖，旁边缀一颗果粒。"""
    _row(px, 12, 2, 13, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 4, 11, DISH_D, DISH_M, DISH_D)
    _row(px, 7, 6, 9, MILK_L, MILK_D, MILK_D)
    _row(px, 8, 5, 10, MILK_M, MILK_L, MILK_D)
    _row(px, 9, 5, 10, MILK_M, MILK_L, MILK_D)
    _row(px, 10, 4, 11, MILK_M, MILK_L, MILK_D)
    _row(px, 11, 4, 11, MILK_D, MILK_M, MILK_D)
    _row(px, 6, 6, 9, HONEY_M, HONEY_L, HONEY_D)
    _put(px, 7, 5, HONEY_L)
    _put(px, 11, 8, BERRY_M)


def _draw_fruit_cake(px):
    """水果蛋糕：奶油裱花与果粒装点的蛋糕切块。"""
    _rect(px, 3, 8, 12, 13, DOUGH_M)
    _row(px, 8, 3, 12, DOUGH_L, CRUST_D, CRUST_D)
    _row(px, 13, 3, 12, CRUST_D, OUT, OUT)
    _hline(px, 10, 4, 11, CRUST_M)
    _row(px, 7, 3, 12, MILK_L, MILK_D, MILK_D)
    _row(px, 6, 4, 11, MILK_M, MILK_L, MILK_D)
    _put(px, 5, 6, MILK_L)
    _put(px, 10, 6, MILK_L)
    for x, m in ((5, BERRY_M), (8, GRAPE_M), (11, BERRY_M)):
        _put(px, x, 5, m)
        _put(px, x, 4, BERRY_L)


def _draw_special_pizza(px):
    """特制披萨：切角披萨上铺着肉丁、蘑菇片与菜叶。"""
    _ellipse(px, 8, 8, 5.5, 5.5, CRUST_M)
    _ellipse(px, 8, 8, 4.5, 4.5, CHEESE_M)
    _hline(px, 4, 6, 10, CRUST_L)
    _hline(px, 13, 6, 10, OUT)
    _put(px, 5, 6, CHEESE_L)
    for x, y, c in ((6, 6, MEAT_M), (9, 7, MUSH_M), (7, 9, VEG_M),
                    (10, 10, MEAT_M), (11, 7, VEG_M), (8, 11, MUSH_M)):
        _put(px, x, y, c)


# ----------------------------------------------------------------------
# 新增食材
# ----------------------------------------------------------------------

def _draw_rice(px):
    """大米：一捧堆起的白米，颗粒分明。"""
    for y, x0, x1 in ((6, 7, 8), (7, 5, 10), (8, 4, 11), (9, 3, 12),
                      (10, 3, 12), (11, 4, 11)):
        _hline(px, y, x0, x1, RICE_M)
    _hline(px, 6, 7, 8, RICE_L)
    _hline(px, 11, 4, 11, RICE_D)
    for x, y, c in ((6, 8, RICE_L), (9, 8, RICE_L), (5, 10, RICE_D), (8, 9, RICE_L),
                    (11, 9, RICE_D), (7, 10, RICE_L), (10, 11, RICE_D)):
        _put(px, x, y, c)


def _draw_coconut(px):
    """椰子：棕色椰壳剖面，露出雪白椰肉。"""
    _ellipse(px, 8, 9, 4.5, 4.0, COCO_M)
    _hline(px, 6, 7, 9, COCO_L)
    _hline(px, 13, 7, 9, COCO_D)
    _ellipse(px, 8, 7, 3.4, 1.8, COCO_F)
    _hline(px, 6, 6, 10, (255, 255, 255, 255))
    _put(px, 4, 9, COCO_L)
    _put(px, 12, 10, COCO_D)
    for x, y in ((5, 10), (11, 11), (8, 12), (6, 12)):
        _put(px, x, y, COCO_D)


def _draw_blueberry(px):
    """蓝莓：三颗饱满的蓝紫浆果，顶端带花萼。"""
    for cx, cy in ((6, 9), (10, 10), (8, 6)):
        _ellipse(px, cx, cy, 2.3, 2.3, BLUE_M)
        _put(px, cx - 1, cy - 1, BLUE_L)
        _put(px, cx + 1, cy + 1, BLUE_D)
        _hline(px, cy - 2, cx - 1, cx + 1, BLUE_D)


def _draw_chili(px):
    """辣椒：细长红椒，尾端收尖，顶端带绿蒂。"""
    for x, y in ((4, 12), (5, 11), (6, 10), (7, 9), (8, 8), (9, 7), (10, 6), (11, 5)):
        _put(px, x, y, CHILI_M)
        _put(px, x + 1, y, CHILI_L)
        _put(px, x, y + 1, CHILI_D)
        _put(px, x + 1, y + 1, CHILI_M)
    _put(px, 12, 4, CHILI_D)
    _put(px, 3, 13, LEAF_D)
    _put(px, 4, 13, LEAF_M)
    _put(px, 3, 12, LEAF_M)
    _put(px, 4, 12, LEAF_D)


def _draw_tomato(px):
    """番茄：饱满的红番茄，顶部有绿色蒂叶。"""
    _ellipse(px, 8, 9, 4.5, 4.0, TOMATO_M)
    _hline(px, 6, 6, 10, TOMATO_L)
    _hline(px, 12, 7, 9, TOMATO_D)
    _put(px, 5, 8, TOMATO_L)
    _put(px, 11, 11, TOMATO_D)
    _hline(px, 4, 7, 9, LEAF_D)
    _put(px, 8, 4, LEAF_M)
    _put(px, 6, 4, LEAF_M)
    _put(px, 10, 4, LEAF_D)
    _put(px, 8, 3, LEAF_L)
    _put(px, 5, 6, LEAF_M)
    _put(px, 11, 6, LEAF_D)


def _draw_shrimp(px):
    """鲜虾：弯身蜷曲，带节纹、尾扇与触须。"""
    for x, y in ((5, 6), (4, 7), (4, 8), (5, 9), (6, 10), (7, 11), (8, 11), (9, 11),
                 (10, 10), (10, 9)):
        _put(px, x, y, SHRIMP_M)
        _put(px, x, y + 1, SHRIMP_D)
    _put(px, 5, 5, SHRIMP_L)
    _put(px, 4, 6, SHRIMP_L)
    _put(px, 3, 6, SHRIMP_D)
    _put(px, 8, 12, SHRIMP_D)
    _put(px, 11, 8, SHRIMP_L)
    _put(px, 12, 8, SHRIMP_M)
    _put(px, 12, 7, SHRIMP_D)
    _put(px, 3, 5, SHRIMP_D)
    _put(px, 2, 4, SHRIMP_D)


def _draw_cooking_oil(px):
    """食用油：细颈玻璃瓶里的金黄油液，瓶颈塞着木塞。"""
    _row(px, 2, 6, 9, WOOD_M, WOOD_L, WOOD_D)
    _row(px, 3, 7, 8, OIL_D, OIL_D, OIL_D)
    _row(px, 4, 7, 8, OIL_M, OIL_L, OIL_D)
    for y in (5, 6, 7, 8, 9, 10):
        _row(px, y, 4, 11, OIL_M, OIL_L, OIL_D)
    _row(px, 11, 4, 11, OIL_D, OIL_M, OIL_D)
    _row(px, 12, 5, 10, OIL_D, OIL_D, OIL_D)
    _hline(px, 5, 5, 10, OIL_L)
    _put(px, 5, 7, GLASS_HL)
    _put(px, 5, 8, GLASS_HL)


# ----------------------------------------------------------------------
# 《明日之后》新增料理
# ----------------------------------------------------------------------

def _bowl(px):
    """共用陶碗轮廓（碗沿 y=8/9，逐行收窄到 y=13）。"""
    _row(px, 8, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    _row(px, 9, 2, 13, BOWL_M, BOWL_L, BOWL_D)
    _row(px, 10, 3, 12, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 11, 4, 11, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 12, 5, 10, BOWL_D, BOWL_M, BOWL_D)
    _row(px, 13, 6, 9, BOWL_D, BOWL_D, BOWL_D)


def _plate(px):
    """共用浅盘轮廓。"""
    _row(px, 12, 2, 13, DISH_M, DISH_L, DISH_D)
    _row(px, 13, 4, 11, DISH_D, DISH_M, DISH_D)


def _draw_mushroom_kebab(px):
    """蘑菇肉串：木签上交替串着肉块与蘑菇。"""
    for i in range(11):
        _put(px, 3 + i, 12 - i, WOOD_M)
    _put(px, 3, 12, WOOD_L)
    _put(px, 13, 2, WOOD_D)
    for cx, cy, m, l, d in ((5, 10, MEAT_M, MEAT_L, MEAT_D),
                            (7, 8, MUSH_M, MUSH_L, MUSH_D),
                            (9, 6, MEAT_M, MEAT_L, MEAT_D),
                            (11, 4, CAP_M, CAP_L, CAP_D)):
        _rect(px, cx - 1, cy - 1, cx + 1, cy + 1, m)
        _put(px, cx - 1, cy - 1, l)
        _put(px, cx + 1, cy + 1, d)


def _draw_spicy_diced_meat(px):
    """麻辣肉丁：白盘里油亮的肉丁与红椒段交杂。"""
    _plate(px)
    for x, y in ((4, 9), (6, 8), (8, 9), (10, 8), (5, 11), (7, 10), (9, 11), (11, 10)):
        _rect(px, x - 1, y, x + 1, y + 1, MEAT_M)
        _put(px, x - 1, y, MEAT_L)
        _put(px, x, y + 1, MEAT_D)
    for x, y in ((5, 7), (9, 7), (7, 9)):
        _hline(px, y, x, x + 1, CHILI_M)
        _put(px, x, y, CHILI_L)
    _put(px, 6, 11, OIL_L)


def _draw_orange_steamed_egg(px):
    """橙子蒸蛋：橙壳做盅，里面是嫩黄的蒸蛋。"""
    _ellipse(px, 8, 9, 5.0, 4.0, CARROT_M)
    _hline(px, 9, 4, 12, CARROT_L)
    _hline(px, 12, 6, 10, CARROT_D)
    _put(px, 4, 9, CARROT_D)
    _put(px, 12, 10, CARROT_D)
    _ellipse(px, 8, 7, 3.6, 1.8, EGG_Y)
    _hline(px, 6, 6, 10, EGG_Y_L)
    _hline(px, 8, 7, 9, EGG_Y_D)
    _put(px, 9, 7, LEAF_M)


def _draw_honeyed_mushroom(px):
    """蜜烧蘑菇：两朵蘑菇裹着油亮蜜汁。"""
    for cx, cy in ((6, 8), (10, 8)):
        _ellipse(px, cx, cy, 2.8, 1.9, MUSH_M)
        _hline(px, cy - 1, cx - 1, cx + 1, MUSH_L)
        _hline(px, cy + 1, cx - 1, cx + 1, MUSH_D)
        _rect(px, cx - 1, cy + 2, cx + 1, cy + 4, STEM_M)
        _hline(px, cy + 4, cx - 1, cx + 1, STEM_D)
        _hline(px, cy, cx, cx + 1, HONEY_M)
        _put(px, cx, cy, HONEY_L)
    _put(px, 8, 6, HONEY_L)


def _draw_tomato_stewed_meat(px):
    """番茄炖肉：陶碗里浓稠的番茄肉汤，浮着肉块。"""
    _bowl(px)
    _ellipse(px, 8, 8, 4.5, 2.0, TOMATO_M)
    _hline(px, 7, 5, 10, TOMATO_L)
    for x, y, c in ((6, 8, MEAT_M), (9, 8, MEAT_L), (8, 9, MEAT_D), (11, 8, MEAT_M)):
        _put(px, x, y, c)
    _put(px, 6, 7, LEAF_M)
    _put(px, 10, 8, LEAF_D)


def _draw_meat_egg_dumpling(px):
    """鲜肉蛋饺：金黄蛋皮裹着肉馅的饺子。"""
    _plate(px)
    for cx, cy in ((5, 9), (8, 7), (11, 9)):
        _ellipse(px, cx, cy, 2.6, 1.9, EGG_Y)
        _hline(px, cy - 1, cx - 2, cx + 2, EGG_Y_L)
        _hline(px, cy + 1, cx - 2, cx + 2, EGG_Y_D)
        _put(px, cx - 1, cy, MEAT_M)
        _put(px, cx + 1, cy, MEAT_D)


def _draw_veggie_fruit_salad(px):
    """蔬果沙拉：白盘里生菜叶衬着各色果块。"""
    _plate(px)
    _ellipse(px, 6, 9, 3.0, 2.2, VEG_M)
    _ellipse(px, 10, 9, 2.8, 2.0, VEG_M)
    _put(px, 4, 8, VEG_L)
    _put(px, 8, 10, VEG_D)
    _put(px, 12, 10, VEG_D)
    for x, y, c in ((6, 8, BERRY_M), (8, 7, TOMATO_M), (11, 8, GRAPE_M),
                    (7, 10, CARROT_M), (10, 10, BERRY_D), (5, 10, CARROT_D)):
        _put(px, x, y, c)


def _draw_vegetable_pie(px):
    """蔬菜千层饼：酥皮盖着，缝隙透出青绿菜馅。"""
    for y, m, l, d in ((4, DOUGH_M, DOUGH_L, DOUGH_D),
                       (6, CRUST_M, CRUST_L, CRUST_D),
                       (8, DOUGH_M, DOUGH_L, DOUGH_D),
                       (10, CRUST_M, CRUST_L, CRUST_D)):
        _row(px, y, 3, 12, m, l, d)
        _row(px, y + 1, 3, 12, d, m, d)
    _hline(px, 7, 5, 10, VEG_M)
    _hline(px, 9, 5, 10, VEG_D)
    _row(px, 12, 4, 11, CRUST_D, CRUST_D, CRUST_D)
    _put(px, 6, 7, VEG_L)
    _put(px, 9, 7, CARROT_M)


def _draw_mushroom_udon(px):
    """蘑菇乌冬面：陶碗清汤粗面，浮着蘑菇片与葱花。"""
    _bowl(px)
    _ellipse(px, 8, 8, 4.5, 2.0, MILK_M)
    _hline(px, 7, 5, 10, MILK_L)
    for y in (7, 9):
        _hline(px, y, 5, 10, DOUGH_L)
        _hline(px, y + 1, 6, 9, DOUGH_M)
    _ellipse(px, 6, 6, 1.8, 1.2, MUSH_M)
    _ellipse(px, 10, 6, 1.8, 1.2, MUSH_M)
    _put(px, 6, 5, MUSH_L)
    _put(px, 10, 5, MUSH_L)
    _put(px, 8, 8, LEAF_M)


def _draw_coconut_milk_cake(px):
    """椰奶雪花糕：切成方块的白玉糕，表面裹着椰丝。"""
    _plate(px)
    for cx, cy in ((5, 9), (9, 8), (11, 10)):
        _rect(px, cx - 2, cy - 2, cx + 1, cy + 1, COCO_F)
        _hline(px, cy - 2, cx - 2, cx + 1, (255, 255, 255, 255))
        _hline(px, cy + 1, cx - 2, cx + 1, MILK_D)
        _vline(px, cx - 2, cy - 2, cy + 1, COCO_F)
        _vline(px, cx + 1, cy - 2, cy + 1, MILK_D)
    for x, y in ((4, 7), (8, 6), (12, 8)):
        _put(px, x, y, (255, 255, 255, 255))


def _draw_coconut_roast_meat(px):
    """椰香烤肉：炙烤肉块表面沾满椰丝。"""
    _plate(px)
    for cx, cy in ((6, 9), (10, 8)):
        _rect(px, cx - 2, cy - 2, cx + 2, cy + 1, MEAT_M)
        _row(px, cy - 2, cx - 2, cx + 2, MEAT_L, MEAT_D, MEAT_D)
        _row(px, cy + 1, cx - 2, cx + 2, SEAR_D, SEAR_M, SEAR_D)
    for x, y in ((5, 8), (7, 9), (9, 7), (11, 9), (6, 7), (10, 10)):
        _put(px, x, y, COCO_F)


def _draw_fruit_coconut_milk(px):
    """水果椰奶：玻璃杯里的雪白椰奶，浮着果粒。"""
    _rect(px, 4, 4, 11, 13, COCO_F)
    _row(px, 4, 4, 11, (255, 255, 255, 255), MILK_D, MILK_D)
    _row(px, 13, 4, 11, MILK_D, OUT, OUT)
    _vline(px, 4, 4, 13, MILK_L)
    _vline(px, 11, 4, 13, MILK_L)
    for x, y, c in ((6, 6, BERRY_M), (9, 7, GRAPE_M), (7, 9, TOMATO_M), (9, 11, BERRY_M)):
        _put(px, x, y, c)
    _put(px, 5, 8, (255, 255, 255, 255))


def _draw_blueberry_coconut_crisp(px):
    """蓝莓椰丝脆饼：金黄脆饼上嵌着蓝莓与椰丝。"""
    _ellipse(px, 8, 9, 5.0, 4.0, CRUST_M)
    _hline(px, 6, 6, 10, CRUST_L)
    _hline(px, 12, 6, 10, CRUST_D)
    _hline(px, 13, 7, 9, OUT)
    for cx, cy in ((6, 8), (10, 9), (8, 11)):
        _ellipse(px, cx, cy, 1.6, 1.5, BLUE_M)
        _put(px, cx - 1, cy - 1, BLUE_L)
    for x, y in ((7, 6), (11, 7), (5, 10)):
        _put(px, x, y, COCO_F)


def _draw_egg_tart(px):
    """葡式蛋挞：层层酥皮挞壳里盛着焦斑蛋奶液。"""
    _ellipse(px, 8, 9, 4.6, 3.6, CRUST_M)
    _hline(px, 6, 7, 9, CRUST_L)
    _row(px, 12, 5, 11, CRUST_D, CRUST_M, CRUST_D)
    _row(px, 13, 6, 10, CRUST_D, CRUST_D, CRUST_D)
    _vline(px, 4, 8, 11, CRUST_D)
    _vline(px, 12, 8, 11, CRUST_D)
    _ellipse(px, 8, 8, 3.0, 2.2, EGG_Y)
    _hline(px, 7, 6, 10, EGG_Y_L)
    _put(px, 7, 8, EGG_Y_D)
    _put(px, 9, 7, EGG_Y_D)


def _croissant(px, glaze_l, glaze_m):
    """牛角包共用外形：弯月酥皮 + 指定釉色（牛奶 / 蜂蜜）。"""
    for x, y in ((3, 10), (4, 9), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 9), (11, 10)):
        _put(px, x, y, CRUST_M)
        _put(px, x, y + 1, CRUST_D)
        _put(px, x, y - 1, CRUST_L)
    _put(px, 2, 11, CRUST_D)
    _put(px, 12, 11, CRUST_D)
    for x in (5, 8, 10):
        _put(px, x, 9, CRUST_D)
    _hline(px, 7, 6, 9, glaze_l)
    _hline(px, 8, 6, 9, glaze_m)


def _draw_milky_croissant(px):
    """奶味牛角包：弯月酥皮刷着奶浆。"""
    _croissant(px, MILK_L, MILK_M)


def _draw_honey_croissant(px):
    """蜂蜜牛角包：弯月酥皮刷着蜜釉。"""
    _croissant(px, HONEY_L, HONEY_M)


def _draw_fruit_lollipop(px):
    """水果棒棒糖：红白螺旋纹的圆形糖球插着木棒。"""
    _ellipse(px, 8, 6, 4.5, 4.2, BERRY_M)
    for i in range(4):
        _hline(px, 3 + i * 2, 5 + i, 10 - i, BERRY_L)
    _put(px, 7, 4, (255, 255, 255, 255))
    _put(px, 6, 8, (255, 255, 255, 255))
    _vline(px, 8, 11, 14, WOOD_M)
    _put(px, 7, 11, WOOD_D)


def _draw_steamed_egg_custard(px):
    """蛋羹：陶碗里一碗平滑的嫩黄蛋羹，点缀葱花与香油。"""
    _bowl(px)
    _ellipse(px, 8, 8, 4.5, 2.0, EGG_Y)
    _hline(px, 7, 5, 10, EGG_Y_L)
    _hline(px, 9, 6, 9, EGG_Y_D)
    _put(px, 7, 8, LEAF_M)
    _put(px, 9, 8, LEAF_L)
    _put(px, 10, 7, OIL_L)


def _draw_shrimp_baked_egg(px):
    """虾仁焗蛋：白盘里金黄烘蛋，嵌着粉红虾仁。"""
    _plate(px)
    _ellipse(px, 8, 9, 5.0, 3.2, EGG_Y)
    _hline(px, 6, 6, 10, EGG_Y_L)
    _hline(px, 12, 6, 10, EGG_Y_D)
    for cx, cy in ((6, 8), (10, 9), (8, 11)):
        _ellipse(px, cx, cy, 1.8, 1.3, SHRIMP_M)
        _put(px, cx - 1, cy - 1, SHRIMP_L)
        _put(px, cx + 1, cy, SHRIMP_D)


def _draw_tomato_scrambled_egg(px):
    """番茄炒蛋：白盘里红番茄块与金黄蛋块交杂，撒着葱花。"""
    _plate(px)
    for x, y, c in ((5, 8, EGG_Y), (7, 7, TOMATO_M), (9, 8, EGG_Y), (6, 10, TOMATO_M),
                    (8, 10, EGG_Y), (10, 10, TOMATO_M), (11, 8, EGG_Y), (4, 9, TOMATO_M)):
        _ellipse(px, x, y, 1.5, 1.2, c)
        _put(px, x, y - 1, EGG_Y_L if c == EGG_Y else TOMATO_L)
    _put(px, 7, 9, LEAF_M)
    _put(px, 9, 9, LEAF_L)


def _draw_cold_tomato(px):
    """凉拌番茄：白盘里码着番茄切片，表面撒白糖。"""
    _plate(px)
    for cx, cy in ((5, 9), (8, 7), (11, 9)):
        _ellipse(px, cx, cy, 2.4, 2.0, TOMATO_M)
        _ellipse(px, cx, cy, 1.3, 1.0, TOMATO_L)
        _put(px, cx, cy, TOMATO_D)
    for x, y in ((5, 7), (9, 6), (12, 8)):
        _put(px, x, y, (252, 250, 244, 255))


# ----------------------------------------------------------------------
# 《明日之后》新增料理 · 鱼类
# ----------------------------------------------------------------------

def _draw_clear_fish_soup(px):
    """清炖鱼汤：陶碗清汤里卧着鱼块，飘着葱花。"""
    _bowl(px)
    _ellipse(px, 8, 8, 4.5, 2.0, WATER_M)
    _hline(px, 7, 5, 10, WATER_L)
    _ellipse(px, 7, 8, 2.6, 1.5, FISH_M)
    _put(px, 6, 7, FISH_L)
    _put(px, 9, 9, FISH_D)
    _put(px, 11, 8, LEAF_M)


def _draw_fried_fish_cake(px):
    """炸鱼饼：白盘里两块金黄鱼饼，外壳微焦。"""
    _plate(px)
    for cx, cy in ((6, 9), (10, 9)):
        _ellipse(px, cx, cy, 3.0, 2.2, CRUST_M)
        _ellipse(px, cx, cy, 2.2, 1.5, CRUST_L)
        _put(px, cx - 1, cy - 1, CRUST_M)
        _put(px, cx + 1, cy, CRUST_D)
        _put(px, cx, cy + 1, FISH_M)


def _draw_spicy_boiled_fish(px):
    """麻辣水煮鱼：陶碗红汤里浮着鱼块与辣椒段。"""
    _bowl(px)
    _ellipse(px, 8, 8, 4.5, 2.0, CHILI_M)
    _hline(px, 7, 5, 10, CHILI_L)
    _ellipse(px, 7, 8, 2.2, 1.4, FISH_M)
    _put(px, 6, 7, FISH_L)
    _put(px, 10, 7, CHILI_L)
    _put(px, 11, 9, CHILI_D)
    _put(px, 5, 9, LEAF_M)


# 物品名 -> (贴图绘制函数, lang 词条)
ITEMS = {
    "unspeakable_food": (
        _draw_unspeakable,
        {"en_us": "Unspeakable Food", "zh_cn": "不可名状的食物"},
    ),
    "jam": (
        _draw_jam,
        {"en_us": "Jam", "zh_cn": "果酱"},
    ),
    # 基础料理（4 份同类食材）
    "grilled_meat": (
        _draw_grilled_meat,
        {"en_us": "Grilled Meat", "zh_cn": "烤肉"},
    ),
    "roast_vegetables": (
        _draw_roast_vegetables,
        {"en_us": "Roast Vegetables", "zh_cn": "蒸烤蔬菜"},
    ),
    "toast_bread": (
        _draw_toast_bread,
        {"en_us": "Toast", "zh_cn": "烤面包"},
    ),
    "roast_mushroom": (
        _draw_roast_mushroom,
        {"en_us": "Roast Mushroom", "zh_cn": "烤蘑菇"},
    ),
    "grilled_fish": (
        _draw_grilled_fish,
        {"en_us": "Grilled Fish", "zh_cn": "烤鱼"},
    ),
    "hot_water": (
        _draw_hot_water,
        {"en_us": "Hot Water", "zh_cn": "热水"},
    ),
    "hot_milk": (
        _draw_hot_milk,
        {"en_us": "Hot Milk", "zh_cn": "热牛奶"},
    ),
    "honey_syrup": (
        _draw_honey_syrup,
        {"en_us": "Honey Syrup", "zh_cn": "蜜糖"},
    ),
    # 双食材料理
    "kebab": (
        _draw_kebab,
        {"en_us": "Kebab", "zh_cn": "肉串"},
    ),
    "meatball": (
        _draw_meatball,
        {"en_us": "Meatball", "zh_cn": "肉丸子"},
    ),
    "meat_pie": (
        _draw_meat_pie,
        {"en_us": "Meat Pie", "zh_cn": "肉馅千层饼"},
    ),
    "mushroom_pie": (
        _draw_mushroom_pie,
        {"en_us": "Mushroom Pie", "zh_cn": "蘑菇千层饼"},
    ),
    "fruit_bread": (
        _draw_fruit_bread,
        {"en_us": "Fruit Bread", "zh_cn": "果味面包"},
    ),
    "fruit_popsicle": (
        _draw_fruit_popsicle,
        {"en_us": "Fruit Popsicle", "zh_cn": "果味冰棒"},
    ),
    "honeyed_meat": (
        _draw_honeyed_meat,
        {"en_us": "Honeyed Meat", "zh_cn": "蜜汁烤肉"},
    ),
    "candied_fruit": (
        _draw_candied_fruit,
        {"en_us": "Candied Fruit", "zh_cn": "水果蜜饯"},
    ),
    # 三·四食材料理
    "fruit_milk": (
        _draw_fruit_milk,
        {"en_us": "Fruit Milk", "zh_cn": "水果牛奶"},
    ),
    "cream_soup": (
        _draw_cream_soup,
        {"en_us": "Cream Soup", "zh_cn": "奶油浓汤"},
    ),
    "dumpling": (
        _draw_dumpling,
        {"en_us": "Dumpling", "zh_cn": "水饺"},
    ),
    "sandwich": (
        _draw_sandwich,
        {"en_us": "Sandwich", "zh_cn": "三明治"},
    ),
    "fruit_pie": (
        _draw_fruit_pie,
        {"en_us": "Fruit Pie", "zh_cn": "水果派"},
    ),
    "milk_pudding": (
        _draw_milk_pudding,
        {"en_us": "Milk Pudding", "zh_cn": "牛奶布丁"},
    ),
    "fruit_cake": (
        _draw_fruit_cake,
        {"en_us": "Fruit Cake", "zh_cn": "水果蛋糕"},
    ),
    "special_pizza": (
        _draw_special_pizza,
        {"en_us": "Special Pizza", "zh_cn": "特制披萨"},
    ),
    # 新增食材
    "rice": (
        _draw_rice,
        {"en_us": "Rice", "zh_cn": "大米"},
    ),
    "coconut": (
        _draw_coconut,
        {"en_us": "Coconut", "zh_cn": "椰子"},
    ),
    "blueberry": (
        _draw_blueberry,
        {"en_us": "Blueberry", "zh_cn": "蓝莓"},
    ),
    "chili": (
        _draw_chili,
        {"en_us": "Chili Pepper", "zh_cn": "穿天椒"},
    ),
    "tomato": (
        _draw_tomato,
        {"en_us": "Tomato", "zh_cn": "番茄"},
    ),
    "shrimp": (
        _draw_shrimp,
        {"en_us": "Shrimp", "zh_cn": "鲜虾"},
    ),
    "cooking_oil": (
        _draw_cooking_oil,
        {"en_us": "Cooking Oil", "zh_cn": "食用油"},
    ),
    # 战斗增益
    "mushroom_kebab": (
        _draw_mushroom_kebab,
        {"en_us": "Mushroom Kebab", "zh_cn": "蘑菇肉串"},
    ),
    "spicy_diced_meat": (
        _draw_spicy_diced_meat,
        {"en_us": "Spicy Diced Meat", "zh_cn": "麻辣肉丁"},
    ),
    "orange_steamed_egg": (
        _draw_orange_steamed_egg,
        {"en_us": "Orange Steamed Egg", "zh_cn": "橙子蒸蛋"},
    ),
    "honeyed_mushroom": (
        _draw_honeyed_mushroom,
        {"en_us": "Honeyed Mushroom", "zh_cn": "蜜烧蘑菇"},
    ),
    "tomato_stewed_meat": (
        _draw_tomato_stewed_meat,
        {"en_us": "Tomato Stewed Meat", "zh_cn": "番茄炖肉"},
    ),
    "meat_egg_dumpling": (
        _draw_meat_egg_dumpling,
        {"en_us": "Meat Egg Dumpling", "zh_cn": "鲜肉蛋饺"},
    ),
    # 采集提速
    "veggie_fruit_salad": (
        _draw_veggie_fruit_salad,
        {"en_us": "Veggie Fruit Salad", "zh_cn": "蔬果沙拉"},
    ),
    "vegetable_pie": (
        _draw_vegetable_pie,
        {"en_us": "Vegetable Pie", "zh_cn": "蔬菜千层饼"},
    ),
    "mushroom_udon": (
        _draw_mushroom_udon,
        {"en_us": "Mushroom Udon", "zh_cn": "蘑菇乌冬面"},
    ),
    "coconut_milk_cake": (
        _draw_coconut_milk_cake,
        {"en_us": "Coconut Milk Cake", "zh_cn": "椰奶雪花糕"},
    ),
    "coconut_roast_meat": (
        _draw_coconut_roast_meat,
        {"en_us": "Coconut Roast Meat", "zh_cn": "椰香烤肉"},
    ),
    "fruit_coconut_milk": (
        _draw_fruit_coconut_milk,
        {"en_us": "Fruit Coconut Milk", "zh_cn": "水果椰奶"},
    ),
    # 烘焙甜点
    "blueberry_coconut_crisp": (
        _draw_blueberry_coconut_crisp,
        {"en_us": "Blueberry Coconut Crisp", "zh_cn": "蓝莓椰丝脆"},
    ),
    "egg_tart": (
        _draw_egg_tart,
        {"en_us": "Egg Tart", "zh_cn": "葡式蛋挞"},
    ),
    "milky_croissant": (
        _draw_milky_croissant,
        {"en_us": "Milky Croissant", "zh_cn": "奶味牛角包"},
    ),
    "honey_croissant": (
        _draw_honey_croissant,
        {"en_us": "Honey Croissant", "zh_cn": "蜂蜜牛角包"},
    ),
    "fruit_lollipop": (
        _draw_fruit_lollipop,
        {"en_us": "Fruit Lollipop", "zh_cn": "水果棒棒糖"},
    ),
    # 蛋料理
    "steamed_egg_custard": (
        _draw_steamed_egg_custard,
        {"en_us": "Steamed Egg Custard", "zh_cn": "蛋羹"},
    ),
    "shrimp_baked_egg": (
        _draw_shrimp_baked_egg,
        {"en_us": "Shrimp Baked Egg", "zh_cn": "虾仁焗蛋"},
    ),
    "tomato_scrambled_egg": (
        _draw_tomato_scrambled_egg,
        {"en_us": "Tomato Scrambled Egg", "zh_cn": "番茄炒蛋"},
    ),
    "cold_tomato": (
        _draw_cold_tomato,
        {"en_us": "Cold Tomato", "zh_cn": "凉拌西红柿"},
    ),
    # 鱼类
    "clear_fish_soup": (
        _draw_clear_fish_soup,
        {"en_us": "Clear Fish Soup", "zh_cn": "清炖鱼汤"},
    ),
    "fried_fish_cake": (
        _draw_fried_fish_cake,
        {"en_us": "Fried Fish Cake", "zh_cn": "炸鱼饼"},
    ),
    "spicy_boiled_fish": (
        _draw_spicy_boiled_fish,
        {"en_us": "Spicy Boiled Fish", "zh_cn": "麻辣水煮鱼"},
    ),
}


def main():
    for name, (draw, lang) in ITEMS.items():
        img = Image.new("RGBA", (16, 16))
        draw(img.load())
        img.save(os.path.join(TEX_ITEM, name + ".png"))

        with open(os.path.join(MODEL_ITEM, name + ".json"), "w", encoding="utf-8") as f:
            json.dump({
                "parent": "minecraft:item/generated",
                "textures": {"layer0": "lifecooker:item/" + name},
            }, f, ensure_ascii=False, indent=2)
            f.write("\n")

        for locale, text in lang.items():
            path = os.path.join(LANG, locale + ".json")
            with open(path, encoding="utf-8") as f:
                data = json.load(f)
            data["item.lifecooker." + name] = text
            with open(path, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
                f.write("\n")

        print("generated item:", name)


if __name__ == "__main__":
    main()
