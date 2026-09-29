import turtle
import util_draw
import random
import util_persist
import math

screen = turtle.Screen()
screen.setup(width=1.0, height=1.0)
# turtle.speed(0)
turtle.tracer(0)
turtle.bgcolor("black")





def circles_linear(t, bounds, c_size, pensize, coloring_method):

    width = bounds['width']
    height = bounds['height']

    x = bounds['from_x'] + (c_size + pensize)
    y = bounds['from_y'] - (c_size * 2)

    print("y = ", y)
    print('width=', width, ' height=', height, ' c_size=', c_size)

    t.pensize(pensize)

    count = 0

    # limit_right = (width / 2)
    limit_right = bounds['to_x']
    limit_bottom = bounds['to_y'] - c_size

    points = []
    orig = []
    while y > limit_bottom:
        # for j in range(1000):
        count = count + 1

        if x > limit_right:
            x = -(limit_right) + c_size + pensize
            y = y - (c_size * 2) - pensize

        if y < limit_bottom:
            break
        print(" y=", y, ' x=', x, ' limit_right=', limit_right, ' limit_bottom=', limit_bottom, 'count=', count)

        points.append({
            'x': x,
            'y': y
        })
        orig.append({
            'x': x,
            'y': y
        })
        x = x + (c_size * 2) + pensize

    random.shuffle(points)

    for i in range(len(points)):
        p = points[i]
        t.penup()
        # print(' draw ', p['x'], ' cu ', p['y'])
        t.goto(p['x'], p['y'])
        t.pendown()
        color = coloring_method()
        t.color(color)
        t.fillcolor(color)
        t.begin_fill()
        t.circle(c_size)
        t.end_fill()

    print('iterations =', count)

def circle_web(t, radius, iterations, pensize):
    t.pensize(pensize)
    for i in range(iterations):
        angle = i
        center = 0

        ora_x = center + (radius) * math.cos(angle)
        ora_y = center + (radius) * math.sin(angle)
        t.color(util_draw.random_light_color())
        t.goto(ora_x, ora_y)
        if(i > iterations / 2):
            radius = radius - 1
        else:
            radius = radius + 1
        # print(i, ' din ', screen.window_height())

import util_turtle

bounds = util_turtle.get_screen_bounds(screen)

circles_linear(turtle, bounds, 40, 2, util_draw.random_light_color)
turtle.penup()
turtle.goto(0, 0)
turtle.pendown()
# circle_web(turtle, random.randint(1, 40), 500, 5)
circle_web(turtle,
        random.randint(2, 40),
        min(bounds['width'], bounds['height']),
        random.choice([2, 3, 4, 5])
)
print("done")
turtle.done()
