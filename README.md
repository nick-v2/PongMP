# Pong MP

A basic Pong game with a twist, it can be played multiplayer over the internet. This project was done to extend my knowledge on the use of Sockets.

## Getting Started

Download the files into a Netbean project for easiest modification and testing. Alternativly the files can be downloaded into a local folder and the jar file can be executed.

### Prerequisites

The newest update of Java 8 JDK and JRE.
(Optional) Netbeans IDE.

### Use

To play PongMP, double-click the jar file titled "PongMP". The menu should open showing you an option to connect to a server on the left half of the screen, and single player, open server, and options on the right. 

To play single player, just click the single player button, click the game window that pops up, and use your mouse to control the paddle. 

To start a server, open the server creation window by clicking open server. Set the gamemode of the game and the amount of players you want to allow to join the game. Click Start. For players to join your server over the internet, you must port forward the port 22333 ([How to port forward](https://www.wikihow.com/Set-Up-Port-Forwarding-on-a-Router)) and then give your public IP address to the players you want to join the game.

To join a server, type in the IP address and click connect. To join a server hosted in the same house as you, use the local IP address given in the server creation window.

## Built With

* [Netbeans](https://netbeans.org/downloads/) - The IDE used
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java JDK

## Authors

* **Nick Vocaire** - *All work*

## License

This project is licensed under the GNU General Public Licence - see the [COPYING.txt](COPYING.txt) file for details

## Documentation

* [Auto-generated Javadoc](https://njvnba11.github.io/PongMP/)

## Acknowledgments

* Allen Alcorn, the man behind the wonderful game of pong
* [Eli Delventhal](http://www.java-gaming.org/index.php?topic=24220.0) - Had a good design for the update loop, I modified it slightly though.