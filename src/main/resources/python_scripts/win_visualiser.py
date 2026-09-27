
import turtle as l
import colorsys
import random
import util_draw

screen = l.Screen()
screen.title('States')
#set size:


util_draw.test_hello()


screen.setup(width = 1.0, height = 1.0)


canvas = screen.getcanvas()
root = canvas.winfo_toplevel()
# root.overrideredirect(1)  #ascunde bara

l.bgcolor("black")
l.tracer(100)
l.pensize(1) #grosime linie

h = 0.5


def random_rgb():
    r = random.randint(0, 255)
    g = random.randint(0, 255)
    b = random.randint(0, 255)
    return (r, g, b)

def random_hex():
    return '#{:02x}{:02x}{:02x}'.format(
        random.randint(0, 255),
        random.randint(0, 255),
        random.randint(0, 255),
    )


for i in range(450):
    # c = colorsys.hsv_to_rgb(h, 0.2, 0.1)
    # h = 0.0008 + i
    l.fillcolor(random_hex())
    l.begin_fill()
    l.fd(i)
    l.lt(100)
    l.circle(10)
    for j in range(2):
        l.fd(i * j)
        l.rt(109)
    l.end_fill()