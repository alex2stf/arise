import tkinter
import datetime
import random

size = 600

root = tkinter.Tk()
root.title('Time')

canvas = tkinter.Canvas(root, width=size, height=size, bg='white')
canvas.pack(anchor=tkinter.CENTER, expand=True)
width = size / 20
padding = 4
x0 = 0
y0 = 20
x1 = 20
y1 = 40
numbers = []

for i in range(20):
    numbers.append(i + 20)

random.shuffle(numbers)


blocks = []
for i in range(20):
    pos_x = x0 + padding
    y1 = numbers[i]
    rectangle = canvas.create_rectangle( pos_x, 0, x1, y1, fill='red', outline = 'black')
    text = canvas.create_text(x1, y1,fill="black",font="Times 10 italic bold",
                       text=str(y1))
    blocks.append({
        'rect': rectangle,
        'text': text,
        'rect_x0': pos_x,
        'rect_y0': y0,
        'rect_x1': x1,
        'rect_y1': y1,
        'num': y1
    })
    x0 = x0 + width
    x1 = x1 + width
    # y1 = y1 + 20 #random.randint(20, 180)

    print('x0=', x0, ' x1=', x1)


# canvas.create_rectangle( 0, 0, 20, 30, fill='red', outline = 'black')
# canvas.create_rectangle( 22, 0, 40, 30, fill='red', outline = 'black')
# canvas.create_rectangle( 42, 0, 60, 30, fill='red', outline = 'black')
# canvas.create_rectangle( 62, 0, 80, 30, fill='red', outline = 'black')
# canvas.create_rectangle( 82, 0, 100, 30, fill='red', outline = 'black')



def compare_is_less_than(a, b):
    print('a=', a , ' b=', b)
    return a['num'] < b['num']

import time

def swap_elements(i1, i2, arr, update_ui):
    # print('swap...', arr[i1]['num'], ' cu ', arr[i2]['num'])
    print('swap...', i1, ' cu ', i2)
    ca = canvas.coords(arr[i1]['text'])
    cb = canvas.coords(arr[i2]['text'])
    print(ca, ' CU ', arr[i1])
    # canvas.moveto(arr[i1]['text'], arr[i2]['rect_x0'], arr[i2]['rect_y1'])
    # canvas.moveto(arr[i2]['text'], tmp['rect_x0'], tmp['rect_y1'])
    if update_ui:
        canvas.coords(arr[i1]['text'], cb[0], 60)
        canvas.coords(arr[i2]['text'], ca[0], 60)
    time.sleep(1)
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
            swap_elements(i, j, arr, True)
    swap_elements(i + 1, high, arr, True)
    return i + 1

def quick_sort(arr, low, high):
    if low < high:
        p = partition(arr, low, high)

        # root.after(1000, quick_sort(arr, low, p - 1))
        # root.after(1000, quick_sort(arr, p + 1, high))
        quick_sort(arr, low, p - 1)
        quick_sort(arr, p + 1, high)




def animate():
    # for i in range(20):
    #     n = 0
    #     if i > 0:
    #         n = i - 1
    #     print(i, 'n = ', n)
    #     swap_elements(i, n, blocks, True)
    quick_sort(blocks, 0, len(blocks) - 1)
    # root.update()
    # for i in blocks:
    #     print(i['num'])


root.after(1000, animate)
# animate()


root.mainloop()