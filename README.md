Piano Leopard
=============

What is it?
-----------

Piano Leopard is an easy to use program for learning how to play a synthesizer or piano, by playing along with
MIDI files. It supports foot pedals as well as lighted keyboards.

![Preview of Piano Leopard](../master/preview.gif)

Installation
------------
Piano Leopard can be [downloaded here](https://repo1.maven.org/maven2/net/kreatious/pianoleopard/pianoleopard/1.1.1/pianoleopard-1.1.1-jar-with-dependencies.jar)
from Maven Central.

You need [Java 8](http://java.com/en/) if you don't have it already, and an electronic MIDI Keyboard Controller
similar to [this lighted keyboard](http://amzn.com/B005N4N2CQ). I highly recommend lighted keyboards. You will
also need MIDI files. All major operating systems are supported.

MIDI Files
----------
You need MIDI files in order to play Piano Leopard. They can be found by using the search keywords `piano only midi`
along with a song or artist. They do not have to be piano only. Here are a few of my favorites to get started:

* [FF1prolo.mid](http://www.midishrine.com/index.php?id=44)
* [8notes.com Easy level](http://www.8notes.com/piano/sheet_music/?difficulty=2)
* [Final Fantasy Piano Collections](http://www.oocities.org/groveman64/ffpc.htm)
* [Video Game Music](http://www.vgmusic.com/music/other/miscellaneous/piano/)

### Technical Documentation
* [Maven site](http://gstuder.github.io/piano-leopard/)
* [Package design](../master/package-dependencies.png)
* [Javadoc](http://gstuder.github.io/piano-leopard/apidocs/index.html)
* [Issues](../../issues)

### Technical Challenges
* Interval Tree - How to efficiently determine which rectangles to draw
* UI Design
* Performance optimization
* Clean code
* JAR signing
 
### Contributing
Make a pull request. For larger contributions, ask me first.

[Download](https://repo1.maven.org/maven2/net/kreatious/pianoleopard/pianoleopard/1.1.1/pianoleopard-1.1.1-jar-with-dependencies.jar)
=============================