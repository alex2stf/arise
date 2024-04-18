from PIL import Image, ImageDraw, ImageFont
import sys
import subprocess
import os

from random import randint, choice

print(sys.argv[1])
sw = 0
sh = 0

sw = 1980
sh = 1210


BLACK, DARKGRAY, GRAY = ((0,0,0), (63,63,63), (127,127,127))
LIGHTGRAY, WHITE = ((191,191,191), (255,255,255))
BLUE, GREEN, RED = ((0, 0, 255), (0, 255, 0), (255, 0, 0))


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


color_palette = [BLUE, RED, WHITE]
region = Rect(0, 0, sw, sh)

if randint(0, 1) == 0:
    print("V")
    vert_gradient(draw, region, gradient_color, color_palette)
else:
    print("H")
    horz_gradient(draw, region, gradient_color, color_palette)

#font = ImageFont.load_default(size=60)
# font = ImageFont.truetype('arial.ttf', size=60)

# draw.text(
#     (20, 20),  # Coordinates
#     sys.argv[3],  # Text
#     LIGHTGRAY,  # Color
#     font=font
# )
im.putalpha(200)

bg_w, bg_h = im.size


offset = ((bg_w - img_w) // 2, (bg_h - img_h) // 2)
# background.paste(img, offset)
# background.save('out_merged.png')

im.paste(img, offset)
im.save(sys.argv[2])

from subprocess import Popen, PIPE
import time
os.system("killall pcmanfm")

time.sleep(1)
os.spawn(os.P_DETACH, "pcmanfm --desktop --profile lubuntu")
# subprocess.Popen([ "pcmanfm", "--desktop", "--profile", "lubuntu"])
# os.system("killall pcmanfm & pcmanfm --desktop --profile lubuntu")

# subprocess = Popen("killall pcmanfm;pcmanfm --desktop --profile lubuntu", shell = True)