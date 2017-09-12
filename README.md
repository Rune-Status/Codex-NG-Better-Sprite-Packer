# Better Sprite Packer
## What is better sprite packer?
**Better sprite packer (BSP)** is a program designed to pack sprites into RuneScape's 2006 file system. Revision #317

### Features

* Follows 317-377 sprite format
* No needed libraries
* No needed classes
* Supports GIF, PNG
* Exports to PNG-8
* Includes jfxrt (some jre's don't contain the javafx libs especially pre 1.8)
Compatiable with Linux and Windows 10 (I don't have a mac so I can't say it works on there. If you have a mac and this program works for you let me know)

### Versions

* 1.0 - 1.44: Designed to replace Galkon's custom sprite cache.
* 1.45 - 1.49: New experimental formats.
* 2.0 + Designed to work with RuneScape's file system.

### Notes

* Don't use JPEG to pack sprites. JPEG is for realistic photos which has a lot more distinct colors. RuneScape sprites are similar to GIf's however there can only be 256 colors in a single archive. A single image can contain 256 colors if one of the rgb values is 0. If not then there can only be 255 colors.
* Transparency is set in the client, not BSP
* For more information regarding the format visit [RS2 Sprite Format](https://www.rune-server.ee/runescape-development/rs2-server/informative-threads/661911-rs2-sprite-format-depth.html)

### Issues

* If the program isn't working for you, feel free to submit an issue request. [Issues](https://github.com/nshusa/better-sprite-packer-gui/issues)

## Media
![alt tag](http://i.imgur.com/ccimVaW.png)