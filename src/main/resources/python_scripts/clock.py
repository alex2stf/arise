from tkinter import *
from tkinter.ttk import *
import datetime
import random


palette = [
    {'f': 'white', 'b': 'black'}
    ,{'f': 'black', 'b': 'white'}
    ,{'f': '#FFDFDF', 'b': '#560505'}
    ,{'f': '#FFF5DF', 'b': '#554111'}
    ,{'f': '#F9BCE4', 'b': '#420D2F'}
    ,{'f': '#F4F1FF', 'b': '#14083A'}
    ,{'f': '#F0FFEF', 'b': '#054502'}
    ,{'f': '#D386AC', 'b': '#2A0438'}
    ,{'f': '#F1FA9F', 'b': '#553003'}
    ,{'f': '#F59BA6', 'b': '#552A03'}
    ,{'f': '#DF9EFE', 'b': '#1C0F02'}
]

# creating tkinter window
root = Tk()
root.title('Time')

# root.wm_attributes("-alpha", 0.8) #transparency

wdays = ["Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"]
mths =["Gerar", "Făurar", "Mărțișor", "Prier", "Florar", "Cireșar", "Cuptor", "Gustar", "Răpciune", "Brumărel", "Brumar", "Undrea"]

color = random.choice(palette)
back = color['b']
fron = color['f']

def time():
    # now = datetime.date(1900, 4, 1)
    global back
    global fron

    now = datetime.datetime.now()
    if now.second!= 0 and now.second % 31 == 0:
        color = random.choice(palette)
        back = color['b']
        fron = color['f']

    # bg = PhotoImage(file = "C:\\Users\\Administrator\\Pictures\\poza.png")
    root.configure(bg=back)
    # string = strftime('%H:%M:%S %p')
    str_time = now.strftime('%I:%M %p')
    str_day = mths[now.month - 1] + " " + wdays[now.weekday()] + " " + str(now.day)
    # wdays[now.weekday()] + " " + str(now.day)  + "/" + str(now.month) + ": "+ now.strftime('%I:%M %p')
    # fill="#"+("%06x"%random.randint(0,16777215))
    lbl_time.config(text=str_time, background=back, foreground=fron)
    lbl_day.config(text=str_day, background=back, foreground=fron)
    lbl_time.after(1000, time)


# Styling the label widget so that clock
# will look more attractive
lbl_time = Label(root, font=('calibri', 35, 'bold'),
            background='purple',
            foreground='white')

lbl_day = Label(root, font=('calibri', 15, 'bold italic'),
            background='#00FF00',
            foreground='white')

# Placing clock at the centre
# of the tkinter window
lbl_time.pack(anchor='center')
lbl_day.pack(anchor='center')
time()

mainloop()