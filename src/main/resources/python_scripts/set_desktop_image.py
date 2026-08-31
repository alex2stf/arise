from PIL import Image, ImageDraw, ImageFont
import json
import random
import socket
import requests
import shutil
import sys
import subprocess
import os
import pathlib

TIMEOUT = 60

socket.setdefaulttimeout(TIMEOUT)

try:
    from urllib.request import Request, urlopen  # Python 3
except ImportError:
    from urllib2 import Request, urlopen  # Python 2


####################################
##### global params and args #######
####################################
WORKING_DIR = 'src/main/resources/suggestions/'
term = 'xxx'
w_text = 'Text'


if len(sys.argv) > 1:
    term = sys.argv[1]

if len(sys.argv) > 2:
    w_text = sys.argv[2]

if len(sys.argv) > 3:
    WORKING_DIR = os.path.abspath(sys.argv[3])  #TODO more wdir search

print("term = ", term)
print("text = ", w_text)
print("working_dir = ", WORKING_DIR)

####################################
##### download image logic #########
####################################
def file_suggestions():
    return os.path.join(WORKING_DIR, "suggestions.json")

def file_images():
    return os.path.join(WORKING_DIR, "images.txt")

def load_sgjson():
    print(file_suggestions())
    with open(file_suggestions()) as f:
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
    return load_as_list(file_images())


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
    for key, value in build_headers().items():
        req.add_header(key, value)
    content = urlopen(req, timeout=TIMEOUT).read()
    with open(output, 'wb') as localFile:
        localFile.write(content.read())
    return output


