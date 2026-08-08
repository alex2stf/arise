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
speed=100

canvas = tk.Canvas(root, width=width, height=height, bg='white')
canvas.configure(bg='black')

canvas.pack(anchor=tk.CENTER, expand=True)

def redraw():
    canvas.delete("all")
    canvas.after(speed, redraw)
    for number in range(math.floor(width / line_width)):
        fill="#"+("%06x"%random.randint(0,16777215))
        #bar_top
        # canvas.create_line((number * line_width, 0), (number * line_width, random.randint(0, 600)), width=line_width, fill=fill)
        #bar_bottom
        canvas.create_line((number * line_width, 600), ( number * line_width, random.randint(0, 600)), width=line_width, fill=fill)
    canvas.update()

canvas.after(speed, redraw)
root.mainloop()