from turtle import *
import turtle
import util_turtle
import random
import math
# Set up the turtle
# t = turtle.Turtle()

# Define dimensions
# height = 200
# width = 100

screen = turtle.Screen()
screen.setup(width=1.0, height=1.0)
screen.colormode(255)
bounds = util_turtle.get_screen_bounds(screen)
turtle.speed(200)

class Disc(Turtle):
    def __init__(self, n):
        Turtle.__init__(self, shape="square", visible=False)
        self.penup()
        self.shapesize(n*2, 2, 2) # square-->rectangle
        self.fillcolor((255, n, math.floor(255 // n) ))
        self.showturtle()
        self.speed(2)
#
numbers = []
for i in range(1, 25):
    numbers.append(i)

random.shuffle(numbers)

x_poz = bounds['from_x'] + 20
discs = []

for i in numbers:
    d = Disc(i)
    # d.speed(200)
    d.speed(100)
    d.setposition(x_poz, bounds['from_y'])
    x_poz = x_poz + 50
    print(d.xcor(), ' y=', d.ycor(), ' x=', d.position()[0])
    discs.append({
        'd': d,
        'num': i
    })
    # d.speed(1)
# Disc(1)
# Disc(2).setposition(200, 200)


def swap_elements(i1, i2, arr):
    print('swap...', arr[i1]['num'], ' cu ', arr[i2]['num'])
    # print('swap...', i1, ' cu ', i2)
    # canvas.moveto(arr[i1]['text'], arr[i2]['rect_x0'], arr[i2]['rect_y1'])
    # canvas.moveto(arr[i2]['text'], tmp['rect_x0'], tmp['rect_y1'])
    a = arr[i1]['d'].position()
    b = arr[i2]['d'].position()
    arr[i1]['d'].setposition(b[0], b[1])
    arr[i2]['d'].setposition(a[0], a[1])

    tmp = arr[i1]
    arr[i1] = arr[i2]
    arr[i2] = tmp


    # canvas.moveto(arr[i1]['rect'], 20, arr[i2]['rect_y0'])
    # canvas.moveto(arr[i2]['rect'], 20, arr[i2]['rect_y0'])

def partition(arr, low, high):
    pivot = arr[high]
    i = low - 1
    for j in range(low, high):
        if arr[j]['num'] < pivot['num']:
            i += 1
            swap_elements(i, j, arr)
    swap_elements(i + 1, high, arr)
    return i + 1

def quick_sort(arr, low, high):
    if low < high:
        p = partition(arr, low, high)
        initcolor = arr[p]['d'].fillcolor()
        # print(initcolor)
        # arr[p]['d'].fillcolor('blue')
        quick_sort(arr, low, p - 1)
        quick_sort(arr, p + 1, high)
        # arr[p]['d'].fillcolor(initcolor)

quick_sort(discs, 0, len(discs) - 1)

for i in discs:
    print(i['num'])

turtle.done()
# def draw_rectangle(t, width, height):
# for _ in range(2):
#     t.forward(height)
#     t.left(90)
#     t.forward(width)
#     t.left(90)



# Draw the rectangle



# draw_rectangle(t, 20, 20)
# Keep the window open
# turtle.done()