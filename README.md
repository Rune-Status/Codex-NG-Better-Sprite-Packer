# Better Sprite Packer
## What is better sprite packer?
**Better sprite packer (BSP)** is a program designed to pack sprites into RuneScape's 2006 file system. Revision #317

### Features

* Follows 317-377 sprite format
* No needed libraries
* No needed classes
* Does not reduce image colors (you have to do that, photoshop or gimp both do this really well)
* Supports GIF, PNG
* Exports to PNG-8
* Exports meta information into a file called Meta.json in the root of your file archive
* Includes jfxrt (some jre's don't contain the javafx libs especially pre 1.8)
Compatiable with Linux and Windows 10 (I don't have a mac so I can't say it works on there. If you have a mac and this program works for you let me know)

### Versions

* 1.0 - 1.44: Designed to replace Galkon's custom sprite cache.
* 1.45 - 1.49: New experimental formats.
* 2.0 + Designed to work with RuneScape's file system.

### Meta format

```json
{
  "mod_icons": [
    {
      "id": 0,
      "offsetX": 0,
      "offsetY": 1,
      "resizeWidth": 13,
      "resizeHeight": 13,
      "format": 1
    },
    {
      "id": 1,
      "offsetX": 0,
      "offsetY": 1,
      "resizeWidth": 13,
      "resizeHeight": 13,
      "format": 1
    }
  ],
  "number_button": [
    {
      "id": 0,
      "offsetX": 0,
      "offsetY": 0,
      "resizeWidth": 64,
      "resizeHeight": 64,
      "format": 0
    }
  ]
}
```

### Notes

* A single image archive can only contain 256 colors (255 if rgb 0 is not present)
* To reduce image colors you can use either [photoshop](http://www.adobe.com/products/photoshop.html) or [gimp](https://www.gimp.org/) image -> mode -> indexed then change max number of colors or use web palette
* Transparency is set in the client, not BSP. (In a #317 rgb 0 aka black is used for transparency)
* For more information regarding the format visit [RS2 Sprite Format](https://www.rune-server.ee/runescape-development/rs2-server/informative-threads/661911-rs2-sprite-format-depth.html)

### Issues

* If the program isn't working for you, feel free to submit an issue request. [Issues](https://github.com/nshusa/better-sprite-packer-gui/issues)

## Media
![alt tag](http://i.imgur.com/ccimVaW.png)