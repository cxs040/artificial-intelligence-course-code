
How to build and run this thing:

1- Install dependencies:
   Debian, Ubuntu, Mint, etc.:
       sudo apt-get install libpng12-dev libsdl1.2-dev
   Red hat, CentOS, etc.:
       sudo yum install [corresponding packages]
   Windows, Mac:
       I'm not sure. You will probably have to download and build libPNG12 and libSDL1.2. Good luck.
       (Note: I have carefully avoided any platform-specific components, but I have not yet tested
             on these platforms, so you will certainly have to do a small amount of porting.)

2- Compile:
   cd src
   make opt
   (Note: To link debug symbols into the binary, and skip optimizing, do "make dbg" instead.)

3- Run it:
   cd ../bin
   ./game
   and start tapping keys or the mouse button
   (Note: If you did "make dbg", use "./gamedbg" instead.)

