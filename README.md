#Bukkit-Autogen

Bukkit-Autogen is a utility for automatically generating plugin.yml files. Autogen uses a little-known feature of java; annotation parsers.

##Usage

Using Autogen is relatively simple.

###Installation
####Eclipse
Installing Autogen with Eclipse is very easy. First, download bukkit-autogen_v1.0.jar and place it somewhere in a bukkit plugin project (i.e. /myplugin/libs/bukkit-autogen_v1.0.jar. Then open Eclipse and go the the preferences for the project. Go to the Java Compiler section and reference the jar as a annotation parser.

###Writing annotations
Autogen uses three different types of annotations:
* ```PluginInfo```
* ```CommandInfo```
* ```PermissionInfo```
Each one of these annotations represents an element of the plugin.yml file. Each annotation contains fields for it's respective yaml values. The list of plugin yaml values is avaliable [here](http://wiki.bukkit.org/Plugin_YAML). In the annotations, hypens are replaced with underscores.

###Example Usage
