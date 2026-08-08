import tkinter as tk
import math
import random

root = tk.Tk()
root.geometry('600x600')
root.title('Canvas Demo')
root.configure(bg='black')


width=600
height=600
line_width=4
speed=50
MAX = 60
# center = 300
center_x = random.randint(200, 400)
center_y = random.randint(200, 400)
radius = 200
i = 0
iterations = 0

canvas = tk.Canvas(root, width=width, height=height, bg='white')
canvas.pack(anchor=tk.CENTER, expand=True)
canvas.configure(bg='black')


def redraw():
    global i
    global  center_x
    global  center_y
    global  iterations
    angle = i * (3 * math.pi / MAX)
    radius = random.randint(0, 200)
    if iterations < 10:
        fill="#"+("%06x"%random.randint(0,16777215))
    else:
        fill = 'black'
    x = center_x + radius * math.cos(angle)
    y = center_y + radius * math.sin(angle)
    canvas.create_line((center_x, center_y), ( x, y), width=line_width, fill=fill)
    i = i + 1

    if i > MAX:
        i = 0
        center_x = random.randint(200, 400)
        center_y = random.randint(200, 400)
        iterations = iterations + 1
        if iterations > 14:
            iterations = 0

    canvas.after(speed,redraw)
    canvas.update()

canvas.after(speed, redraw)

root.mainloop()