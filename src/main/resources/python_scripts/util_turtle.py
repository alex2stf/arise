def get_screen_bounds(s):
    width = s.window_width()
    height = s.window_height()
    return {
        'width': width,
        'height': height,
        'from_x': -(width / 2),
        'from_y': (height / 2),
        'to_x': (width / 2),
        'to_y': -(height / 2)
    }