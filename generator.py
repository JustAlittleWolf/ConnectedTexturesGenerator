import json
import math
import os
from PIL import Image

path = os.path.dirname(os.path.realpath(__file__))
userinput = input("Please provide the file location: ")
source = Image.open(userinput)
width, height = source.size
px = source.load()

path += "\\" + os.path.splitext(os.path.basename(userinput))[0]
print(path)

try:
    os.mkdir(path)
except OSError:
    print("Creation of the directory %s failed" % path)
else:
    print("Successfully created the directory %s " % path)

# corner
cr = Image.new("RGBA", (8, 8), color=(0, 0, 0, 0))
crpx = cr.load()
i = 0
while(i < 64):
    crpx[i % 8, math.floor(i/8)] = px[i % 8, math.floor(i/8)]
    i += 1

# edge
ed = Image.new("RGBA", (8, 8), color=(0, 0, 0, 0))
edpx = ed.load()
i = 0
while(i < 64):
    edpx[i % 8, math.floor(i/8)] = px[i % 8, math.floor(i/8) + 8]
    i += 1

# overflow
of = Image.new("RGBA", (8, 8), color=(0, 0, 0, 0))
ofpx = of.load()
i = 0
while(i < 64):
    ofpx[i % 8, math.floor(i/8)] = px[i % 8 + 8, math.floor(i/8)]
    i += 1

# null
nu = Image.new("RGBA", (8, 8), color=(0, 0, 0, 0))
nupx = nu.load()
i = 0
while(i < 64):
    nupx[i % 8, math.floor(i/8)] = px[(i % 8) + 8, math.floor(i/8) + 8]
    i += 1

align = open(os.path.dirname(os.path.realpath(__file__)) +
             "\\alignments.json", "r")
aligns = json.load(align)

tempa = []
temp = []
j = 0
while j < 47:
    tempa.append(Image.new("RGBA", (16, 16), color=(0, 0, 0, 0)))
    temp.append(tempa[j].load())

    if aligns[str(j)]["tl"] == "cr":
        i = 0
        while i < 64:
            temp[j][i % 8, math.floor(i/8)] = crpx[i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tl"] == "edh":
        i = 0
        while i < 64:
            temp[j][i % 8, math.floor(
                i/8)] = edpx[math.floor(i / 8), 7 - i % 8]
            i += 1
    elif aligns[str(j)]["tl"] == "edv":
        i = 0
        while i < 64:
            temp[j][i % 8, math.floor(i/8)] = edpx[i % 8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tl"] == "of":
        i = 0
        while i < 64:
            temp[j][i % 8, math.floor(i/8)] = ofpx[7 - i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tl"] == "nu":
        i = 0
        while i < 64:
            temp[j][i % 8, math.floor(i/8)] = nupx[7 - i %
                                                   8, 7 - math.floor(i/8)]
            i += 1

    if aligns[str(j)]["tr"] == "cr":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, math.floor(i/8)] = crpx[7 - i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tr"] == "edh":
        i = 0
        while i < 64:
            temp[j][8 + i % 8, math.floor(
                i/8)] = edpx[math.floor(i / 8), i % 8]
            i += 1
    elif aligns[str(j)]["tr"] == "edv":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, math.floor(i/8)] = edpx[7 - i % 8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tr"] == "of":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, math.floor(i/8)] = ofpx[i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["tr"] == "nu":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, math.floor(i/8)] = nupx[i % 8, 7 - math.floor(i/8)]
            i += 1

    if aligns[str(j)]["bl"] == "cr":
        i = 0
        while i < 64:
            temp[j][i % 8, 8 + math.floor(i/8)] = crpx[i %
                                                       8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["bl"] == "edh":
        i = 0
        while i < 64:
            temp[j][i % 8, 8 + math.floor(
                i/8)] = edpx[7 - math.floor(i / 8), 7 - i % 8]
            i += 1
    elif aligns[str(j)]["bl"] == "edv":
        i = 0
        while i < 64:
            temp[j][i % 8, 8 + math.floor(i/8)] = edpx[i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["bl"] == "of":
        i = 0
        while i < 64:
            temp[j][i %
                    8, 8 + math.floor(i/8)] = ofpx[7 - i % 8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["bl"] == "nu":
        i = 0
        while i < 64:
            temp[j][i %
                    8, 8 + math.floor(i/8)] = nupx[7 - i % 8, math.floor(i/8)]
            i += 1

    if aligns[str(j)]["br"] == "cr":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, 8 + math.floor(i/8)] = crpx[7 - i % 8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["br"] == "edh":
        i = 0
        while i < 64:
            temp[j][8 + i % 8, 8 + math.floor(
                i/8)] = edpx[7 - math.floor(i / 8), i % 8]
            i += 1
    elif aligns[str(j)]["br"] == "edv":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, 8 + math.floor(i/8)] = edpx[7 - i % 8, math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["br"] == "of":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, 8 + math.floor(i/8)] = ofpx[i % 8, 7 - math.floor(i/8)]
            i += 1
    elif aligns[str(j)]["br"] == "nu":
        i = 0
        while i < 64:
            temp[j][8 + i %
                    8, 8 + math.floor(i/8)] = nupx[i % 8, math.floor(i/8)]
            i += 1

    if width == 32:
        i = 0
        while i < 256:
            if temp[j][i % 16, math.floor(i/16)][2] == 0:
                temp[j][i % 16, math.floor(i/16)] = px[16 + i % 16, math.floor(i/16)]
            i += 1

    tempa[j].save(path + "/" + str(j) + ".png")
    j += 1

print("\nDone")

read = open(path + "/README.txt", "w+")
read.write("Put this folder in assets\\minecraft\\mcpatcher\\ctm for it to be working.\n\nYou may delete this README.\n\nPlease note that you might have to rename " +
           os.path.splitext(os.path.basename(userinput))[0] + " and it's contents in order for the textures to work properly. If you are not sure how, contact me on discord: JustAlittleWolf#7032")

properties = open(
    path + "/" + os.path.splitext(os.path.basename(userinput))[0] + ".properties", "w+")
properties.write("method=ctm\ntiles=0-46")
