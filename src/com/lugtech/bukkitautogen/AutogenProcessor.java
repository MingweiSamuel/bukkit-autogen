package com.lugtech.bukkitautogen;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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
	
	//private boolean done = false;
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		//if (done) return true;
		//done = true; //only do this once
		
		try {
			// SETUP //
			Filer filer = super.processingEnv.getFiler();
			
			Yaml.LOG = true;
			Scanner prelog = new Scanner(filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen.aglog").openInputStream());
			PrintStream log = new PrintStream(filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen.aglog").openOutputStream());
			while (prelog.hasNextLine())
				log.println(prelog.nextLine());
			prelog.close();
			Yaml.logFile(log);
			
			FileObject obj = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "../plugin.yml");
			boolean write = true;
			Yaml yaml = new Yaml(obj.openInputStream()); 
			YamlNode root = yaml.getRootNode();
			
			
			
			
			Messager msg = super.processingEnv.getMessager();
			Types typeUtil = super.processingEnv.getTypeUtils();
			Elements elementUtil = super.processingEnv.getElementUtils();
			
			
			// GENERAL //
			Set<? extends Element> plugins = roundEnv.getElementsAnnotatedWith(PluginInfo.class);
			for (Element element : plugins) {
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
					
					//usage is valid
					PluginInfo info = element.getAnnotation(PluginInfo.class);
					
					if (info.name().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Name must be set in @PluginInfo", element);
						return true;
					}
					root.addValue("name", info.name());
					
					if (info.version().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Version must be set in @PluginInfo", element);
						return true;
					}
					root.addValue("version", info.version());
					
					if (info.main().matches(isSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Main must be set in @PluginInfo", element);
						return true;
					}
					if (info.main().matches(hasSpace)) {
						msg.printMessage(Diagnostic.Kind.ERROR, "Main main not contain whitespace in @PluginInfo", element);
						return true;
					}
					root.addValue("main", info.main());
					
					root.addValue("description", info.description());
					root.addValue("load", info.load());
					root.addValue("author", info.author());
					root.addValue("authors", info.authors());
					root.addValue("website", info.website());
					root.addValue("database", info.database());
					root.addValue("depend", info.depend());
					root.addValue("prefix", info.prefix());
					root.addValue("softdepend", info.softdepend());
					root.addValue("loadbefore", info.loadbefore());
				}
			}
			//}
			
			// COMMANDS //
			if (!root.getChildren().containsKey("commands"))
				root.addNode("commands");
			YamlNode cmds = root.getChildren().get("commands");
			
			Set<? extends Element> commands = roundEnv.getElementsAnnotatedWith(CommandInfo.class);
			for (Element element : commands) {
				if (element instanceof TypeElement) {
					TypeElement typeElement = (TypeElement) element;
					Iterator<? extends TypeMirror> interfaces = typeElement.getInterfaces().iterator();
					
					boolean valid;
					while (true) {
						if (!interfaces.hasNext()) {
							msg.printMessage(Diagnostic.Kind.ERROR, "Any class using the @CommandInfo annotation must implement the org.bukkit.command.CommandExecutor interface", element);
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
					
					//the usage is valid
					CommandInfo info = element.getAnnotation(CommandInfo.class);
					if (info.command().matches(hasSpace)) { //contains whitespace
						msg.printMessage(Diagnostic.Kind.ERROR, "Command may not contain whitespace in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					if (info.command().matches(isSpace)) { //sorta redundant
						msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					cmds.addNode(info.command());
					YamlNode cmd = cmds.getChildren().get(info.command());
					
					List<String> aliases = new ArrayList<String>(0);
					for (int i = 0; i < info.aliases().length; i++) {
						if (info.aliases()[i].matches(hasSpace)) { //contains whitespace
							msg.printMessage(Diagnostic.Kind.ERROR, "Alias string may not contain whitespace in @CommandInfo", element);
							continue; //go on to next alias
						}
						aliases.add(info.aliases()[i]);
					}
					cmd.addValue("aliases", aliases.toArray(new String[aliases.size()]));
					cmd.addValue("description", info.description());
					cmd.addValue("permissions", info.permission());
					cmd.addValue("permission-message", info.permission_message());
					cmd.addValue("usage", info.usage());
				}
			}
			
			// PERMISSIONS //
			if (!root.getChildren().containsKey("permissions"))
				root.addNode("permissions");
			YamlNode permits = root.getChildren().get("permissions");
			
			Set<? extends Element> missions = roundEnv.getElementsAnnotatedWith(PermissionInfo.class);
			for (Element element : missions) {
				if (element instanceof TypeElement) {
					PermissionInfo info = element.getAnnotation(PermissionInfo.class);
					
					if (info.permission().matches(isSpace)) { //sorta redundant
						msg.printMessage(Diagnostic.Kind.ERROR, "Command must be set in @CommandInfo", element);
						continue; //go on to next command / ignore this one
					}
					
					permits.addNode(info.permission());
					YamlNode permit = permits.getChildren().get(info.permission());
					permit.addValue("default", info.default_value());
					permit.addValue("description", info.description());
					permit.addValue("children", info.children());
				}
			}
			
			try {
				if (write) {
					FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../plugin.yml");
					yaml.save(new PrintStream(fileObj.openOutputStream()));
					
					log.close(); //save log
				}
			} catch (IOException ioe) {
				msg.printMessage(Diagnostic.Kind.WARNING, "Autogen failed to access to plugin.yml: " + ioe.getLocalizedMessage());
			}
		} catch (Exception e) { //pokemon catch
			try {
				Filer filer = super.processingEnv.getFiler();
				FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "../autogen-errors.aglog");
				PrintWriter writer = new PrintWriter(fileObj.openOutputStream()); //open the generated file
				writer.println(e); //write the strings
				writer.println(e.getLocalizedMessage());
				for (StackTraceElement trace : e.getStackTrace()) {
					writer.println(trace);
				}
				writer.close();
			} catch (IOException ioe) { } //we're sunk
		}
		return true;
	}
}
