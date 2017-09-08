# Better Sprite Packer
## What is better sprite packer?
**Better sprite packer (BSP)** is a program that packs sprites into 317-377 format.

Version 2.0 allows you to use your own operating system's file explorer to modify the sprites in your cache. You can unpack the sprites into their raw formats and pack them back into rs2 format. Because of this you don't need any libraries or any extra classes to unpack them. This works with a 317 deob.

### Features

* Follows 317-377 sprite format
* No needed libraries
* No needed classes
* I included javafx libs in the jar that's why it's a little bigger than normal. This is to add support for linux users and for people who don't have the javafx libs in their jre. (not all version of jre has jfxrt)
* To change transparency you can change that in the sprite class (in your client). Most clients nowadays use the fusha/magenta color as transparency
Compatiable with Linux and Windows 10 (I don't have a mac so I can't say it works on there. If you have a mac and this program works for you let me know)

### Notes
* Because the sprites in the RS2 format only have 256 colors it's best not to use image formats such as jpeg that are known to have a lot of distinct colors.
* For more information regarding the format visit [RS2 Sprite Format](https://www.rune-server.ee/runescape-development/rs2-server/informative-threads/661911-rs2-sprite-format-depth.html)

## Media
![alt tag](http://i.imgur.com/ccimVaW.png)