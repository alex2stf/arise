# A.R.I.S.E
package-based autonomous framework on demand


Quick start:
1. At first run a default application.properties file is generated
2. Create a folder called local_env




Development notes:
- use AppSetting.throwOrExit instead of throwing random exceptions. If app.onerror.exit is defined as "true" application will close when exception is met.




arise is a monolith package-based solution framework


you can choose from:

- net -> servers utilities
- core -> core exportable tools
- weland -> device controller
- cargo -> dependency manager, export utilities and language wrappers
- canter -> CommAnd eveNT rEgistry 
- geeks -> custom algorithms

Advices for devs:
arise is a small compilable codebase and doesn't respect all the java community standards

####A.R.I.S.E is...
- not your average architecture model
- full of nested classes
- full of single-letter variables
- compiled, run and tested on raspberry pi, Windows XP and low CPU architectures
- aiming to provide big and useful features using a few lines of code



setup raspberry pi

locate vlc libs using:

```whereis vlc```