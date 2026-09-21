import time
from pathlib import Path
from PIL import Image, ImageChops

folder = Path(__file__).parent

# near-white / near-gray checkerboard specks (e.g. 249,249,249 next to 254,254,254)
# baked into the source art as opaque pixels, not real alpha - flatten those too.
NEAR_WHITE_MIN = 235
NEAR_GRAY_TOLERANCE = 10


def flatten_checker(rgb):
    r, g, b = rgb.split()
    min_channel = ImageChops.darker(ImageChops.darker(r, g), b)
    max_channel = ImageChops.lighter(ImageChops.lighter(r, g), b)
    max_diff = ImageChops.difference(max_channel, min_channel)
    is_bright = min_channel.point(lambda p: 255 if p >= NEAR_WHITE_MIN else 0)
    is_neutral = max_diff.point(lambda p: 255 if p <= NEAR_GRAY_TOLERANCE else 0)
    mask = ImageChops.darker(is_bright, is_neutral)
    white = Image.new("RGB", rgb.size, "#ffffff")
    return Image.composite(white, rgb, mask)


for png in folder.glob("*.png"):
    with Image.open(png) as im:
        img = im.convert("RGBA")
    bg = Image.new("RGBA", img.size, "#ffffff")
    bg.paste(img, mask=img)
    rgb = flatten_checker(bg.convert("RGB"))
    for attempt in range(5):
        try:
            rgb.save(png)
            break
        except OSError:
            time.sleep(0.5)
    print(f"done: {png.name}")
