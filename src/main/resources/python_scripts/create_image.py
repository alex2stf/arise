from PIL import Image, ImageDraw, ImageFont
import math
import random
WIDTH = 1680
HEIGHT = 1050
#  = 1000
# h = 1000

def colors2():
    return [
        ['#2BF58B', '#F52B93']
        ,['black', 'white']
        ,['white', 'black']
    ]

#src at https://htmlcolorcodes.com/color-picker/
def color_arrays():
    return [
        ['#44EB11', '#11EB4C', '#11EBB8']
        ,['#9BEB11', '#2EEB11', '#11EB61']
        ,['#E4EB11', '#77EB11', '#11EB18']
        ,['#EBAA11', '#BFEB11', '#52EB11']
        ,['#EB6F11', '#EBDC11', '#8DEB11']
        ,['#EB6F11', '#EBDC11', '#8DEB11']
        ,['#EB1C11', '#EB8911', '#E0EB11']
        ,['#EB1140', '#EB4F11', '#EBBC11']
        ,['#EB1177', '#EB1811', '#EB8511']
        ,['#11CAEB', '#115DEB', '#3211EB']
        ,['#DCB352', '#C0DC52', '#7BDC52', '#527BDC', '#B352DC']
        ,['#B052DC', '#DC52C3', '#DC527E', '#7EDC52', '#52DCB0']
        ,['#526BDC', '#7E52DC', '#C352DC', '#DCC352', '#6BDC52']
        ,['#89DC52', '#52DC60', '#52DCA5', '#A552DC', '#DC5289']
        ,['#DCBA52', '#BADC52', '#74DC52', '#5274DC', '#BA52DC']
        ,['#41BA77', '#41BAB4', '#4184BA', '#BA4184', '#BA7741']
    ]

def colors4():
    return [
        '#8AD2F6', '#E48AF6', '#F6AE8A', '#9CF68A'
    ]

# out = Image.new("RGB", (WIDTH, HEIGHT), (255, 255, 255))




def img_sinusoid(color, background, w, h, lw):
    out = Image.new("RGB", (w, h), background)
    d = ImageDraw.Draw(out)
    rmax = random.randint(10, 18)
    print(rmax, ' sinuses ', lw, ' width')
    rmaxval = rmax - 1
    bw = w + lw
    wbase = bw / rmax
    for i in range(0, rmax):
        x0 = (wbase * i) - lw
        x1 = bw - (wbase * rmaxval)
        dv =  random.choice([ 0,  4, 6, 8, 10])
        if dv > 0:
            y0 = h / dv
            y1 = h - y0
        else:
            y0 = 0
            y1 = h
        if i % 2 == 0:
            s = 180
            e = 0
        else:
            s = 0;
            e = 180
        d.arc([( x0, y0), (x1, y1)], start = s, end = e, fill=color, width=lw)
        rmaxval = rmaxval - 1
    return out


def draw_lines(d, w, h, lw, colors):
    clen = len(colors)
    if clen % 2 == 0:
        mid = random.choice(colors)
    else:
        mid = colors[clen // 2]
    half = h / 2
    d.line([-lw, half, w, half], fill=mid, width=lw)  #o diagonala
    max = clen
    print('max ', max)

    step = lw * 2
    for i in range(0, max):
        d.line([-lw, half - step, w + lw, half - step], fill=colors[i], width=lw) # lines top
        step = step + lw * 2

    step = lw * 2
    for i in range(0, max):
        d.line([-lw, half + step, w + lw, half + step], fill=colors[i], width=lw)  #lines bottom
        step = step + lw * 2

# draw_lines(draw, WIDTH, HEIGHT, 10)

def draw_z(d, w, h, lw, fill, outline):
    w6 = w / 6
    w2 = w / 2
    h2 = h / 2
    triangle_vertices = [
        (w2 + w6, 0),
        (w2 - w6, h2),
        (w2 + w6, h2),
        (w2 - w6, h),
        (w2, h2)
    ]
    d.polygon(
        triangle_vertices,
        fill=fill,     # Color inside the triangle
        outline=outline,       # Color of the border
        width=lw               # Border thickness in pixels
    )

def draw_z2(d, w, h, lw, fill, outline):
    w6 = w / 6
    w2 = w / 2
    h2 = h / 2
    h6 = h / 6
    wf = 2
    hf = 2
    triangle_vertices = [
        (w2 + w6, 0),
        (w2 - w6 * wf, h2 + h6),
        (w2 , h2 + h6 / hf),
        (w2 - w6, h),
        (w2 + w6 * wf, h2 - h6),
        (w2, h2 - h6 / hf),
        (w2 + w6, 0)
    ]
    d.polygon(
        triangle_vertices,
        fill=fill,     # Color inside the triangle
        outline=outline,       # Color of the border
        width=lw               # Border thickness in pixels
    )


def img_with_lines(color, background, w, h, lw, line_colors):
    print('lw=', lw)
    out = Image.new("RGB", (w, h), background)
    d = ImageDraw.Draw(out)
    draw_lines(d, w, h, lw, line_colors) #ok
    # draw_z2(d, w, h, 0, color, 'red') #ok
    # d.ellipse([(20, 20), (w / 6, w / 6)], color) #ok, arata ca o luna

    cw = lw * len(color) * random.choice([1, 1.5, 2])
    w2 = random.choice(
        [
            w - cw / 2, w / 2, w / 1.5, w, w / random.randint(3, 9)
        ]
    )
    h2 = h / 2
    d.ellipse([( w2 - cw, h2 - cw ), (w2 + cw, h2 + cw)], color) #ok
    return out

# color = random.choice(colors3())
# draw_z2(draw, WIDTH, HEIGHT, 20, color[0], color[1])

# draw_lines(draw, WIDTH, HEIGHT, 20)

# img = img_sinusoid('black', 'white', WIDTH, HEIGHT, 10)
# img = img_with_lines('red', 'black', WIDTH, HEIGHT, random.randint(8, 15), ['magenta', 'blue', 'red'])
img = img_with_lines(
    random.choice(['#2E90FF', '#D40000', '#F5FF2E', '#2EFF49'
                   '#2EFFEA', '#F12EFF', '#FFAB2E', '#FF662E', '#FF2E2E', '#4A2EFF', '#972EFF',
                   '#D52EFF', '#FF2EBD', '#FF2E6D', '#00D40E', '#0019D4'
                   ])
, 'black', WIDTH, HEIGHT, random.randint(8, 15), ['white'] * random.randint(1, 5))

img.save('generated.png')

img.show()