def build_headers():
    mozilla = 'Mozilla/' + str(random.randint(3, 5)) + '.' + str(random.randint(0, 10))
    apple_webkit = 'AppleWebKit/'+ str(random.choice([537,538,539,605,523])) + '.' + str(random.randint(47, 212))
    chrome = 'Chrome/' + str(random.randint(123, 578)) + '.' + str(random.randint(0, 9)) + '.' + str(random.randint(0, 9)) + '.' + str(random.randint(0,9))
    safari = 'Safari/' + str(random.randint(523, 749)) + '.' + str(random.randint(20, 80))
    geko = 'Gecko/'+str(random.randint(2010, 2027))+'0'+str(random.randint(1,9))+'0' + str(random.randint(1, 9))
    edge = 'Edg/' + str(random.randint(123, 345)) + '.' + str(random.randint(0, 0)) + '.' + str(random.randint(0, 9)) + '.' + str(random.randint(0, 9))
    vivaldi = 'Vivaldi/8.1.' + str(random.randint(1000, 5000)) + '.' + str(random.randint(20, 63))
    firefox = 'Firefox/154.' + str(random.randint(0, 20))
    rv = str(random.choice([154,155,167,278]))

    os_data = random.choice([
        {
            'platform': 'Windows',
            'uag': '(Windows NT 10.0; Win64; x64)',
            'vers': [
                'Mozilla/5.0 (Windows NT 10.0; WOW64) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari + ' ' + vivaldi,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:' + rv + '.0) ' + geko + ' ' + firefox,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari + ' ' + edge,
                'Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko'
            ]
        },
        {
            'platform': 'macOs',
            'uag': '(Macintosh; Intel Mac OS X 15_7_9)',
            'vers': [
                'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_9) ' + apple_webkit + ' (KHTML, like Gecko) Version/26.0 ' + safari,
                'Mozilla/5.0 (Macintosh; Intel Mac OS X 15.7; rv:' + rv + '.0) ' + geko + ' ' + firefox,
                'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_9) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_9) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari + ' ' + vivaldi,
                'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_9) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari + ' ' + edge
            ]
        },
        {
            'platform': 'Android',
            'uag': '(Linux; Android 17)',
            'vers': [
                'Mozilla/5.0 (Linux; Android 17) ' + apple_webkit + ' (KHTML, like Gecko)  ' + chrome + ' Mobile ' + safari,
                'Mozilla/5.0 (Linux; Android 17; ' + random.choice(['SM-A205U', 'SM-A102U', 'SM-G960U', 'LM-Q720', 'LG-M255']) + ') ' + apple_webkit + ' (KHTML, like Gecko)  ' + chrome + ' Mobile ' + safari,
                'Mozilla/5.0 (Android 17; Mobile; rv:' + random.choice(['68', '70', '83', '154']) + '.0) ' + geko + ' ' + firefox,
                'Mozilla/5.0 (Android 17; Mobile; ' + random.choice(['SM-A205U', 'SM-A102U', 'SM-G960U', 'LM-Q720', 'LG-M255']) + '; rv:' + random.choice(['68', '70', '83', '154']) + '.0) ' + geko + ' ' + firefox
            ]
        },
        {
            'platform': 'Linux',
            'uag': '(X11; Linux x86_64; rv:130.0)',
            'vers': [
                'Mozilla/5.0 (X11; Linux x86_64) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.8.1.9) Gecko/20071103 BonEcho/2.0.0.9',
                'Mozilla/5.0 (X11; Linux x86_64) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari + ' ' + edge,
                'Mozilla/5.0 (X11; Linux x86_64; rv:56.0; Waterfox) ' + geko + ' ' + firefox,
                'Mozilla/5.0 (X11; Linux x86_64; Ubuntu 22.04) ' + apple_webkit + ' (KHTML, like Gecko) ' + safari,
                'Mozilla/5.0 (X11; U; Linux; nb-NO) ' + apple_webkit + ' (KHTML, like Gecko, ' + safari + ') Arora/0.2',
                'Mozilla/5.0 (X11; Linux x86_64; rv:56.0) Gecko/ff19::1:2:3 ' + firefox + ' Waterfox/56.2.10',
                'Mozilla/5.0 (X11; Linux x86_64; rv:56.0) Gecko/20100101 ' +  firefox + ' Waterfox/56.3'
            ]
        },
        # rare
        {
            'platform': 'Chromium OS',
            'uag': '(X11; CrOS armv7l 16733.57.0)',
            'vers': [
                'Mozilla/5.0 (X11; CrOS x86_64 16733.57.0) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (X11; CrOS x86_64 16733.57.0) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (X11; CrOS armv7l 16733.57.0) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari,
                'Mozilla/5.0 (X11; CrOS aarch64 16733.57.0) ' + apple_webkit + ' (KHTML, like Gecko) ' + chrome + ' ' + safari
            ]
        },
        {
            'platform': 'iOS',
            'uag': '(iPhone; CPU iPhone OS 18_7_8 like Mac OS X)',
            'vers': [
                'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7_8 like Mac OS X) ' + apple_webkit + ' (KHTML, like Gecko) Version/26.0 Mobile/15E148 ' + safari,
                'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7_8 like Mac OS X) ' + apple_webkit + ' (KHTML, like Gecko) CriOS/152.0.7977.64 Mobile/15E148 ' + safari,
                'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7_8 like Mac OS X) ' + apple_webkit + ' (KHTML, like Gecko) FxiOS/154.0 Mobile/15E148 ' + safari
            ]
        },
    ])

    userAgent = random.choice(os_data['vers'])
    print('platform', os_data['platform'], ' \nus=', userAgent)

    return {
        'accept-encoding': 'gzip, deflate, br, zstd'
        ,'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/jpg,image/jpeg,image/png,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7'
        ,'Sec-CH-UA-Platform': os_data['platform']
        ,'Sec-Fetch-Dest': 'document'
        ,'cache-control': 'max-age=0'
        # ,"cookie": "GeoIP=RO:B:Bucharest:44.43:26.10:v4; WMF-Uniq=TwDbipya-BY7nZBx9EECkgOlAAYDAFvd76k_xW8sl4PHpVQ7IPd14qxDteyZC6EM"
        ,'User-Agent': userAgent
    }


