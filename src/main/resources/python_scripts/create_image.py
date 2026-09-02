from PIL import Image, ImageDraw, ImageFont

w = 1680
h = 1050
# h = 400



out = Image.new("RGB", (w, h), (255, 255, 255))
d = ImageDraw.Draw(out)

w16 = w / 16
h4 = h / 4
lw = 15
color  = 'red'


d.arc([(-lw, 0), (w16, h - h4)], start = 0, end = 180, fill=color, width=lw)
d.arc([(w16 - lw, 0), ( w - (w16 * 14) , h)], start = 180, end = 0, fill=color, width=lw)
d.arc([( (w16 * 2) - lw, 0), ( w - (w16 * 13), h)], start = 0, end = 180, fill=color, width=lw)
d.arc([( (w16 * 3) - lw, h / 4), ( w - (w16 * 12), h)], start = 180, end = 0, fill=color, width=lw)
d.arc([( (w16 * 4) - lw, h / 4), ( w - (w16 * 11), h - (h / 6))], start = 0, end = 180, fill=color, width=lw)
d.arc([( (w16 * 5) - lw, h / 5), ( w - (w16 * 10) , h)], start = 180, end = 0, fill=color, width=lw)
d.arc([( (w16 * 6) - lw, h / 6), ( w - (w16 * 9) , h)], start = 0, end = 180, fill=color, width=lw)
d.arc([( (w16 * 7) - lw, h - (h / 2) ), ( w - (w16 * 8), h - (h / 4))], start = 180, end = 0, fill=color, width=lw)
d.arc([( (w16 * 8) - lw, h / 3.5 ), ( w - (w16 * 7), h - (h / 8) )], start = 0, end = 180, fill=color, width=lw)
# d.arc([(0, 0), (1680, 1050)], start = 0, end = 90, fill ="red") #la fel ca pieslice

# d.line([0, 0, w, h], fill='green', width=40)  #o diagonala

#smiley
d.ellipse((0,0,90,90),'yellow','blue') #capul
d.ellipse((25,20,35,30),'yellow','blue') #ochi dreapta
d.ellipse((50,20,60,30),'yellow','blue') #ochi stanga
d.arc((20,40,70,70), 0, 180, 0) #gura

# draw.line([10, 10, 120, 120], fill='red')
# draw.rectangle((0, 0, 1, 1), (0, 255, 0, 127))
# draw.chord([0, 0, 10, 10], fill='blue')

out.save('generated.png')

out.show()