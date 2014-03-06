#Bukkit-Autogen

Bukkit-Autogen is a utility for automatically generating plugin.yml files. Autogen uses a little-known feature of java; annotation parsers.

##Usage

Using Autogen is relatively simple.

###Installation
####Eclipse
Installing Autogen with Eclipse is very easy. First, download bukkit-autogen_v1.0.jar and place it somewhere in a bukkit plugin project (i.e. /myplugin/libs/bukkit-autogen_v1.0.jar. Then open Eclipse and go the the preferences for the project. Go to the Java Compiler section and reference the jar as a annotation parser.

###Writing annotations
Autogen uses three different types of annotations:
* `@PluginInfo`
* `@CommandInfo`
* `@PermissionInfo`

Each one of these annotations represents an element of the plugin.yml file. Each annotation contains fields for its respective YAML values. The list of plugin YAML values is avaliable [here](http://wiki.bukkit.org/Plugin_YAML). In the annotations, hypens are replaced with underscores.

####@PluginInfo
Classes using the `@PluginInfo` annotation must implement the `Plugin` interface.

####@CommandInfo
Classes using the `@CommandInfo` annotation must implement the `CommandExecutor` interface.

####@PermissionInfo
Any class can use the `@PermissionInfo` annotation, however, for organizational purposes it may be best to use them with respective `CommandExecutor` classes.

###Example Usage
```java
@CommandInfo(
		command = "flagrate",
		description = "Set yourself on fire.",
		permission = "inferno.flagrate",
		aliases = {"combust_me", "combustMe"}.
		usage = "Syntax error! Simply type /&lt;command&gt; to ignite yourself"
		)
@PermissionInfo(
		permission = "inferno.flagrate",
		description = "Allows you to ignite yourself",
		default_value = "true"
		)
public class FlagrateExecutor implements CommandExecutor {
  ...
}
```
In this example command executor, a command is defined as _flagrate_. Additional command parameters are defined as well. Permission information is defined as well.
