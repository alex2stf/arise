import random
import util_persist

def test_hello():
    print("hello world")


def random_hex():
    return '#{:02x}{:02x}{:02x}'.format(
        random.randint(0, 255),
        random.randint(0, 255),
        random.randint(0, 255),
    )


def random_light_color():
    return util_persist.rand_pick_from_list(light_colors())

#src at https://htmlcolorcodes.com/color-picker/
def light_colors():
    return [
        '#F5D0D0', '#F7F3BC', '#E9F7BC', '#ACFAA5', '#A5FAE3',
        '#A5C6FA', '#AFA5FA', '#E8A5FA', '#FAA5ED', '#FAA5D5',
        '#FAA5B2', '#F2C9CF', '#C29199', '#D17D8E', '#BD5C71',
        '#EACCDC', '#DDACC5', '#BD5C73', '#B55CBD', '#DB8BE0', '#EDC7F0', '#D6B4D9', '#E1DAE3',
        '#A08CA8', '#B69AD6', '#BB91ED', '#A155FA', '#555BFA', '#9296F0', '#AAADE3', '#777AA6',
        '#D4D5E3', '#9CADBF', '#D4DBE3', '#779CA6', '#A5C8D1', '#C5D6D9', '#7FF0DD', '#BFF8EE',
        '#BEE8E1', '#71BDA4', '#84F0CC', '#84D1B8', '#84D1A0', '#84E8A9', '#5BF092', '#5BF06C',
        '#7FC789', '#92C77F', '#84ED5F', '#9FED5F', '#BAE398', '#B7E66C', '#C3E391', '#CFE391',
        '#B5D45D', '#E8E57B', '#E0EBA0', '#EBE4A0', '#F0E369', '#F0CE69', '#DECE9E', '#EBD596',
        '#EBBC96', '#F2AB6F', '#F2C096', '#F59F53', '#F57E53', '#F2A588', '#F28888', '#E3A8A8',
        '#E89797'
    ]