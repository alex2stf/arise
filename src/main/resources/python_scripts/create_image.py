from PIL import Image, ImageDraw, ImageFont
import math
import random
WIDTH = 1680
HEIGHT = 1050
#  = 1000
# h = 1000



out = Image.new("RGB", (WIDTH, HEIGHT), (255, 255, 255))
draw = ImageDraw.Draw(out)



def draw_sinusoid(d, color, w, h):
    lw = 15
    rmax = random.randint(10, 18)
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


draw_sinusoid(draw, 'blue', WIDTH, HEIGHT)

# d.arc([(-lw, h / 4), (w16, h - (h / 4) )], start = 0, end = 180, fill=color, width=lw)
# d.arc([(w16 - lw, 0), ( w - (w16 * 14) , h)], start = 180, end = 0, fill=color, width=lw)
# d.arc([( (w16 * 2) - lw, 0), ( w - (w16 * 13), h)], start = 0, end = 180, fill=color, width=lw)
# d.arc([( (w16 * 3) - lw, h / 4), ( w - (w16 * 12), h)], start = 180, end = 0, fill=color, width=lw)
# d.arc([( (w16 * 4) - lw, h / 4), ( w - (w16 * 11), h - (h / 6))], start = 0, end = 180, fill=color, width=lw)
# d.arc([( (w16 * 5) - lw, h / 5), ( w - (w16 * 10) , h)], start = 180, end = 0, fill=color, width=lw)
# d.arc([( (w16 * 6) - lw, h / 6), ( w - (w16 * 9) , h)], start = 0, end = 180, fill=color, width=lw)
# d.arc([( (w16 * 7) - lw, h - (h / 2) ), ( w - (w16 * 8), h - (h / 4))], start = 0, end = 360, fill='cyan', width=lw)
# d.arc([( (w16 * 8) - lw, (h / 2) - (h / 4) ), ( w - (w16 * 7), h - (h / 6) )], start = 0, end = 360, fill='green', width=lw)

# d.arc([( (w16 * 9) - lw, (h / 2) - (h / 4) ), ( w - (w16 * 6), h - (h / 8) )], start = 180, end = 0, fill='green', width=lw)
# d.arc([( (w16 * 10) - lw, 0 ), ( w - (w16 * 5), h )], start = 0, end = 180, fill='blue', width=lw)
# d.arc([( (w16 * 11) - lw, 0 ), ( w - (w16 * 4), h )], start = 180, end = 0, fill='green', width=lw)
# d.arc([( (w16 * 12) - lw, (h / 2) - (h / 4) ), ( w - (w16 * 3), h - (h / 4))], start = 0, end = 180, fill=color, width=lw)

# d.arc([(0, 0), (1680, 1050)], start = 0, end = 90, fill ="red") #la fel ca pieslice

# d.line([0, 0, w, h], fill='green', width=40)  #o diagonala

#smiley
# d.ellipse((0,0,90,90),'yellow','blue') #capul
# d.ellipse((25,20,35,30),'yellow','blue') #ochi dreapta
# d.ellipse((50,20,60,30),'yellow','blue') #ochi stanga
# d.arc((20,40,70,70), 0, 180, 0) #gura

# draw.line([10, 10, 120, 120], fill='red')
# draw.rectangle((0, 0, 1, 1), (0, 255, 0, 127))
# draw.chord([0, 0, 10, 10], fill='blue')

out.save('generated.png')

out.show()