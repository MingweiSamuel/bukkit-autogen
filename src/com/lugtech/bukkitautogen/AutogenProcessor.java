package com.lugtech.bukkitautogen;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.lugtech.bukkitautogen.yaml.Yaml;
import com.lugtech.bukkitautogen.yaml.YamlNode;

@SupportedAnnotationTypes("com.lugtech.bukkitautogen.*")
public class AutogenProcessor extends AbstractProcessor {
	
	private static final String hasSpace = "(?s)(.*)(\\s+)(.*)";
	private static final String isSpace = "(\\s*)";
	
	// boolean done = false;
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		// if (done) return true;
		// done = true;
		
		try {
			// SETUP //
			Filer filer = super.processingEnv.getFiler();
			
			Yaml pluginYaml = new Yaml(filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "../plugin.yml").openInputStream());
			YamlNode root = pluginYaml.getRootNode();
			
			Yaml autogenYaml = new Yaml(filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen.yml").openInputStream());
			YamlNode autogenRoot = autogenYaml.getRootNode();
			
			Messager msg = super.processingEnv.getMessager();
			Types typeUtil = super.processingEnv.getTypeUtils();
			Elements elementUtil = super.processingEnv.getElementUtils();
			
			// GENERAL //
			Set<? extends Element> plugins = roundEnv.getElementsAnnotatedWith(PluginInfo.class);
			for (Element element : plugins) { //should only be one, but we have no good/consistent way of checking / won't cause problems
				if (element instanceof TypeElement) {
					TypeElement typeElement = (TypeElement) element;
					Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
					
					boolean valid;
					while (true) {
						if (!interfaces.hasNext()) {
							msg.printMessage(Diagnostic.Kind.ERROR, "Classes using the @PluginInfo annotation must implement the org.bukkit.plugin.Plugin interface", element);
							valid = false;
							break;
						}
						if (typeUtil.isSameType(interfaces.next(), elementUtil.getTypeElement("org.bukkit.plugin.Plugin").asType())) {
							valid = true;
							break;
						}
					}
					if (!valid)
						continue;
					
					// usage is valid
					PluginInfo info = element.getAnnotation(PluginInfo.class);
					
					if (info.name().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Name must be set in @PluginInfo", element);
						return true;
					}
					root.putValue("name", info.name());
					
					if (info.version().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Version must be set in @PluginInfo", element);
						return true;
					}
					root.putValue("version", info.version());
					
					if (info.main().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Main must be set in @PluginInfo", element);
						return true;
					}
					if (info.main().matches(hasSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Main main not contain whitespace in @PluginInfo", element);
						return true;
					}
					root.putValue("main", info.main());
					
					root.putValue("description", info.description());
					root.putValue("load", info.load());
					root.putValue("author", info.author());
					root.putValue("authors", info.authors());
					root.putValue("website", info.website());
					root.putValue("database", info.database());
					root.putValue("depend", info.depend());
					root.putValue("prefix", info.prefix());
					root.putValue("softdepend", info.softdepend());
					root.putValue("loadbefore", info.loadbefore());
				}
			}
			
			// COMMANDS //
			Set<? extends Element> commands = roundEnv.getElementsAnnotatedWith(CommandInfo.class);
			if (!commands.isEmpty()) {
				if (!root.containsKey("commands"))
					root.putNode("commands");
				YamlNode cmds = root.get("commands");
				
				for (Element element : commands) {
					if (element instanceof TypeElement) {
						TypeElement typeElement = (TypeElement) element;
						Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
						
						boolean valid;
						while (true) {
							if (!interfaces.hasNext()) {
								msg.printMessage(Diagnostic.Kind.ERROR, "Any class using the @CommandInfo annotation must implement the org.bukkit.command.CommandExecutor interface",
										element);
								valid = false;
								break;
							}
							if (typeUtil.isSameType(interfaces.next(), elementUtil.getTypeElement("org.bukkit.command.CommandExecutor").asType())) {
								valid = true;
								break;
							}
						}
						if (!valid)
							continue;
						
						// PLUGIN.YML
						CommandInfo info = element.getAnnotation(CommandInfo.class);
						if (info.command().matches(hasSpace)) { // contains
																// whitespace
							msg.printMessage(Diagnostic.Kind.ERROR, "Command may not contain whitespace in @CommandInfo", element);
							continue; // go on to next command / ignore this one
						}
						if (info.command().matches(isSpace)) { // sorta redundant
							msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
							continue; // go on to next command / ignore this one
						}
						cmds.putNode(info.command());
						YamlNode cmd = cmds.get(info.command());
						
						List<String> aliases = new ArrayList<String>(0);
						for (int i = 0; i < info.aliases().length; i++) {
							if (info.aliases()[i].matches(hasSpace)) { // contains
																		// whitespace
								msg.printMessage(Diagnostic.Kind.ERROR, "Alias string may not contain whitespace in @CommandInfo", element);
								continue; // go on to next alias
							}
							aliases.add(info.aliases()[i]);
						}
						cmd.putValue("aliases", aliases.toArray(new String[aliases.size()]));
						cmd.putValue("description", info.description());
						cmd.putValue("permission", info.permission());
						cmd.putValue("permission-message", info.permission_message());
						cmd.putValue("usage", info.usage());
						
						// AUTOGEN.YML
						autogenRoot.putNode(info.command());
						autogenRoot.get(info.command()).putValue("classpath", typeElement.getQualifiedName().toString());
					}
				}
			}
			
			// PERMISSIONS //
			Set<? extends Element> missions = roundEnv.getElementsAnnotatedWith(PermissionInfo.class);
			if (!missions.isEmpty()) {
				if (!root.containsKey("permissions"))
					root.putNode("permissions");
				YamlNode permits = root.get("permissions");
				
				for (Element element : missions) {
					PermissionInfo info = element.getAnnotation(PermissionInfo.class);
					
					if (info.permission().matches(isSpace)) { // sorta redundant
						msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
						continue; // go on to next command / ignore this one
					}
					
					permits.putNode(info.permission());
					YamlNode permit = permits.get(info.permission());
					permit.putValue("default", info.default_value());
					permit.putValue("description", info.description());
					permit.putValue("children", info.children());
				}
			}
			
			try {
				//save plugin.yml
				pluginYaml.save(new PrintStream(filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../plugin.yml").openOutputStream()));
				
				//save autogen.yml
				autogenYaml.save(new PrintStream(filer.createResource(StandardLocation.SOURCE_OUTPUT,  "", "../autogen.yml").openOutputStream()));
			}
			catch (IOException ioe) {
				msg.printMessage(Diagnostic.Kind.WARNING, "Autogen failed to access to plugin.yml: " + ioe.getLocalizedMessage());
			}
		}
		catch (Exception e) { // pokemon catch
			try {
				Filer filer = super.processingEnv.getFiler();
				FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen-errors.aglog");
				PrintWriter writer = new PrintWriter(fileObj.openOutputStream()); // open the generated file
				writer.println(e); // write the strings
				writer.println(e.getLocalizedMessage());
				for (StackTraceElement trace : e.getStackTrace()) {
					writer.println(trace);
				}
				writer.close();
			}
			catch (IOException ioe) {} // we're sunk
		}
		return true;
	}
}
