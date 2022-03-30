# ConnectedTexturesGenerator
This python script provides a quick and easy way to generate connected textures with minimal effort.

## Usage
In order to use this script you need to store both `generator.py` and `alignments.py` locally. Additionally you need to provide an image from which the script s going to create the connected textures. This image can either be 16x16 or 32x16.

To generate the connected textures simply create a texture as described in [Image Format](##Image Format). Then run the `generator.py` with `python generator.py`. When prompted, enter the location of your texture. The generated textures will be located in `/texure_name` where the `generator.py` is located. Only thing left to do is put the generated folder inside `assets\minecraft\mcpatcher\ctm` of a texture pack. Ideally the name of the provided texture should be the same as the minecraft block name. If that isn't the case simply rename the folder and the `.properties` file inside to match the block name.

## Image Format
For using the most basic version (16x16) you have to uniquely paint each of the corners of the image.
<br>
![Exampele Texture 16x16](https://imgur.com/w14INLq.png)
<br>
The image above is an example of how to split the texture.
The top left corner (1) is what a corner in the final version will look like.
The bottom left corner (2) is what the edge of a block in the final version will look like.
The top right corner (3) is what a block with no connections diagonally will look like.
The bottom right corner (4) is what a fully surrounded block will look like.

Here is what the 4 sectors look like after using the script to make a texture pack:
<br>
![Generated Textures](https://imgur.com/lc6fWRL.png)
<br>

**The 16x16 Format only works well for symmetrical block textures**

There is also the option of using a 32x16 texture, the first 16x16 pixels function the same way as previously. The second 16x16 area is used to fill transparent pixels after the textures have been generated. This is useful for generating **non-symmetrical block textures**, the version used to generate the connected yellow clay looks as follows:
<br>
![Example Texture 32x16](https://imgur.com/rCdoLM3.png)
