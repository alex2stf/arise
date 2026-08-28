print("hi")

from PIL import Image, ImageDraw, ImageFont
import json
import random
import socket
import requests
import shutil
import sys
import subprocess
import os

socket.setdefaulttimeout(15)

try:
    from urllib.request import Request, urlopen  # Python 3
except ImportError:
    from urllib2 import Request, urlopen  # Python 2

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36"



####################################
##### download image logic #########
####################################

def load_sgjson():
    with open('../weland/config/commons/suggestions.json') as f:
        return json.load(f)


def load_as_list(fname):
    list = []
    with open(fname) as file:
        for line in file:
            lx = (line.rstrip())
            if lx:
                list.append(lx)
    return list


def load_images():
    return load_as_list('../pictures/images.txt')


def search_term(term):
    data = load_sgjson()
    for item in data['suggestions']:
        key = (item['key'])
        if (term.lower().find(key.lower()) > -1):
            return (random.choice(item['icons']))


def write_pfile_zero(list, fname):
    indices = []
    for index, element in enumerate(list):
        indices.append(index)

    random.shuffle(indices)

    with open(fname, 'w') as f:
        f.write('0\n')
        for i in indices:
            f.write("%s\n" % i)


def rand_pick_persistent(list):
    file = get_tmp_file("pilst.txt")

    if not os.path.exists(file) or 0 == os.path.getsize(file):
        print('no file found, writing')
        write_pfile_zero(list, file)

    lines = load_as_list(file)
    index = int(lines[0])
    if index > len(list) - 1:
        write_pfile_zero(list, file)
        index = 0
        print("overflow, re-writing...")

    res = list[index]
    lines[0] = str(index + 1)

    with open(file, "w") as f:
        for i in lines:
            f.write("%s\n" % i)

    return res


def download_image_with_urllib(img_url, output):
    print('download_image_with_urllib ', img_url, ' to ', output)
    req = Request(img_url)
    req.add_header('User-Agent', USER_AGENT)
    content = urlopen(req, timeout=15).read()
    with open(output, 'wb') as localFile:
        localFile.write(content.read())
    return output


def download_image_with_requests(img_url, output):
    print('download_image_with_requests ', img_url, ' to ', output)
    r = requests.get(img_url, stream=True,
                     headers={'User-agent': USER_AGENT})
    if r.status_code == 200:
        with open(output, 'wb') as f:
            r.raw.decode_content = True
            shutil.copyfileobj(r.raw, f)
            return output
    else:
        raise Exception(str(r.status_code) + ' for ' + img_url)


def solve_path(path):
    if path.startswith('classpath:'):
        abspath = os.path.abspath('../' + path[len('classpath:'):])
        if(os.path.exists(abspath)):
            return abspath
    if path.startswith("http:") or path.startswith("https:"):
        return download_image(path)


def download_image(img_url):
    extension = '.jpg'
    if img_url.endswith('.png'):
        extension = '.png'
    output = get_tmp_file("pilmg") + extension
    try:
        return download_image_with_requests(img_url, output)
    except Exception as err:
        print("requests failed with error ", err)
        try:
            return download_image_with_urllib(img_url, output)
        except Exception as er2:
            print("urllib failed with error ", er2)
            return None


def build_local_image(term):
    url = search_term(term)
    if not url:
        images_urls = load_images()
        url = rand_pick_persistent(images_urls)
        print("no suggestion found for term [", term, "] using random " + url)

    image_file = solve_path(url)

    if not image_file:
        print("doing 2nd interation try")
        images_urls = load_images()
        url = rand_pick_persistent(images_urls)
        image_file = solve_path(url)

    return os.path.abspath(image_file)




####################################
##### build image logic ############
####################################

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

# Computes intermediate RGB color of a value in the range of minval  to maxval (inclusive) based on a color_palette representing the range.
def gradient_color(minval, maxval, val, color_palette):
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


def random_gradient(draw, region):
    rint = random.randint(0, 7)
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


def get_pictures_dir():
    user_folder = os.path.expanduser("~")
    if not os.path.exists(user_folder):
        user_folder = '/arise-tmp'
        os.mkdir(user_folder)
    return os.path.join(user_folder, "Pictures")

def get_tmp_folder():
    user_folder = os.path.expanduser("~")
    if not os.path.exists(user_folder):
        user_folder = '/arise-tmp'
        os.mkdir(user_folder)
    app_fldr = os.path.abspath(os.path.join(user_folder, "arise-app"))
    if not os.path.exists(app_fldr):
        os.mkdir(app_fldr)
    return app_fldr

def get_tmp_file(name):
    return os.path.join(get_tmp_folder(), name)

def pcmanf_kill():
    try:
        subprocess.Popen(["killall", "pcmanfm"], start_new_session=True)
    except:
        print("could not execute killall pcmanfm")

def pcmanf_start():
    try:
        subprocess.Popen(["pcmanfm", "--desktop", "--profile", "lubuntu"], start_new_session=True)
    except:
        print("could not execute pcmanfm restart")


############## start main ##############

#parametrii:

pcmanf_kill()

term = 'xxx'
w_text = 'Text'

if len(sys.argv) > 0:
    term = sys.argv[1]
    print("term = ", term)

if len(sys.argv) > 1:
    w_text = sys.argv[2]
    print("text = ", w_text)

desk_out = os.path.join(get_pictures_dir(), "arise-desktop.png")
desired_width = 1680
desired_height = 1050


try:
    font = ImageFont.load_default(size=30)
except:
    print("loading din usr-share font")
    font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', size=30)


image_file = build_local_image(term)
# image_file = get_tmp_file("pilmg.jpg")
print('dowloaded file: ', image_file)


img = Image.open(image_file, 'r')
img_w, img_h = img.size

offset = ((desired_width - img_w) // 2, (desired_height - img_h) // 2)
do_resize = False

if img_w * img_h > desired_width * desired_height:
    print("img sursa mai mare decat plansa")
    place_gradient = False
    do_resize = True
    offset = (0,0) #important ptr resize



im = Image.new('RGB', (desired_width, desired_height), 'orange')
draw = ImageDraw.Draw(im)

region = Rect(0, 0, desired_width, desired_height)
random_gradient(draw, region)

if do_resize:
    # cp = img.resize((desired_width, desired_height), Image.Resampling.LANCZOS)
    cp = img.resize((desired_width, desired_height), 1)
    im.paste(cp, offset)
else:
    im.paste(img, offset)

im.save(desk_out)

#pune text
img2 = Image.open(desk_out, 'r')
draw2 = ImageDraw.Draw(img2)
draw2.text(
    (330, 20),  # Coordinates
    w_text,  # Text
    WHITE,  # Color
    font=font
)
img2.save(desk_out)
# img2.show()

pcmanf_start()