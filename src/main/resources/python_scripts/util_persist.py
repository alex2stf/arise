import os
import random

def app_folder():
    user_folder = os.path.expanduser("~")
    if not os.path.exists(user_folder):
        user_folder = '/arise-user'
        os.mkdir(user_folder)
    app_fldr = os.path.abspath(os.path.join(user_folder, "arise-app"))
    if not os.path.exists(app_fldr):
        os.mkdir(app_fldr)
    return app_fldr

def get_tmp_file(name):
    return os.path.join(app_folder(), name)


def write_pfile_zero(list, fname):
    indices = []
    for index, element in enumerate(list):
        indices.append(index)
    random.shuffle(indices)
    with open(fname, 'w') as f:
        f.write('0\n')
        for i in indices:
            f.write("%s\n" % i)
    return list[0]



def load_file_as_list(fname):
    list = []
    with open(fname) as file:
        for line in file:
            lx = (line.rstrip())
            if lx:
                list.append(lx)
    return list

def rand_pick_from_list(list):
    length = len(list)
    fname = "pil" + str(length) + ".txt"
    file = get_tmp_file(fname)

    if not os.path.exists(file) or 0 == os.path.getsize(file):
        print('no file found, writing ', fname)
        write_pfile_zero(list, file)


    lines = load_file_as_list(file)
    index = int(lines[0])

    # print("pick index ", index, 'from ', len(list), 'INdex', list[11])
    if index > (len(list) - 1):
        write_pfile_zero(list, file)
        index = 0
        print("overflow, re-writing... ", fname)


    res = list[int(lines[index + 1])] #mereu index + 1 ptr ca pe pozitia 0 se afla indexul salvat

    lines[0] = str(index + 1)

    with open(file, "w") as f:
        for i in lines:
            f.write("%s\n" % i)

    return res