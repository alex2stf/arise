from PIL import Image, ImageDraw, ImageFont
import sys
import subprocess
import os

from random import randint, choice
#os.system("killall pcmanfm")
subprocess.Popen(["killall", "pcmanfm"], start_new_session=True)

print(sys.argv[1])
sw = 0
sh = 0

sw = 1680
sh = 1050


BLACK, DARKGRAY, GRAY = ((0,0,0), (63,63,63), (127,127,127))
LIGHTGRAY, WHITE = ((191,191,191), (255,255,255))
M1, M2, M3 = ((56, 21, 46), (156, 77, 56), (255, 0, 0))
B1, B2, B3 = ((31, 67, 144), (18, 149, 211), (17, 42, 92))
G1, G2, G3 = ((242, 204, 54), (237, 104, 187), (25, 154, 208))

class Point(object):
    def __init__(self, x, y):
        self.x, self.y = x, y

class Rect(object):
    def __init__(self, x1, y1, x2, y2):
        minx, maxx = (x1,x2) if x1 < x2 else (x2,x1)
        miny, maxy = (y1,y2) if y1 < y2 else (y2,y1)
        self.min = Point(minx, miny)
        self.max = Point(maxx, maxy)

    width  = property(lambda self: self.max.x - self.min.x)
    height = property(lambda self: self.max.y - self.min.y)


def gradient_color(minval, maxval, val, color_palette):
    """ Computes intermediate RGB color of a value in the range of minval
        to maxval (inclusive) based on a color_palette representing the range.
    """
    max_index = len(color_palette)-1
    delta = maxval - minval
    if delta == 0:
        delta = 1
    v = float(val-minval) / delta * max_index
    i1, i2 = int(v), min(int(v)+1, max_index)
    (r1, g1, b1), (r2, g2, b2) = color_palette[i1], color_palette[i2]
    f = v - i1
    return int(r1 + f*(r2-r1)), int(g1 + f*(g2-g1)), int(b1 + f*(b2-b1))

def horz_gradient(draw, rect, color_func, color_palette):
    minval, maxval = 1, len(color_palette)
    delta = maxval - minval
    width = float(rect.width)  # Cache.
    for x in range(rect.min.x, rect.max.x+1):
        f = (x - rect.min.x) / width
        val = minval + f * delta
        color = color_func(minval, maxval, val, color_palette)
        draw.line([(x, rect.min.y), (x, rect.max.y)], fill=color)

def vert_gradient(draw, rect, color_func, color_palette):
    minval, maxval = 1, len(color_palette)
    delta = maxval - minval
    height = float(rect.height)  # Cache.
    for y in range(rect.min.y, rect.max.y+1):
        f = (y - rect.min.y) / height
        val = minval + f * delta
        color = color_func(minval, maxval, val, color_palette)
        draw.line([(rect.min.x, y), (rect.max.x, y)], fill=color)

img = Image.open(sys.argv[1], 'r')
img_w, img_h = img.size

img.putalpha(200)

# background = Image.open('desk.jpg', 'r')
im = Image.new('RGB', (sw,sh), 'orange')


draw = ImageDraw.Draw(im)

region = Rect(0, 0, sw, sh)
rint = randint(0, 7)

if rint == 0:
	color_palette = [B1, B2, B3]
	vert_gradient(draw, region, gradient_color, color_palette)
elif rint == 1:
	color_palette = [B1, B2, B3]
	horz_gradient(draw, region, gradient_color, color_palette)
elif rint == 2:
	color_palette = [M1, M2, M3]
	vert_gradient(draw, region, gradient_color, color_palette)
elif rint == 3:
	color_palette = [M1, M2, M3]
	horz_gradient(draw, region, gradient_color, color_palette)
elif rint == 4:
	color_palette = [B3, B1, B2, M3]
	horz_gradient(draw, region, gradient_color, color_palette)
elif rint == 5:
	color_palette = [B3, B1, B2, M3]
	vert_gradient(draw, region, gradient_color, color_palette)
elif rint == 6:
	color_palette = [G3, G2, G1]
	horz_gradient(draw, region, gradient_color, color_palette)
elif rint == 7:
	color_palette = [G1, G2, G3]
	vert_gradient(draw, region, gradient_color, color_palette)
else:
	color_palette = [B1, BLACK, GREEN]
	horz_gradient(draw, region, gradient_color, color_palette)

#font = ImageFont.load_default()
font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', size=30)

draw.text(
	(0, 0),  # Coordinates
	'arise',  # Text
	WHITE,  # Color
	font=font
)



im.putalpha(200)

bg_w, bg_h = im.size


offset = ((bg_w - img_w) // 2, (bg_h - img_h) // 2)



im.paste(img, offset)



im.save(sys.argv[2])

#os.system("pcmanfm --desktop --profile lubuntu")
subprocess.Popen(["pcmanfm", "--desktop", "--profile", "lubuntu"], start_new_session=True)

