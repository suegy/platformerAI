# platformerAI
artificial intelligence and computation intelligence framework to develop/evolve/design Levels&amp;Bots for 2D platform games

============

### Origin
The project is originally based on the platformersAI(now defunct site) toolkit which does not exist anymore. An original codebase can still be found on [Julian Togelius's website](http://julian.togelius.com/mariocompetition2009/). The main purpose of this repository is to maintain a working and updated version of the codebase. 

### What you can do with this code
The project allows you to build/evolve bots for a 2D platform game using Java. There are also hooks inside the project for python but those are left untouched right now. The framework runs nicely on Java8 inside either Eclipse or IntelliJ and contains all required libraries for building CharacterAI or generating Levels by including your favourite [PCG](http://pcgbook.com/) method.

With the recent version you can load ASCII art levels which can also edit manually in a regular text editor. Simply use the `-loadASCII` parameter or edit the new config.xml file. You can find existing levels in the [THEVGLC](https://github.com/TheVGLC/TheVGLC) repository. The config file is in the rsrc folder same as a sample level.


#### Where to start
You can just 'git clone' the project and then run the Play class as Main. This will allow you to play around with game side of the framework. Inside the "doc" folder you can find some additional command line flags for controlling difficulty '-ld \<number>' or the generator seed '-ls \<number>'. 

### Next planned steps
* change the assets to something entirely royalty free
* a tutorial for a single agent 
* further clean up of the codebase
