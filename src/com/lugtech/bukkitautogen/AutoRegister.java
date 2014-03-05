package com.lugtech.bukkitautogen;

import java.io.FileInputStream;
import java.io.IOException;
import javax.tools.StandardLocation;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import com.lugtech.bukkitautogen.yaml.Yaml;
import com.lugtech.bukkitautogen.yaml.YamlNode;

public class AutoRegister {
	
	public static void register(JavaPlugin plugin) {
		try {
			YamlNode commands = new Yaml(new FileInputStream(StandardLocation.SOURCE_OUTPUT.getName() + "../autogen.yml")).getRootNode();
			
			for (String commandName : commands.keySet()) {
				YamlNode command = commands.get(commandName).get("classpath");
				if (command == null || command.getStrings() == null || commands.values().size() < 1)
					continue; //skip
				
				
				CommandExecutor executor;
				try {
					Object obj = Class.forName(command.getStrings()[0]).newInstance();
					if (!(obj instanceof CommandExecutor))
						continue; //skip this
					executor = (CommandExecutor) obj;
				}
				catch (ReflectiveOperationException roe) {
					continue; //skip this
				}
				
				plugin.getCommand(commandName).setExecutor(executor); //set it up
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
