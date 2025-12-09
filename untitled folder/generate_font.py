import os
from PIL import Image, ImageDraw, ImageFont

# Settings
FONT_SIZE = 32
FONT_PATH = "/Library/Fonts/Arial Unicode.ttf"  # Use accessible system font
CHARS = ''.join([chr(i) for i in range(32, 127)])  # Basic ASCII
IMG_WIDTH = 512
IMG_HEIGHT = 64
OUTPUT_DIR = "assets/ui"
FONT_IMAGE = os.path.join(OUTPUT_DIR, "font.png")
FONT_FNT = os.path.join(OUTPUT_DIR, "font.fnt")

# Create image
img = Image.new("RGBA", (IMG_WIDTH, IMG_HEIGHT), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)
font = ImageFont.truetype(FONT_PATH, FONT_SIZE)

x = 0
char_data = []
for c in CHARS:
    bbox = font.getbbox(c)
    w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    draw.text((x, 0), c, font=font, fill=(255, 255, 255, 255))
    char_data.append((c, x, w, h))
    x += w
img.save(FONT_IMAGE)

# Write .fnt file (BMFont text format)
with open(FONT_FNT, "w") as f:
    f.write("info face=\"Arial Unicode\" size=%d bold=0 italic=0 charset=\"\" unicode=0 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1\n" % FONT_SIZE)
    f.write("common lineHeight=%d base=%d scaleW=%d scaleH=%d pages=1 packed=0\n" % (FONT_SIZE, FONT_SIZE, IMG_WIDTH, IMG_HEIGHT))
    f.write("page id=0 file=\"font.png\"\n")
    f.write("chars count=%d\n" % len(char_data))
    for i, (c, x, w, h) in enumerate(char_data):
        f.write("char id=%d x=%d y=0 width=%d height=%d xoffset=0 yoffset=0 xadvance=%d page=0 chnl=0\n" % (ord(c), x, w, h, w))
print("font.png and font.fnt generated in assets/ui/")
