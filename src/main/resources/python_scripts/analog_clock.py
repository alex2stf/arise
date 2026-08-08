import tkinter as tk
import math
import datetime

size = 500
line_width=4

root = tk.Tk()
root.geometry(str(size) + 'x' + str(size))


root.title('Clock analogic')

root.wm_attributes("-alpha", 0.5)  #transparenta full

MAX_MINUTES=60
center = size / 2
radius = size / 2
fill='red'
# root.configure(bg='black')

canvas = tk.Canvas(root, width=size, height=size, bg='black')
canvas.pack(anchor=tk.CENTER, expand=True)

p_ore = []
p_minute = []
p_secunde = []
ids = []

hour = 0
for i in range(MAX_MINUTES):
    start = i - 15
    angle = start * (2 * math.pi / MAX_MINUTES)
    ora_x = center + (radius/3) * math.cos(angle)
    ora_y = center + (radius/3) * math.sin(angle)

    p_ore.append((ora_x, ora_y))
    min_x = center + (radius/2) * math.cos(angle)
    min_y = center + (radius/2) * math.sin(angle)

    p_minute.append((min_x, min_y))


    sec_x = center + (radius / 1.8) * math.cos(angle)
    sec_y = center + (radius / 1.8) * math.sin(angle)

    p_secunde.append((sec_x, sec_y))

    spf_x = center + (radius / 1.7) * math.cos(angle)
    spf_y = center + (radius / 1.7) * math.sin(angle)

    spto_x = center + (radius / 1.6) * math.cos(angle)
    spto_y = center + (radius / 1.6) * math.sin(angle)

    txt_x = center + (radius / 1.5) * math.cos(angle)
    txt_y = center + (radius / 1.5) * math.sin(angle)

    # canvas.create_text(txt_x,txt_y,fill="darkblue",font="Times 10 italic bold", text=str(i))

    if i%5==0:
        canvas.create_text(txt_x,txt_y,fill="white",font="Times 10 italic bold",
                            text=str(hour))
        hour = 1 + hour

    canvas.create_line((spf_x, spf_y), (spto_x, spto_y), width=line_width, fill='white')
    # canvas.create_line((center, center), (sec_x, sec_y), width=line_width, fill='yellow')
    # canvas.create_line((center, center), (min_x, min_y), width=line_width, fill='blue')
    # id = canvas.create_line((center, center), (ora_x, ora_y), width=line_width, fill='red')
    # canvas.delete(id) #sterge doar o linie


secundar = ''
minutar = ''
orar = ''
txt_box = ''

def redraw():
    global secundar
    global minutar
    global orar
    global txt_box
    canvas.delete(secundar)
    canvas.delete(minutar)
    canvas.delete(orar)
    canvas.delete(txt_box)
    now = datetime.datetime.now()
    minut = now.minute
    sec_poz = p_secunde[now.second]
    min_poz = p_minute[minut]
    hour_12 = int(now.strftime('%I'))
    index_ora = (hour_12 * 5) + math.floor(now.minute / 12)
    ora_poz = p_ore[index_ora]
    secundar = canvas.create_line((center, center), sec_poz, width=line_width, fill='green')
    minutar = canvas.create_line((center, center), min_poz, width=line_width, fill='blue')
    orar = canvas.create_line((center, center), ora_poz, width=line_width, fill='red')
    txt_box = canvas.create_text(size / 2, size - 28,fill="white",font="Times 14 italic bold",
                                               text=now.strftime('%H:%M:%S %p'))
    canvas.after(1000, redraw)
    canvas.update()

redraw()
root.mainloop()