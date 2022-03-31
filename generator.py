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
print("\n")

try:
    os.mkdir(path)
except OSError:
    print("Creation of the directory %s failed. Path already exists?" % path)
    if input("Continue script execution? (y/n)") == "n":
        print("Exiting script")
        quit()
else:
    print("Successfully created the directory %s " % path)

# corner
cr = Image.new("RGBA", (math.floor(height/2), math.floor(height/2)), color=(0, 0, 0, 0))
crpx = cr.load()
i = 0
while(i < math.floor((height*height)/4)):
    crpx[i % (math.floor(height/2)), math.floor(i/math.floor(height/2))] = px[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
    i += 1

# edge
ed = Image.new("RGBA", (math.floor(height/2), math.floor(height/2)), color=(0, 0, 0, 0))
edpx = ed.load()
i = 0
while(i < math.floor((height*height)/4)):
    edpx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = px[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2))) + (math.floor(height/2))]
    i += 1

# overflow
of = Image.new("RGBA", (math.floor(height/2), math.floor(height/2)), color=(0, 0, 0, 0))
ofpx = of.load()
i = 0
while(i < math.floor((height*height)/4)):
    ofpx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = px[i % (math.floor(height/2)) + (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
    i += 1

# null
nu = Image.new("RGBA", (math.floor(height/2), math.floor(height/2)), color=(0, 0, 0, 0))
nupx = nu.load()
i = 0
while(i < math.floor((height*height)/4)):
    nupx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = px[(i % (math.floor(height/2))) + (math.floor(height/2)), math.floor(i/(math.floor(height/2))) + (math.floor(height/2))]
    i += 1

align = open(os.path.dirname(os.path.realpath(__file__)) +
             "\\alignments.json", "r")
aligns = json.load(align)

tempa = []
temp = []
j = 0
while j < 47:
    tempa.append(Image.new("RGBA", (height, height), color=(0, 0, 0, 0)))
    temp.append(tempa[j].load())

    if aligns[str(j)]["tl"] == "cr":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = crpx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tl"] == "edh":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), math.floor(
                i/(math.floor(height/2)))] = edpx[math.floor(i / (math.floor(height/2))), (math.floor(height/2)-1) - i % (math.floor(height/2))]
            i += 1
    elif aligns[str(j)]["tl"] == "edv":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = edpx[i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tl"] == "of":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = ofpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tl"] == "nu":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = nupx[(math.floor(height/2)-1) - i %(math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1

    if aligns[str(j)]["tr"] == "cr":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = crpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tr"] == "edh":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i % (math.floor(height/2)), math.floor(
                i/(math.floor(height/2)))] = edpx[math.floor(i / (math.floor(height/2))), i % (math.floor(height/2))]
            i += 1
    elif aligns[str(j)]["tr"] == "edv":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = edpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tr"] == "of":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = ofpx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["tr"] == "nu":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), math.floor(i/(math.floor(height/2)))] = nupx[i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1

    if aligns[str(j)]["bl"] == "cr":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = crpx[i %
                                                       (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["bl"] == "edh":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), (math.floor(height/2)) + math.floor(
                i/(math.floor(height/2)))] = edpx[(math.floor(height/2)-1) - math.floor(i / (math.floor(height/2))), (math.floor(height/2)-1) - i % (math.floor(height/2))]
            i += 1
    elif aligns[str(j)]["bl"] == "edv":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i % (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = edpx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["bl"] == "of":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i %
                    (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = ofpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["bl"] == "nu":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][i %
                    (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = nupx[(math.floor(height/2)-1) - i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1

    if aligns[str(j)]["br"] == "cr":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = crpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["br"] == "edh":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i % (math.floor(height/2)), (math.floor(height/2)) + math.floor(
                i/(math.floor(height/2)))] = edpx[(math.floor(height/2)-1) - math.floor(i / (math.floor(height/2))), i % (math.floor(height/2))]
            i += 1
    elif aligns[str(j)]["br"] == "edv":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = edpx[(math.floor(height/2)-1) - i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["br"] == "of":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i %
                    (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = ofpx[i % (math.floor(height/2)), (math.floor(height/2)-1) - math.floor(i/(math.floor(height/2)))]
            i += 1
    elif aligns[str(j)]["br"] == "nu":
        i = 0
        while i < math.floor((height*height)/4):
            temp[j][(math.floor(height/2)) + i % (math.floor(height/2)), (math.floor(height/2)) + math.floor(i/(math.floor(height/2)))] = nupx[i % (math.floor(height/2)), math.floor(i/(math.floor(height/2)))]
            i += 1

    if width == 2 * height:
        i = 0
        while i < height*height:
            if temp[j][i % height, math.floor(i/height)][3] == 0:
                temp[j][i % height, math.floor(i/height)] = px[height + i % height, math.floor(i/height)]
            i += 1

    tempa[j].save(path + "/" + str(j) + ".png")
    j += 1

print("Done")

read = open(path + "/README.txt", "w+")
read.write("Put this folder in assets\\minecraft\\mcpatcher\\ctm for it to be working.\n\nYou may delete this README.\n\nPlease note that you might have to rename " +
           os.path.splitext(os.path.basename(userinput))[0] + " and it's contents in order for the textures to work properly. If you are not sure how, contact me on discord: JustAlittleWolf#7032")

properties = open(
    path + "/" + os.path.splitext(os.path.basename(userinput))[0] + ".properties", "w+")
properties.write("method=ctm\ntiles=0-46")