def download_image_with_requests(img_url, output):
    print('download_image_with_requests ', img_url, ' to ', output)
    r = requests.get(img_url, stream=True,
                     headers=build_headers(), timeout = 30)
    print('status_code', r.status_code)
    if r.status_code < 359:
        with open(output, 'wb') as f:
            f.write(r.content)
            # r.raw.decode_content = True
            # shutil.copyfileobj(r.raw, f)
            print("written to system at ", output)
            return output
    else:
        raise Exception(str(r.status_code) + ' for ' + img_url)


#TODO foloseste WORKING_DIR
def solve_path(path):
    if path.startswith('classpath:'):
        part = os.path.abspath('../' + path[len('classpath:'):])
        abspath = os.path.join(WORKING_DIR, part)
        if(os.path.exists(abspath)):
            return abspath
    if path.startswith("http:") or path.startswith("https:"):
        return download_image(path)


def download_image(img_url):
    extension = '.jpg'
    if img_url.endswith('.png'):
        extension = '.png'
    if img_url.endswith('.jpeg'):
        extension = '.jpeg'
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

    #default ia din local #TODO cauta in niste locatii din sistem
    if not image_file:
        rand = random.randint(0, 7)
        return os.path.join(WORKING_DIR, 'desk' + str(rand) + '.jpg')
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
    global GREEN
    rint = random.randint(0, 10)
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
    elif rint == 8:
        color_palette = [BLACK, WHITE, BLACK]
        vert_gradient(draw, region, gradient_color, color_palette)
    elif rint == 9:
        color_palette = [WHITE, BLACK, WHITE]
        vert_gradient(draw, region, gradient_color, color_palette)
    elif rint == 10:
        color_palette = [WHITE, BLACK]
        vert_gradient(draw, region, gradient_color, color_palette)
    elif rint == 10:
        color_palette = [BLACK, WHITE]
        horz_gradient(draw, region, gradient_color, color_palette)
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



def get_font():
    try:
        return ImageFont.load_default(size=30)
    except:
        print("loading font din usr/share")
        return ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', size=30)

############## start main ##############

#parametrii:



pcmanf_kill()



desk_out = os.path.join(get_pictures_dir(), "arise-desktop.png")
desired_width = 1680
desired_height = 1050





# image_file = 'C:\\Users\\Administrator\\Pictures\\portret.jpeg' #linie de test
image_file = build_local_image(term)
# image_file = download_image('https://ro.cam4.com/female')#linie de test

try:
    img = Image.open(image_file, 'r')
except Exception as err:
    print('eroare la parsare imagine ', err)
    print('........... 2nd build iteration')
    image_file = build_local_image(term)
    img = Image.open(image_file, 'r')



img_w, img_h = img.size

offset = ((desired_width - img_w) // 2, (desired_height - img_h) // 2)
do_resize = False
resize_w = desired_width
resize_h = desired_height

#todo aici e gresit, calculeaza ratio
if img_h > desired_height or img_w > desired_width:
    if img_w > desired_width:
        ratio = img_w / desired_width
        print("[-] > plansa ratio = ", ratio, ' din ', img_w, '/', desired_width)
    elif img_h > desired_height:
        ratio = img_h / desired_height
        print("[|] > plansa ratio = ", ratio, ' din ', img_h, '/', desired_height)
    if ratio > 0:
        do_resize=True
        resize_w = int(img_w // ratio)
        resize_h = int(img_h // ratio)
        print('resize_w=', resize_w, 'resize_h=', resize_h)
        offset = ((desired_width - resize_w) // 2, (desired_height - resize_h) // 2)



im = Image.new('RGB', (desired_width, desired_height), 'orange')
draw = ImageDraw.Draw(im)

region = Rect(0, 0, desired_width, desired_height)
random_gradient(draw, region)

if do_resize:
    # try:
    #     cp = img.resize((desired_width, desired_height), Image.Resampling.LANCZOS)
    # except:
    # cp = img.resize((resize_w, resize_h), Image.BICUBIC)
    print('do resize')
    cp = img.resize((resize_w, resize_h), Image.BICUBIC)
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
    font=get_font()
)
img2.save(desk_out)
# img2.show()

pcmanf_start()