# Better Sprite Packer
## What is better sprite packer?
**Better sprite packer (BSP)** is a program that takes sprites (images) converts them into bytes, then performs the XZ compression algortithm on them and packs them into a single file.

### Features
* BSP has very high compression with no quality loss.
	* 45.8% smaller output than PNG
	* 30 % smaller output than GZip
	* 15 % smaller output than BZip2
* Uses [XY compression](http://tukaani.org/xz/) which is based on LZMA2 known as the Lempel�Ziv�Markov chain algorithm. [Lempel�Ziv�Markov chain algorithm](https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Markov_chain_algorithm)
* Can pack thousands of sprites.
* Supports PNG, BMP, and JPEG
* Add/Remove/Delete/Rearrange sprites

### Try it out!
[Release 1.49](https://www.mediafire.com/?s8gpy5dm7dnac2o)

## Media
![alt tag](http://i.imgur.com/lLGj3PD.png)

![alt tag](http://i.imgur.com/7Q8bC3g.gif)

## Testing
Compression testing was done on 4,277 sprites, size of each sprite varies from 16x16-512x512 pixels

#### Packed by pixels (no compression)
![alt tag](http://i.imgur.com/AuaDfMW.png)

#### PNG by itself is compressed
![alt tag](http://i.imgur.com/yYX6xzt.png)

#### GZIP
![alt tag](http://i.imgur.com/fCJWrJg.png)

#### BZip2
![alt tag](http://i.imgur.com/aV9gWwE.png)

#### XZ (LZMA2)
![alt tag](http://i.imgur.com/e3HXjSh.png)