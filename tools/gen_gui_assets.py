# -*- coding: utf-8 -*-
"""Generate cooking GUI background textures for lifecooker (明日料理).

输出:
  src/main/resources/assets/lifecooker/textures/gui/container/cooking.png
      家用灶炉 / 电磁炉：4 食材槽 + 产出槽 + 纵向燃料 / 能量罐（无固体燃料槽）
  src/main/resources/assets/lifecooker/textures/gui/container/cooking_grill.png
      烧烤炉：4 食材槽 + 产出槽 + 固体燃料槽 + 横向燃烧余量条
  tools/_preview_gui.png
      放大预览（并排比对，仅用于目视校验，不参与打包）

所有坐标必须与 com.homemine.lifecooker.menu.CookingMenu 中的布局常量保持一致；
进度 / 燃料填充区域会由 CookingScreen.renderBg() 以纯色 fill 覆盖，故此处只画凹陷槽位。
两张贴图均为 256×256 画布，176×166 的面板画在左上角（原版容器贴图约定）。

用法:  py -3 tools/gen_gui_assets.py
"""
import os

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..")
TEX_GUI = os.path.join(ROOT, "src", "main", "resources", "assets", "lifecooker",
                       "textures", "gui", "container")
PREVIEW = os.path.join(os.path.dirname(__file__), "_preview_gui.png")
os.makedirs(TEX_GUI, exist_ok=True)

# 面板尺寸（与 CookingScreen 的 imageWidth / imageHeight 一致）
W, H = 176, 166
# 贴图画布尺寸：原版容器贴图统一为 256×256。GuiGraphics.blit 的 7 参数重载
# 按 256×256 归一化 UV，若贴图本身只有 176×166，只会采样左上角一小块并拉伸，
# 表现为「放大且显示不全」，故面板必须画在 256×256 画布的左上角。
TEX_W, TEX_H = 256, 256

FILL = (198, 198, 198, 255)
LIGHT = (255, 255, 255, 255)
SHADOW = (85, 85, 85, 255)
WELL_DARK = (55, 55, 55, 255)
WELL_FILL = (139, 139, 139, 255)

# ---- 与 CookingMenu 对齐的布局常量（槽位中心坐标） ----
INGREDIENT_POS = [(26, 20), (44, 20), (62, 20), (80, 20)]
OUTPUT_POS = (134, 20)
FUEL_POS = (26, 46)
GAUGE_POS = (26, 46)
GAUGE_W, GAUGE_H = 16, 24
BURN_BAR_POS = (48, 50)
BURN_BAR_W, BURN_BAR_H = 70, 6
ARROW_POS = (100, 25)
ARROW_W, ARROW_H = 26, 8


def panel():
    """整块面板：浅灰底 + 左上高光 / 右下暗边的经典立体描边。"""
    img = Image.new("RGBA", (W, H), FILL)
    px = img.load()
    for x in range(W):
        px[x, 0] = LIGHT
        px[x, H - 1] = SHADOW
    for y in range(H):
        px[0, y] = LIGHT
        px[W - 1, y] = SHADOW
    return img


def to_canvas(panel_img):
    """把 176×166 面板放到 256×256 透明画布的左上角（原版容器贴图约定）。"""
    canvas = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    canvas.paste(panel_img, (0, 0))
    return canvas


def slot_well(px, sx, sy):
    """18x18 物品格凹陷：槽位中心 (sx, sy)，凹槽外框占 (sx-1, sy-1)。"""
    x, y = sx - 1, sy - 1
    for i in range(18):
        px[x + i, y] = WELL_DARK
        px[x, y + i] = WELL_DARK
        px[x + 17, y + i] = LIGHT
        px[x + i, y + 17] = LIGHT
    for yy in range(1, 17):
        for xx in range(1, 17):
            px[x + xx, y + yy] = WELL_FILL


def recess(px, x, y, w, h):
    """通用凹陷区域（进度条 / 燃烧条 / 燃料罐）：(x, y) 为外框左上角。"""
    for i in range(w):
        px[x + i, y] = WELL_DARK
        px[x + i, y + h - 1] = LIGHT
    for i in range(h):
        px[x, y + i] = WELL_DARK
        px[x + w - 1, y + i] = LIGHT
    for yy in range(1, h - 1):
        for xx in range(1, w - 1):
            px[x + xx, y + yy] = WELL_FILL


def player_inventory(px):
    """玩家 3 行主物品栏 + 1 行快捷栏（与 CookingMenu 的 84 / 142 行一致）。"""
    for row in range(3):
        for col in range(9):
            slot_well(px, 8 + col * 18, 84 + row * 18)
    for col in range(9):
        slot_well(px, 8 + col * 18, 142)


def common(px):
    for sx, sy in INGREDIENT_POS:
        slot_well(px, sx, sy)
    slot_well(px, *OUTPUT_POS)
    recess(px, ARROW_POS[0] - 1, ARROW_POS[1] - 1, ARROW_W + 2, ARROW_H + 2)
    player_inventory(px)


def build_base():
    """家用灶炉 / 电磁炉：纵向燃料 / 能量罐占据原燃料槽位置。"""
    img = panel()
    px = img.load()
    common(px)
    recess(px, GAUGE_POS[0] - 1, GAUGE_POS[1] - 1, GAUGE_W + 2, GAUGE_H + 2)
    return img


def build_grill():
    """烧烤炉：固体燃料槽 + 右侧横向燃烧余量条。"""
    img = panel()
    px = img.load()
    common(px)
    slot_well(px, *FUEL_POS)
    recess(px, BURN_BAR_POS[0] - 1, BURN_BAR_POS[1] - 1, BURN_BAR_W + 2, BURN_BAR_H + 2)
    return img


def main():
    base = to_canvas(build_base())
    grill = to_canvas(build_grill())
    base.save(os.path.join(TEX_GUI, "cooking.png"))
    grill.save(os.path.join(TEX_GUI, "cooking_grill.png"))

    scale = 4
    gap = 8
    canvas = Image.new("RGBA", ((W * 2 + gap) * scale, H * scale), (0, 0, 0, 0))
    canvas.paste(base.crop((0, 0, W, H)).resize((W * scale, H * scale), Image.NEAREST), (0, 0))
    canvas.paste(grill.crop((0, 0, W, H)).resize((W * scale, H * scale), Image.NEAREST),
                 ((W + gap) * scale, 0))
    canvas.save(PREVIEW)
    print("wrote cooking.png / cooking_grill.png ->", os.path.normpath(TEX_GUI))
    print("preview ->", os.path.normpath(PREVIEW))


if __name__ == "__main__":
    main()
