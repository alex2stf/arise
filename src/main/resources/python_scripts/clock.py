from tkinter import *
from tkinter.ttk import *


from time import strftime

# creating tkinter window
root = Tk()
root.title('Time')
root.wm_attributes("-alpha", 0.8)


def time():
    # string = strftime('%H:%M:%S %p')
    string = strftime('%I:%M %p')
    lbl.config(text=string)
    lbl.after(1000, time)


# Styling the label widget so that clock
# will look more attractive
lbl = Label(root, font=('calibri', 40, 'bold'),
            background='purple',
            foreground='white')

# Placing clock at the centre
# of the tkinter window
lbl.pack(anchor='center')
time()

mainloop()